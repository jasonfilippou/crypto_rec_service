package com.xm.cryptorecservice.io;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.xm.cryptorecservice.model.CryptoPrice;
import com.xm.cryptorecservice.persistence.DatabaseConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoPriceReader {

    private final DatabaseConnection db;

    public void persistAllCSVsInDirectory(String directory){
        File[] csvs = (File[]) Arrays.stream(Objects.requireNonNull(new File(directory).listFiles()))
                .filter(file->file.getName().endsWith(".csv")).toArray();
        int numOfCSVs = csvs.length;
        if(numOfCSVs == 0){
            return;
        }
        for(File csv: csvs){
            String cryptoName = csv.getName().substring(0, csv.getName().length() - 3); // Assuming format "name.csv"
            log.info("Creating table corresponding to crypto: " + cryptoName);
            db.createTable(cryptoName);
            log.info("Table corresponding to crypto: " + cryptoName + " created.");
            List<CryptoPrice> cryptoPrices = null;
            try {
               cryptoPrices = readCSV(csv);
            } catch (IOException exception){
                log.error("Encountered IOException: " + exception);
                throw new RuntimeException(exception);
            }
            log.info("Inserting all prices for crypto: " + cryptoName);
            db.insertAll(cryptoName, cryptoPrices);
            log.info("Inserted all prices for crypto: " + cryptoName);
        }
    }

    private List<CryptoPrice> readCSV(File csv) throws IOException {
        FileReader reader = new FileReader(csv);
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(',')
                .withIgnoreQuotations(true)
                .build();
        try(CSVReader csvReader = new CSVReaderBuilder(reader)
                .withSkipLines(1)
                .withCSVParser(parser)
                .build()){
            return csvReader.readAll().stream().map(CryptoPrice::fromCSVRow).collect(Collectors.toList()); // memory-intensive operation, consider parallelStream
        } catch (CsvException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
