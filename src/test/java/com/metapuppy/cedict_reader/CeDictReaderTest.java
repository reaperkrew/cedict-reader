package com.metapuppy.cedict_reader;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import junit.framework.TestCase;

/**
 * This tests the CeDictReader class's methods of loading and querying the
 * words in the dictionary. Do NOT run these tests unless you have already
 * stored the CEDict in a location on your file system specified in the 
 * properties file. The CeDictReader cannot be initialized unless it has
 * read and write access to the 'pathToDict' location.
 * @author gmccarthy
 *
 */
public class CeDictReaderTest extends TestCase{
	
	@Ignore("Run this only after you have made a directory and given privileges to the application.")
	public void testDownloadDict(){
		Path pathToDict = Paths.get("/etc/cedict");
		CeDictReader.downloadDictionary(pathToDict);
		assertTrue(CeDictReader.dictExists(pathToDict));
	}
	
	@Test
	public void testLoad(){
		CeDictReader reader = new CeDictReader();
		assertNotNull(reader);
	}	
	
	@Ignore("Run this only after you have made a directory and given privileges to the application.")
	public void testLoadUserSpecifiedPath(){
		Path pathToDict = Paths.get("/etc/cedict");
		CeDictReader reader = new CeDictReader(pathToDict);
		assertNotNull(reader);
	}
	
	@Test
	public void testContains(){
		String wordInDict = "中国";
		String wordNotInDict = "绝对不在词典里";
		CeDictReader reader = new CeDictReader();
		assertTrue(reader.contains(wordInDict));
		assertFalse(reader.contains(wordNotInDict));
	}



}
