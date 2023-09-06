package com.xm.cryptorecservice.io;

import com.xm.cryptorecservice.persistence.CryptoPricePersister;
import com.xm.cryptorecservice.persistence.DatabaseConnection;
import com.xm.cryptorecservice.util.logger.Logged;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.xm.cryptorecservice.util.Constants.MAX_THREADS;

/**
 * A utility class responsible for parsing a given directory of CSV files with crypto prices. Creates one database table
 * per CSV file. Spawns multiple worker threads to parallelize this process.
 *
 * @author jason
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Logged
public class CryptoDirectoryParser {

    private final DatabaseConnection db;
    private final CryptoPriceFileReader csvReader;

    /**
     * Persist all the CSVs in the directory in the database, persisting one table per each CSV,
     * and creates another table with all the names of supported cryptos. Employs multiple threads to speed up the process.
     *
     * @param directory An absolute or relative path towards the directory that contains the .csv files.
     * @return A list of crypto names, corresponding to the names of the .csv files that were parsed.
     */
    public List<String> persistAllCSVsInDirectory(String directory) {
        List<File> csvs =
                Arrays.stream(Objects.requireNonNull(new File(directory).listFiles()))
                        .filter(file -> file.isFile() && file.getName().endsWith(".csv"))
                        .toList();
        int numWorkers = Math.min(csvs.size(), MAX_THREADS);
        ExecutorService workers = Executors.newFixedThreadPool(numWorkers);
        CountDownLatch latch = new CountDownLatch(numWorkers);
        for (File csv : csvs) {
            workers.submit(new CryptoPricePersister(db, csv, csvReader, latch));
        }
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
        List<String> cryptoNames =
                csvs.stream()
                        .map(file -> file.getName().substring(0, file.getName().length() - 4))
                        .toList();
        db.createTableOfCryptoNames(cryptoNames);
        return cryptoNames;
    }
}
