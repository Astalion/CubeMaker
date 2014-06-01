package cubeMaker;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
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
	private JTextField cubeFileText;
	
	private JButton saveDirButton;
	private JTextField saveDirText;
	
	private JButton makeButton;
	
	private CubeMakerGUI window = this;
	
	public CubeMakerGUI() {
		super("Cube Maker");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		JPanel bgPane = new JPanel();
		
		JPanel inputPane = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.ipady = 4;
		c.ipadx = 6;
		c.insets = new Insets(5, 2, 0, 2);
		
		/*
		 * Cube file chooser
		 */
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		inputPane.add(new JLabel("Cube file:"), c);
		
		cubeFileText = new JTextField(noFile);
		cubeFileText.setPreferredSize(new Dimension(400, 20));
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		inputPane.add(cubeFileText, c);
		
		cubeFileButton = new JButton("Choose");
		cubeFileButton.addActionListener(new cubeFileHandler());
		cubeFileButton.setPreferredSize(new Dimension(80, 20));
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.CENTER;
		inputPane.add(cubeFileButton, c);
		
		/*
		 * Save directory chooser
		 */
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		inputPane.add(new JLabel("Save directory:"), c);
		
		saveDirText = new JTextField(noFile);
		saveDirText.setPreferredSize(new Dimension(400, 20));
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		inputPane.add(saveDirText, c);
		
		saveDirButton = new JButton("Choose");
		saveDirButton.addActionListener(new saveDirHandler());
		saveDirButton.setPreferredSize(new Dimension(80, 20));
		c.gridx = 2;
		c.gridy = 1;
		c.anchor = GridBagConstraints.CENTER;
		inputPane.add(saveDirButton, c);

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
		bgPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
		bgPane.add(inputPane);
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
					//cubeFile = new File(match.group(2));
					cubeFileText.setText(match.group(2));
				} else if(match.group(1).equals("save")) {
					//saveDir = new File(match.group(2));
					saveDirText.setText(match.group(2));					
				}
			}
		} catch (FileNotFoundException e) {
			cubeFileText.setText(noFile);
			saveDirText.setText(noFile);
		}
	}
	
	/*
	 * Saves path config to file
	 */
	private void saveConfig() {
		FileWriter fw;
		try {
			fw = new FileWriter(configFile);
			if(cubeFileText.getText() != "") {
				fw.write("cube\t" + cubeFileText.getText() + "\n");
			}
			if(saveDirText.getText() != "") {
				fw.write("save\t" + saveDirText.getText() + "\n");				
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
			File f = new File(cubeFileText.getText());
			if(f.exists()) {
				f = f.getParentFile();
				if(!f.exists()) {
					f = new File(".");
				}
			} else {
				f = new File(".");
			}
			// Open file choice dialog in current directory
			final JFileChooser fc = new JFileChooser(f);
			int returnVal = fc.showOpenDialog(cubeFileButton);
			
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				// Set chosen file
				File cubeFile = fc.getSelectedFile();
				try {
					cubeFileText.setText(cubeFile.getCanonicalPath());
				} catch (IOException e) {
					//
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
			File f = new File(saveDirText.getText());
			if(f.exists()) {
				f = f.getParentFile();
				if(!f.exists()) {
					f = new File(".");
				}
			} else {
				f = new File(".");
			}
			// Open file choice dialog in current directory
			final JFileChooser fc = new JFileChooser(f);
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showOpenDialog(saveDirButton);
			
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				// Set chosen file
				File saveDir = fc.getSelectedFile();
				try {
					saveDirText.setText(saveDir.getCanonicalPath());
				} catch (IOException e) {
					//
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
			final File saveDir = new File(saveDirText.getText());
			final File cubeFile = new File(cubeFileText.getText());
			if(!saveDir.exists()) {
				JOptionPane.showMessageDialog(
						window, "Save directory not found", "Folder not found", JOptionPane.ERROR_MESSAGE);
			} else if(!cubeFile.exists()) {
				JOptionPane.showMessageDialog(
						window, "Cube file not found", "File not found", JOptionPane.ERROR_MESSAGE);				
			} else {
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
