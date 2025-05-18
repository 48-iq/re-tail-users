package dev.ilya_anna.user_service.services;

import dev.ilya_anna.user_service.dto.UpdateUserDto;
import dev.ilya_anna.user_service.dto.UserDto;
import dev.ilya_anna.user_service.entities.User;
import dev.ilya_anna.user_service.entities.UserSettings;
import dev.ilya_anna.user_service.events.UserCreatedEvent;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;
import dev.ilya_anna.user_service.repositories.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;


@Validated
@Service
public class DaoUserService implements UserService{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    public UserDto getUserAllInfo(String userId){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("user with id " + userId + " not found"));

        return UserDto.builder()
                .name(user.getName())
                .surname(user.getSurname())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .registeredAt(user.getRegisteredAt())
                .announcementsCount(getAnnouncementsCount(userId))
                .about(user.getAbout())
                .avatarImageId(user.getAvatarImageId())
                .build();
    }

    public UserDto getUser(String userId){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("user with id " + userId + " not found"));
        UserSettings userSettings = user.getUserSettings();

        return UserDto.builder()
                .name(userSettings.isNameVisibility()?user.getName():null)
                .surname(userSettings.isNameVisibility()?user.getSurname():null)
                .nickname(user.getNickname())
                .email(userSettings.isNameVisibility()?user.getEmail():null)
                .phone(userSettings.isNameVisibility()?user.getPhone():null)
                .address(userSettings.isNameVisibility()?user.getAddress():null)
                .registeredAt(user.getRegisteredAt())
                .announcementsCount(getAnnouncementsCount(userId))
                .about(user.getAbout())
                .avatarImageId(userSettings.isNameVisibility()?user.getAvatarImageId():null)
                .build();
    }

    @Transactional
    public UserDto updateUser(String userId, @Valid UpdateUserDto updateUserDto){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("user with id " + userId + " not found"));
        user.setName(updateUserDto.getName());
        user.setSurname(updateUserDto.getSurname());
        user.setNickname(updateUserDto.getNickname());
        user.setEmail(updateUserDto.getEmail());
        user.setPhone(updateUserDto.getPhone());
        user.setAddress(updateUserDto.getAddress());
        user.setAbout(updateUserDto.getAbout());
        userRepository.save(user);

        return UserDto.builder()
                .name(user.getName())
                .surname(user.getSurname())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .registeredAt(user.getRegisteredAt())
                .announcementsCount(getAnnouncementsCount(userId))
                .about(user.getAbout())
                .avatarImageId(user.getAvatarImageId())
                .build();
    }

    @Transactional
    public void createUser(UserCreatedEvent userCreatedEvent){
        User user = User.builder()
                .id(userCreatedEvent.getUserId())
                .name(userCreatedEvent.getName())
                .surname(userCreatedEvent.getSurname())
                .nickname(userCreatedEvent.getNickname())
                .phone(userCreatedEvent.getPhone())
                .email(userCreatedEvent.getEmail())
                .registeredAt(userCreatedEvent.getTime())
                .build();

        userRepository.save(user);
    }

    private int getAnnouncementsCount(String userId) {
        String url = "/api/v1/announcement/get-announcements-count?userId=" + userId;
        return restTemplate.getForObject(url, Integer.class, userId);
    }
}
