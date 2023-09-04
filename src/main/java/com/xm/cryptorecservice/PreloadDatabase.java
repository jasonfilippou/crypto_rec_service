package com.xm.cryptorecservice;

import com.xm.cryptorecservice.io.CryptoPriceReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class PreloadDatabase {

    private static final String DIR_PATH = "./task/prices";

    @Bean
    CommandLineRunner initDatabase(CryptoPriceReader reader){
        return args -> {
            log.info("Preloading database with data from " + DIR_PATH);
            reader.persistAllCSVsInDirectory(DIR_PATH);
            log.info("Preloaded database.");
        };
    }
}
