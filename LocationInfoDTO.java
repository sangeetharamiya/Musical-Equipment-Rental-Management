package com.rental.music.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LocationInfoDTO(
    @JsonProperty("post code") String postCode,
    String country,
    List<Place> places
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Place(
        @JsonProperty("place name") String placeName,
        @JsonProperty("state abbreviation") String stateAbbreviation
    ) {}
}