package cubeMaker;

import java.io.File;

/**
 * Progress bar for cards
 */
public interface ProgBar {
	/** 
	 * Set input file to use - determines progress length, etc - 
	 * and initialize counter
	 */
	public void initProgress(File cardFile);
	
	/** Update progress with a new card */
	public void progCard(String cardName);
	
	/** Update progress to arbitrary string */
	public void updateProgress(String status);
	
	/** Dispose of bar or whatever */
	public void finish();
}
