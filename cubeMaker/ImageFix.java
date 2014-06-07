package cubeMaker;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import utilities.ImageUtilities;



public class ImageFix {
	public static BufferedImage fixImage(File infile) throws IOException {
		BufferedImage img =  ImageIO.read(infile);
		return fixImage(img);		
	}
	
	public static BufferedImage fixImage(BufferedImage img) throws IOException {		
		if(img.getWidth() == 375) {	// Default size when exporting from MSE
	        img = ImageUtilities.cropImage(img, 11, 11, 11, 10);
		} else if(img.getWidth() == 223) {	// mtgimage.com low-res image
	        img = ImageUtilities.cropImage(img, 8, 8, 7, 8);			
		}
        
        BufferedImage newImage = new BufferedImage( img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        newImage.createGraphics().drawImage( img, 0, 0, Color.BLACK, null);
        
        return newImage;
	}
}
