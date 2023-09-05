package com.xm.cryptorecservice.model.crypto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import static com.xm.cryptorecservice.util.Constants.BIG_DECIMAL_SCALE;
@Data
@NoArgsConstructor
public class CryptoPriceStats {
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal firstPrice;
    private BigDecimal lastPrice;

    // Fields used for caching results of BigDecimal operations,
    // which can be computationally intensive.

    private BigDecimal priceRange;
    private BigDecimal priceDifference;
    private BigDecimal normalizedPrice;

    public BigDecimal getPriceRange() {
        if (priceRange == null) {
            priceRange = maxPrice.subtract(minPrice);
        }
        return priceRange;
    }

    public BigDecimal getNormalizedPrice() {
        if (normalizedPrice == null) {
            normalizedPrice = getPriceRange().divide(minPrice, BIG_DECIMAL_SCALE, RoundingMode.HALF_EVEN);
        }
        return normalizedPrice;
    }

    public BigDecimal getPriceDifference() {
        if (priceDifference == null) {
            priceDifference = lastPrice.subtract(firstPrice);
        }
        return priceDifference;
    }

    public boolean gain() {
        return getPriceDifference().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean loss() {
        return getPriceDifference().compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean neitherGainNorLoss() {
        return getPriceDifference().compareTo(BigDecimal.ZERO) == 0;
    }
}
