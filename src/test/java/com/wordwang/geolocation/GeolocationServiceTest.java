package com.wordwang.geolocation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Only exercises the private/loopback/null short-circuit paths, which never make a network call -
 * this test suite must stay runnable with no internet access, so it never triggers a real lookup
 * against a public IP.
 */
class GeolocationServiceTest {

    private final GeolocationService service = new GeolocationService();

    @Test
    void nullIpResolvesToUnknown() {
        assertThat(service.locate(null)).isEqualTo("Unknown");
    }

    @Test
    void loopbackIpResolvesToUnknown() {
        assertThat(service.locate("127.0.0.1")).isEqualTo("Unknown");
        assertThat(service.locate("::1")).isEqualTo("Unknown");
    }

    @Test
    void privateIpResolvesToUnknown() {
        // Deliberately not testing a non-IP-literal string here: InetAddress.getByName falls back
        // to a real DNS lookup for anything that isn't already a valid IP literal, which would
        // make this test's outcome depend on network/DNS availability.
        assertThat(service.locate("192.168.1.50")).isEqualTo("Unknown");
        assertThat(service.locate("10.0.0.5")).isEqualTo("Unknown");
        assertThat(service.locate("172.16.0.5")).isEqualTo("Unknown");
    }
}
