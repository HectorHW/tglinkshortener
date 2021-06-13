package com.github.hectorhw.tglinkshortener.database;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import swaydb.KeyVal;
import swaydb.java.Map;
import swaydb.java.Stream;
import swaydb.java.memory.MemoryMap;

import java.util.Optional;

import static swaydb.java.serializers.Default.stringSerializer;

@Component
public class DatabaseController {

    private final Map<String, DatabaseEntry, Void> map = MemoryMap
        .functionsOff(stringSerializer(), new DatabaseSerializer())
        .get();

    public Optional<DatabaseEntry> getData(String key){
        return map.get(key);
    }

    //inserts data with unique key, returns key
    public String insertData(long userId, String url){
        String key = getRandomStringKey();
        while (this.map.contains(key)){
            key = getRandomStringKey();
        }

        DatabaseEntry record = DatabaseEntry.of(userId, url);
        this.map.put(key, record);
        return key;

    }

    public void remove(String key){
        this.map.remove(key);
    }

    public Stream<KeyVal<String, DatabaseEntry>> getUserRecords(long userId){
        return map.stream().filter(x -> x.value().getOwnerId()==userId);
    }

    private static String getRandomStringKey(){
        return RandomStringUtils.randomAlphanumeric(6);
    }
}
