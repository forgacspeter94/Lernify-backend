package com.lernify.lernify_backend.security;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class TokenService {

    private final Set<String> blacklist = Collections.synchronizedSet(new HashSet<>());

    public void blacklistToken(String token) {
        blacklist.add(token);
        System.out.println("🔒 Token blacklisted: " + token);
    }

    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
}