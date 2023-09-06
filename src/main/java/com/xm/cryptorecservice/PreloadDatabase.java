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

/**
 * On-disk and in-memory database pre-loader. Reads all CSV files from the directory ./task/prices and based on them
 * creates on-disk database tables and loads aggregate stats to the in-memory database to allow for efficient endpoint
 * response down the line. Uses multiple worker threads to accomplish this.
 *
 * @author jason
 */
@Configuration
@Slf4j
@Logged
public class PreloadDatabase {

    private static final String DIR_PATH = "./task/prices";

    /**
     * Initialize the on-disk and in-memory databases.
     * @param directoryParser A wired-in {@link CryptoDirectoryParser} instance.
     * @param statsService A wired-in {@link StatsCalculationService} instance.
     * @return A {@link CommandLineRunner} instance.
     */
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
