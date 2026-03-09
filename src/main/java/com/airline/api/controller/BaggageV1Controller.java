package com.airline.api.controller;

import com.airline.api.dto.BaggageQuoteRequest;
import com.airline.api.dto.BaggageQuoteResponse;
import com.airline.model.Baggage;
import com.airline.model.BaggageType;
import com.airline.service.BaggageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/baggage")
public class BaggageV1Controller {

    private final BaggageService baggageService;

    public BaggageV1Controller(BaggageService baggageService) {
        this.baggageService = baggageService;
    }

    @PostMapping("/quote")
    public ResponseEntity<BaggageQuoteResponse> quote(@Valid @RequestBody BaggageQuoteRequest request) {
        Baggage baggage = baggageService.calculateBaggageCharge(request.weight(), request.requestedType());

        BigDecimal baseFee = baseFeeFor(baggage.getType());
        BigDecimal overweightFee = baggage.getCharge().subtract(baseFee).max(BigDecimal.ZERO);

        return ResponseEntity.ok(new BaggageQuoteResponse(
                baggage.getWeight(),
                request.requestedType(),
                baggage.getType(),
                baseFee,
                overweightFee,
                baggage.getCharge(),
                baggage.getDescription()
        ));
    }

    private static BigDecimal baseFeeFor(BaggageType type) {
        return switch (type) {
            case CARRY_ON -> BigDecimal.ZERO;
            case CHECKED, OVERWEIGHT -> new BigDecimal("35.00");
        };
    }
}

