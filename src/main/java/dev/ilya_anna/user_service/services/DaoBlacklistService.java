package dev.ilya_anna.user_service.services;

import dev.ilya_anna.user_service.entities.SignOutMark;
import dev.ilya_anna.user_service.events.UserSignOutEvent;
import dev.ilya_anna.user_service.repositories.SignOutMarkRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class DaoBlacklistService implements BlacklistService {
    @Value("${app.jwt.access.duration}")
    private long accessTokenDuration;
    @Autowired
    private SignOutMarkRepository signOutMarkRepository;

    public boolean isInBlacklist(String userId){
        return signOutMarkRepository.existsByUserId(userId);
    }

    public void addToBlacklist(UserSignOutEvent userSignOutEvent){
        SignOutMark signOutMark = SignOutMark.builder()
                .id(userSignOutEvent.getId())
                .userId(userSignOutEvent.getUserId())
                .signOutTime(userSignOutEvent.getTime())
                .ttl(accessTokenDuration)
                .build();
        signOutMarkRepository.save(signOutMark);
    }
}
