package com.wordwang.admin;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final AdminCredentialRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdminCredentialRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isSetupRequired() {
        return repository.count() == 0;
    }

    /** Sets the one admin password. Only allowed once - see isSetupRequired(). */
    public void setupPassword(String rawPassword) {
        if (!isSetupRequired()) {
            throw new IllegalStateException("Admin password is already set up");
        }
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        repository.save(new AdminCredential(passwordEncoder.encode(rawPassword)));
    }

    public boolean verifyPassword(String rawPassword) {
        return repository.findFirstByOrderByIdAsc()
                .map(credential -> passwordEncoder.matches(rawPassword, credential.getPasswordHash()))
                .orElse(false);
    }
}
