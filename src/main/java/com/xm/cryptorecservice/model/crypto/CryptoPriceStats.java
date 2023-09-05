package com.xm.cryptorecservice.model.crypto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@RequiredArgsConstructor
public class CryptoPriceStats {
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final BigDecimal firstPrice;
    private final BigDecimal lastPrice;

    // Caching some stats to avoid potentially expensive BigDecimal re-computations.
    private BigDecimal priceRange;
    private BigDecimal priceDifference;
    private BigDecimal normalizedPrice;

    public BigDecimal getPriceRange(){
        if(priceRange == null){
            priceRange = maxPrice.subtract(minPrice);
        }
        return priceRange;
    }

    public BigDecimal getNormalizedPrice(){
        if (normalizedPrice == null) {
            normalizedPrice = getPriceRange().divide(minPrice, RoundingMode.HALF_EVEN);
        }
        return normalizedPrice;
    }

    public BigDecimal getPriceDifference(){
        if(priceDifference == null){
            priceDifference = lastPrice.subtract(firstPrice);
        }
        return priceDifference;
    }

    public boolean gain(){
        return getPriceDifference().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean loss(){
        return getPriceDifference().compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean neitherGainNorLoss(){
        return getPriceDifference().compareTo(BigDecimal.ZERO) == 0;
    }
}
