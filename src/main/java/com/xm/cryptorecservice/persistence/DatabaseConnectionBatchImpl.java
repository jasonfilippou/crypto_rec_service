package com.xm.cryptorecservice.persistence;


import com.xm.cryptorecservice.model.CryptoPrice;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DatabaseConnectionBatchImpl implements DatabaseConnection {

    private final JdbcTemplate jdbcTemplate;
    private static final int BATCH_SIZE = 100;

    @Override
    public void createTable(@NonNull String tableName) {
        String query = String.format("CREATE TABLE %s (id BIGINT NOT NULL AUTO_INCREMENT, timestamp TIMESTAMP NOT" +
                " NULL, price DECIMAL(20, 10) NOT NULL, PRIMARY KEY (id))", tableName.toUpperCase(Locale.ROOT));
        jdbcTemplate.execute(query);
    }

    @Override
    public void insertAll(@NonNull String tableName, @NonNull List<CryptoPrice> cryptoPrices) {
        // Batch insert
        String query = String.format("INSERT INTO %s (timestamp, price) VALUES (?, ?)", tableName);
        jdbcTemplate.batchUpdate(query,
                cryptoPrices, BATCH_SIZE,
                (PreparedStatement ps, CryptoPrice price) -> {
                    ps.setTimestamp(1, price.getTimestamp());
                    ps.setBigDecimal(2, price.getPrice()); // TODO: parameter indices?
                });
    }

    @Override
    public Optional<CryptoPrice> getCryptoPriceById(@NonNull String cryptoName, @NonNull Long id) {
        String query = String.format("SELECT FROM %s WHERE ID = ?", cryptoName.toUpperCase(Locale.ROOT));
        return Optional.ofNullable(jdbcTemplate.queryForObject(query, new BeanPropertyRowMapper<>(CryptoPrice.class), id));
    }
}
