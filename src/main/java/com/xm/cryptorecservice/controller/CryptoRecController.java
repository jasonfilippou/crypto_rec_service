package com.xm.cryptorecservice.controller;

import com.xm.cryptorecservice.service.CryptoRecService;
import com.xm.cryptorecservice.util.logger.Logged;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cryptorecapi")
@RequiredArgsConstructor
@Logged
@Slf4j
@Tag(name = "2. Crypto Recommendations API")
public class CryptoRecController {
    
    private final CryptoRecService service;

    @Operation(summary = "Return aggregate stats")
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
}
