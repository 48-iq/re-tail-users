package dev.ilya_anna.user_service.services;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class JwtBlacklistService {
    private final Set<String> blacklistedTokens = new HashSet<>();

    @KafkaListener(topics = "user-sign-out-events-topic")
    public void listenForBlacklistedTokens(String token) {
        blacklistedTokens.add(token);
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
