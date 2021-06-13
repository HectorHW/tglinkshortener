package com.github.hectorhw.tglinkshortener.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.github.hectorhw.tglinkshortener.database.DatabaseController;
import com.github.hectorhw.tglinkshortener.database.DatabaseEntry;

import java.net.URI;
import java.util.NoSuchElementException;
import java.util.Optional;


@RestController
public class UrlShortenerController {

    @Autowired
    private DatabaseController db;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity getUrl(@PathVariable String id){
        try {
            Optional<DatabaseEntry> dbEntry = db.getData(id);
            if (dbEntry.isEmpty()) throw new NoSuchElementException();

            return Redirect(dbEntry.get());

        }catch (IllegalArgumentException | NoSuchElementException e ){
            return ResponseEntity.ok("404: not found");
        }

    }

    private ResponseEntity Redirect(DatabaseEntry databaseEntry) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(databaseEntry.getTargetUrl()));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

}
