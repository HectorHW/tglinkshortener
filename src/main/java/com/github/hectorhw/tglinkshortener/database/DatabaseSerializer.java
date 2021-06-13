package com.github.hectorhw.tglinkshortener.database;

import swaydb.data.slice.Slice;
import swaydb.data.slice.SliceReader;
import swaydb.data.util.ByteOps;
import swaydb.java.serializers.Serializer;

public class DatabaseSerializer implements Serializer<DatabaseEntry> {

    @Override
    public Slice<Byte> write(DatabaseEntry data) {
        return
            Slice.ofBytesJava(512)
                .addLong(data.getOwnerId(), ByteOps.Java())
                .addStringUTF8(data.getTargetUrl(), ByteOps.Java())
                .close();
    }

    @Override
    public DatabaseEntry read(Slice<Byte> slice) {
        SliceReader<Byte> reader = slice.createReader(ByteOps.Java());
        return
            DatabaseEntry.of(
                reader.readLong(),
                reader.readRemainingAsStringUTF8()
            );
    }
}
