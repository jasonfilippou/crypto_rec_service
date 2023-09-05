package com.xm.cryptorecservice.service;


import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.xm.cryptorecservice.model.crypto.CryptoPriceStats;
import com.xm.cryptorecservice.persistence.CryptoPricesForDateMiner;
import com.xm.cryptorecservice.persistence.DatabaseConnection;
import com.xm.cryptorecservice.persistence.InMemoryAggregateStats;
import com.xm.cryptorecservice.util.SortOrder;
import com.xm.cryptorecservice.util.logger.Logged;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Logged
public class CryptoRecService {

    private final DatabaseConnection onDiskDb;
    private final InMemoryAggregateStats inMemoryDb;
    private final static int MAX_THREADS = 10;
    public Map<String, CryptoPriceStats> getAggregateStats() {
        return inMemoryDb.copyOfStats();
    }

    public boolean cryptoSupported(String cryptoName) {
        return inMemoryDb.containsCrypto(cryptoName);
    }

    public CryptoPriceStats getAggregateStatsOfCrypto(String cryptoName) {
        return inMemoryDb.get(cryptoName);
    }

    public Map<String, BigDecimal> getCryptosSortedByNormalizedPrice(SortOrder sortOrder){
        return inMemoryDb.cryptosSortedByNormalizedPriceDescending(sortOrder);
    }

    public Map.Entry<String, BigDecimal> getBestCryptoForDate(String date){
        Set<String> cryptos = inMemoryDb.getSupportedCryptos(); // Assuming that in-mem DB is in sync with on-disk DB.
        int numWorkers = Math.min(cryptos.size(), MAX_THREADS);
        ExecutorService workers = Executors.newFixedThreadPool(numWorkers);

        // Maps.newConcurrentMap() currently returns an instance of ConcurrentHashMap.
        // For this use case, we could also try a synchronized TreeMap with the keys sorted by the values.
        // This would lead to a slightly more inefficient put() by each worker, but no need for sorting later.

        ConcurrentMap<String, BigDecimal> cryptoNormalizedPricesForDate = Maps.newConcurrentMap();
        CountDownLatch latch = new CountDownLatch(numWorkers);
        for(String crypto : cryptos){
            workers.submit(new CryptoPricesForDateMiner(onDiskDb, crypto, date, cryptoNormalizedPricesForDate, latch));
        }
        try {
            latch.await();
        } catch (InterruptedException ignored){}

        // Sort by normalized price descending. This would not be required if the Map were a synchronized Treemap.
        if (!cryptoNormalizedPricesForDate.isEmpty()) {
            SortedMap<String, BigDecimal> sortedMap =
                    ImmutableSortedMap.copyOf(
                            cryptoNormalizedPricesForDate,
                            Comparator.comparing(cryptoNormalizedPricesForDate::get).reversed());
            return Map.entry(
                    sortedMap.firstKey(), // Could also do sort ascending and get the last key.
                    Objects.requireNonNull(sortedMap.get(sortedMap.firstKey())));
        }
        return null;
    }
}
