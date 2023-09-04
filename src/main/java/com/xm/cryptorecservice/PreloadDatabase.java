package com.xm.cryptorecservice;

import com.xm.cryptorecservice.io.CryptoDirectoryParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class PreloadDatabase {

    private static final String DIR_PATH = "./task/prices";

    @Bean
    CommandLineRunner initDatabase(CryptoDirectoryParser reader){
        return args -> {
            log.info("Preloading database with data from " + DIR_PATH);
            long timeStart = System.currentTimeMillis();
            reader.persistAllCSVsInDirectory(DIR_PATH);
            log.info("Loading database took: " + (System.currentTimeMillis() - timeStart) + " ms.");
        };
    }
}
