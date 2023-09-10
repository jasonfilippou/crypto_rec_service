package com.xm.cryptorecservice.unit.service;

import static com.xm.cryptorecservice.unit.TestUtils.collectionIsSortedByFieldInGivenDirection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import com.xm.cryptorecservice.model.crypto.CryptoPriceStats;
import com.xm.cryptorecservice.persistence.DatabaseConnection;
import com.xm.cryptorecservice.persistence.InMemoryAggregateStats;
import com.xm.cryptorecservice.service.CryptoRecService;
import com.xm.cryptorecservice.util.SortOrder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Map;
import java.util.SortedMap;

@RunWith(MockitoJUnitRunner.class)
public class CryptoRecServiceUnitTests {

    @InjectMocks
    private CryptoRecService cryptoRecService;

    @Mock
    private DatabaseConnection onDiskDb;

    @Mock
    private InMemoryAggregateStats inMemoryDb;

    @Test
    public void whenInMemoryDB_returnsAggStats_thenSoDoWe(){
        Map<String, CryptoPriceStats> aggStatsMap = Map.of(
                "BTC", new CryptoPriceStats(new BigDecimal("0.01"), new BigDecimal("12.00"),
                        BigDecimal.ONE, BigDecimal.TEN),
                "LTC", new CryptoPriceStats(BigDecimal.ONE, BigDecimal.TEN, new BigDecimal("0.02"), new BigDecimal("0.012")));
        when(inMemoryDb.copyOfStats()).thenReturn(aggStatsMap);
        assertEquals(aggStatsMap, cryptoRecService.getAggregateStats());
    }

    @Test
    public void whenInMemoryDBTellsUsTheCryptoIsSupported_thenSoDoWe(){
        String crypto = "ETH";
        when(inMemoryDb.containsCrypto(crypto)).thenReturn(true);
        assertTrue(cryptoRecService.cryptoSupported(crypto));
    }

    @Test
    public void whenInMemoryDBTellsUsTheCryptoIsNotSupported_thenSoDoWe(){
        String crypto = "ETH";
        when(inMemoryDb.containsCrypto(crypto)).thenReturn(false);
        assertFalse(cryptoRecService.cryptoSupported(crypto));
    }

    @Test
    public void whenInMemoryDBReturnsAggregateStatsOfGivenCrypto_thenWeReturnThemAsWell(){
        String crypto = "LTC";
        CryptoPriceStats returnedStats = new CryptoPriceStats(new BigDecimal("0.11"), BigDecimal.TEN,
                new BigDecimal("7.6"), BigDecimal.ONE);
        when(inMemoryDb.get(crypto)).thenReturn(returnedStats);
        assertEquals(returnedStats, cryptoRecService.getAggregateStatsOfCrypto(crypto));
    }

    @Test
    public void whenInMemoryDBReturnsCryptosSortedByNormalizedPrice_thenWeGetTheCryptosInTheRequiredManner(){
        SortOrder sortOrder = SortOrder.ASC;
        SortedMap<String, BigDecimal> cryptosSortedByNormalizedPrice =
                cryptoRecService.getCryptosSortedByNormalizedPrice(sortOrder);
        assertTrue(collectionIsSortedByFieldInGivenDirection(cryptosSortedByNormalizedPrice.keySet(),
                "normalizedPrice", sortOrder));
        sortOrder = SortOrder.DESC;
        cryptosSortedByNormalizedPrice =
                cryptoRecService.getCryptosSortedByNormalizedPrice(sortOrder);
        assertTrue(collectionIsSortedByFieldInGivenDirection(cryptosSortedByNormalizedPrice.keySet(),
                "normalizedPrice", sortOrder));
    }

}
