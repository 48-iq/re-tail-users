package dev.ilya_anna.user_service.events;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSignOutEvent {
    private String id;
    private String userId;
    private ZonedDateTime time;

    public static final String TOPIC = "user-sign-out-events-topic";

    public static Map<String, Object> toMap(UserSignOutEvent userSignOutEvent) {
        return Map.of(
                "id", userSignOutEvent.getId(),
                "userId", userSignOutEvent.getUserId(),
                "time", userSignOutEvent.getTime().toString()
        );
    }

    public static UserSignOutEvent fromMap(Map<String, Object> map) {
        return UserSignOutEvent.builder()
                .id((String) map.get("id"))
                .userId((String) map.get("userId"))
                .time(ZonedDateTime.parse((String) map.get("time")))
                .build();
    }
}
