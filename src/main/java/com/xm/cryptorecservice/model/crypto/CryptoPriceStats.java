package com.xm.cryptorecservice.model.crypto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
public class CryptoPriceStats {
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal firstPrice;
    private BigDecimal lastPrice;

    public BigDecimal getPriceRange(){
        return maxPrice.subtract(minPrice);
    }

    public BigDecimal getNormalizedPrice(){
        return getPriceRange().divide(minPrice, RoundingMode.HALF_EVEN);
    }

    public BigDecimal getPriceDifference(){
        return lastPrice.subtract(firstPrice);
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
