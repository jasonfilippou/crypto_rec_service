package com.xm.cryptorecservice.model.crypto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoPrice {
    private Timestamp timestamp;
    private BigDecimal price;

    public static CryptoPrice fromCSVRow(String[] csvRowParts) {
        return new CryptoPrice(
                Timestamp.from(Instant.ofEpochMilli(Long.parseLong(csvRowParts[0]))),
                new BigDecimal(csvRowParts[2]));
    }
}
