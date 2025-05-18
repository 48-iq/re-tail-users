package dev.ilya_anna.user_service.entities;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.ZonedDateTime;

@RedisHash
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignOutMark {
    @Id
    private String id;
    private String userId;
    private ZonedDateTime signOutTime;
    @TimeToLive
    private Long ttl;
}
