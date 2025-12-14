package com.lernify.lernify_backend.controller;

import com.lernify.lernify_backend.dto.AuthRequest;
import com.lernify.lernify_backend.security.TokenService;
import com.lernify.lernify_backend.util.JwtUtil;
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

    private final Map<String, String> users = new HashMap<>();       // registered users
    private final Set<String> loggedInUsers = new HashSet<>();       // simple log only
    private final TokenService tokenService;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody AuthRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username and password must not be empty"));
        }

        if (users.containsKey(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "User already exists"));
        }

        users.put(request.getUsername(), request.getPassword());
        System.out.println("✅ User registered: " + request.getUsername());
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username and password must not be empty"));
        }

        String storedPassword = users.get(request.getUsername());
        if (storedPassword != null && storedPassword.equals(request.getPassword())) {
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
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        System.out.println("Logout endpoint called. Auth header: " + authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenService.blacklistToken(token);
            String username = JwtUtil.validateTokenAndGetUsername(token);
            loggedInUsers.remove(username);

            System.out.println("👋 Logout: " + username);
            System.out.println("📋 Logged-in users: " + loggedInUsers);
        } else {
            System.out.println("❗ Authorization header missing or invalid during logout.");
        }
        return ResponseEntity.ok().build();
    }
}