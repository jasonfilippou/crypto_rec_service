package com.xm.cryptorecservice.unit.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.xm.cryptorecservice.model.crypto.CryptoPriceStats;
import com.xm.cryptorecservice.persistence.CryptoPriceAggregateStatsMiner;
import com.xm.cryptorecservice.persistence.DatabaseConnection;
import com.xm.cryptorecservice.persistence.InMemoryAggregateStats;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

@RunWith(MockitoJUnitRunner.class)
public class CryptoPriceAggregateStatsMinerTests {

    @InjectMocks private CryptoPriceAggregateStatsMiner miner;

    @Mock private DatabaseConnection dbConn;

    @Mock private InMemoryAggregateStats inMemDb;

    private static final String CRYPTO_NAME = "MEM";

    private static final CryptoPriceStats STATS =
            new CryptoPriceStats(
                    BigDecimal.ONE, BigDecimal.TEN, new BigDecimal("2.0"), new BigDecimal("6.0"));

    private CountDownLatch latch;

    @Before
    public void setUpLatch(){
        latch = new CountDownLatch(1);
    }

    // These tests ensure that the latch is tripped whether the crypto was part of our
    // supported cryptos or not.
    @Test
    public void whenSomeCryptoPriceStatsExistInOnDiskDB_thenLatchIsCountedDown() {
        when(dbConn.getCryptoPriceStats(CRYPTO_NAME)).thenReturn(Optional.of(STATS));
        doNothing().when(inMemDb).add(CRYPTO_NAME, STATS);
        miner = new CryptoPriceAggregateStatsMiner(dbConn, inMemDb, CRYPTO_NAME, latch);
        miner.run();
        assertEquals( 0, latch.getCount());
    }

    @Test
    public void whenNoCryptoPriceStatsExistInOnDiskDB_thenLatchIsStillCountedDown(){
        when(dbConn.getCryptoPriceStats(CRYPTO_NAME)).thenReturn(Optional.empty());
        miner = new CryptoPriceAggregateStatsMiner(dbConn, inMemDb, CRYPTO_NAME, latch);
        miner.run();
        assertEquals(0, latch.getCount());
    }
}
