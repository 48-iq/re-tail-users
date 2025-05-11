package dev.ilya_anna.user_service.services;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.ilya_anna.user_service.dto.UpdateUserDto;
import dev.ilya_anna.user_service.dto.UserAllInfoDto;
import dev.ilya_anna.user_service.dto.UserDto;
import dev.ilya_anna.user_service.entities.User;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;
import dev.ilya_anna.user_service.exceptions.JwtAuthenticationException;
import dev.ilya_anna.user_service.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class DaoUserService implements UserService{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;

    public UserAllInfoDto getUserAllInfo(String userId, String authHeader){
        validateAuthorization(userId, authHeader);

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
                .announcementsCount(user.getAnnouncementsCount())
                .about(user.getAbout())
                .avatarImageId(user.getAvatarImageId())
                .build();
    }

    public UserDto getUser(String userId, String authHeader){
        if (authHeader == null) {
            throw new JwtAuthenticationException("Missing or invalid Authorization header");
        }

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
                .announcementsCount(user.getAnnouncementsCount())
                .about(user.getAbout())
                .avatarImageId(user.getAvatarImageId())
                .build();
    }

    public UserAllInfoDto updateUser(String userId, @Valid UpdateUserDto updateUserDto, String authHeader){
        validateAuthorization(userId, authHeader);

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
                .announcementsCount(user.getAnnouncementsCount())
                .about(user.getAbout())
                .avatarImageId(user.getAvatarImageId())
                .build();
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
