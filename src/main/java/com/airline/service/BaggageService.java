package com.airline.service;

import com.airline.config.BaggagePricingProperties;
import com.airline.model.Baggage;
import com.airline.model.BaggageType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class BaggageService {

    private final BaggagePricingProperties pricing;

    public BaggageService(BaggagePricingProperties pricing) {
        this.pricing = pricing;
    }

    public Baggage calculateBaggageCharge(Double weight, BaggageType requestedType) {
        if (weight == null || weight < 0) {
            throw new IllegalArgumentException("Weight must be positive");
        }

        BaggageType actualType = determineBaggageType(weight, requestedType);
        BigDecimal charge = calculateCharge(weight, actualType);
        String description = generateDescription(weight, actualType, requestedType);

        return new Baggage(weight, actualType, charge, description);
    }

    private BaggageType determineBaggageType(Double weight, BaggageType requestedType) {
        if (requestedType == BaggageType.CARRY_ON && weight > BaggageType.CARRY_ON.getWeightLimit()) {
            return BaggageType.CHECKED;
        }
        if (requestedType == BaggageType.CHECKED && weight > BaggageType.CHECKED.getWeightLimit()) {
            return BaggageType.OVERWEIGHT;
        }
        return requestedType;
    }

    private BigDecimal calculateCharge(Double weight, BaggageType type) {
        BigDecimal charge = BigDecimal.ZERO;

        switch (type) {
            case CARRY_ON:
                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            case CHECKED:
                charge = pricing.getCheckedBaseFee();
                break;
            case OVERWEIGHT:
                charge = pricing.getCheckedBaseFee();
                double excessWeight = weight - BaggageType.CHECKED.getWeightLimit();
                BigDecimal overweightCharge = pricing.getOverweightPerKg()
                        .multiply(BigDecimal.valueOf(excessWeight));
                charge = charge.add(overweightCharge);
                break;
        }

        return charge.setScale(2, RoundingMode.HALF_UP);
    }

    private String generateDescription(Double weight, BaggageType actualType, BaggageType requestedType) {
        StringBuilder description = new StringBuilder();
        description.append(String.format("Baggage: %.1f kg - %s", weight, actualType.getDisplayName()));

        if (actualType != requestedType) {
            description.append(String.format(" (upgraded from %s due to weight)", requestedType.getDisplayName()));
        }

        if (actualType == BaggageType.OVERWEIGHT) {
            double excessWeight = weight - BaggageType.CHECKED.getWeightLimit();
            description.append(String.format(" - Excess: %.1f kg", excessWeight));
        }

        return description.toString();
    }

    public void printBaggageInfo() {
        System.out.println("\n=== BAGGAGE POLICY ===");
        System.out.println("Carry-on: Up to 7 kg (approx. 15 lb) - FREE");
        System.out.println("Checked: Up to 23 kg (approx. 50 lb) - base fee $" + pricing.getCheckedBaseFee());
        System.out.println("Overweight: Above 23 kg - base fee + $" + pricing.getOverweightPerKg() + " per excess kg");
        System.out.println("========================\n");
    }
}