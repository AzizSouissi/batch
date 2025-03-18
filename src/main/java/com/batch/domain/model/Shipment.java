package com.batch.domain.model;

import com.batch.domain.enumeration.CarrierOption;
import com.batch.domain.enumeration.Gender;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "shipment_requests")
@JsonInclude(JsonInclude.Include.NON_NULL)
@CompoundIndexes({
        @CompoundIndex(name = "carrier_status_idx", def = "{'carrierName': 1, 'lastEventType': 1}"),
        @CompoundIndex(name = "status_date_idx", def = "{'lastEventType': 1, 'lastUpdateDate': -1}"),
        @CompoundIndex(name = "carrier_processing_idx", def = "{'carrierName': 1, 'processingInProgress': 1, 'nextCheckDate': 1}"),
        @CompoundIndex(name = "pending_updates_idx", def = "{'processingInProgress': 1, 'lastUpdateDate': 1, 'retryCount': 1}")
})
public class Shipment {

    @Id
    private String id;

    @Indexed
    private String reference;

    private String externalReference;
    private Address toAddress;
    private Address fromAddress;

    @Builder.Default
    private List<Parcel> parcels = new ArrayList<>();

    private String contentDescription;

    // Carrier information
    private String carrierName;
    private String carrierService;
    private String carrierID;

    @Builder.Default
    private List<CarrierOption> carrierOptions = new ArrayList<>();

    // Insurance data
    private Amount insurance;

    // Tracking information
    @Indexed(unique = true)
    private String trackingNumber;

    private String reservationNumber;
    private String trackingUrl;
    private String labelUrl;

    // Processing & Status information
    @Indexed
    private LocalDateTime creationDate;

    private LocalDateTime lastUpdateDate;

    private String lastEventType;

    private Boolean processingInProgress;

    private Integer retryCount;

    private LocalDateTime nextCheckDate;

    // External information
    @Builder.Default
    private Map<String, Object> businessContext = new HashMap<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder(toBuilder = true)
    public static class Address {
        private String name;
        private String company;
        private String street1;
        private String street2;
        private String city;
        private String state;
        private String zip;
        private String country;
        private String phone;
        private String email;
        private Gender gender;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Parcel {
        @Builder.Default
        private Float length = 30F;

        @Builder.Default
        private Float width = 30F;

        @Builder.Default
        private Float height = 30F;

        @Builder.Default
        private Float weight = 1f;

        @Builder.Default
        private String unitWeight = "KGM";

        private Amount purchasePrice;
        private String originCountry;
        private String code;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Amount {
        private BigDecimal value;
        private Currency currencyCode;
    }

    public boolean isInternational() {
        if (fromAddress == null || toAddress == null ||
                fromAddress.getCountry() == null || toAddress.getCountry() == null) {
            return false;
        }
        return !fromAddress.getCountry().equalsIgnoreCase(toAddress.getCountry());
    }

    public boolean isProductDanger() {
        return carrierOptions.stream()
                .anyMatch(carrierOption -> carrierOption == CarrierOption.DANGER);
    }

    public <T> Optional<T> getBusinessContextByKey(String key, Class<T> targetClass) {
        return Optional.ofNullable(businessContext)
                .map(map -> map.get(key))
                .filter(targetClass::isInstance)
                .map(targetClass::cast);
    }
}
