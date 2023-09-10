package com.xm.cryptorecservice.unit.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.xm.cryptorecservice.controller.CryptoRecController;
import com.xm.cryptorecservice.model.crypto.CryptoPriceStats;
import com.xm.cryptorecservice.service.CryptoRecService;
import com.xm.cryptorecservice.util.SortOrder;
import com.xm.cryptorecservice.util.exceptions.BadDateFormatException;
import com.xm.cryptorecservice.util.exceptions.DateOutOfStoredRangeException;
import com.xm.cryptorecservice.util.exceptions.UnsupportedCryptoException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@RunWith(MockitoJUnitRunner.class)
public class CryptoRecControllerUnitTests {
    
    @InjectMocks
    private CryptoRecController controller;
    
    @Mock 
    private CryptoRecService service;

    private static final String CRYPTO_NAME = "XRP";

    private static final String DATE = "2022-01-01";
    
    @Test
    public void whenServiceReturnsAggStats_thenControllerReturnsResponseEntityWithThoseStats(){
        Map<String, CryptoPriceStats> aggStatsMap = Map.of(
                "BTC", new CryptoPriceStats(new BigDecimal("0.01"), new BigDecimal("12.00"),
                        BigDecimal.ONE, BigDecimal.TEN),
                "LTC", new CryptoPriceStats(BigDecimal.ONE, BigDecimal.TEN, new BigDecimal("0.02"),
                        new BigDecimal("0.012")));
        when(service.getAggregateStats()).thenReturn(aggStatsMap);
        assertEquals(ResponseEntity.ok(service.getAggregateStats()), controller.getAggregateStats());
    }

    @Test
    public void whenServiceReturnsAggStatsForAProvidedCrypto_thenControllerReturnsResponseEntityWithThoseStats(){
        CryptoPriceStats stats = new CryptoPriceStats(BigDecimal.ONE, BigDecimal.TEN,
                new BigDecimal("2.301"), new BigDecimal("10.76"));
        when(service.cryptoSupported(CRYPTO_NAME)).thenReturn(true);
        when(service.getAggregateStatsOfCrypto(CRYPTO_NAME)).thenReturn(stats);
        assertEquals(ResponseEntity.ok(stats), controller.getAggregateStats(CRYPTO_NAME));
    }

    @Test(expected = UnsupportedCryptoException.class)
    public void whenServiceSaysTheCryptoIsNotSupported_thenUnsupportedCryptoExceptionIsThrown(){
        when(service.cryptoSupported(CRYPTO_NAME)).thenReturn(false);
        controller.getAggregateStats(CRYPTO_NAME);
    }

    @Test
    public void whenServiceReturnsCryptosSortedByNormalizedPrice_thenControllerReturnsResponseEntityWithThoseStats(){
        SortedMap<String, BigDecimal> mockedMap =
                new TreeMap<>(Map.of("LTC", new BigDecimal("100.1"), "ETH", new BigDecimal("310.009")));
        when(service.getCryptosSortedByNormalizedPrice(SortOrder.DESC)).thenReturn(mockedMap);
        assertEquals(ResponseEntity.ok(mockedMap), controller.getCryptosSortedByNormalizedPrice());
    }

    @Test
    public void whenServiceReturnsTheBestCryptoForProvidedDate_thenControllerReturnsItToo(){
        Map.Entry<String, BigDecimal> mockedEntry = Map.entry("XRP", new BigDecimal("0.0192817546"));
        when(service.getBestCryptoForDate(DATE)).thenReturn(mockedEntry);
        assertEquals(ResponseEntity.ok(mockedEntry), controller.bestCryptoOfTheDay(DATE));
    }

    @Test(expected = BadDateFormatException.class)
    public void whenTheUserSuppliesADateInTheWrongFormat_thenBadDateFormatExceptionIsThrown(){
        controller.bestCryptoOfTheDay("01/01/2022");
    }

    @Test(expected = DateOutOfStoredRangeException.class)
    public void whenServiceReturnsNull_thenDateOutOfStoredRangeExceptionIsThrown(){
        String outOfRangeDate = "2021-01-01";
        when(service.getBestCryptoForDate(outOfRangeDate)).thenReturn(null);
        controller.bestCryptoOfTheDay(outOfRangeDate);
    }
}
