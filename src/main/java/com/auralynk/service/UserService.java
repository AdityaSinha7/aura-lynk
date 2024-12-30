package com.auralynk.service;

import com.auralynk.model.User;
import com.auralynk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(String email, String password, String name) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setName(name);
        
        return userRepository.save(user);
    }

    public User createGoogleUser(String email, String name, String googleId) {
        User user = userRepository.findByEmail(email)
                .orElseGet(User::new);
        
        user.setEmail(email);
        user.setName(name);
        user.setGoogleId(googleId);
        
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
} 