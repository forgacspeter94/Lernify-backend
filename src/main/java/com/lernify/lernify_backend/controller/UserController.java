package com.lernify.lernify_backend.controller;

import com.lernify.lernify_backend.model.User;
import com.lernify.lernify_backend.repository.UserRepository;
import com.lernify.lernify_backend.security.TokenService;
import com.lernify.lernify_backend.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public UserController(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    /** GET CURRENT USER  */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing token"));
        }

        String token = authHeader.substring(7);
        try {
            String username = JwtUtil.validateTokenAndGetUsername(token);

            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            User user = userOpt.get();
            return ResponseEntity.ok(Map.of(
                    "username", user.getUsername(),
                    "email", user.getEmail()
            ));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
        }
    }

    /** UPDATE USER */
    @PutMapping
    public ResponseEntity<?> updateUser(@RequestHeader("Authorization") String authHeader,
                                        @RequestBody Map<String, String> updates) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing token"));
        }

        String token = authHeader.substring(7);
        try {
            String username = JwtUtil.validateTokenAndGetUsername(token);

            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            User user = userOpt.get();

            // Update fields if provided
            if (updates.containsKey("username")) user.setUsername(updates.get("username"));
            if (updates.containsKey("email")) user.setEmail(updates.get("email"));
            if (updates.containsKey("password")) user.setPassword(updates.get("password")); // TODO: hash in production

            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "User updated successfully",
                    "username", user.getUsername(),
                    "email", user.getEmail()
            ));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
        }
    }

    /**  DELETE ACCOUNT */
    @DeleteMapping
    public ResponseEntity<?> deleteAccount(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing token"));
        }

        String token = authHeader.substring(7);
        try {
            String username = JwtUtil.validateTokenAndGetUsername(token);

            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            userRepository.delete(userOpt.get());
            tokenService.blacklistToken(token); // invalidate token

            return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
        }
    }
}
