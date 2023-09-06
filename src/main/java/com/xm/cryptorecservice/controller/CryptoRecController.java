package com.xm.cryptorecservice.controller;

import static com.xm.cryptorecservice.util.Constants.DATE_FORMATTER;
import static com.xm.cryptorecservice.util.SortOrder.DESC;

import com.xm.cryptorecservice.service.CryptoRecService;
import com.xm.cryptorecservice.util.exceptions.BadDateFormatException;
import com.xm.cryptorecservice.util.exceptions.DateOutOfStoredRangeException;
import com.xm.cryptorecservice.util.exceptions.UnsupportedCryptoException;
import com.xm.cryptorecservice.util.logger.Logged;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * A {@link RestController} responsible for serving up endpoints, receiving and sending data to the user.
 * 
 * @author jason 
 */
@RestController
@RequestMapping("/cryptorecapi")
@RequiredArgsConstructor
@Logged
@Validated
@Tag(name = "2. Crypto Recommendations API")
public class CryptoRecController {

    private final CryptoRecService service;

    /**
     * Return the aggregate stats for all loaded cryptos. Those include minimum price, maximum price, first price, last price,
     * price range (max - min), price difference (last - first) and normalized price ( (max - min) / min).
     * 
     * @return A {@link ResponseEntity} containing a JSON payload with crypto names as keys, and the aforementioned stats
     * as values. The response payload is sorted lexicographically by keys, in ascending order.
     * 
     * @see #getAggregateStats(String) 
     */
    @Operation(summary = "Return aggregate stats for all cryptos")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Stats successfully returned",
                        content = @Content),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthenticated user",
                        content = @Content),
            })
    @GetMapping("/aggregate")
    public ResponseEntity<?> getAggregateStats() {
        return ResponseEntity.ok(service.getAggregateStats());
    }

    /**
     * Get aggregate stats for a specific crypto. Those include minimum price, maximum price, first price, last price,
     * price range (max - min), price difference (last - first) and normalized price ((max - min) / min).
     * @param cryptoName The crypto to return aggregate stats for.
     * @return A {@link ResponseEntity} with a single entry consisting of the provided crypto as the key and the aforementioned 
     * stats as the value.
     * @throws UnsupportedCryptoException if the user provides an unsupported cryptocurrency.
     * @see #getAggregateStats() 
     */
    @Operation(summary = "Return aggregate stats for a specific crypto")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Stats successfully returned",
                        content = @Content),
                @ApiResponse(
                        responseCode = "400",
                        description = "Blank name provided",
                        content = @Content),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthenticated user",
                        content = @Content),
                @ApiResponse(
                        responseCode = "404",
                        description = "Unsupported cryptocurrency.",
                        content = @Content)
            })
    @GetMapping("/aggregate/{cryptoName}")
    public ResponseEntity<?> getAggregateStats(@PathVariable @NotBlank String cryptoName)
            throws UnsupportedCryptoException {
        cryptoName = cryptoName.trim();
        if (!service.cryptoSupported(cryptoName)) {
            throw new UnsupportedCryptoException(cryptoName);
        }
        return ResponseEntity.ok(service.getAggregateStatsOfCrypto(cryptoName));
    }

    /**
     * Return the cryptos sorted by normalized aggregate price, in descending order.
     * @return A {@link ResponseEntity} containing the cryptos as keys and their normalized aggregate price as values,
     * where the entries are sorted in descending order by the values.
     */
    @Operation(summary = "Return cryptos sorted by normalized aggregate price in descending order")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Sorted cryptos returned",
                            content = @Content),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthenticated user",
                            content = @Content)
                    })
    @GetMapping("/sorted")
    public ResponseEntity<?> getCryptosSortedByNormalizedPrice(){
        return ResponseEntity.ok(service.getCryptosSortedByNormalizedPrice(DESC));
    }

    /**
     * Returns the crypto with the highest normalized price for the given day.
     * @param date A date string, which MUST be in YYYY-mm-dd format.
     * @return A single entry consisting of the crypto with the highest normalized price for the given day as the key,
     * and that normalized price as the value.
     * @throws BadDateFormatException If the date string provided is NOT in YYYY-mm-dd format.
     * @throws DateOutOfStoredRangeException If the date provided does not have data for ANY supported cryptocurrency.
     */
    @Operation(summary = "Return the crypto with the highest normalized price for the given day. Day MUST be in YYYY-mm-dd format.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Best crypto of the day returned",
                            content = @Content),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Date format not in YYYY-mm-dd",
                            content = @Content),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthenticated user",
                            content = @Content),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Date provided out of stored range.",
                            content = @Content),
            })
    @GetMapping("/bestofday")
    public ResponseEntity<?> bestCryptoOfTheDay(@RequestParam(name = "date") @NotBlank String date)
            throws BadDateFormatException, DateOutOfStoredRangeException {
        date = date.strip();
        // Check to see if the user supplied the date in the required format.
        try {
            LocalDate.parse(date, DATE_FORMATTER); // Call for side-effect
        } catch (DateTimeParseException exception){
            throw new BadDateFormatException("Date " + date + " not in YYYY-mm-dd format.");
        }
        Map.Entry<String, BigDecimal> bestCryptoOfDay = service.getBestCryptoForDate(date);
        if(bestCryptoOfDay == null){ // Signifies that we couldn't find data for *any* crypto for that date.
            throw new DateOutOfStoredRangeException(date);
        }
        return ResponseEntity.ok(bestCryptoOfDay);
    }
}
