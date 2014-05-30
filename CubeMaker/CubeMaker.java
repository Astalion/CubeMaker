package CubeMaker;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import Utilities.FileUtilities;
import Utilities.ImageUtilities;


public class CubeMaker {

	private static final String format = "png";
	private static final String ext = "." + format;
	private static final String link = "http://magiccards\\.info/scans/en/[^\"]*";
	private static final Pattern linkPattern = Pattern.compile(link);
	private static String currDir;
	private static Integer i = 0;
	private static Integer n = 1;
	private static BufferedImage[] parts = new BufferedImage[9];
	
	private static final Integer A4w = 2480;
	private static final Integer A4h = 3508;
	// Should be 700x995 for 300dpi
	private static final Integer cardW = 718;
	private static final Integer cardH = 1022;
	
	private static final String dataDir = System.getenv("APPDATA") + "\\Cubemaker";
	private static final String cacheDir = dataDir + "\\cached";

	private static File getImage(String dir, String imageName) {
		File f = new File(dir , imageName + ".jpg");
		if(!f.exists()) f = new File(dir , imageName + ".png");
		return f;
	}
	
	private static void mergeImages(File output, int chunks) throws IOException{
	    int rows = 3; 
        int cols = 3;
        
        int margin = A4w/100;
        int offsetW = (A4w - cols*cardW - margin*(cols-1))/2;
        int offsetH = (A4h - rows*cardH - margin*(rows-1))/2;
  
       //creating a buffered image array from image files  
        BufferedImage[] buffImages = new BufferedImage[chunks];  
        for (int i = 0; i < chunks; i++) {
            buffImages[i] = ImageUtilities.scaleImage(parts[i], cardW, cardH);
        }
  
        //Initializing the final image  
        BufferedImage finalImg = new BufferedImage(A4w, A4h, BufferedImage.TYPE_INT_ARGB);  
        Graphics2D canvas = finalImg.createGraphics();
        canvas.setColor(new Color(255,255,255));
        canvas.fillRect(0, 0, A4w, A4h);
  
        int num = 0;  
        for (int i = 0; i < rows; i++) {  
            for (int j = 0; j < cols; j++) {
            	if(i * cols + j >= chunks){
            		break;
            	}
                canvas.drawImage(buffImages[num], 
                		offsetW + (cardW + margin) * j, 
                		offsetH + (cardH + margin) * i, null);  
                num++;  
            }  
        }
        //System.out.println("Finished " + output.getName());  
        //ImageIO.write(finalImg, format, output);  
        ImageUtilities.saveImage(finalImg, output);
	}
	
	private static Boolean findOnline(String cardName) throws IOException {
		FileUtilities.saveURL("http://magiccards.info/query?q=!" + cardName.replaceAll(" ", "+"), new File("temp.html"));
		String found = FileUtilities.findInFile("temp.html", linkPattern);
		if(found != null) {			
			// Save file to cache
			File saved = new File(cacheDir, cardName.replaceAll("[',]" , "") + ".jpg");
			FileUtilities.saveURL(found, saved);
			
			parts[i] = ImageIO.read(saved);
			return true;
		} else {
			return false;
		}
	}
	
	private static Boolean findLocal(String cardName) throws IOException {
		String tempName = cardName.replaceAll("[',]" , "");
		File mse = getImage(currDir + "\\mse", tempName);
		if(mse.exists()) {
			parts[i] = MSEImageFix.fixImage(mse, cardW, cardH);
			return true;
		} 
		
		File pref = getImage(currDir + "\\pref", tempName);
		if(pref.exists()) {
			parts[i] = ImageIO.read(pref);
			return true;
		}
		
		File cached = getImage(cacheDir, tempName);
		if(cached.exists()) {
			parts[i] = ImageIO.read(cached);
			return true;
		}
		
		return false;
	}
	
	private static Boolean findCard(String cardName) throws IOException {
		if (findLocal(cardName)) {
			return true;
		} else if(findOnline(cardName)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void main(String[] args) {
		// A4 = 2480 X 3508 pixels (300 dpi)
		//1051x1487 = A4
		final String cubeFile = "cube.txt";
		try {
			FileWriter fw = new FileWriter(new File("missing.txt"));
			fw.write("");
			fw.close();
			Scanner s = new Scanner(new File(cubeFile));
			currDir = new File(".").getCanonicalPath();
			
			File imgDir = new File(currDir + "\\images");
			FileUtilities.deleteDirectory(imgDir);
			imgDir.mkdir();
			
			ProgressWindow pw = new ProgressWindow(cubeFile);
			s.useDelimiter("\\s*\\n");
			while(s.hasNext()){
				String cardName = s.next();
				pw.progCard(cardName);
				
				FileUtilities.saveURL("http://magiccards.info/query?q=!" + cardName.replaceAll(" ", "+"), new File("temp.html"));
				Boolean found = findCard(cardName);
				if(found) {
					if(i == 8){
						pw.updateProgress("Merging image #" + n);
						i = 0;
						mergeImages(new File(imgDir, "img" + n + ext), 9);
						System.out.println("Made image " + n);
						n++;
					} else {
						i++;
					}
				} else {
					fw = new FileWriter(new File("missing.txt"), true);
					fw.write(cardName+"\n");
					fw.close();
					System.out.println("Couldn't find " + cardName);
					try {
						FileUtilities.copyFile(new File(currDir, "temp.html"), new File(currDir + "\\errors", cardName +".html"));					
					} catch (Exception e) {
						
					}
				}
			}
			if(i != 0){
				pw.updateProgress("Merging image #" + n);
				mergeImages(new File(currDir + "\\images", "img" + n + ext), i);
			}
			System.out.println("Done!");
			pw.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}

@SuppressWarnings("serial")
class ProgressWindow extends JFrame {
	private JProgressBar progressBar;
	private JLabel label;
	int progress;

	public static int countLines(String filename) {
		try {
		    LineNumberReader reader  = new LineNumberReader(new FileReader(filename));
			int cnt = 0;
			while (reader.readLine() != null) {}
			
			cnt = reader.getLineNumber(); 
			reader.close();
			return cnt;			
		} catch (Exception e) {
			return 0;
		}
	}
	
	public ProgressWindow(String fileName) {
	    /* Make a frame to show progress bar */
	    super("Download Progress");
	    setLocationRelativeTo(null); //Put frame near middle of screen
	    JPanel bgPane = new JPanel();
	    
	    label = new JLabel("Downloading: ");
	    
	    int numLines = countLines(fileName);
	    int numResult = (numLines - 1)/9 + 1;
	    
	    progress = 0;	    
	    progressBar = new JProgressBar(0, numLines + numResult); //Initiate a progress bar with appropriate length
	    progressBar.setValue(0);
	    progressBar.setStringPainted(true);

	    /* Fancy layout stuff */
	    bgPane.setLayout(new BoxLayout(bgPane, BoxLayout.Y_AXIS));
	    bgPane.add(label);
	    bgPane.add(progressBar);
	    bgPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	    
	    /* Final frame setup */
	    add(bgPane);
	    pack();
	    setVisible(true);
	}
	
	public void progCard(String cardName) {
		updateProgress("Downloading: " + cardName);
	}
	
	public void updateProgress(String status) {
		label.setText(status);
		progressBar.setValue(progress);
		progress++;
	}
}