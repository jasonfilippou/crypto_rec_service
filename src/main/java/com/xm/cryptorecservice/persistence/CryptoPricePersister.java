package com.xm.cryptorecservice.persistence;

import com.xm.cryptorecservice.io.CryptoPriceFileReader;
import com.xm.cryptorecservice.model.crypto.CryptoPrice;

import com.xm.cryptorecservice.util.logger.Logged;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@RequiredArgsConstructor
@Slf4j
@Logged
public class CryptoPricePersister implements Runnable {

    private final DatabaseConnection dbConnection;
    private final File csv;
    private final CryptoPriceFileReader csvReader;
    private final CountDownLatch latch;

    @Override
    public void run() {
        String cryptoName =
                csv.getName()
                        .substring(0, csv.getName().length() - 4); // Assuming format "name.csv"
        createTable(cryptoName);
        List<CryptoPrice> cryptoPrices = null;
        try {
            cryptoPrices = csvReader.readCSV(csv);
        } catch (IOException e) {
            log.warn("Exception received: " + e.getMessage());
            throw new RuntimeException(e);
        }
        persistCryptoPrices(cryptoPrices, cryptoName);
        latch.countDown();
    }

    private void createTable(String tableName) {
        log.info("Creating table corresponding to crypto: " + tableName);
        dbConnection.createCryptoPriceTable(tableName);
        log.info("Created table corresponding to crypto: " + tableName);
    }

    private void persistCryptoPrices(List<CryptoPrice> cryptoPrices, String tableName) {
        dbConnection.insertAllCryptoPrices(tableName, cryptoPrices);
        log.info("Inserted all prices for crypto: " + tableName);
    }
}
