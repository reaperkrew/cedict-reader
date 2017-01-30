package com.metapuppy.cedict_reader;

import java.util.List;

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
public class CeDictReaderTest 
			extends TestCase{

	@Ignore("Not ready yet") @Test
	public void testDownloadDict(){
		
	}
	
	@Test
	public void testLoad(){
		CeDictReader reader = new CeDictReader();
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

	public void testLoadDict(){
		CeDictReader dict = new CeDictReader();
		if(dict.contains("和") == true){
			System.out.println("Yes it is there!");
			List<DictionaryEntry> entries = dict.getEntries("和");
			for(DictionaryEntry entry : entries){
				System.out.println(entry.simplified);
				System.out.println(entry.traditional);
				System.out.println(entry.pinyin);
				for(String s : entry.definitions){
					System.out.println(s);
				}
				System.out.println("");
			}
		}
	}

}
