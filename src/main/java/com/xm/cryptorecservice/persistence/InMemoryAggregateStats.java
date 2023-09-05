package com.xm.cryptorecservice.persistence;


import com.google.common.collect.ImmutableSortedMap;
import com.xm.cryptorecservice.model.crypto.CryptoPriceStats;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An in-memory &quot; database &quot; of crypto stats with thread-safe structural modification operations.
 * @author jason 
 */
@Component
public class InMemoryAggregateStats {

    private final Map<String, CryptoPriceStats> cryptoPriceStats = new ConcurrentHashMap<>();
    public void add(String crypto, CryptoPriceStats stats){
        cryptoPriceStats.put(crypto, stats);
    }
    
    public CryptoPriceStats get(String crypto){
        return cryptoPriceStats.get(crypto);
    }

    public boolean containsCrypto(String crypto){
        return cryptoPriceStats.containsKey(crypto);
    }

    public void printAllStats(){ // Not thread-safe, used mainly for debugging purposes.
        for(Map.Entry<String, CryptoPriceStats> entry: cryptoPriceStats.entrySet()){
            System.out.println(entry.getKey() + "->" + entry.getValue());
        }
    }

    public Map<String, CryptoPriceStats> copyOfStats(){
        // An ImmutableSortedMap will print the k-v pairs in ascending lexicographical order of key.
        return ImmutableSortedMap.copyOf(cryptoPriceStats);
    }
}
