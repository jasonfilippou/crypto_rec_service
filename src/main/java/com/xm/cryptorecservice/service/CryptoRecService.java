package com.xm.cryptorecservice.service;

import static com.xm.cryptorecservice.util.Constants.BIG_DECIMAL_SCALE;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.xm.cryptorecservice.model.crypto.CryptoPrice;
import com.xm.cryptorecservice.model.crypto.CryptoPriceStats;
import com.xm.cryptorecservice.persistence.DatabaseConnection;
import com.xm.cryptorecservice.persistence.InMemoryAggregateStats;
import com.xm.cryptorecservice.util.SortOrder;
import com.xm.cryptorecservice.util.logger.Logged;

import jakarta.validation.constraints.NotEmpty;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
@Logged
public class CryptoRecService {

    private final DatabaseConnection onDiskDb;
    private final InMemoryAggregateStats inMemoryDb;

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
        // Single - threaded for now, to figure out what we're doing at the DB level.
        Map<String, BigDecimal> cryptoNormalizedPricesForDate = Maps.newConcurrentMap();
        for(String crypto : cryptos){
            List<CryptoPrice> pricesOfCryptoForDate = onDiskDb.getCryptoPricesForDate(crypto, date);
            if(!pricesOfCryptoForDate.isEmpty()){
                cryptoNormalizedPricesForDate.put(crypto, getNormalizedPrice(pricesOfCryptoForDate));
            }
        }
        // Sort by normalized price:
        if (!cryptoNormalizedPricesForDate.isEmpty()) {
            SortedMap<String, BigDecimal> sortedMap =
                    ImmutableSortedMap.copyOf(
                            cryptoNormalizedPricesForDate,
                            Comparator.comparing(cryptoNormalizedPricesForDate::get).reversed());
            return Map.entry(
                    sortedMap.firstKey(),
                    Objects.requireNonNull(sortedMap.get(sortedMap.firstKey())));
        }
        return null;
    }

    private BigDecimal getNormalizedPrice(@NotEmpty List<CryptoPrice> cryptoPrices){
        BigDecimal maxPrice = cryptoPrices.stream().map(CryptoPrice::getPrice)
                .max(BigDecimal::compareTo).get();
        BigDecimal minPrice = cryptoPrices.stream().map(CryptoPrice::getPrice).min(BigDecimal::compareTo).get();
        return (maxPrice.subtract(minPrice)).divide(minPrice, BIG_DECIMAL_SCALE, RoundingMode.HALF_EVEN);
    }
}
