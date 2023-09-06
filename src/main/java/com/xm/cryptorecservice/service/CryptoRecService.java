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

import static com.xm.cryptorecservice.util.Constants.MAX_THREADS;

/**
 * Service layer class that supports the operations of {@link com.xm.cryptorecservice.controller.CryptoRecController}.
 * Interfaces with a {@link DatabaseConnection} instance and an {@link InMemoryAggregateStats} instance to achieve its goals.
 *
 * @author jason
 *
 * @see com.xm.cryptorecservice.controller.CryptoRecController
 */
@Service
@RequiredArgsConstructor
@Logged
public class CryptoRecService {

    private final DatabaseConnection onDiskDb;
    private final InMemoryAggregateStats inMemoryDb;

    /**
     * Get the aggregate stats for all cryptos.
     * @return A {@link Map} keyed by crypto name, and instances of {@link CryptoPriceStats} as values.
     */
    public Map<String, CryptoPriceStats> getAggregateStats() {
        return inMemoryDb.copyOfStats();
    }

    /**
     * Queries the in-memory DB (which the app keeps in sync with the on-disk DB) for the support of a particular cryptocurrency.
     * @param cryptoName The name of the crypto to ask our support of.
     * @return {@literal true} if the cryptocurrency is supported by the application, {@literal false} otherwise.
     */
    public boolean cryptoSupported(String cryptoName) {
        return inMemoryDb.containsCrypto(cryptoName);
    }

    /**
     * Retrieves the aggregate stats (minimum, maximum, first, last price, price difference, range and normalized price)
     * of the provided cryptocurrency.
     * @param cryptoName The crypto to retrieve the aggregate stats of.
     * @return An instance of {@link CryptoPriceStats} if the crypto is supported, {@literal null} otherwise.
     */
    public CryptoPriceStats getAggregateStatsOfCrypto(String cryptoName) {
        return inMemoryDb.get(cryptoName);
    }


    /**
     * Retrieve the cryptos in sorted order of normalized price.
     * @param sortOrder A {@link SortOrder} instance that specifies if we want the order to be ascending or descending.
     * @return A {@link SortedMap} with crypto names as keys and normalized prices as values, where the keys are sorted
     * by values according to the provided sort order.
     */
    public SortedMap<String, BigDecimal> getCryptosSortedByNormalizedPrice(SortOrder sortOrder){
        return inMemoryDb.cryptosSortedByNormalizedPriceDescending(sortOrder);
    }

    /**
     * Retrieve the &quot; best scoring &quot; crypto for the provided date (in YYYY-mm-dd format), or {@literal null}
     * if no data existed for <b>any</b> crypto for that date. The best-scoring crypto for a given date is defined as the one
     * with the greatest normalized price for the given date.
     * @param date A date in YYYY-mm-dd format.
     * @return A {@link Map.Entry} with the crypto name as key and the normalized daily price as value, or {@literal null}
     * if there was no date for <b>any</b> crypto for the provided date.
     */
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
