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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import utilities.FileUtilities;

/**
 * Basic GUI for the cube maker
 */
@SuppressWarnings("serial")
public class CubeMakerGUI extends JFrame implements ProgBar {
	/*
	 * Constants
	 */
	private static final String noFile = "<none chosen>";
	
	private static final File configFile = new File(CubeMaker.dataDir, "config.ini");
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
	
	private JRadioButton directButton;
	private JRadioButton versionedButton;
	
	private JButton makeButton;
	private JButton printedButton;
	
	private CubeMakerGUI window = this;
	
	public CubeMakerGUI() {
		super("Cube Maker");
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
		 * Radio buttons
		 */
		JPanel radioPane = new JPanel();
		
		directButton = new JRadioButton("Direct");
		radioPane.add(directButton);
		
		versionedButton = new JRadioButton("Versioned");
		versionedButton.setSelected(true);
		radioPane.add(versionedButton);
		
		ButtonGroup group = new ButtonGroup();
		group.add(directButton);
		group.add(versionedButton);

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
		
		printedButton = new JButton("Mark as printed");
		printedButton.addActionListener(new markHandler());
		makePane.add(printedButton);
		
		/*
		 * Finishing touches
		 */
		bgPane.setLayout(new BoxLayout(bgPane, BoxLayout.PAGE_AXIS));
		//bgPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

		bgPane.setBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(5, 5, 5, 5),
					BorderFactory.createCompoundBorder(
							BorderFactory.createEtchedBorder(),
							BorderFactory.createEmptyBorder(5, 10, 5, 10)
						)						
					)
				);
		//bgPane.setBorder(BorderFactory.createEtchedBorder());
		bgPane.add(inputPane);
		bgPane.add(radioPane);
		bgPane.add(progPane);
		bgPane.add(makePane);
		
		/*
		JPanel appPane = new JPanel();
		appPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
		appPane.add(bgPane);
		*/	
		
		loadConfig();
		
		add(bgPane);
		pack();
		setLocationRelativeTo(null);
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
				if(directButton.isSelected()) {
					new SwingWorker<Integer, Integer>() {
						@Override
						protected Integer doInBackground() throws Exception {
							CubeMaker cm = new CubeMaker(saveDir, cubeFile, (ProgBar) window);
							cm.makeCube();
							return null;
						}					
					}.execute();					
				} else {
					new SwingWorker<Integer, Integer>() {
						@Override
						protected Integer doInBackground() throws Exception {
							File tempCube = new File(CubeMaker.dataDir, "tempCube.txt");
							File replaceFile = new File(saveDir, "replace.txt");
							CopyCheck.getDiff(cubeFile, tempCube, replaceFile);
							
							CubeMaker cm = new CubeMaker(saveDir, tempCube, (ProgBar) window);
							cm.makeCube();
							tempCube.delete();
							return null;
						}					
					}.execute();
					
				}
			}
		}
	}
	
	private final static String confirmAdd = "Are you sure you want to mark %s as printed?\n" +
			"Doing this means none of its cards will be added to new images";
	private class markHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			final File cubeFile = new File(cubeFileText.getText());
			if(cubeFile.exists()) {
				int resp = JOptionPane.showConfirmDialog(window, 
						String.format(confirmAdd, cubeFile.getName()), 
						"Are you sure?", 
						JOptionPane.YES_NO_OPTION);
				if(resp == JOptionPane.YES_OPTION) {
					new SwingWorker<Integer, Integer>() {
						@Override
						protected Integer doInBackground() throws Exception {
							String fileName = cubeFile.getName();
							
							// Copy cube file
							File destFile = new File(CopyCheck.printedDir, fileName);
							FileUtilities.copyFile(cubeFile, destFile);
							
							// Add to list
							FileWriter fw = new FileWriter(CopyCheck.printedFile, true);
							fw.write(fileName + "\n");
							fw.close();
							return null;
						}					
					}.execute();					
				}			
			} else {
				JOptionPane.showMessageDialog(
						window, "Cube file not found", "File not found", JOptionPane.ERROR_MESSAGE);				
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
		progText.setText("Starting...");
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
	public void finish(int numImages) {
		progBar.setValue(progress);
		progText.setText("Done! Created " + numImages + " images");
		
		// TODO Hide progress container		
	}

}
