package com.batch.domain.model;

import com.batch.domain.enumeration.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "shipments")
@CompoundIndexes({
        @CompoundIndex(name = "carrier_status_idx", def = "{'carrierName': 1, 'lastEventType': 1}"),
        @CompoundIndex(name = "status_date_idx", def = "{'lastEventType': 1, 'lastUpdateDate': -1}")
})
public class Shipment {
    @Id
    private String id;

    @Indexed
    private String reference;

    @Indexed
    private String trackingNumber;

    @Indexed
    private String carrierName;

    private String carrierService;
    private String reservationNumber;
    private String trackingUrl;
    private String labelUrl;

    @Indexed
    private EventType lastEventType;

    @Indexed
    private LocalDateTime lastUpdateDate;

    private LocalDateTime creationDate;
    private LocalDateTime nextCheckDate;

    @Builder.Default
    private List<ShippingEvent> events = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    private boolean processingInProgress;
    private int retryCount;

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