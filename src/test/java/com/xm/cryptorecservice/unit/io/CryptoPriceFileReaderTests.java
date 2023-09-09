package com.xm.cryptorecservice.unit.io;

import com.xm.cryptorecservice.io.CryptoPriceFileReader;
import com.xm.cryptorecservice.model.crypto.CryptoPrice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class CryptoPriceFileReaderTests {

    private final CryptoPriceFileReader fileReader = new CryptoPriceFileReader();

    @Test
    public void whenValidFileProvided_thenAllLinesAreRead(){
        List<CryptoPrice> ethereumPrices = null;
        try {
            ethereumPrices = fileReader.readCSV(new File("./task/prices/ETH.csv"));
        } catch (IOException e) {
            fail("Did not expect an IOException to be thrown");
        }
        assertEquals(95, ethereumPrices.size());
    }

    @Test(expected = IOException.class)
    public void whenInvalidFileProvided_thenIOExceptionIsThrown() throws IOException {
        fileReader.readCSV(new File("not_a_valid_file.csv"));
    }
}
