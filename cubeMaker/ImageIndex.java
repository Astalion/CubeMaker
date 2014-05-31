package cubeMaker;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class ImageIndex {
	private static final String dataDir = System.getenv("APPDATA") + "\\Cubemaker";
	private static final String cacheDir = dataDir + "\\cached";
	private static final File indexFile = new File(cacheDir, "index.txt");
	private static final String entry = "([^\\t]*)\\t([^\\t]*)";
	private static final Pattern entryPattern = Pattern.compile(entry);

	private HashMap<String, String> map;
	
	public ImageIndex() {
		map = new HashMap<String, String>();
		loadFile();
	}
	
	private void loadFile() {
		try {			
			Scanner s = new Scanner(indexFile);
			s.useDelimiter("\n");
			while(s.hasNext(entryPattern)) {
				s.next(entryPattern);
				MatchResult match = s.match();
				
				map.put(match.group(1), match.group(2));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addEntry(Card c, String set) {
		String key = c.getFileName();
		String oldV = map.get(key);
		if(oldV != null) {
			set = oldV + "," + set;
		}
		map.put(key, set);
	}
}
