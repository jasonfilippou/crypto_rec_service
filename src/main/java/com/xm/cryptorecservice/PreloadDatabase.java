package com.xm.cryptorecservice;

import com.xm.cryptorecservice.io.CryptoDirectoryParser;
import com.xm.cryptorecservice.service.StatsCalculationService;
import com.xm.cryptorecservice.util.logger.Logged;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
@Logged
public class PreloadDatabase {

    private static final String DIR_PATH = "./task/prices";

    @Bean
    CommandLineRunner initDatabase(
            CryptoDirectoryParser directoryParser, StatsCalculationService statsService) {
        return args -> {
            log.info("Preloading database with data from " + DIR_PATH);
            long timeStart = System.currentTimeMillis();
            List<String> cryptos = directoryParser.persistAllCSVsInDirectory(DIR_PATH);
            log.info("Loading database took: " + (System.currentTimeMillis() - timeStart) + " ms.");
            statsService.computeAndLoadAllStats(cryptos);
            log.info("Generated in-memory stats.");
        };
    }
}
