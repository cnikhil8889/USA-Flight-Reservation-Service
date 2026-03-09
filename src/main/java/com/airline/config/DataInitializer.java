package com.airline.config;

import com.airline.model.Flight;
import com.airline.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private FlightService flightService;

    @Override
    public void run(String... args) throws Exception {
        // Load some sample flights only if DB is empty
        if (flightService.getAllFlights().isEmpty()) {
            initializeSampleFlights();
            System.out.println("Sample US domestic flights initialized!");
        }
    }

    private void initializeSampleFlights() {
        LocalDateTime now = LocalDateTime.now();

        // Flight 1: New York (JFK) to Los Angeles (LAX)
        Flight flight1 = new Flight(
                "AA101",  // American Airlines
                "New York (JFK)",
                "Los Angeles (LAX)",
                now.plusDays(1).withHour(8).withMinute(0),
                now.plusDays(1).withHour(11).withMinute(15),
                new BigDecimal("399.00"),
                180
        );

        // Flight 2: Los Angeles (LAX) to Chicago (ORD)
        Flight flight2 = new Flight(
                "UA202",  // United Airlines
                "Los Angeles (LAX)",
                "Chicago (ORD)",
                now.plusDays(2).withHour(14).withMinute(15),
                now.plusDays(2).withHour(16).withMinute(30),
                new BigDecimal("279.00"),
                150
        );

        // Flight 3: Chicago (ORD) to Dallas (DFW)
        Flight flight3 = new Flight(
                "DL303",  // Delta
                "Chicago (ORD)",
                "Dallas (DFW)",
                now.plusDays(3).withHour(10).withMinute(30),
                now.plusDays(3).withHour(13).withMinute(0),
                new BigDecimal("199.00"),
                120
        );

        // Flight 4: Dallas (DFW) to Miami (MIA)
        Flight flight4 = new Flight(
                "AA404",  // American Airlines
                "Dallas (DFW)",
                "Miami (MIA)",
                now.plusDays(4).withHour(16).withMinute(0),
                now.plusDays(4).withHour(18).withMinute(30),
                new BigDecimal("159.00"),
                140
        );

        // Flight 5: Seattle (SEA) to San Francisco (SFO)
        Flight flight5 = new Flight(
                "AS505",  // Alaska Airlines
                "Seattle (SEA)",
                "San Francisco (SFO)",
                now.plusDays(5).withHour(20).withMinute(0),
                now.plusDays(5).withHour(21).withMinute(15),
                new BigDecimal("149.00"),
                160
        );

        // save them to the DB
        flightService.saveFlight(flight1);
        flightService.saveFlight(flight2);
        flightService.saveFlight(flight3);
        flightService.saveFlight(flight4);
        flightService.saveFlight(flight5);
    }
}