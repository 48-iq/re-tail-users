package dev.ilya_anna.user_service.services;

import dev.ilya_anna.user_service.dto.UpdateUserDto;
import dev.ilya_anna.user_service.dto.UserAllInfoDto;
import dev.ilya_anna.user_service.dto.UserDto;
import dev.ilya_anna.user_service.entities.User;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;
import dev.ilya_anna.user_service.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DaoUserService implements UserService{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    public UserAllInfoDto getUserAllInfo(String userId){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("user with id " + userId + " not found"));

        return UserAllInfoDto.builder()
                .username(user.getUsername())
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

    public UserAllInfoDto updateUser(String userId, @Valid UpdateUserDto updateUserDto){
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

        return UserAllInfoDto.builder()
                .username(user.getUsername())
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

    public int getAnnouncementsCount(String userId) {
        String url = "/api/v1/announcement/get-announcements-count?userId=" + userId;
        return restTemplate.getForObject(url, Integer.class, userId);
    }
}
