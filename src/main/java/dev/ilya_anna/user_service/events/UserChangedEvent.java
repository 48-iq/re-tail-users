package dev.ilya_anna.user_service.events;

import dev.ilya_anna.user_service.entities.UserSettings;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToOne;
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
public class UserChangedEvent {
    private String id;
    private String userId;
    private String name;
    private String surname;
    private String nickname;
    private String email;
    private String phone;
    private String address;
    private ZonedDateTime registeredAt;
    private String about;

    public static final String TOPIC = "user-changed-events-topic";

    public static Map<String, Object> toMap(UserChangedEvent userChangedEvent) {
        return Map.of(
                "id", userChangedEvent.getId(),
                "userId", userChangedEvent.getUserId(),
                "name", userChangedEvent.getName(),
                "surname", userChangedEvent.getSurname(),
                "nickname", userChangedEvent.getNickname(),
                "phone", userChangedEvent.getPhone(),
                "email", userChangedEvent.getEmail(),
                "address", userChangedEvent.getAddress(),
                "registeredAt", userChangedEvent.getRegisteredAt().toString(),
                "about", userChangedEvent.getAbout()
        );
    }

    public static UserChangedEvent fromMap(Map<String, Object> map) {
        return UserChangedEvent.builder()
                .id((String) map.get("id"))
                .userId((String) map.get("userId"))
                .name((String) map.get("name"))
                .surname((String) map.get("surname"))
                .nickname((String) map.get("nickname"))
                .phone((String) map.get("phone"))
                .email((String) map.get("email"))
                .address((String) map.get("address"))
                .registeredAt(ZonedDateTime.parse((String) map.get("registeredAt")))
                .about((String) map.get("about"))
                .build();
    }
}
