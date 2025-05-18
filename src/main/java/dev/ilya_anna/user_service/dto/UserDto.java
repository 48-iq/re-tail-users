package dev.ilya_anna.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User info", name = "UserDto")
public class UserDto {
    @Schema(description = "User name", example = "John")
    private String name;

    @Schema(description = "User surname", example = "Doe")
    private String surname;

    @Schema(description = "User nickname that will be visible to other users", example = "my_nickname")
    private String nickname;

    @Schema(description = "User email", example = "Q2t5t@example.com")
    private String email;

    @Schema(description = "User phone number", example = "+380501234567")
    private String phone;

    @Schema(description = "User address", example = "79 Spring St, Moscow")
    private String address;

    @Schema(description = "Date and time of creating user account", example = "2023-05-18T14:30:45+03:0")
    private ZonedDateTime registeredAt;

    @Schema(description = "Count of user announcements", example = "7")
    private Integer announcementsCount;

    @Schema(description = "User account description", example = "Aspiring entrepreneur")
    private String about;

    @Schema(description = "Id of user avatar", example = "ur833jr38rur8u3cru")
    private String avatarImageId;
}
