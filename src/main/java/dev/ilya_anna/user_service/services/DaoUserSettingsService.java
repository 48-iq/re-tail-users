package dev.ilya_anna.user_service.services;

import dev.ilya_anna.user_service.dto.UserSettingsDto;
import dev.ilya_anna.user_service.entities.User;
import dev.ilya_anna.user_service.entities.UserSettings;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;
import dev.ilya_anna.user_service.repositories.UserRepository;
import dev.ilya_anna.user_service.repositories.UserSettingsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DaoUserSettingsService implements UserSettingsService{
    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UuidService uuidService;

    @Transactional
    public UserSettingsDto updateUserSettings(String userId, UserSettingsDto userSettingsDto){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("user with id " + userId + " not found"));
        UserSettings userSettings = user.getUserSettings();
        if (userSettings == null){
            userSettings = createUserSettings(userId);
        }

        userSettings.setNameVisibility(userSettingsDto.isNameVisibility());
        userSettings.setSurnameVisibility(userSettingsDto.isSurnameVisibility());
        userSettings.setEmailVisibility(userSettingsDto.isEmailVisibility());
        userSettings.setPhoneVisibility(userSettingsDto.isPhoneVisibility());
        userSettings.setAddressVisibility(userSettingsDto.isAddressVisibility());
        userSettings.setAvatarVisibility(userSettingsDto.isAvatarVisibility());
        userSettingsRepository.save(userSettings);

        return userSettingsDto;
    }

    public UserSettingsDto getUserSettings(String userId){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("user with id " + userId + " not found"));
        UserSettings userSettings = user.getUserSettings();
        if (userSettings == null){
            userSettings = createUserSettings(userId);
        }

        return UserSettingsDto.builder()
                .nameVisibility(userSettings.isNameVisibility())
                .surnameVisibility(userSettings.isSurnameVisibility())
                .emailVisibility(userSettings.isEmailVisibility())
                .phoneVisibility(userSettings.isPhoneVisibility())
                .addressVisibility(userSettings.isAddressVisibility())
                .avatarVisibility(userSettings.isAvatarVisibility())
                .build();
    };

    @Transactional
    private UserSettings createUserSettings(String userId){
        User user = userRepository.getReferenceById(userId);
        UserSettings userSettings = UserSettings.builder()
                .id(uuidService.generate())
                .nameVisibility(true)
                .surnameVisibility(true)
                .emailVisibility(true)
                .phoneVisibility(true)
                .addressVisibility(true)
                .avatarVisibility(true)
                .user(user)
                .build();
        userSettingsRepository.save(userSettings);

        return userSettings;
    }
}
