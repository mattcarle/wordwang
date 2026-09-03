package com.wordwang.geolocation;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Shape of a https://ipapi.co/{ip}/json/ response, trimmed to the fields GeolocationService uses. */
record IpApiResponse(
        String city,
        String region,
        @JsonProperty("country_name") String countryName,
        Boolean error) {
}
