package com.lernify.lernify_backend.controller;

import com.lernify.lernify_backend.dto.AuthRequest;
import com.lernify.lernify_backend.model.User;
import com.lernify.lernify_backend.repository.UserRepository;
import com.lernify.lernify_backend.security.TokenService;
import com.lernify.lernify_backend.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AuthController(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    /** REGISTER */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {

        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username, email, and password are required"));
        }

        // Check if username or email already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username already exists"));
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already exists"));
        }

        // Save user to database
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // TODO: hash password in production
        userRepository.save(user);

        System.out.println("✅ User registered: " + request.getUsername());
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    /**  LOGIN  */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {

        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username and password must not be empty"));
        }

        return userRepository.findByUsername(request.getUsername())
                .filter(u -> u.getPassword().equals(request.getPassword())) // TODO: use hashed passwords
                .map(u -> {
                    String token = JwtUtil.generateToken(u.getUsername());
                    System.out.println("Generated JWT token: " + token);
                    return ResponseEntity.ok(Map.of("token", token));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid credentials")));
    }

    /**  LOGOUT  */
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

            System.out.println("👋 Logout: " + username);
        } catch (JwtException e) {
            System.out.println("⚠️ Invalid token during logout");
        }

        return ResponseEntity.ok().build();
    }

    /**  GET CURRENT USER  */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing token"));
        }

        String token = authHeader.substring(7);
        try {
            String username = JwtUtil.validateTokenAndGetUsername(token);

            return userRepository.findByUsername(username)
                    .map(user -> Map.of(
                            "username", user.getUsername(),
                            "email", user.getEmail()
                    ))
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "User not found")));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
        }
    }
}
