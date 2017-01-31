package com.metapuppy.cedict_reader;

import java.util.Arrays;
import java.util.List;

public class DictionaryEntry {
	public final String traditional;
	public final String simplified;
	public final String pinyin;
	public final List<String> definitions;
	
	public DictionaryEntry(String traditional, 
							String simplified, 
							String pinyin, 
							String[] definitions){
		
		this.traditional = traditional;
		this.simplified = simplified;
		this.pinyin = pinyin;
		this.definitions = Arrays.asList(definitions);
	}
	
}
