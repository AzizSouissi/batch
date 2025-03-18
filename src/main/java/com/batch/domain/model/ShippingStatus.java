package com.batch.domain.model;

import fr.platana.shipment.domain.enumeration.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ShippingStatus {

    @Builder.Default
    private List<ShippingEvent> events = new ArrayList<>();
    private String trackingNumber;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class ShippingEvent {
        private String label;
        private EventType eventType;
        private LocalDateTime date;
    }
}

