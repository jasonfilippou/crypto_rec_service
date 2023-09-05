package com.xm.cryptorecservice;

import com.xm.cryptorecservice.io.CryptoDirectoryParser;
import com.xm.cryptorecservice.service.StatsCalculationService;
import com.xm.cryptorecservice.util.logger.Logged;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.xm.cryptorecservice.util.Constants.MAX_THREADS;

@Configuration
@Slf4j
@Logged
public class PreloadDatabase {

    private static final String DIR_PATH = "./task/prices";

    @Bean
    CommandLineRunner initDatabase(
            CryptoDirectoryParser directoryParser, StatsCalculationService statsService) {
        return args -> {
            log.info("Preloading on-disk and in-memory database with data from " + DIR_PATH);
            long timeStart = System.currentTimeMillis();
            List<String> cryptos = directoryParser.persistAllCSVsInDirectory(DIR_PATH);
            statsService.computeAndLoadAllStats(cryptos);
            log.info("Loading on-disk and in-memory databases employed " + Math.min(cryptos.size(), MAX_THREADS) +
                    " threads and took " + (System.currentTimeMillis() - timeStart) + " ms.");
        };
    }
}
