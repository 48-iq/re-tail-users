package dev.ilya_anna.user_service.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserDto {
    @Pattern(regexp = "^[a-zA-Zа-яёА-ЯЁ -]{1,64}$")
    private String name;
    @Pattern(regexp = "^[a-zA-Zа-яёА-ЯЁ -]{1,64}$")
    private String surname;
    @Pattern(regexp = "^[a-zA-Z0-9.-]{1,32}$")
    private String nickname;
    @Pattern(regexp = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    private String email;
    @Pattern(regexp = "^(\\+\\d{1,3}( )?)?((\\(\\d{1,3}\\))|\\d{1,3})[- .]?\\d{3,4}[- .]?\\d{4}$")
    private String phone;
    @Pattern(regexp = "^[a-zA-Zа-яёА-ЯЁ0-9 .,/'-]{0,128}$")
    private String address;
    @Pattern(regexp = "^.{0,500}$")
    private String about;
}