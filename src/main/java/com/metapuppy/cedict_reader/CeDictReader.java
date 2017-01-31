package com.metapuppy.cedict_reader;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.metapuppy.cedict_reader.util.URLParseUtility;
import com.metapuppy.cedict_reader.util.UnzipUtility;
import com.metapuppy.cedict_reader.DictionaryEntry;


/**
 * 
 * @author gmccarthy
 *
 */
public class CeDictReader {
	private static final Logger logger = LogManager.getLogger(CeDictReader.class.getName());
	private List<DictionaryEntry> entries;
	private Path userSpecifiedPath = null;
	private static final String UNZIPPED_FILENAME = "cedict_ts.u8";
	private static final String CEDICT_DOWNLOAD_URL = "https://www.mdbg.net/chindict/export/cedict/cedict_1_0_ts_utf-8_mdbg.zip";
	
	
	/**
	 * creates a new <code>CeDictReader</code> object and loads the 
	 * CEDict specified by in the <code>Path</code>.
	 * @param pathToDict the <code>Path</code> where the CEDict file can be found.
	 */
	public CeDictReader(Path pathToDict){			
		this.userSpecifiedPath = pathToDict;		
		if(dictExists(pathToDict) == false){
			logger.fatal("Dictionary not found in {}. You should make sure this application has "
					+ "sufficient privileges to access the folder. You can call downloadDictionary(Path) "
					+ "for the application to download it automatically.", pathToDict.toString());
			System.exit(0);			
		}
		this.loadDictionary();
	}
	
	/**
	 * creates a new <code>CeDictReader</code> object and loads the 
	 * default CEDict packaged with the application's resources.
	 */
	public CeDictReader(){
		this.loadDictionary();
	}
		
	/**
	 * loads the CE-Dict file into memory.
	 */
	private void loadDictionary(){
		StopWatch sw = new StopWatch();
		sw.start();
		
		Path dict =	this.userSpecifiedPath == null ? 
					Paths.get(getClass().getClassLoader().getResource(UNZIPPED_FILENAME).getPath()) : 
						this.userSpecifiedPath.resolve(UNZIPPED_FILENAME);
		
		if(this.userSpecifiedPath == null){
			logger.info("loading dictionary from application resources.");
		}else{
			logger.info("loading dictionary from user specified location: {}", 
					this.userSpecifiedPath.resolve(UNZIPPED_FILENAME).toString());
		}
					
		this.entries = new ArrayList<DictionaryEntry>();

		//read file into stream, try-with-resources
		try(Stream<String> stream = Files.lines(dict).filter(s->s.startsWith("#") == false))
		{
		for (String s : (Iterable<String>) () -> stream.iterator()) {
	        DictionaryEntry entry = new DictionaryEntry();
	        entry.traditional = s.split(" ")[0];
	        entry.simplified = s.split(" ")[1];
	        entry.pinyin = StringUtils.substringBetween(s, "[", "]");
	        entry.definitions = Arrays.copyOfRange(s.split("/"), 1, s.split("/").length);
	        this.entries.add(entry);
	    }

		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal("Caught an IOException while parsing the dictionary: " + e.getMessage());
			System.exit(0);
		} catch(Exception e){
			e.printStackTrace();
			logger.fatal("Caught an unknown error while parsing the dictionary: " + e.getMessage());
			System.exit(0);
		}
		
		sw.stop();
		logger.info("Finished loading dictionary. Time: {}ms. Entries: {}", sw.getTime(), entries.size());

	}
	

	/**
	 * downloads the latest CE-Dict dictionary and stores it 
	 * in <code>destination</code> specified by the user.
	 */
	public static void downloadDictionary(Path destination){		

		String dictURL = CEDICT_DOWNLOAD_URL;
		logger.info("preparing to download CE dict from default URL {}...", dictURL);
		File dir = null;
		
		try{
			dir = new File(destination.toString());
			if(dir.exists() == false){
				throw new InvalidPathException(destination.toString(), "Path not found.");
			}
		}catch(InvalidPathException e){

			logger.error("Error accessing path " + destination.toString() + ": " + e.getMessage());
			return;	
		}
		
		String zippedFilename = URLParseUtility.getFilenameFromURL(dictURL);
		
		File dict = destination.resolve(zippedFilename).toFile();
		try {
			FileUtils.copyURLToFile(new URL(dictURL), dict);
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return;
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return;
		}
		
		// verify file is there
		if(dict.exists() == false){
			logger.fatal("Can't find " + dict.getAbsolutePath());
			return;
		} else{
			logger.info("CEDict saved to " + dict.getAbsolutePath() + ". Unzipping...");
		}
		
		
		try{
			if(Files.exists(destination.resolve(UNZIPPED_FILENAME))){
				logger.info("Deleting old dictionary before unzipping new one...");
				Files.delete(destination.resolve(UNZIPPED_FILENAME));
			}

		}catch(InvalidPathException e){
			logger.error("Error accessing old dictionary");
		}catch(IOException e){
			logger.error("Error attempting to delete old dictionary: " + e.getMessage());		
		}

		UnzipUtility unzipper = new UnzipUtility();
		try {
			unzipper.unzip(dict.getAbsolutePath(), destination.toString());
		} catch (IOException e) {
			logger.fatal(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
		
		// verify file exists
		try{
			if(dictExists(destination) == false){
				throw new InvalidPathException(destination.resolve(UNZIPPED_FILENAME).toString(), "Not found");
			}

		}catch(InvalidPathException e){
			logger.fatal(e.getMessage());
			System.exit(0);
		}

		logger.info("Deleting .zip file...");
		try
		{
			dict.delete();
		}
		catch(Exception e){
			logger.error("Caught an exception trying to delete the .zip file after downloading the dictionary. Continuing execution...");
		} 
	}
	
	/**
	 * gets the first entry in the CE-Dict that matches <code>word</code>.
	 * @param word the word to search for in the dictionary.
	 * @return a single <code>DictionaryEntry</code> object. Returns <code>null</code> if no entry is found.
	 */
	public DictionaryEntry getFirstEntry(String word) {
		for(DictionaryEntry entry : entries){
			if(entry.simplified.equals(word) || entry.traditional.equals(word)){
				return entry;
			}
		}		
		return null;
	}

	/**
	 * gets all entries in the CE-Dict that match <code>word</code>
	 * @param word the word to search for in the dictionary.
	 * @return a list of zero or more <code>DictionaryEntry</code> objects. If no entries are found the size will be zero.
	 */
	public List<DictionaryEntry> getEntries(String word) {
		List<DictionaryEntry> resultEntries = new ArrayList<DictionaryEntry>();
		for(DictionaryEntry entry : entries){
			if(entry.simplified.equals(word) || entry.traditional.equals(word)){
				resultEntries.add(entry);
			}
		}
		
		return resultEntries;
	}
	
	/**
	 * checks the dictionary to see if <code>word</code> exists.
	 * @param word the word to search for in the dictionary.
	 * @return true if an entry is found.
	 */
	public boolean contains(String word) {
		for(DictionaryEntry entry : entries){
			if(entry.simplified.equals(word) || entry.traditional.equals(word)){
				return true;
			}
		}	
		return false;
	}
	
	/**
	 * tests whether a CEDict dictionary file exists in the specified <code>Path</code>.
	 * @param pathToDict the path to check that contains the CEDict dictionary file.
	 * @return true if a CEDict dictionary file is found.
	 */
	public static boolean dictExists(Path pathToDict){
		try{
			return Files.exists(pathToDict.resolve(UNZIPPED_FILENAME));		
		}catch(Exception e){
			return false;
		}
	}

}
