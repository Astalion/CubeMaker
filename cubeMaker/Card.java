package cubeMaker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Card {
	private static String[] badsets = {"vma"};
	private static String excludestring;
	
	private String name;
	private String set;
	private int count;

	/*
	 * Constructors
	 */
	public Card(String name, String exp, int count) {
		if(excludestring == null) {
			StringBuilder sb = new StringBuilder("+not+(");
			for(int i = 0; i < badsets.length; i++) {
				sb.append("e:" + badsets[i]);
				if(i == badsets.length - 1) break;
				sb.append("+or+");
			}
			sb.append(")");
			excludestring = sb.toString();
		}
		this.name = name;
		this.set = exp;
		this.count = count;
	}
	public Card(String name) {
		this(name, null, 1);
	}
	public Card(String name, String exp) {
		this(name, exp, 1);
	}
	public Card(String name, int count) {
		this(name, null, count);
	}
	

	/*
	 * Matches a card line.
	 * Groups are:
	 * 	1 - Count
	 * 	2 - Card name
	 * 	3 - Expansion
	 */
	private static final String lineString = "(?:([0-9]+)[Xx]?\\s)?\\s*([^\\[\\]]+)\\s*(?:\\s+\\[([^\\[\\]]+)\\])?[^\\[\\]]*";
	private static final Pattern linePattern = Pattern.compile(lineString);
	public static Card parseLine(String line) {
		if(line.charAt(0) == '\"') {
			return new Card(line.substring(1, line.length()-1));
		}
		Matcher m = linePattern.matcher(line);
		if(m.matches()) {
			Integer count;
			try {
				count = Integer.parseInt(m.group(1));				
			} catch (NumberFormatException e) {
				count = null;
			}
			String name = m.group(2);
			String exp = m.group(3);
			
			if(count != null) {
				if(exp != null) {
					// name & exp & count
					return new Card(name, exp, count);
				} else {
					// name & count
					return new Card(name, count);
				}
			} else {
				if(exp != null) {
					// name & exp
					return new Card(name, exp);
				} else {
					// name
					return new Card(name);
				}				
			}
		} else {
			return null;
		}
	}
	
	public static void main(String[] args) {
		String s2 = "Akroma, Angel of Wrath";
		System.out.println(s2.replaceAll("[',]", ""));
		String s = "2 Pack Rat [RTR]    ";
		Matcher m = linePattern.matcher(s);
		System.out.println(m.matches());
		System.out.println(Integer.parseInt(m.group(1)));
		for(int i = 0; i < 4; i++) {
			System.out.println(i + ": " + m.group(i));
		}
	}
	
	/*
	 * Getters
	 */
	private static final String mcards = "http://magiccards.info/query?q=";
	public String getMcardsURL() {
		return mcards + "n!\"" + name.replace(" ", "+") + (set != null ? "\"%20e:" + set : "\"" + excludestring);
	}
	
	private static final String mtgImage = "http://mtgimage.com/";
	public String getMtgImageURL() {
		return mtgImage + (set == null ? "card/" : "set/" + set + "/") + name.replace(" ", "_").replace("[\\?\\!]", "") + ".jpg";
	}
	public String getMtgImageCrop() {
		return mtgImage + (set == null ? "card/" : "set/" + set + "/") + name.replace(" ", "_") + ".crop.jpg";
	}
	
	public String getFileName() {
		return name.replaceAll("[',\\!\\?]", "");
	}
	public String getName() {
		return name;
	}
	public String getSet() {
		return set;
	}
	public int getCount() {
		return count;
	}
	
	/*
	 * Setters
	 */
	public void decCount(int amount) {
		this.count -= amount;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	
}
