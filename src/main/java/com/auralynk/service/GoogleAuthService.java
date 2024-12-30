package com.auralynk.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthService {

    @Value("${google.client.id}")
    private String clientId;

    private final UserService userService;

    public String verifyGoogleToken(String token) {
        log.info("Starting Google token verification");
        log.debug("Client ID configured: {}", clientId);
        try {
            log.debug("Creating Google ID Token verifier");
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new JacksonFactory())
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            log.debug("Attempting to verify token");
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken != null) {
                log.info("Token verified successfully");
                Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String googleId = payload.getSubject();

                log.debug("Token payload - Email: {}, Name: {}, Google ID: {}", 
                    email, name, googleId);

                userService.createGoogleUser(email, name, googleId);
                log.info("User created/updated successfully");
                return email;
            }
            log.error("Token verification failed - token is invalid");
            throw new RuntimeException("Invalid ID token.");
        } catch (Exception e) {
            log.error("Failed to verify Google token", e);
            throw new RuntimeException("Failed to verify Google token", e);
        }
    }
} 