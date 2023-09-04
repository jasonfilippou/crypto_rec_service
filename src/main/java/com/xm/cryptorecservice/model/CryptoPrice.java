package com.xm.cryptorecservice.model;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class CryptoPrice {
    private Timestamp timestamp;
    private BigDecimal price;
}
