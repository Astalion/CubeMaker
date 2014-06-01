package cubeMaker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
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
	
	public static final ImageIndex instance = new ImageIndex();

	private HashMap<String, String> map;
	
	private ImageIndex() {
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
			// Nothing to load, which is probably fine
		}
	}
	
	/*
	private void saveFile() {
		try {
			FileUtilities.fixDirs(indexFile);
			PrintWriter pw = new PrintWriter(indexFile);
			for(Map.Entry<String, String> pair : map.entrySet()) {
				pw.println(pair.getKey() + "\t" + pair.getValue());
			}
			pw.close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	
	public void addEntry(Card c, String set) {
		String key = c.getFileName();
		String value = map.get(key);
		
		if(value == null) {
			map.put(key, set);
			try {
				// Append new set to file
				FileUtilities.fixDirs(indexFile);
				FileWriter pw = new FileWriter(indexFile, true);
				pw.write(key + "\t" + set  + "\n");
				pw.close();			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public File findCard(Card c) {
		String name = c.getFileName();
		
		String set = c.getSet();
		if(set == null) {
			set = map.get(name);
			if(set == null) return null;
		}
		
		File f = new File(new File(cacheDir, set), name + ".jpg");
		if(f.exists()) {
			return f;
		} else {
			return null;
		}
	}
	
	/*
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
	*/
}
