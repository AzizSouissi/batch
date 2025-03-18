package com.batch.domain.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ShippingReservationResult {
    private String reservationNumber;
    private String trackingNumber;
    private String trackingUrl;
    private String labelUrl;
    private String shipmentErrorMessage;

    public static ShippingReservationResult buildErrorShippingReservationResult(String errorMessage) {
        return ShippingReservationResult.builder()
                .shipmentErrorMessage(errorMessage)
                .build();
    }
}