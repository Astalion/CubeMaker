package cubeMaker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Scanner;
import java.util.regex.MatchResult;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

/**
 * Basic GUI for the cube maker
 */
@SuppressWarnings("serial")
public class CubeMakerGUI extends JFrame implements ProgBar {
	/*
	 * Constants
	 */
	private static final String noFile = "<none chosen>";
	
	private static final String dataDir = System.getenv("APPDATA") + "\\Cubemaker";
	private static final File configFile = new File(dataDir, "config.ini");
	private static final String configPattern = "([^\\t]*)\\t([^\\t]*)";
	
	/*
	 * Member variables
	 */	
	private JProgressBar progBar;
	private JLabel progText;
	private int progress;
	
	private JButton cubeFileButton;
	private JLabel cubeFileText;
	private File cubeFile;
	
	private JButton saveDirButton;
	private JLabel saveDirText;
	private File saveDir;
	
	private JButton makeButton;
	
	private CubeMakerGUI window = this;
	
	public CubeMakerGUI() {
		super("Cube Maker");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		JPanel bgPane = new JPanel();
		
		/*
		 * Cube file chooser
		 */
		JPanel cubeFilePane = new JPanel();
		cubeFileButton = new JButton("Choose cube file");
		cubeFileButton.addActionListener(new cubeFileHandler());
		cubeFilePane.add(cubeFileButton);
		
		cubeFileText = new JLabel(noFile);
		cubeFilePane.add(cubeFileText);
		
		/*
		 * Save directory chooser
		 */
		JPanel saveDirPane = new JPanel();
		saveDirButton = new JButton("Choose save directory");
		saveDirButton.addActionListener(new saveDirHandler());
		saveDirPane.add(saveDirButton);
		
		saveDirText = new JLabel(noFile);
		saveDirPane.add(saveDirText);
		
		/*
		 * Progress bar and text
		 */
		JPanel progPane = new JPanel();
		progPane.setLayout(new BoxLayout(progPane, BoxLayout.PAGE_AXIS));
		
		progText = new JLabel(" ");
		progText.setAlignmentX(CENTER_ALIGNMENT);
		progPane.add(progText);
		
		progBar = new JProgressBar();
	    progBar.setStringPainted(true);
		progPane.add(progBar);
		
		/*
		 * Make cube button
		 */
		JPanel makePane = new JPanel();
		
		makeButton = new JButton("Make cube");
		makeButton.addActionListener(new makeHandler());
		makePane.add(makeButton);
		
		/*
		 * Finishing touches
		 */
		bgPane.setLayout(new BoxLayout(bgPane, BoxLayout.PAGE_AXIS));
		bgPane.add(cubeFilePane);
		bgPane.add(saveDirPane);
		bgPane.add(progPane);
		bgPane.add(makePane);
		
		loadConfig();
		
		add(bgPane);
		pack();
		setVisible(true);
		setResizable(false);
	}
	
	/*
	 * Loads paths from config
	 */
	private void loadConfig() {
		try {			
			Scanner s = new Scanner(configFile);
			s.useDelimiter("\n");
			while(s.hasNext(configPattern)) {
				s.next(configPattern);
				MatchResult match = s.match();
				
				if(match.group(1).equals("cube")) {
					cubeFile = new File(match.group(2));
					cubeFileText.setText(cubeFile.getCanonicalPath());
				} else if(match.group(1).equals("save")) {
					saveDir = new File(match.group(2));
					saveDirText.setText(saveDir.getCanonicalPath());					
				}
			}
		} catch (FileNotFoundException e) {
			cubeFile = null;
			cubeFileText.setText(noFile);
			saveDir = null;
			saveDirText.setText(noFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Saves path config to file
	 */
	private void saveConfig() {
		FileWriter fw;
		try {
			fw = new FileWriter(configFile);
			if(cubeFile != null) {
				fw.write("cube\t" + cubeFile.getAbsolutePath() + "\n");
			}
			if(saveDir != null) {
				fw.write("save\t" + saveDir.getAbsolutePath() + "\n");				
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Handles choosing cube input file
	 */
	private class cubeFileHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			// Open file choice dialog in current directory
			final JFileChooser fc = new JFileChooser(new File("."));
			int returnVal = fc.showOpenDialog(cubeFileButton);
			
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				// Set chosen file
				cubeFile = fc.getSelectedFile();
				try {
					cubeFileText.setText(cubeFile.getCanonicalPath());
				} catch (IOException e) {
					cubeFileText.setText(cubeFile.getName());
				}
			}
			saveConfig();
			window.pack();
		}
	}
	
	/*
	 * Handles choosing output file directory
	 */
	private class saveDirHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			// Open file choice dialog in current directory
			final JFileChooser fc = new JFileChooser(new File("."));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showOpenDialog(saveDirButton);
			
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				// Set chosen file
				saveDir = fc.getSelectedFile();
				try {
					saveDirText.setText(saveDir.getCanonicalPath());
				} catch (IOException e) {
					saveDirText.setText(saveDir.getName());
				}
			}
			saveConfig();
			window.pack();
		}
	}
	
	/*
	 * Handles initiating the making of cueb
	 */
	private class makeHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			if(saveDir != null && cubeFile != null) {
				new SwingWorker<Integer, Integer>() {
					@Override
					protected Integer doInBackground() throws Exception {
						CubeMaker cm = new CubeMaker(saveDir, cubeFile, (ProgBar) window);
						cm.makeCube();
						return null;
					}					
				}.execute();			
			}
		}
	}

	public static void main(String[] args) {
		new CubeMakerGUI();
	}
	
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
	@Override
	public void initProgress(File cardFile) {
		int numLines = countLines(cardFile);
	    int numResult = (numLines - 1)/9 + 1;
	    
	    progress = 0;
	    progBar.setMinimum(0);
	    progBar.setMaximum(numLines + numResult);
	    
	    // TODO Show progress container
	}

	@Override
	public void progCard(String cardName) {
		updateProgress("Loading: " + cardName);		
	}

	@Override
	public void updateProgress(String status) {
		progText.setText(status);
		progBar.setValue(progress);
		progress++;
		//pack();
	}

	@Override
	public void finish() {
		progBar.setValue(progress);
		progText.setText("Done!");
		
		// TODO Hide progress container		
	}

}
