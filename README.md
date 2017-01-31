# CE-Dict Reader
## by reaperkrew

CE-Dict Reader is a Java library for parsing the CE-Dict Chinese-English dictionary. This code comes packaged with a CE-Dict file in the resources folder, but it is recommended you use the latest version.

CE-Dict's website can be found at https://cc-cedict.org/wiki/
CE-Dict's download URL can be found at https://www.mdbg.net/chindict/chindict.php?page=cc-cedict

Special thanks to Paul Andrew Denisowski, the original creator of CEDICT

### Usage:
Get all entries for a word.
```java
    CeDictReader dict = new CeDictReader();
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
```

Get the first entry for a word.
```java
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
```


