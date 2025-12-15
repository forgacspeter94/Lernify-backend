package com.lernify.lernify_backend.controller;

import com.lernify.lernify_backend.dto.AuthRequest;
import com.lernify.lernify_backend.dto.User;
import com.lernify.lernify_backend.security.TokenService;
import com.lernify.lernify_backend.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final Map<String, User> users = new HashMap<>();       // registered users
    private final Set<String> loggedInUsers = new HashSet<>();       // simple log only
    private final TokenService tokenService;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {

        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username, email and password are required"));
        }

        if (users.containsKey(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "User already exists"));
        }

        users.put(
                request.getUsername(),
                new User(request.getUsername(), request.getEmail(), request.getPassword())
        );

        System.out.println("✅ User registered: " + request.getUsername());
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username and password must not be empty"));
        }

        User user = users.get(request.getUsername());
        if (user != null && user.getPassword().equals(request.getPassword())){
            String token = JwtUtil.generateToken(request.getUsername());

            // Debug log to print the generated token
            System.out.println("Generated JWT token: " + token);

            loggedInUsers.add(request.getUsername());

            System.out.println("✅ Login: " + request.getUsername());
            System.out.println("📋 Logged-in users: " + loggedInUsers);

            return ResponseEntity.ok(Map.of("token", token));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid credentials"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok().build();
        }

        String token = authHeader.substring(7);

        try {
            String username = JwtUtil.validateTokenAndGetUsername(token);
            tokenService.blacklistToken(token);
            loggedInUsers.remove(username);

            System.out.println("👋 Logout: " + username);
        } catch (JwtException e) {
            System.out.println("⚠️ Invalid token during logout");
        }

        return ResponseEntity.ok().build();
    }
}
