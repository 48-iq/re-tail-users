package dev.ilya_anna.user_service.services;

import dev.ilya_anna.user_service.dto.UserSettingsDto;
import dev.ilya_anna.user_service.entities.UserSettings;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;
import dev.ilya_anna.user_service.repositories.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DaoUserSettingsService implements UserSettingsService{
    @Autowired
    private UserSettingsRepository userSettingsRepository;

    public UserSettingsDto updateUserSettings(String userId, UserSettingsDto userSettingsDto){
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
}
