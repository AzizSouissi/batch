package com.batch.service.carrier;

import com.batch.domain.model.ShippingStatus;

/**
 * Interface for carrier-specific tracking services.
 */
public interface CarrierTrackingService {

    /**
     * Get the carrier name handled by this service.
     * @return The carrier name
     */
    String getCarrierName();

    /**
     * Get the shipping status for a given tracking number.
     * @param trackingNumber The tracking number
     * @return The shipping status
     */
    ShippingStatus getShippingStatus(String trackingNumber);
}