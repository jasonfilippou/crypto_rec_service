package com.xm.cryptorecservice.persistence;

import com.xm.cryptorecservice.model.crypto.CryptoPrice;
import com.xm.cryptorecservice.model.crypto.CryptoPriceStats;

import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface DatabaseConnection {

    void createCryptoPriceTable(@NonNull String tableName);

    void createTableOfCryptoNames(@NonNull List<String> cryptoNames);

    void insertAllCryptoPrices(@NonNull String tableName, @NonNull List<CryptoPrice> cryptoPrices);

    Optional<CryptoPrice> getCryptoPriceById(@NonNull String cryptoName, @NonNull Long id);

    Optional<CryptoPriceStats> getCryptoPriceStats(@NonNull String cryptoName);

    List<CryptoPrice> getCryptoPricesForDate(String cryptoName, String date);
}
