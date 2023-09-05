package com.xm.cryptorecservice.persistence;

import com.xm.cryptorecservice.model.crypto.CryptoPriceStats;
import com.xm.cryptorecservice.util.logger.Logged;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

@RequiredArgsConstructor
@Logged
public class CryptoPriceAggregateStatsMiner implements Runnable {
    private final DatabaseConnection dbConnection;
    private final InMemoryAggregateStats inMemoryDb;
    private final String cryptoName;
    private final CountDownLatch latch;

    @Override
    public void run() {
        Optional<CryptoPriceStats> priceStats = dbConnection.getCryptoPriceStats(cryptoName);
        priceStats.ifPresent(cryptoPriceStats -> inMemoryDb.add(cryptoName, cryptoPriceStats));
        latch.countDown();
    }
}
