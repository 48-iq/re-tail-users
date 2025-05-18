package dev.ilya_anna.user_service.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SeedUuidService implements UuidService {
    @Value("${app.uuid.seed}")
    private String seed;
    @Override
    public String generate() {
        return seed + UUID.randomUUID();
    }
}
