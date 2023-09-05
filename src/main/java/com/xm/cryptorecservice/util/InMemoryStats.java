package com.xm.cryptorecservice.util;


import com.xm.cryptorecservice.model.crypto.CryptoPriceStats;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An in-memory &quot; database &quot; of crypto stats with thread-safe operations.
 * @author jason 
 */
@Component
public class InMemoryStats {

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

    public void printAllStats(){
        for(Map.Entry<String, CryptoPriceStats> entry: cryptoPriceStats.entrySet()){
            System.out.println(entry.getKey() + "->" + entry.getValue());
        }
    }
}
