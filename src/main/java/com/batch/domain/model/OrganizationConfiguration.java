package com.batch.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationConfiguration {
    private String organizationCode;
    private String label;
    private String language;
    private Contact contact;
    private Address address;
    private String countryCode;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder(toBuilder = true)
    public static class Contact {
        private String phoneNumber;
        private String mobileNumber;
        private String email;
        private String fax;
    }


    @Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        private String countryName;
        private String countryCode;
        private String city;
        private String zipCode;
        private String address;
        private String secondAddress;
        private String fullAddress;
    }
}
