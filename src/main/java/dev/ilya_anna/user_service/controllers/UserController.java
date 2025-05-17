package dev.ilya_anna.user_service.controllers;

import dev.ilya_anna.user_service.dto.UpdateUserDto;
import dev.ilya_anna.user_service.dto.UserAllInfoDto;
import dev.ilya_anna.user_service.dto.UserDto;
import dev.ilya_anna.user_service.dto.UserSettingsDto;
import dev.ilya_anna.user_service.exceptions.AvatarNotFoundException;
import dev.ilya_anna.user_service.exceptions.JwtAuthenticationException;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;
import dev.ilya_anna.user_service.security.DaoUserDetails;
import dev.ilya_anna.user_service.services.AvatarService;
import dev.ilya_anna.user_service.services.UserService;
import dev.ilya_anna.user_service.services.UserSettingsService;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @Autowired
    private AvatarService avatarService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserSettingsService userSettingsService;

    @GetMapping("/get-all-info/{userId}")
    public ResponseEntity<UserAllInfoDto> getUserAllInfo(
            @PathVariable String userId, @AuthenticationPrincipal DaoUserDetails userDetails) {

        if (userDetails == null || !userDetails.getUsername().equals(userId)) {
            log.warn("Access denied for user {}", userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            return ResponseEntity.ok(userService.getUserAllInfo(userId));
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable String userId){
        try {
            return ResponseEntity.ok(userService.getUser(userId));
        } catch (UserNotFoundException e) {
            log.error("User {} not found", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/update-user/{userId}")
    public ResponseEntity<UserAllInfoDto> updateUser(@PathVariable String userId,
                                                     @RequestBody UpdateUserDto updateUserDto,
                                                     @AuthenticationPrincipal DaoUserDetails userDetails) {
        if (userDetails == null || !userDetails.getUsername().equals(userId)) {
            log.warn("Access denied for user {}", userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(userService.updateUser(userId, updateUserDto));
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/update-user-settings/{userId}")
    public ResponseEntity<UserSettingsDto> updateUserSettings(@PathVariable String userId,
                                                              @RequestBody UserSettingsDto userSettingsDto,
                                                              @AuthenticationPrincipal DaoUserDetails userDetails) {
        if (userDetails == null || !userDetails.getUsername().equals(userId)) {
            log.warn("Access denied for user {}", userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(userSettingsService.updateUserSettings(userId, userSettingsDto));
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/get-avatar/{userId}")
    public ResponseEntity<Resource> getUserAvatar(@PathVariable String userId){
        try {
            return ResponseEntity.ok(avatarService.getAvatar(userId));
        } catch (UserNotFoundException e) {
            log.error("User {} not found", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AvatarNotFoundException e) {
            log.error("Avatar not found for user {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e){
            log.error("Failed to upload avatar for user {}", userId);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @PostMapping("/update-avatar/{userId}")
    public ResponseEntity<String> updateUserAvatar(@PathVariable String userId,
                                                   @RequestParam("avatar") MultipartFile avatarFile,
                                                   @AuthenticationPrincipal DaoUserDetails userDetails){
        if (userDetails == null || !userDetails.getUsername().equals(userId)) {
            log.warn("Access denied for user {}", userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(avatarService.updateAvatar(userId, avatarFile));
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AvatarNotFoundException e) {
            log.error("Avatar not found for user {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e){
            log.error("Failed to upload avatar for user {}", userId);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}
