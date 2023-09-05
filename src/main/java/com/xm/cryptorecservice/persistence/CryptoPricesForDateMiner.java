package com.xm.cryptorecservice.persistence;

import com.xm.cryptorecservice.model.crypto.CryptoPrice;
import com.xm.cryptorecservice.util.logger.Logged;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import static com.xm.cryptorecservice.util.Constants.BIG_DECIMAL_SCALE;

@Logged
@RequiredArgsConstructor
public class CryptoPricesForDateMiner implements Runnable {

    private final DatabaseConnection dbConnection;
    private final String cryptoName;
    private final String date;
    private final ConcurrentMap<String, BigDecimal> map;
    private final CountDownLatch latch;
    @Override
    public void run() {
        List<CryptoPrice> pricesOfCryptoForDate = dbConnection.getCryptoPricesForDate(cryptoName, date);
        if(!pricesOfCryptoForDate.isEmpty()){
            map.put(cryptoName, getNormalizedPrice(pricesOfCryptoForDate));
        }
        latch.countDown();
    }

    private BigDecimal getNormalizedPrice(@NotEmpty List<CryptoPrice> cryptoPrices){
        BigDecimal maxPrice = cryptoPrices.stream().map(CryptoPrice::getPrice)
                .max(BigDecimal::compareTo).get();
        BigDecimal minPrice = cryptoPrices.stream().map(CryptoPrice::getPrice).min(BigDecimal::compareTo).get();
        return (maxPrice.subtract(minPrice)).divide(minPrice, BIG_DECIMAL_SCALE, RoundingMode.HALF_EVEN);
    }
}
