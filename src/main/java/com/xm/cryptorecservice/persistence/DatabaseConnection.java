package com.xm.cryptorecservice.persistence;

import com.xm.cryptorecservice.model.CryptoPrice;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface DatabaseConnection {

    void createTable(@NonNull String tableName);
    void insertAll(@NonNull String tableName, @NonNull List<CryptoPrice> cryptoPrices);
    Optional<CryptoPrice> getCryptoPriceById(@NonNull String cryptoName, @NonNull Long id);
}

