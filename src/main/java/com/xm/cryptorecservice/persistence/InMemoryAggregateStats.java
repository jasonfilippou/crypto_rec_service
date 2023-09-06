package com.xm.cryptorecservice.persistence;

import static com.xm.cryptorecservice.util.SortOrder.ASC;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.xm.cryptorecservice.model.crypto.CryptoPriceStats;
import com.xm.cryptorecservice.util.SortOrder;
import com.xm.cryptorecservice.util.logger.Logged;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
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

    private final Map<String, CryptoPriceStats> cryptoPriceStats = Maps.newConcurrentMap();

    /**
     * Add the pair &lt; crypto, stats &gt; to the in-memory DB.
     * @param crypto The name of the cryptocurrency. It uniquely identifies the cryptocurrency in the in-memory DB.
     * @param stats An instance of {@link CryptoPriceStats} to associate the cryptocurrency with.
     */
    public void add(String crypto, CryptoPriceStats stats) {
        cryptoPriceStats.put(crypto, stats);
    }

    /**
     * Retrieve the aggregate stats associated with the provided crypto.
     * @param crypto The unique crypto name to return the aggregate stats of.
     * @return An instance of {@link CryptoPriceStats} corresponding to the provided crypto, or {@literal null} if the
     * crypto is not in the DB.
     */
    public CryptoPriceStats get(String crypto) {
        return cryptoPriceStats.get(crypto);
    }

    /**
     * Queries the in-memory DB for existence of a particular cryptocurrency name.
     * @param crypto The crypto name to search for.
     * @return {@literal true} if the crypto is in our database, {@literal false} otherwise.
     */
    public boolean containsCrypto(String crypto) {
        return cryptoPriceStats.containsKey(crypto);
    }

    /**
     * A method that prints all the available states to stdout, useful mostly for debugging. Is not thread-safe,
     * but also does not structurally modify {@literal this} in any way.
     */
    public void printAllStats() { // Not thread-safe, used mainly for debugging purposes.
        for (Map.Entry<String, CryptoPriceStats> entry : cryptoPriceStats.entrySet()) {
            System.out.println(entry.getKey() + "->" + entry.getValue());
        }
    }

    /**
     * Performs a shallow copy of the statistics held by {@literal  this}. Not thread-safe, but also not structurally
     * modifying the store in any way.
     * 
     * @return A {@link java.util.SortedMap} containing the crypto names of the in-memory DB as keys and the relevant
     * aggregate stats as values. It is sorted by lexicographical order of keys, ascending.
     */
    public Map<String, CryptoPriceStats> copyOfStats() {
        // An ImmutableSortedMap created this way will print the k-v pairs
        // in ascending lexicographical order of key.
        return ImmutableSortedMap.copyOf(cryptoPriceStats);
    }

    /**
     * Returns the cryptocurrencies we support in ascending or descending order of normalized price. Not thread safe,
     * but also not modifying the store in any way.
     * @param sortOrder A {@link SortOrder} instance that determines if we want ascending or descending sort order.
     * @return A {@link SortedMap} whose keys are sorted by value, in ascending or descending order.
     */
    public SortedMap<String, BigDecimal> cryptosSortedByNormalizedPriceDescending(SortOrder sortOrder) {
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

    /**
     * Return the cryptocurrencies that the application supports.
     * @return A {@link Set} with all the supported cryptocurrency names.
     */
    public Set<String> getSupportedCryptos(){
        return cryptoPriceStats.keySet();
    }
}
