package com.batch.service.carrier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Factory for retrieving the appropriate carrier tracking service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CarrierTrackingServiceFactory {

    private final List<CarrierTrackingService> trackingServices;

    /**
     * Get the tracking service for a specific carrier.
     * @param carrierName The carrier name
     * @return An Optional containing the tracking service, or empty if not found
     */
    public Optional<CarrierTrackingService> getTrackingService(String carrierName) {
        if (carrierName == null || carrierName.isBlank()) {
            return Optional.empty();
        }

        return trackingServices.stream()
                .filter(service -> carrierName.equalsIgnoreCase(service.getCarrierName()))
                .findFirst();
    }
}