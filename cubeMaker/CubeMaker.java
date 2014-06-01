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
import java.util.regex.Matcher;
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
	// Group 0 is full link, 1 is the set
	private static final String link = "http://magiccards\\.info/scans/en/([^/]*)/[^\"]*";
	private static final Pattern linkPattern = Pattern.compile(link);
	
	private static final Integer A4w = 2480;
	private static final Integer A4h = 3508;
	// Should be 700x995 for 300dpi
	private static final Integer cardW = 718;
	private static final Integer cardH = 1022;
	
	public static final String dataDir = System.getenv("APPDATA") + "\\Cubemaker";
	public static final String cacheDir = dataDir + "\\cached";
	public static final String errorDir = dataDir + "\\errors";
	
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
		// TODO: Bunch of things that could be constants
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
	
	private void updateCount(Card c, int count) {
		c.decCount(count);
		i += count;
	}
	private void updateCount(Card c) {
		updateCount(c, 1);
	}
	
	/**
	 * Download image file from magiccards.info, then save in local cache
	 * 
	 * @param c card to find
	 * @return true if successful, false if something failed
	 * @throws IOException
	 */
	private BufferedImage findOnline(Card c) throws IOException {
		File tempHtml = new File("temp.html");
		FileUtilities.saveURL("http://magiccards.info/query?q=" + c.getURLName(), tempHtml);		
		Matcher m = FileUtilities.matchInFile(tempHtml, linkPattern);
		tempHtml.delete();
		
		if(m != null) {			
			// Save file to cache
			File saved = new File(new File(cacheDir, m.group(1)), c.getFileName() + ".jpg");
			ImageIndex.instance.addEntry(c, m.group(1));
			FileUtilities.saveURL(m.group(), saved);
			
			return ImageIO.read(saved);
		} else {
			return null;
		}
	}
	
	/**
	 * Find local image file for the given card.
	 * Searches in ./mse, ./pref, %appdata%/CubeMaker/cached, in that order
	 * 
	 * @param c card to find
	 * @return true if file found, false otherwise
	 * @throws IOException
	 */
	private BufferedImage findLocal(Card c) throws IOException {
		String tempName = c.getFileName();
		
		if(c.getSet() == null) {
			File mse = getImage(currDir + "\\mse", tempName);
			if(mse.exists()) {
				return MSEImageFix.fixImage(mse);
			} 
			
			File pref = getImage(currDir + "\\pref", tempName);
			if(pref.exists()) {
				return ImageIO.read(pref);
			}			
		}
		
		File cached = ImageIndex.instance.findCard(c);
		if(cached != null) {
			return ImageIO.read(cached);
		}
		
		return null;
	}
	
	private Boolean findCard(Card c) throws IOException {
		BufferedImage img;
		if ((img = findLocal(c)) != null) {
			// Found locally
		} else if((img = findOnline(c)) != null) {
			// Found online
		} else {
			// Not found
			return false;
		}
		while(i < 9 && c.getCount() > 0) {
			parts[i] = img;
			updateCount(c);
		}
		return true;
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
			FileWriter fw = new FileWriter(new File(errorDir, "missing.txt"));
			fw.write("");
			fw.close();
			
			Scanner s = new Scanner(cubeFile);
			currDir = new File(".").getCanonicalPath();
			
			pBar.initProgress(cubeFile);
			s.useDelimiter("\\s*\\n");
			Card c = new Card("empty", 0);
			while(s.hasNext() || c.getCount() > 0){
				if(c.getCount() == 0) {
					String line = s.next();
					c = Card.parseLine(line);					
				}
				pBar.progCard(c.getName());
				
				Boolean found = findCard(c);
				if(found) {
					if(i == 9){
						pBar.updateProgress("Merging image #" + n);
						mergeImages(9);
						
						i = 0;
						n++;
					}
				} else {
					c.setCount(0);
					fw = new FileWriter(new File(errorDir, "missing.txt"), true);
					fw.write(c.getName()+"\n");
					fw.close();
					System.out.println("Couldn't find " + c.getName());
					try {
						FileUtilities.saveURL(
								"http://magiccards.info/query?q=" + c.getURLName(),
								new File(errorDir, c.getName() +".html")
							);
					} catch (Exception e) {
						
					}
				}
			}
			if(i != 0){
				pBar.updateProgress("Merging image #" + n);
				mergeImages(i);
				n++;
			}
			pBar.finish(n-1);
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
	public void finish(int numImages) {
		this.dispose();
	}
}