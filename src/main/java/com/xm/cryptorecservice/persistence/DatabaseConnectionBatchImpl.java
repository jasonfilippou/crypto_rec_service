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

    private static final int PRICES_BATCH_SIZE = 100;
    private static final int NAMES_BATCH_SIZE = 10;
    private static final String CRYPTO_NAME_TABLE_NAME = "CRYPTOS";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void createCryptoPriceTable(@NonNull String tableName) {
        String query =
                String.format(
                        "CREATE TABLE IF NOT EXISTS %s (id BIGINT NOT NULL AUTO_INCREMENT, timestamp TIMESTAMP NOT"
                                + " NULL, price DECIMAL(20, 10) NOT NULL, PRIMARY KEY (id))",
                        tableName.toUpperCase(Locale.ROOT));
        jdbcTemplate.execute(query);
    }

    @Override
    public void createTableOfCryptoNames(@NonNull List<String> cryptoNames) {
        // Create table if it does not exist
        String createQuery =
                String.format(
                        "CREATE TABLE IF NOT EXISTS %s (id INTEGER NOT NULL AUTO_INCREMENT, name VARCHAR(10) NOT NULL, PRIMARY KEY (id))",
                        CRYPTO_NAME_TABLE_NAME);
        jdbcTemplate.execute(createQuery);
        // Truncate its data
        String truncateQuery = String.format("TRUNCATE TABLE %s ", CRYPTO_NAME_TABLE_NAME);
        jdbcTemplate.execute(truncateQuery);
        // Insert the new data
        String insertQuery =
                String.format("INSERT INTO %s (name) VALUES (?)", CRYPTO_NAME_TABLE_NAME);
        jdbcTemplate.batchUpdate(
                insertQuery,
                cryptoNames,
                NAMES_BATCH_SIZE,
                (PreparedStatement ps, String s) -> ps.setString(1, s));
    }

    @Override
    public void insertAllCryptoPrices(
            @NonNull String tableName, @NonNull List<CryptoPrice> cryptoPrices) {
        // Truncate
        String truncateQuery = String.format("TRUNCATE TABLE %s", tableName);
        jdbcTemplate.execute(truncateQuery);
        // Batch insert
        String insertQuery = String.format("INSERT INTO %s (timestamp, price) VALUES (?, ?)", tableName);
        jdbcTemplate.batchUpdate(
                insertQuery,
                cryptoPrices,
                PRICES_BATCH_SIZE,
                (PreparedStatement ps, CryptoPrice price) -> {
                    ps.setTimestamp(1, price.getTimestamp());
                    ps.setBigDecimal(2, price.getPrice());
                });
    }

    @Override
    public Optional<CryptoPrice> getCryptoPriceById(@NonNull String cryptoName, @NonNull Long id) {
        String query =
                String.format("SELECT FROM %s WHERE ID = ?", cryptoName.toUpperCase(Locale.ROOT));
        return Optional.ofNullable(
                jdbcTemplate.queryForObject(
                        query, new BeanPropertyRowMapper<>(CryptoPrice.class), id));
    }
}
