package com.metapuppy.cedict_reader;

import java.util.List;

public class Test {

	public static void main(String[] args) {

		Test test = new Test();
		test.testFirstEntry();
	}
	
	public void testFirstEntry(){
		CeDictReader dict = new CeDictReader();
		DictionaryEntry entry = dict.getFirstEntry("中华");
		if(entry != null){
			System.out.println(entry.simplified);
			System.out.println(entry.traditional);
			System.out.println(entry.pinyin);
			for(String s : entry.definitions){
				System.out.println(s);
			}
		}

	}
	
	public void testLoadDict(){
		CeDictReader dict = new CeDictReader();
		if(dict.contains("和") == true){
			System.out.println("Yes it is there!");
			List<DictionaryEntry> entries = dict.getEntries("得");
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
