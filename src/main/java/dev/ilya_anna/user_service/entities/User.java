package dev.ilya_anna.user_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {
    @Id
    private String id;
    private String name;
    private String surname;
    private String nickname;
    private String email;
    private String phone;
    private String address;
    private ZonedDateTime registeredAt;
    private String about;
    private String avatarImageId;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserSettings userSettings;
}
