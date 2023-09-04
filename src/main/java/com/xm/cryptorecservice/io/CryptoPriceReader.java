package com.xm.cryptorecservice.io;

import com.xm.cryptorecservice.persistence.DatabaseConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoPriceReader {

    private static final int MAX_THREADS = 10;

    private final DatabaseConnection db;

    public void persistAllCSVsInDirectory(String directory){
        File[] csvs = (File[]) Arrays.stream(Objects.requireNonNull(new File(directory).listFiles())).filter(file->file.getName().endsWith(".csv")).toArray();
        int numOfCsvs = csvs.length;
        if(numOfCsvs == 0){
            return;
        }
        ExecutorService csvReaders = Executors.newFixedThreadPool(Math.max(MAX_THREADS, numOfCsvs));

    }
}
