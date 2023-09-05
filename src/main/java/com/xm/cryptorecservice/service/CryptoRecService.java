package com.xm.cryptorecservice.service;

import com.xm.cryptorecservice.model.crypto.CryptoPriceStats;
import com.xm.cryptorecservice.persistence.DatabaseConnection;
import com.xm.cryptorecservice.persistence.InMemoryAggregateStats;
import com.xm.cryptorecservice.util.logger.Logged;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Logged
public class CryptoRecService {

    private final DatabaseConnection onDiskDb;
    private final InMemoryAggregateStats inMemoryDb;

    public Map<String, CryptoPriceStats> getAggregateStats(){
        return inMemoryDb.copyOfStats();
    }
    
    public boolean cryptoSupported(String cryptoName){
        return inMemoryDb.containsCrypto(cryptoName);
    }
    
    public CryptoPriceStats getAggregateStatsOfCrypto(String cryptoName){
        return inMemoryDb.get(cryptoName);
    }


}
