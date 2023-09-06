package com.xm.cryptorecservice.persistence;

import com.xm.cryptorecservice.model.crypto.CryptoPrice;
import com.xm.cryptorecservice.model.crypto.CryptoPriceStats;
import com.xm.cryptorecservice.util.logger.Logged;

import jakarta.validation.constraints.NotBlank;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * An implementation of {@link DatabaseConnection} that performs batch inserts for
 * {@link DatabaseConnection#insertAllCryptoPrices(String, List)}.
 *
 * @author jason
 */
@Repository
@RequiredArgsConstructor
@Logged
@Slf4j
public class DatabaseConnectionImpl implements DatabaseConnection {

    private static final int PRICES_BATCH_SIZE = 100;
    private static final int NAMES_BATCH_SIZE = 10;
    private static final String CRYPTO_NAME_TABLE_NAME = "CRYPTOS";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void createCryptoPriceTable(@NonNull @NotBlank String tableName) {
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
            @NonNull @NotBlank String tableName, @NonNull List<CryptoPrice> cryptoPrices) {
        // Truncate
        String truncateQuery = String.format("TRUNCATE TABLE %s", tableName);
        jdbcTemplate.execute(truncateQuery);
        // Batch insert
        String insertQuery =
                String.format("INSERT INTO %s (timestamp, price) VALUES (?, ?)", tableName);
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
    public Optional<CryptoPrice> getCryptoPriceById(
            @NonNull @NotBlank String cryptoName, @NonNull Long id) {
        String query =
                String.format(
                        "SELECT timestamp, trim(price)+0 FROM %s WHERE ID = ?",
                        cryptoName.toUpperCase(Locale.ROOT));
        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(
                            query, new BeanPropertyRowMapper<>(CryptoPrice.class), id));
        } catch (
                EmptyResultDataAccessException
                        exc) { // queryForObject will throw this if the result set is empty
            log.warn("Empty result encountered when looking for ID {} in table {}", id, cryptoName);
            return Optional.empty();
        }
    }

    @Override
    public Optional<CryptoPriceStats> getCryptoPriceStats(@NonNull @NotBlank String cryptoName) {
        String selectQuery =
                String.format(
                        """
                SELECT (select min(trim(price)+0) from %1$s) as 'minprice',\s
                (select max(trim(price)+0) from %1$s) as 'maxprice',\s
                (select trim(price)+0 from %1$s order by timestamp asc limit 1) as 'firstprice',\s
                (select trim(price)+0 from %1$s order by timestamp desc limit 1) as 'lastprice'""",
                        cryptoName.toUpperCase(Locale.ROOT));
        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(
                            selectQuery, new BeanPropertyRowMapper<>(CryptoPriceStats.class)));
        } catch (EmptyResultDataAccessException exc) {
            log.warn("Empty result encountered when getting stats from table {}", cryptoName);
            return Optional.empty();
        }
    }

    @Override
    public List<CryptoPrice> getCryptoPricesForDate(
            @NonNull @NotBlank String cryptoName, @NonNull @NotBlank String date) {
        String selectQuery =
                String.format(
                        "SELECT timestamp as timestamp, trim(price)+0 as price " +
                        "from %s where DATE_FORMAT(timestamp, '%%Y-%%m-%%d') = '%s'", // MySQL format is %Y-%m-%d
                        cryptoName, date);
        try {
            return jdbcTemplate.query(selectQuery, new BeanPropertyRowMapper<>(CryptoPrice.class));
        } catch(DataAccessException dataAccessException){
            log.warn("Received exception: {}", dataAccessException.getMessage());
            throw new RuntimeException(dataAccessException.getMessage());
        }
    }
}
