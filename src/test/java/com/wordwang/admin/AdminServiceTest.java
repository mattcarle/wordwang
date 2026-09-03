package com.wordwang.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class AdminServiceTest {

    @Autowired
    private AdminCredentialRepository repository;

    // Constructed in @BeforeEach (not as a field initializer) since @Autowired injection of
    // `repository` happens after construction - a field initializer here would see it as null.
    // AdminService isn't a component this slice test's ApplicationContext registers (only the
    // JPA layer is), so it can't just be @Autowired either.
    private AdminService service;

    @BeforeEach
    void setUp() {
        service = new AdminService(repository, new BCryptPasswordEncoder());
    }

    @Test
    void setupIsRequiredUntilAPasswordHasBeenSet() {
        assertThat(service.isSetupRequired()).isTrue();

        service.setupPassword("correct-horse-battery");

        assertThat(service.isSetupRequired()).isFalse();
    }

    @Test
    void rejectsSettingUpASecondTime() {
        service.setupPassword("correct-horse-battery");

        assertThatThrownBy(() -> service.setupPassword("another-password"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsPasswordsShorterThanEightCharacters() {
        assertThatThrownBy(() -> service.setupPassword("short1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsAnEightCharacterPassword() {
        service.setupPassword("12345678");

        assertThat(service.isSetupRequired()).isFalse();
    }

    @Test
    void verifyPasswordMatchesOnlyTheStoredPassword() {
        service.setupPassword("correct-horse-battery");

        assertThat(service.verifyPassword("correct-horse-battery")).isTrue();
        assertThat(service.verifyPassword("wrong-password")).isFalse();
    }

    @Test
    void verifyPasswordIsFalseBeforeAnyPasswordIsSet() {
        assertThat(service.verifyPassword("anything")).isFalse();
    }
}
