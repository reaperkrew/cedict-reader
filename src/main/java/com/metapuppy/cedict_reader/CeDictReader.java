package com.metapuppy.cedict_reader;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
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
	private final Properties props = new Properties();
	private static final Logger logger = LogManager.getLogger(CeDictReader.class.getName());
	private List<DictionaryEntry> entries;
	
	public CeDictReader(Path pathToPropertiesFile){
		
	}
	
	public CeDictReader(){
		this.loadConfiguration();
		this.loadDictionary();
	}
	
	/**
	 * loads the CE-Dict file into memory.
	 */
	private void loadDictionary(){
		StopWatch sw = new StopWatch();
		sw.start();
		if(dictExists() == false){
			logger.fatal("Dictionary not found. Either manually place it in " + props.getProperty("pathToDict") +
						" or call loadDictionary(true) so that the application can download it automatically.");
			System.exit(0);			
		}
		
		Path dir = Paths.get(props.getProperty("pathToDict"));
		Path dict = dir.resolve(props.getProperty("unzippedFilename"));
		
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
		} catch(Exception e){
			
		}
		
		sw.stop();
		logger.info("Finished loading dictionary. Time: {}ms. Entries: {}", sw.getTime(), entries.size());

	}
	
	
	private void loadConfiguration(){
		    try (final InputStream stream =
					getClass().getClassLoader().getResourceAsStream("cedict-reader.properties")){
				this.props.load(stream);
			} catch (IOException e) {
				e.printStackTrace();
				logger.fatal("Error loading properties file: " + e.getMessage());
				System.exit(0);
			}
	}
	
	/**
	 * downloads the latest CE-Dict dictionary and stores it 
	 * in <code>pathToDict</code> specified in the properties file.
	 */
	public void downloadDictionary(){
		String dictURL = this.props.getProperty("urlToCedictDownload");
		logger.info("preparing to download CE dict from default URL {}...", dictURL);
		String pathToDict = this.props.getProperty("pathToDict");
		Path dir = null;
		try{
			dir = Paths.get(pathToDict);
		}catch(InvalidPathException e){

			logger.fatal("Error accessing path " + pathToDict + ": " + e.getMessage());
			System.exit(0);		
		}
		
		String zippedFilename = URLParseUtility.getFilenameFromURL(dictURL);
		
		File dict = dir.resolve(zippedFilename).toFile();
		try {
			FileUtils.copyURLToFile(new URL(dictURL), dict);
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
		
		// verify file is there
		if(dict.exists() == false){
			logger.fatal("Can't find " + dict.getAbsolutePath());
			System.exit(0);
		} else{
			logger.info("CEDict saved to " + dict.getAbsolutePath() + ". Unzipping...");
		}
		
		String unzippedFilename = props.getProperty("unzippedFilename");
		try{
			if(Files.exists(dir.resolve(unzippedFilename))){
				logger.info("Deleting old dictionary before unzipping new one...");
				Files.delete(dir.resolve(unzippedFilename));
			}

		}catch(InvalidPathException e){
			logger.error("Error accessing old dictionary");
		}catch(IOException e){
			logger.error("Error attempting to delete old dictionary: " + e.getMessage());
		}

		UnzipUtility unzipper = new UnzipUtility();
		try {
			unzipper.unzip(dict.getAbsolutePath(), pathToDict);
		} catch (IOException e) {
			logger.fatal(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
		
		// verify file exists
		try{
			dir.resolve(unzippedFilename);

		}catch(InvalidPathException e){
			logger.fatal("Cannot locate " + dir.resolve(unzippedFilename).toString() + " after unzipping! Cannot continue.");
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
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		for(DictionaryEntry entry : entries){
			if(entry.simplified.equals(word) || entry.traditional.equals(word)){
				return true;
			}
		}	
		return false;
	}
	
	private boolean dictExists(){
		try{
			Path dir = Paths.get(props.getProperty("pathToDict"));
			return Files.exists(dir.resolve(props.getProperty("unzippedFilename")));
		
		}catch(InvalidPathException e){
			return false;
		}catch(Exception e){
			return false;
		}
		

	}

}
