package cubeMaker;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Checks what has already been printed, and what cards to remove from previous cube version
 * @author Micke
 *
 */
public class CopyCheck {
	static final File printedFile = new File(CubeMaker.dataDir, "printed.txt");
	static final File printedDir = new File(CubeMaker.dataDir, "printed");
	static File latestFile;
	
	private static HashMap<String, Integer> addFiles(File fileFile) throws FileNotFoundException {
		HashMap<String, Integer> m = new HashMap<String, Integer>();
		Scanner sc = new Scanner(fileFile);
		sc.useDelimiter("\\s*\n");
		while(sc.hasNext()){
			String s = sc.next();
			if (s.matches("\\s*")) { break; };
			File f = new File(printedDir, s);
			m = addFile(f, m);
			latestFile = f;
		}
		sc.close();
		return m;
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, Integer> addFile(File cardFile, HashMap<String, Integer> m) 
			throws FileNotFoundException {		
		HashMap<String, Integer> newMap = (HashMap<String, Integer>) m.clone();
		Scanner sc = new Scanner(cardFile);
		sc.useDelimiter("\\s*\n");
		while(sc.hasNext()){
			String line = sc.next();
			Card c = Card.parseLine(line);
			
			String name = c.getName();
			
			Integer v1 = newMap.get(name);
			if(v1 == null) {
				v1 = 0;
			}
			Integer v2 = c.getCount();
			
			if(v2 > v1) {
				v1 = v2;
			}
			newMap.put(name, v1);
		}
		sc.close();
		return newMap;
	}
	
	public static void copyCheck(HashMap<String, Integer> printed, 
			HashMap<String, Integer> toPrint, File outFile) {
		
		try {
			FileWriter fw = new FileWriter(outFile);
			
			for(Map.Entry<String, Integer> pair : toPrint.entrySet()) {
				String key = pair.getKey();
				Integer val = pair.getValue();
				if (key.matches("\\s*")) { break; };
				
				if(printed.containsKey(key)) {
					Integer numPrinted = printed.get(key);
					if(val > numPrinted) {
						fw.write((val-numPrinted) + "x " + key + "\n");						
					} else {
						System.out.println("Already printed: " + key);
					}
				} else {
					fw.write(val + "x " + key + "\n");
				}	
			}
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		getDiff(new File("cubev2.1large.txt"), new File("cube.txt"), new File("replace.txt"));
	}
	
	public static void getDiff(File newFile, File printFile, File replaceFile) {		
		HashMap<String, Integer> printed;
		HashMap<String, Integer> potentialPrints = new HashMap<String, Integer>();
		HashMap<String, Integer> latestEdition = new HashMap<String, Integer>();
		try {
			printed = addFiles(printedFile);
			potentialPrints = addFile(newFile, potentialPrints);
			latestEdition = addFile(latestFile, latestEdition);
			copyCheck(printed, potentialPrints, printFile);
			copyCheck(potentialPrints, latestEdition, replaceFile);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
	}

}
