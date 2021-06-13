package com.github.hectorhw.tglinkshortener.bot;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TgCommand {
    String value();
}
