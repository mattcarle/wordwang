package com.wordwang.geolocation;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Best-effort city/region/country lookup for a player's IP, via the free ipapi.co public
 * geolocation API - used to populate the game audit trail (see com.wordwang.audit). Never throws:
 * a private/loopback IP, a null IP, or any lookup failure (timeout, rate limit, etc.) all resolve
 * to "Unknown" rather than blocking or failing the caller.
 */
@Service
public class GeolocationService {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);
    private static final String UNKNOWN = "Unknown";

    private final RestClient restClient;

    public GeolocationService() {
        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(TIMEOUT).build());
        requestFactory.setReadTimeout(TIMEOUT);
        this.restClient = RestClient.builder()
                .baseUrl("https://ipapi.co")
                .requestFactory(requestFactory)
                .build();
    }

    public String locate(String ipAddress) {
        if (ipAddress == null || isPrivateOrLoopback(ipAddress)) {
            return UNKNOWN;
        }
        try {
            IpApiResponse response = restClient.get()
                    .uri("/{ip}/json/", ipAddress)
                    .retrieve()
                    .body(IpApiResponse.class);
            return format(response);
        } catch (Exception e) {
            return UNKNOWN;
        }
    }

    private String format(IpApiResponse response) {
        if (response == null || Boolean.TRUE.equals(response.error())) {
            return UNKNOWN;
        }
        StringBuilder result = new StringBuilder();
        appendPart(result, response.city());
        appendPart(result, response.region());
        appendPart(result, response.countryName());
        return result.isEmpty() ? UNKNOWN : result.toString();
    }

    private void appendPart(StringBuilder builder, String part) {
        if (part == null || part.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(", ");
        }
        builder.append(part);
    }

    private boolean isPrivateOrLoopback(String ipAddress) {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            return address.isLoopbackAddress() || address.isSiteLocalAddress()
                    || address.isLinkLocalAddress() || address.isAnyLocalAddress();
        } catch (UnknownHostException e) {
            return true;
        }
    }
}
