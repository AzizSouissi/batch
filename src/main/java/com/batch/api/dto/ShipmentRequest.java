package com.batch.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.platana.shipment.domain.enumeration.CarrierOption;
import fr.platana.shipment.domain.enumeration.Gender;
import fr.platana.shipment.domain.model.OrganizationConfiguration;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;

import static fr.platana.shipment.domain.exception.DomainErrorCodes.*;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShipmentRequest {

    private String reference;
    private String externalReference;

    private Address toAddress;
    private Address fromAddress;
    @Builder.Default
    private List<Parcel> parcels = new ArrayList<>();
    private String contentDescription;

    /**
     * CARRIER INFORMATION
     */
    private String carrierName;
    private String carrierService;
    private String carrierID;
    @Builder.Default
    private List<CarrierOption> carrierOptions = new ArrayList<>();
    /**
     * INSURANCE DATA
     */
    private Amount insurance;
    /**
     * EXTERNAL INFORMATION
     */
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
        @Builder.Default
        private String state = EMPTY;
        private String zip;
        private String country;
        @Setter(AccessLevel.NONE)
        private String phone;
        @Setter(AccessLevel.NONE)
        private String email;
        private Gender gender;

        public void setEmail(String email) {
            this.email = StringUtils.deleteWhitespace(email);
        }

        public void setPhone(String phone) {
            this.phone = StringUtils.deleteWhitespace(phone);
        }

        public boolean hasInValidCountryCode() {
            if (isBlank(country)) {
                return true;
            }
            return !Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2)
                    .contains(country);
        }

        public void replaceWithOrganizationContact(OrganizationConfiguration.Contact organizationContact) {
            if (isNotEmpty(organizationContact)) {
                this.email = organizationContact.getEmail();
                this.phone = organizationContact.getPhoneNumber();
            }
        }
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

        public static BigDecimal getPrice(Amount amount) {
            return Optional.ofNullable(amount)
                    .map(Amount::getValue)
                    .orElse(BigDecimal.ZERO);
        }

        public static String getCurrency(Amount amount) {
            return Optional.ofNullable(amount)
                    .map(Amount::getCurrencyCode)
                    .map(Currency::getCurrencyCode)
                    .orElse(".");
        }
    }

    public String validateShipmentContact() {
        if (isNotEmpty(fromAddress)) {
            return SHIPMENT_INFORMATION_FROM_ADDRESS_NOT_FOUND;
        }

        if (isNotEmpty(toAddress)) {
            return SHIPMENT_INFORMATION_TO_ADDRESS_NOT_FOUND;
        }

        if (toAddress.hasInValidCountryCode() || toAddress.hasInValidCountryCode()) {
            return COUNTRY_CODE_NOT_VALID;
        }
        return null;
    }

    public boolean isInternational() {
        final String destinationCountry = toAddress.getCountry();
        final String originCountry = fromAddress.getCountry();
        return !originCountry.equalsIgnoreCase(destinationCountry);
    }
    public boolean isProductDanger() {
        return carrierOptions
                .stream()
                .anyMatch(carrierOption -> carrierOption == CarrierOption.DANGER);
    }
    public <T> Optional<T> getBusinessContextByKey(String key, Class<T> targetClass) {
        return Optional.ofNullable(businessContext)
                .map(map -> map.get(key))
                .filter(variable -> isNotEmpty(variable) && targetClass.isInstance(variable))
                .map(targetClass::cast);
    }
}
