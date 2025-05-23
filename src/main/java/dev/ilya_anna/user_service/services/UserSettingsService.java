package dev.ilya_anna.user_service.services;

import dev.ilya_anna.user_service.dto.UserSettingsDto;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;

public interface UserSettingsService {
    UserSettingsDto updateUserSettings(String userId, UserSettingsDto userSettingsDto) throws UserNotFoundException;

    UserSettingsDto getUserSettings(String userId) throws UserNotFoundException;
}
