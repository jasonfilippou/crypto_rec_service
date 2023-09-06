package com.xm.cryptorecservice.io;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.xm.cryptorecservice.model.crypto.CryptoPrice;
import com.xm.cryptorecservice.util.logger.Logged;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reads a CSV file in memory and returns a {@link List} of {@link CryptoPrice} instances, one per row of the CSV.
 *
 * @see CSVReader
 * @see CryptoDirectoryParser
 * @see CryptoPrice
 */
@Service
@Logged
public class CryptoPriceFileReader {

    /**
     * Read a single CSV file in memory and parse its rows into a {@link List} of {@link CryptoPrice} instances.
     * @param csv A {@link File} to read data from.
     * @return A {@link List} of {@link CryptoPrice} instances, one per row of the input CSV file.
     * @throws IOException if the argument does not correspond to a valid CSV file.
     * @throws RuntimeException if the {@link CSVReader} employed throws a {@link CsvException}.
     * @throws OutOfMemoryError if the CSV file is too large to fit in memory.
     */
    public List<CryptoPrice> readCSV(File csv) throws IOException {
        FileReader reader = new FileReader(csv);
        CSVParser parser =
                new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(true).build();
        try (CSVReader csvReader =
                new CSVReaderBuilder(reader).withSkipLines(1).withCSVParser(parser).build()) {
            return csvReader.readAll().stream()
                    .map(CryptoPrice::fromCSVRow)
                    .collect(Collectors.toList()); // memory-intensive operation, consider
            // parallelStream
        } catch (CsvException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
