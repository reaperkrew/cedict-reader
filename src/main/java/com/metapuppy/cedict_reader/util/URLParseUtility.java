package com.metapuppy.cedict_reader.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class URLParseUtility {
	private static final Logger logger = LogManager.getLogger(URLParseUtility.class.getName());
	public static String getFilenameFromURL(String URL){
		String filename = "";
		try{
			filename = URL.substring(URL.lastIndexOf("/") + 1);
		}catch (IndexOutOfBoundsException e){
			logger.fatal("Caught an exception attempting to parse the filename from the CEDict download url: " + e.getMessage());
			System.exit(0);
		}
		return filename;
		
	}

}
