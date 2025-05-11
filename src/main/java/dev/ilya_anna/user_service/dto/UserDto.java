package dev.ilya_anna.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private String name;
    private String surname;
    private String nickname;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime registeredAt;
    private Integer announcementsCount;
    private String about;
    private String avatarImageId;
}
