package com.github.hectorhw.tglinkshortener.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigInteger;
import java.util.Base64;

@RequiredArgsConstructor(staticName = "of")
@Getter
@ToString
public class DatabaseEntry {
    private final long ownerId;
    private final String targetUrl;
}
