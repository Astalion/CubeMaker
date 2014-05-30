package cubeMaker;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import utilities.FileUtilities;
import utilities.ImageUtilities;



public class MSEImageFix {

	public static void main(String[] args) {
		try {			
	        ImageIO.write(
	        		fixImage("mse\\Kief the Baaaard.jpg"), 
	        		".jpg",
	        		new File("custom\\Kief the Baaaard.jpg")
	        	);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static BufferedImage fixImage(String infile) throws IOException {
		return fixImage(new File(infile), 312, 445);
	}

	public static BufferedImage fixImage(File infile, int width, int height) throws IOException {
		BufferedImage img =  ImageIO.read(infile);
		
		if(img.getWidth() == 375) {	// Default size when exporting from MSE
	        img = ImageUtilities.cropImage(img, 11, 11, 11, 10);
	        img = ImageUtilities.scaleImage(img, width, height);
		}
        
        BufferedImage newImage = new BufferedImage( img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        newImage.createGraphics().drawImage( img, 0, 0, Color.BLACK, null);
        
        return newImage;
	}
}
