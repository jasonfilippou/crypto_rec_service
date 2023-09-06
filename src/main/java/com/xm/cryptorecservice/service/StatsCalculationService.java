package com.xm.cryptorecservice.service;

import com.xm.cryptorecservice.persistence.CryptoPriceAggregateStatsMiner;
import com.xm.cryptorecservice.persistence.DatabaseConnection;
import com.xm.cryptorecservice.persistence.InMemoryAggregateStats;
import com.xm.cryptorecservice.util.logger.Logged;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.xm.cryptorecservice.util.Constants.MAX_THREADS;

/**
 * Computes the aggregate stats for all provided cryptos and loads them to the in-memory database instance provided
 * at construction. Employs multiple workers to make the process efficient.
 *
 * @author jason
 */
@Service
@RequiredArgsConstructor
@Logged
public class StatsCalculationService {

    private final DatabaseConnection dbConnection;
    private final InMemoryAggregateStats inMemoryDb;

    /**
     * Launch several {@link CryptoPriceAggregateStatsMiner} workers to query the DB for aggregate stats of the given
     * cryptos, and load them to the (thread-safe) in-memory DB.
     * @param cryptos A {@link List} with all the cryptos that we want to find and load aggregate stats of.
     */
    public void computeAndLoadAllStats(List<String> cryptos) {
        // We will launch multiple workers for the computation of these stats
        // and take advantage of the fact that InMemoryStats is a thread-safe class.
        int numWorkers = Math.min(MAX_THREADS, cryptos.size());
        ExecutorService workers = Executors.newFixedThreadPool(numWorkers);
        CountDownLatch latch = new CountDownLatch(numWorkers);
        for (String crypto : cryptos) {
            workers.submit(new CryptoPriceAggregateStatsMiner(dbConnection, inMemoryDb, crypto, latch));
        }
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
    }
}
