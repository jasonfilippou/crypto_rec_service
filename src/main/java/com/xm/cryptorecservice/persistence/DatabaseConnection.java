package com.xm.cryptorecservice.persistence;

import com.xm.cryptorecservice.model.crypto.CryptoPrice;
import com.xm.cryptorecservice.model.crypto.CryptoPriceStats;

import lombok.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * An interface for communicating with a relational database.
 *
 * @author jason
 *
 * @see DatabaseConnectionImpl
 */
public interface DatabaseConnection {

    /**
     * Create a table with the provided name and three columns: id (auto-generated, pkey), timestamp and price.
     *
     * @param tableName The name of the table to create.
     */
    void createCryptoPriceTable(@NonNull String tableName);

    /**
     * Create a table called CRYPTOS with two columns: id (auto-generated, pkey) and name.
     *
     * @param cryptoNames The list of names to add to the table.
     */
    void createTableOfCryptoNames(@NonNull List<String> cryptoNames);

    /**
     * Insert all provided crypto prices into the table specified by {@code tableName}. Implementations are free to do
     * this in batch mode or not.
     * @param tableName The name of the table to insert prices to.
     * @param cryptoPrices A {@link List} of {@link CryptoPrice} instances, every one of which needs to be persisted as a row in the table.
     */
    void insertAllCryptoPrices(@NonNull String tableName, @NonNull List<CryptoPrice> cryptoPrices);


    /**
     * Attempt to retrieve a specific {@link CryptoPrice} entry by its unique ID.
     * @param cryptoName The name of the crypto (and table) whose ID we are querying.
     * @param id The unique ID of the &lt; price, timestamp &gt; we want to return.
     * @return An {@link Optional} which will contain an instance of {@link CryptoPrice} if the database lookup returned
     * a row, or {@link Optional#empty()} otherwise.
     */
    Optional<CryptoPrice> getCryptoPriceById(@NonNull String cryptoName, @NonNull Long id);

    /**
     * Attempt to retrieve the minimum, maximum, first and last crypto price for the provided crypto.
     * @param cryptoName The name of the crypto (and corresponding table).
     * @return An {@link Optional} which will contain an instance of {@link CryptoPriceStats} with the aforementioned stats
     * if the database lookup was able to retrieve them, or {@link Optional#empty()} otherwise.
     */
    Optional<CryptoPriceStats> getCryptoPriceStats(@NonNull String cryptoName);

    /**
     * Get the &lt; timestamp, price &gt; tuples for the provided crypto and corresponding date.
     *
     * @param cryptoName The crypto to search the prices of.
     * @param date The date (in YYYY-mm-dd format) to search the prices for.
     * @return A {@link List} with all the prices we were able to find for the particular date (could be empty if there
     * were no prices or some other error occurred during the lookup).
     */
    List<CryptoPrice> getCryptoPricesForDate(String cryptoName, String date);
}
