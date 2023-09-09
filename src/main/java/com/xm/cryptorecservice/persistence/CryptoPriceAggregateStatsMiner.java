package com.xm.cryptorecservice.persistence;

import com.xm.cryptorecservice.model.crypto.CryptoPriceStats;
import com.xm.cryptorecservice.util.logger.Logged;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * A {@link Runnable} that queries the {@link DatabaseConnection} object with which it is constructed
 * for aggregate stats of the provided crypto.
 *
 * Once finished, it counts down the provided {@link CountDownLatch} instance.
 *
 * @see CryptoPriceStats
 */
@RequiredArgsConstructor
@Logged
public class CryptoPriceAggregateStatsMiner implements Runnable {
    private final DatabaseConnection dbConnection;
    private final InMemoryAggregateStats inMemoryDb;
    private final String cryptoName;
    private final CountDownLatch latch;

    @Override
    public void run() {
        try {
            Optional<CryptoPriceStats> priceStats = dbConnection.getCryptoPriceStats(cryptoName);
            priceStats.ifPresent(cryptoPriceStats -> inMemoryDb.add(cryptoName, cryptoPriceStats));
        } finally {
            latch.countDown();
        }
    }
}
