package cubeMaker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import utilities.FileUtilities;

public class ImageIndex {
	private static final String dataDir = System.getenv("APPDATA") + "\\Cubemaker";
	private static final String cacheDir = dataDir + "\\cached";
	private static final File indexFile = new File(cacheDir, "index.txt");
	private static final String entry = "([^\\t]*)\\t([^\\t]*)";
	private static final Pattern entryPattern = Pattern.compile(entry);

	private HashMap<String, ArrayList<String>> map;
	
	public ImageIndex() {
		map = new HashMap<String, ArrayList<String>>();
		loadFile();
	}
	
	private void loadFile() {
		try {			
			Scanner s = new Scanner(indexFile);
			s.useDelimiter("\n");
			while(s.hasNext(entryPattern)) {
				s.next(entryPattern);
				MatchResult match = s.match();
				
				map.put(match.group(1), strToList(match.group(2)));
			}
		} catch (FileNotFoundException e) {
			// Nothing to load, which is probably fine
		}
	}
	
	private void saveFile() {
		try {
			FileUtilities.fixDirs(indexFile);
			FileWriter fw = new FileWriter(indexFile);
			for(Map.Entry<String, ArrayList<String>> pair : map.entrySet()) {
				fw.write(pair.getKey() + "\t" + listToStr(pair.getValue()));
			}
			fw.close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addEntry(Card c, String set) {
		String key = c.getFileName();
		ArrayList<String> list = map.get(key);
		if(list != null) {
			list.add(set);
		} else {
			list = new ArrayList<String>();
			list.add(set);
			map.put(key, list);
		}
	}
	
	private static ArrayList<String> strToList(String entry) {
		String[] parts = entry.split(",");
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(parts));		
		return list;
	}
	
	private static String listToStr(ArrayList<String> list) {
		StringBuilder sb = new StringBuilder();
		for(String s : list) {
			sb.append(s);
			sb.append(",");
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		ImageIndex ii = new ImageIndex();
		Card c = new Card("Akroma, Angel of Pwn");
		ii.addEntry(c, "tsts");
		ii.addEntry(c, "tmp");
		c = new Card("Akroma, Angel of Pawwn");
		ii.addEntry(c, "tmp");
		for(Map.Entry<String, ArrayList<String>> pair : ii.map.entrySet()) {
			System.out.println(pair.getKey() + ": " + listToStr(pair.getValue()));
		}
		ii.saveFile();
	}
}
