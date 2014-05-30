package CubeMaker;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Checks what has already been printed, and what cards to remove from previous cube version
 * @author Micke
 *
 */
public class CopyCheck {
	static final String oldFile = "printed.txt";
	static final String cubeFile = "cube.txt";
	static final String replaceFile = "replace.txt";
	static String latestFile;
	
	private static HashMap<String, Boolean> addFiles(String fileFile) throws FileNotFoundException {
		HashMap<String, Boolean> m = new HashMap<String, Boolean>();
		Scanner sc = new Scanner(new File(fileFile));
		sc.useDelimiter("\\s*\n");
		while(sc.hasNext()){
			String s = sc.next();
			if (s.matches("\\s*")) { break; };
			m = addFile(s, m);
			latestFile = s;
		}
		return m;
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, Boolean> addFile(String cardFile, HashMap<String, Boolean> m) throws FileNotFoundException {		
		HashMap<String, Boolean> newMap = (HashMap<String, Boolean>) m.clone();
		Scanner sc = new Scanner(new File(cardFile));
		sc.useDelimiter("\\s*\n");
		while(sc.hasNext()){
			String s = sc.next();
			if (s.matches("\\s*")) { break; };
			newMap.put(s, true);
		}		
		return newMap;
	}
	
	public static void copyCheck(HashMap<String, Boolean> printed, HashMap<String, Boolean> toPrint, String outFile) {
		
		try {
			FileWriter fw = new FileWriter(new File(outFile));
			
			for(String s : toPrint.keySet()) {
				if (s.matches("\\s*")) { break; };
				
				if(printed.containsKey(s)) {
					System.out.println("Already printed: " + s);
				} else {
					fw.write(s + "\n");
				}				
			}
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		final String newFile = "cubev2.1large.txt";
		
		HashMap<String, Boolean> printed;
		HashMap<String, Boolean> potentialPrints = new HashMap<String, Boolean>();
		HashMap<String, Boolean> latestEdition = new HashMap<String, Boolean>();
		try {
			printed = addFiles(oldFile);
			potentialPrints = addFile(newFile, potentialPrints);
			latestEdition = addFile(latestFile, latestEdition);
			copyCheck(printed, potentialPrints, cubeFile);
			copyCheck(potentialPrints, latestEdition, replaceFile);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

}
