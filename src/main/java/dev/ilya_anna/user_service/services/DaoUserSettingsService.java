package dev.ilya_anna.user_service.services;
;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.ilya_anna.user_service.dto.UserSettingsDto;
import dev.ilya_anna.user_service.entities.UserSettings;
import dev.ilya_anna.user_service.exceptions.JwtAuthenticationException;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;
import dev.ilya_anna.user_service.repositories.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class DaoUserSettingsService implements UserSettingsService{
    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private JwtService jwtService;

    public UserSettingsDto updateUserSettings(String userId, UserSettingsDto userSettingsDto, String authHeader){
        validateAuthorization(userId, authHeader);

        UserSettings userSettings = userSettingsRepository.findByUserId(userId).orElseThrow(
                () -> new UserNotFoundException("user with id " + userId + " not found"));
        userSettings.setNameVisibility(userSettingsDto.isNameVisibility());
        userSettings.setSurnameVisibility(userSettingsDto.isSurnameVisibility());
        userSettings.setEmailVisibility(userSettingsDto.isEmailVisibility());
        userSettings.setPhoneVisibility(userSettingsDto.isPhoneVisibility());
        userSettings.setAddressVisibility(userSettingsDto.isAddressVisibility());
        userSettings.setAvatarVisibility(userSettingsDto.isAvatarVisibility());
        userSettingsRepository.save(userSettings);

        return userSettingsDto;
    }

    private void validateAuthorization(String userId, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new JwtAuthenticationException("Missing or invalid Authorization header");
        }

        String jwtToken = authHeader.substring(7);
        DecodedJWT decodedJWT = jwtService.verifyAccessToken(jwtToken);
        String tokenUserId = decodedJWT.getClaim("userId").asString();

        if (!userId.equals(tokenUserId)) {
            throw new AccessDeniedException("User ID mismatch");
        }
    }
}
