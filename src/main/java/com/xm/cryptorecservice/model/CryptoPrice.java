package com.xm.cryptorecservice.model;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@Data
public class CryptoPrice {
    private final Timestamp timestamp;
    private final BigDecimal price;

    public static CryptoPrice fromCSVRow(String[] csvRowParts){
        assert csvRowParts.length == 3;
        return new CryptoPrice(Timestamp.from(Instant.ofEpochMilli(Long.parseLong(csvRowParts[0]))),
                new BigDecimal(csvRowParts[2]));
    }
}
