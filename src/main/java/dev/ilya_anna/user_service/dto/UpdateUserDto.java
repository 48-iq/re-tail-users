package dev.ilya_anna.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User data for updating", name = "UpdateUserDto")
public class UpdateUserDto {
    @Schema(description = "User name", example = "John")
    @Pattern(regexp = "^[a-zA-Zа-яёА-ЯЁ -]{1,64}$")
    private String name;

    @Schema(description = "User surname", example = "Doe")
    @Pattern(regexp = "^[a-zA-Zа-яёА-ЯЁ -]{1,64}$")
    private String surname;

    @Schema(description = "User nickname that will be visible to other users", example = "my_nickname")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{1,32}$")
    private String nickname;

    @Schema(description = "User email", example = "Q2t5t@example.com")
    @Pattern(regexp = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    private String email;

    @Schema(description = "User phone number", example = "+380501234567")
    @Pattern(regexp = "^(\\+\\d{1,3}( )?)?((\\(\\d{1,3}\\))|\\d{1,3})[- .]?\\d{3,4}[- .]?\\d{4}$")
    private String phone;

    @Schema(description = "User address", example = "79 Spring St, Moscow")
    @Pattern(regexp = "^[a-zA-Zа-яёА-ЯЁ0-9 .,/'-]{0,128}$")
    private String address;

    @Schema(description = "User account description", example = "Aspiring entrepreneur")
    @Pattern(regexp = "^.{0,500}$")
    private String about;
}