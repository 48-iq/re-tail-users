package dev.ilya_anna.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User settings info (visibility of name, surname, email, phone, address and avatar)",
        name = "UserSettingsDto")
public class UserSettingsDto {
    @Schema(description = "Visibility of user name", example = "true")
    private boolean nameVisibility;

    @Schema(description = "Visibility of user surname", example = "false")
    private boolean surnameVisibility;

    @Schema(description = "Visibility of user email", example = "true")
    private boolean emailVisibility;

    @Schema(description = "Visibility of user phone", example = "true")
    private boolean phoneVisibility;

    @Schema(description = "Visibility of user address", example = "false")
    private boolean addressVisibility;

    @Schema(description = "Visibility of user avatar", example = "false")
    private boolean avatarVisibility;
}
