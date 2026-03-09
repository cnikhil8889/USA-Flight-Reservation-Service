package com.airline.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "airline.baggage.pricing")
public class BaggagePricingProperties {

    /**
     * Flat fee for checked baggage.
     */
    private BigDecimal checkedBaseFee = new BigDecimal("400.00");

    /**
     * Additional fee per kilogram above the checked weight limit.
     */
    private BigDecimal overweightPerKg = new BigDecimal("300.00");

    public BigDecimal getCheckedBaseFee() {
        return checkedBaseFee;
    }

    public void setCheckedBaseFee(BigDecimal checkedBaseFee) {
        this.checkedBaseFee = checkedBaseFee;
    }

    public BigDecimal getOverweightPerKg() {
        return overweightPerKg;
    }

    public void setOverweightPerKg(BigDecimal overweightPerKg) {
        this.overweightPerKg = overweightPerKg;
    }
}

