package com.auralynk.controller;

import com.auralynk.dto.AuthResponse;
import com.auralynk.dto.LoginRequest;
import com.auralynk.dto.RegisterRequest;
import com.auralynk.dto.GoogleTokenRequest;
import com.auralynk.model.User;
import com.auralynk.security.JwtTokenUtil;
import com.auralynk.service.UserService;
import com.auralynk.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final GoogleAuthService googleAuthService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        User user = userService.createUser(request.getEmail(), request.getPassword(), request.getName());
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtTokenUtil.generateToken(userDetails);
        
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getName()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        User user = userService.findByEmail(request.getEmail());
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtTokenUtil.generateToken(userDetails);
        
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getName()));
    }

    @PostMapping("/google/login")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleTokenRequest request) {
        log.info("Received Google login request");
        try {
            String email = googleAuthService.verifyGoogleToken(request.getToken());
            log.debug("Token verified for email: {}", email);
            
            User user = userService.findByEmail(email);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            String token = jwtTokenUtil.generateToken(userDetails);
            
            log.info("Google login successful for user: {}", email);
            return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getName()));
        } catch (Exception e) {
            log.error("Google login failed", e);
            throw e;
        }
    }
} 