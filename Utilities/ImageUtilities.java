package Utilities;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

public class ImageUtilities {

	public static BufferedImage cropImage(BufferedImage original, int top, int bot, int left, int right) {
		return original.getSubimage(left, top, 
				original.getWidth()-left-right, original.getHeight()-top-bot);
		
	}

	public static BufferedImage scaleImage(BufferedImage original, int width, int height){
		
		BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale(((float)width)/original.getWidth(), ((float)height)/original.getHeight());
		AffineTransformOp scaleOp = 
				new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		scaled = scaleOp.filter(original, scaled);
		return scaled;
		
	}
	
	private static final double INCH_2_CM = 2.54;
	// http://stackoverflow.com/a/4833697/2295872
	public static void saveImage(BufferedImage img, File output) throws IOException {
		output.delete();
		
		final String formatName = "png";
		
		for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
		   ImageWriter writer = iw.next();
		   ImageWriteParam writeParam = writer.getDefaultWriteParam();
		   ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
		   IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
		   if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
		      continue;
		   }
		
		   setDPI(metadata, 300);
		
		   final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
		   try {
		      writer.setOutput(stream);
		      writer.write(metadata, new IIOImage(img, null, metadata), writeParam);
		   } finally {
		      stream.close();
		   }
		   break;
		}
	}
	// http://stackoverflow.com/a/4833697/2295872
	
	private static void setDPI(IIOMetadata metadata, int DPI) throws IIOInvalidTreeException {

		// for PNG, it's dots per millimeter
		double dotsPerMilli = 1.0 * DPI / 10 / INCH_2_CM;
		
		IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
		horiz.setAttribute("value", Double.toString(dotsPerMilli));
		
		IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
		vert.setAttribute("value", Double.toString(dotsPerMilli));
		
		IIOMetadataNode dim = new IIOMetadataNode("Dimension");
		dim.appendChild(horiz);
		dim.appendChild(vert);
		
		IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
		root.appendChild(dim);

		    metadata.mergeTree("javax_imageio_1.0", root);
	}
}
