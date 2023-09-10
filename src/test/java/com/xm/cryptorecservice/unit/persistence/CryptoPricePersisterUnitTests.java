package com.xm.cryptorecservice.unit.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.xm.cryptorecservice.io.CryptoPriceFileReader;
import com.xm.cryptorecservice.model.crypto.CryptoPrice;
import com.xm.cryptorecservice.persistence.CryptoPricePersister;
import com.xm.cryptorecservice.persistence.DatabaseConnection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@RunWith(MockitoJUnitRunner.class)
public class CryptoPricePersisterUnitTests {
    
    @InjectMocks
    private CryptoPricePersister persister;
    
    @Mock
    private DatabaseConnection dbConn;

    @Mock
    private File csv;

    @Mock
    private CryptoPriceFileReader fileReader;

    private CountDownLatch latch;

    @Before
    public void setUp(){
        csv = new File("./task/prices/ETH.csv");
        latch = new CountDownLatch(1);
        persister = new CryptoPricePersister(dbConn, csv, fileReader, latch);
    }
    @Test
    public void whenReaderCompletesSuccessfully_thenLatchIsCountedDown(){
        try {
            doNothing().when(dbConn).createCryptoPriceTable(anyString());
            doNothing().when(dbConn).insertAllCryptoPrices(anyString(), anyList());
            when(fileReader.readCSV(csv)).thenReturn(List.of(
                    new CryptoPrice(Timestamp.valueOf("2021-09-12 00:10:20"), new BigDecimal("201.0")),
                    new CryptoPrice(Timestamp.valueOf("2021-09-12 00:10:21"), new BigDecimal("201.1"))
            ));
            persister.run();
            assertEquals(0, latch.getCount());
        } catch (IOException e) {
            fail("Did not expect an IOException to be thrown");
        }
    }

    @Test(expected = RuntimeException.class) // Exceptions is changed inside run().
    public void whenReaderThrowsIOException_thenLatchIsStillCountedDown() throws IOException {
        try {
            doNothing().when(dbConn).createCryptoPriceTable(anyString());
            doThrow(new IOException("IO Error")).when(fileReader).readCSV(csv);
            persister.run();
        } finally {
            assertEquals(0, latch.getCount());
        }
    }


    
}
