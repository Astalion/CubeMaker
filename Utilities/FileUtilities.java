package Utilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtilities {

	private static void fixDirs(File target) {
		File parent = target.getParentFile();
		if (parent != null) {
			parent.mkdirs();			
		}
		
	}
	
	public static void saveURL(String urlString, File outFile)
	{
		fixDirs(outFile);
	    BufferedInputStream in = null;
	    FileOutputStream fout = null;
	    try
	    {
	            in = new BufferedInputStream(new URL(urlString).openStream());
	            fout = new FileOutputStream(outFile);
	
	            byte data[] = new byte[1024*1024];
	            int count;
	            while ((count = in.read(data, 0, 1024*1024)) != -1)
	            {
	                    fout.write(data, 0, count);
	            }
	    } catch (Exception e) {
			e.printStackTrace();
		}
	    finally
	    {
	            if (in != null)
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	            if (fout != null)
					try {
						fout.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	    }
	}
	

	// http://stackoverflow.com/a/115086/2295872
	public static void copyFile(File sourceFile, File destFile) throws IOException {
		fixDirs(destFile);
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }
	
	    FileChannel source = null;
	    FileChannel destination = null;
	
	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}

	public static String findInFile(String fileName, Pattern pattern) throws FileNotFoundException {
		Scanner sFile = new Scanner(new File(fileName));
		
		while (sFile.hasNextLine()) {
			String line = sFile.nextLine();
			Scanner sLine = new Scanner(line);
			String found = sLine.findInLine(pattern);
			if(found != null) {
				return found;
			}
		}
		return null;
	}

	public static String findInFile(String fileName, Pattern pattern, int group) throws FileNotFoundException {
		Scanner sFile = new Scanner(new File(fileName));
		
		while (sFile.hasNextLine()) {
			String line = sFile.nextLine();
			Matcher m = pattern.matcher(line);
			Boolean found = m.find();
			if(found) {
				return m.group(group);
			}
		}
		return null;
	}

	public static ArrayList<String> findAllInFile(String fileName, Pattern pattern) throws FileNotFoundException {
		ArrayList<String> ret = new ArrayList<String>();
		Scanner sFile = new Scanner(new File(fileName));
		
		while (sFile.hasNextLine()) {
			String line = sFile.nextLine();
			Scanner sLine = new Scanner(line);
			String found = sLine.findInLine(pattern);
			if(found != null) {
				ret.add(found);
			}
		}
		return ret;
	}

	public static boolean deleteDirectory(File directory) {
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
	            for(int i=0; i<files.length; i++) {
	                if(files[i].isDirectory()) {
	                    deleteDirectory(files[i]);
	                }
	                else {
	                    files[i].delete();
	                }
	            }
	        }
	    }
	    return(directory.delete());
	}

	public static String fileExt(File f) {
		String fileName = f.getName();
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			return fileName.substring(i+1);
		}
		return "";
	}

}
