package com.rental.music.service;

import com.rental.music.dto.LocationInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Integrates with the public Zippopotam.us REST API to enrich customer bookings
 * with city and state metadata derived from a US postal code.
 *
 * Endpoint Documentation:
 *   GET https://api.zippopotam.us/us/{postalCode}
 *   - Path Variable: postalCode (5-digit US ZIP code, e.g., 90210)
 *   - Response (200 OK): JSON containing "post code", "country", and "places" array
 *     with "place name" and "state abbreviation".
 *   - Failure Handling: Catches RestClientException / 404 / timeouts gracefully and
 *     returns a fallback string so external API downtime never blocks bookings.
 */
@Service
public class LocationLookupService {

    private static final Logger log = LoggerFactory.getLogger(LocationLookupService.class);
    private static final String ZIP_API_URL = "https://api.zippopotam.us/us/{postalCode}";

    private final RestTemplate restTemplate;

    public LocationLookupService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String resolveCityState(String postalCode) {
        if (postalCode == null || postalCode.isBlank()) {
            return "Unknown Location";
        }
        String sanitizedZip = postalCode.trim().split("-")[0];
        try {
            LocationInfoDTO response = restTemplate.getForObject(
                    ZIP_API_URL, LocationInfoDTO.class, sanitizedZip);

            if (response != null && response.places() != null && !response.places().isEmpty()) {
                LocationInfoDTO.Place place = response.places().get(0);
                return place.placeName() + ", " + place.stateAbbreviation();
            }
            return "Location Unverified (" + sanitizedZip + ")";
        } catch (RestClientException ex) {
            log.warn("Postal code lookup failed for '{}': {}", sanitizedZip, ex.getMessage());
            return "Location Lookup Unavailable (" + sanitizedZip + ")";
        }
    }
}