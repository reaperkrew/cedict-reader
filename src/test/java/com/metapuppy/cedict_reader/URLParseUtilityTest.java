package com.metapuppy.cedict_reader;


import org.junit.Test;

import com.metapuppy.cedict_reader.util.URLParseUtility;

import junit.framework.TestCase;

public class URLParseUtilityTest 
				extends TestCase{

	@Test
	public void testGetFilenameFromURL(){
		String url = "https://www.mdbg.net/chindict/export/cedict/cedict_1_0_ts_utf-8_mdbg.zip";
		String expected = "cedict_1_0_ts_utf-8_mdbg.zip";
		String actual = URLParseUtility.getFilenameFromURL(url);
		assertEquals(expected, actual);		
	}

}
