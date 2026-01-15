package com.lernify.lernify_backend.controller;

import com.lernify.lernify_backend.model.User;
import com.lernify.lernify_backend.repository.UserRepository;
import com.lernify.lernify_backend.security.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    /** GET CURRENT USER */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        String username = getCurrentUsername();

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
    }

    /** UPDATE USER */
    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody Map<String, String> updates) {
        String username = getCurrentUsername();

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
    }

    /** DELETE ACCOUNT */
    @DeleteMapping
    public ResponseEntity<?> deleteAccount(@RequestHeader("Authorization") String authHeader) {
        String username = getCurrentUsername();

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }

        // Delete user
        userRepository.delete(userOpt.get());
        
        // Blacklist the token
        String token = authHeader.substring(7);
        tokenService.blacklistToken(token);

        System.out.println("✅ Account deleted for user: " + username);

        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }

    // Helper method to get current username from SecurityContext
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}