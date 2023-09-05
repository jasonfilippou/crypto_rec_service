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

@Service
@Logged
public class CryptoPriceFileReader {

    public List<CryptoPrice> readCSV(File csv) throws IOException {
        FileReader reader = new FileReader(csv);
        CSVParser parser =
                new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(true).build();
        try (CSVReader csvReader =
                new CSVReaderBuilder(reader).withSkipLines(1).withCSVParser(parser).build()) {
            return csvReader.readAll().stream()
                    .map(CryptoPrice::fromCSVRow)
                    .collect(
                            Collectors
                                    .toList()); // memory-intensive operation, consider
                                                // parallelStream
        } catch (CsvException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
