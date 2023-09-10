package com.xm.cryptorecservice.unit.persistence;

import static com.xm.cryptorecservice.util.Constants.BIG_DECIMAL_SCALE;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import com.xm.cryptorecservice.model.crypto.CryptoPrice;
import com.xm.cryptorecservice.persistence.CryptoPricesForDateMiner;
import com.xm.cryptorecservice.persistence.DatabaseConnection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

@RunWith(MockitoJUnitRunner.class)
public class CryptoPricesForDateMinerUnitTests {
    
    @Mock
    private DatabaseConnection dbConnection;
    @InjectMocks
    private CryptoPricesForDateMiner miner;
    
    private static final String CRYPTO_NAME = "LTC";
    
    private static final String DATE = "2021-09-12";


    @Test
    public void whenDBReturnsSomePricesForProvidedCryptoAndDate_thenReturnedMapHasNormalizedPrice_andLatchIsCountedDown(){
        List<CryptoPrice> mockedPrices = List.of(
                new CryptoPrice(Timestamp.valueOf(DATE + " 00:10:20"), new BigDecimal("201.0")),
                new CryptoPrice(Timestamp.valueOf(DATE + " 02:10:20"), new BigDecimal("201.1")),
                new CryptoPrice(Timestamp.valueOf(DATE + " 04:10:22"), new BigDecimal("203.0")),
                new CryptoPrice(Timestamp.valueOf(DATE + " 05:10:21"), new BigDecimal("201.1")));
        BigDecimal maxPrice = mockedPrices.stream().map(CryptoPrice::getPrice).max(BigDecimal::compareTo).get();
        BigDecimal minPrice = mockedPrices.stream().map(CryptoPrice::getPrice).min(BigDecimal::compareTo).get();
        BigDecimal normalizedPrice = maxPrice.subtract(minPrice).divide(minPrice, BIG_DECIMAL_SCALE, RoundingMode.HALF_EVEN);
        CountDownLatch latch = new CountDownLatch(1);
        ConcurrentMap<String, BigDecimal> map = Maps.newConcurrentMap();
        when(dbConnection.getCryptoPricesForDate(CRYPTO_NAME, DATE)).thenReturn(mockedPrices);
        miner = new CryptoPricesForDateMiner(dbConnection, CRYPTO_NAME, DATE, map, latch);
        miner.run();
        assertTrue(map.containsKey(CRYPTO_NAME));
        assertEquals(normalizedPrice, map.get(CRYPTO_NAME));
        assertEquals(0, latch.getCount());
    }

    @Test
    public void whenDBDoesNotReturnPricesForProvidedCryptoAndDate_thenReturnedMapIsEmpty_andLatchIsStillCountedDown(){
        when(dbConnection.getCryptoPricesForDate(CRYPTO_NAME, DATE)).thenReturn(Collections.emptyList());
        ConcurrentMap<String, BigDecimal> map = Maps.newConcurrentMap();
        CountDownLatch latch = new CountDownLatch(1);
        miner = new CryptoPricesForDateMiner(dbConnection, CRYPTO_NAME, DATE, map, latch);
        miner.run();
        assertTrue(map.isEmpty());
        assertEquals(0, latch.getCount());
    }
}
