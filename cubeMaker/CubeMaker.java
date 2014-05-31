package cubeMaker;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import utilities.FileUtilities;
import utilities.ImageUtilities;



public class CubeMaker {
	
	/*
	 * Global constants
	 */
	private static final String format = "png";
	private static final String ext = "." + format;
	private static final String link = "http://magiccards\\.info/scans/en/[^\"]*";
	private static final Pattern linkPattern = Pattern.compile(link);
	
	private static final Integer A4w = 2480;
	private static final Integer A4h = 3508;
	// Should be 700x995 for 300dpi
	private static final Integer cardW = 718;
	private static final Integer cardH = 1022;
	
	private static final String dataDir = System.getenv("APPDATA") + "\\Cubemaker";
	private static final String cacheDir = dataDir + "\\cached";
	
	/*
	 * Member variables
	 */
	private File saveDir;	/* Directory to save images to */
	private File cubeFile;	/* Cube file to read from */
	private ProgBar pBar;	/* Progress Bar */
	
	private String currDir;	/* Current directory (for local images and error files) */
	private Integer i;		/* Current sub-image */
	private Integer n;		/* Number of the current output file */
	private BufferedImage[] parts;	/* Partial images (i.e. cards) */
	
	public CubeMaker(File saveDir, File cubeFile, ProgBar pBar) {
		this.saveDir = saveDir;
		this.cubeFile = cubeFile;
		this.pBar = pBar;
		
		parts = new BufferedImage[9];
	}

	private static File getImage(String dir, String imageName) {
		File f = new File(dir , imageName + ".jpg");
		if(!f.exists()) f = new File(dir , imageName + ".png");
		return f;
	}
	
	private void mergeImages(int chunks) throws IOException{
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

        ImageUtilities.saveImage(finalImg, new File(saveDir, "img" + n + ext));
	}
	
	/**
	 * Download image file from magiccards.info, then save in local cache
	 * 
	 * @param cardName name of the card to find
	 * @return true if successful, false if something failed
	 * @throws IOException
	 */
	private Boolean findOnline(String cardName) throws IOException {
		File tempHtml = new File("temp.html");
		FileUtilities.saveURL("http://magiccards.info/query?q=!" + cardName.replaceAll(" ", "+"), tempHtml);
		String found = FileUtilities.findInFile("temp.html", linkPattern);
		tempHtml.delete();
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
	
	/**
	 * Find local image file for the given card.
	 * Searches in ./mse, ./pref, %appdata%/CubeMaker/cached, in that order
	 * 
	 * @param cardName name of the card to find
	 * @return true if file found, false otherwise
	 * @throws IOException
	 */
	private Boolean findLocal(String cardName) throws IOException {
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
	
	private Boolean findCard(String cardName) throws IOException {
		if (findLocal(cardName)) {
			return true;
		} else if(findOnline(cardName)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void main(String[] args) throws IOException {
		File cubeFile = new File("cube.txt");
		
		String currDir = new File(".").getCanonicalPath();
		File imgDir = new File(currDir + "\\images");
		
		CubeMaker cm = new CubeMaker(imgDir, cubeFile, new ProgressWindow());
		cm.makeCube();		
	}
	
	public void makeCube() {
		// A4 = 2480 X 3508 pixels (300 dpi)
		//1051x1487 = A4
		i = 0;
		n = 1;
		try {
			FileWriter fw = new FileWriter(new File(currDir, "missing.txt"));
			fw.write("");
			fw.close();
			
			Scanner s = new Scanner(cubeFile);
			currDir = new File(".").getCanonicalPath();
			
			pBar.initProgress(cubeFile);
			s.useDelimiter("\\s*\\n");
			while(s.hasNext()){
				String cardName = s.next();
				pBar.progCard(cardName);
				
				Boolean found = findCard(cardName);
				if(found) {
					if(i == 8){
						pBar.updateProgress("Merging image #" + n);
						mergeImages(9);
						
						i = 0;
						n++;
					} else {
						i++;
					}
				} else {
					fw = new FileWriter(new File(currDir, "missing.txt"), true);
					fw.write(cardName+"\n");
					fw.close();
					System.out.println("Couldn't find " + cardName);
					try {
						FileUtilities.saveURL(
								"http://magiccards.info/query?q=!" + cardName.replaceAll(" ", "+"),
								new File(currDir + "\\errors", cardName +".html")
							);
					} catch (Exception e) {
						
					}
				}
			}
			if(i != 0){
				pBar.updateProgress("Merging image #" + n);
				mergeImages(i);
			}
			pBar.finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}

@SuppressWarnings("serial")
class ProgressWindow extends JFrame implements ProgBar {
	private JProgressBar progressBar;
	private JLabel label;
	int progress;

	public static int countLines(File cardfile) {
		try {
		    LineNumberReader reader  = new LineNumberReader(new FileReader(cardfile));
			int cnt = 0;
			while (reader.readLine() != null) {}
			
			cnt = reader.getLineNumber(); 
			reader.close();
			return cnt;			
		} catch (Exception e) {
			return 0;
		}
	}
	
	public ProgressWindow() {
	    /* Make a frame to show progress bar */
	    super("Download Progress");
	    setLocationRelativeTo(null); //Put frame near middle of screen
	    JPanel bgPane = new JPanel();
	    
	    label = new JLabel("Parsing card file...");
	    
	    progressBar = new JProgressBar(0, 1);
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
	}

	@Override
	public void initProgress(File cardFile) {	    
	    int numLines = countLines(cardFile);
	    int numResult = (numLines - 1)/9 + 1;
	    
	    progress = 0;
	    progressBar.setMaximum(numLines + numResult);

	    setVisible(true);
	}

	@Override
	public void progCard(String cardName) {
		updateProgress("Loading: " + cardName);
	}

	@Override
	public void updateProgress(String status) {
		label.setText(status);
		progressBar.setValue(progress);
		progress++;
	}

	@Override
	public void finish() {
		this.dispose();
	}
}