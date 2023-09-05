package com.xm.cryptorecservice.persistence;

import static com.xm.cryptorecservice.util.SortOrder.ASC;

import com.google.common.collect.ImmutableSortedMap;
import com.xm.cryptorecservice.model.crypto.CryptoPriceStats;
import com.xm.cryptorecservice.util.SortOrder;
import com.xm.cryptorecservice.util.logger.Logged;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * An in-memory &quot; database &quot; of crypto stats with thread-safe structural modification
 * operations.
 *
 * @author jason
 */
@Component
@Logged
public class InMemoryAggregateStats {

    private final Map<String, CryptoPriceStats> cryptoPriceStats = new ConcurrentHashMap<>();

    public void add(String crypto, CryptoPriceStats stats) {
        cryptoPriceStats.put(crypto, stats);
    }

    public CryptoPriceStats get(String crypto) {
        return cryptoPriceStats.get(crypto);
    }

    public boolean containsCrypto(String crypto) {
        return cryptoPriceStats.containsKey(crypto);
    }

    public void printAllStats() { // Not thread-safe, used mainly for debugging purposes.
        for (Map.Entry<String, CryptoPriceStats> entry : cryptoPriceStats.entrySet()) {
            System.out.println(entry.getKey() + "->" + entry.getValue());
        }
    }

    public Map<String, CryptoPriceStats> copyOfStats() {
        // An ImmutableSortedMap created this way will print the k-v pairs
        // in ascending lexicographical order of key.
        return ImmutableSortedMap.copyOf(cryptoPriceStats);
    }

    public Map<String, BigDecimal> cryptosSortedByNormalizedPriceDescending(SortOrder sortOrder) {
        // This time we specify a Comparator argument to compare by normalized price and sort
        // accordingly.
        Map<String, BigDecimal> intermediateMap =
                cryptoPriceStats.entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey, e -> e.getValue().getNormalizedPrice()));
        return ImmutableSortedMap.copyOf(
                intermediateMap,
                sortOrder == ASC
                        ? Comparator.comparing(intermediateMap::get)
                        : Comparator.comparing(intermediateMap::get).reversed());
    }
}
