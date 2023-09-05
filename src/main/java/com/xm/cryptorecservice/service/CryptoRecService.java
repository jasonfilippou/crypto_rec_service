package com.xm.cryptorecservice.service;

import com.xm.cryptorecservice.model.crypto.CryptoPriceStats;
import com.xm.cryptorecservice.persistence.DatabaseConnection;
import com.xm.cryptorecservice.persistence.InMemoryAggregateStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CryptoRecService {

    private final DatabaseConnection onDiskDb;
    private final InMemoryAggregateStats inMemoryDb;

    public Map<String, CryptoPriceStats> getAggregateStats(){
        return inMemoryDb.copyOfStats();
    }
}
