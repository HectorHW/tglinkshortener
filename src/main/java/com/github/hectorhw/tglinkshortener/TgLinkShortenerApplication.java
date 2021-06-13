package com.github.hectorhw.tglinkshortener;

import com.github.hectorhw.tglinkshortener.database.DatabaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class TgLinkShortenerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TgLinkShortenerApplication.class, args);
	}

	@Autowired
	private DatabaseController db;

	public static final Logger logger = LoggerFactory.getLogger(TgLinkShortenerApplication.class);

	@PostConstruct
	public void init(){
		String key = db.insertData(31, "https://www.baeldung.com/running-setup-logic-on-startup-in-spring");
		logger.info("added link: {}", key);
	}

}
