package com.xm.cryptorecservice.persistence;

import com.xm.cryptorecservice.io.CryptoPriceFileReader;
import com.xm.cryptorecservice.model.CryptoPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

@RequiredArgsConstructor
@Slf4j
public class CryptoPricePersister implements Runnable {

    private final Lock dbLock;
    private final DatabaseConnection dbConnection;
    private final File csv;
    private final CryptoPriceFileReader csvReader;
    private final CountDownLatch latch;

    @Override
    public void run() {
        String cryptoName = csv.getName().substring(0, csv.getName().length() - 4); // Assuming format "name.csv"
        createTable(cryptoName);
        List<CryptoPrice> cryptoPrices = null;
        try {
            cryptoPrices = csvReader.readCSV(csv);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        persistCryptoPrices(cryptoPrices, cryptoName);
        latch.countDown();
    }

    private void createTable(String tableName){
        dbLock.lock();
        try {
            log.info("Creating table corresponding to crypto: " + tableName);
            dbConnection.createTable(tableName);
            log.info("Created table corresponding to crypto: " + tableName);
        } finally {
            dbLock.unlock();
        }
    }

    private void persistCryptoPrices(List<CryptoPrice> cryptoPrices, String tableName){
        dbLock.lock();
        try {
            dbConnection.insertAll(tableName, cryptoPrices);
            log.info("Inserted all prices for crypto: " + tableName);
        } finally {
            dbLock.unlock();
        }
    }
}
