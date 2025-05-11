package dev.ilya_anna.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettingsDto {
    private boolean nameVisibility;
    private boolean surnameVisibility;
    private boolean emailVisibility;
    private boolean phoneVisibility;
    private boolean addressVisibility;
    private boolean avatarVisibility;
}
