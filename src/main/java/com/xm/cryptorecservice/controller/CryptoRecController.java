package com.xm.cryptorecservice.controller;

import static com.xm.cryptorecservice.util.Constants.DATE_FORMATTER;
import static com.xm.cryptorecservice.util.SortOrder.DESC;

import com.xm.cryptorecservice.service.CryptoRecService;
import com.xm.cryptorecservice.util.exceptions.BadDateFormatException;
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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/cryptorecapi")
@RequiredArgsConstructor
@Logged
@Validated
@Tag(name = "2. Crypto Recommendations API")
public class CryptoRecController {

    private final CryptoRecService service;

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

    @Operation(summary = "Return aggregate stats for a specific crypto")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Stats successfully returned",
                        content = @Content),
                @ApiResponse(
                        responseCode = "400",
                        description = "Unsupported cryptocurrency provided",
                        content = @Content),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthenticated user",
                        content = @Content),
            })
    @GetMapping("/aggregate/{cryptoName}") // TODO: will the same endpoint ("/aggregate") work?
    public ResponseEntity<?> getAggregateStats(@PathVariable String cryptoName)
            throws UnsupportedCryptoException {
        cryptoName = cryptoName.trim();
        if (!service.cryptoSupported(cryptoName)) {
            throw new UnsupportedCryptoException(cryptoName);
        }
        return ResponseEntity.ok(service.getAggregateStatsOfCrypto(cryptoName));
    }

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

    @Operation(summary = "Return the crypto with the highest normalized price for the given day. Day MUST be in YYYY-mm-dd format.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Best crypto of the day returned",
                            content = @Content),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Day out of stored range or bad day format provided.",
                            content = @Content),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthenticated user",
                            content = @Content)
            })
    @GetMapping("/bestofday")
    public ResponseEntity<?> bestCryptoOfTheDay(@RequestParam(name = "day") @NotBlank String day) throws BadDateFormatException {
        day = day.strip();
        // Check to see if the user supplied the date in the required format.
        try {
            LocalDate.parse(day, DATE_FORMATTER); // Call for side-effect
        } catch (DateTimeParseException exception){
            throw new BadDateFormatException("Date " + day + " not in YYYY-mm-dd format.");
        }
        return ResponseEntity.ok("Hooray! Your date can be parsed!");
    }
}
