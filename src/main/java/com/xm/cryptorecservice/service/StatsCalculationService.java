package com.xm.cryptorecservice.service;


import com.xm.cryptorecservice.persistence.CryptoPriceStatsMiner;
import com.xm.cryptorecservice.persistence.DatabaseConnection;
import com.xm.cryptorecservice.persistence.InMemoryAggregateStats;

import com.xm.cryptorecservice.util.logger.Logged;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Logged
public class StatsCalculationService {

    private static final int MAX_THREADS = 10;

    private final DatabaseConnection dbConnection;
    private final InMemoryAggregateStats inMemoryDb;
    
    public void computeAndLoadAllStats(List<String> cryptos){
        // We will launch multiple workers for the computation of these stats
        // and take advantage of the fact that InMemoryStats is a thread-safe class.
        int numWorkers = Math.min(MAX_THREADS, cryptos.size());
        ExecutorService workers = Executors.newFixedThreadPool(numWorkers);
        CountDownLatch latch = new CountDownLatch(numWorkers);
        for(String crypto: cryptos){
            workers.submit(new CryptoPriceStatsMiner(dbConnection, inMemoryDb, crypto, latch));
        }
        try {
            latch.await();
        } catch (InterruptedException ignored){}
    }
}
