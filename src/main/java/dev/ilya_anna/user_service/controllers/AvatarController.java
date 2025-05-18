package dev.ilya_anna.user_service.controllers;

import dev.ilya_anna.user_service.exceptions.AvatarNotFoundException;
import dev.ilya_anna.user_service.exceptions.InvalidImageFormatException;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;
import dev.ilya_anna.user_service.services.AvatarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/users-avatars")
@Tag(name = "Avatar controller",
        description = "Controller for managing user avatars"
)
public class AvatarController {
    @Autowired
    private AvatarService avatarService;

    @Operation(
            summary = "Gets user avatar",
            description = "Gets user avatar by id, " +
                    "returns avatar file"
    )
    @GetMapping("/{userId}")
    public ResponseEntity<Resource> getUserAvatar(@PathVariable String userId){
        try {
            return ResponseEntity.ok(avatarService.getAvatar(userId));
        } catch (AvatarNotFoundException e) {
            log.error("Avatar not found for user {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(
            summary = "Update user avatar",
            description = "Updates user avatar by user id and avatar file, " +
                    "returns avatar id"
    )
    @PostMapping("/{userId}")
    public ResponseEntity<String> updateUserAvatar(@PathVariable String userId,
                                                   @RequestParam("avatar") MultipartFile avatarFile){
        try {
            return ResponseEntity.ok(avatarService.updateAvatar(userId, avatarFile));
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AvatarNotFoundException e) {
            log.error("Avatar not found for user {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidImageFormatException e) {
            log.error("Invalid image format for user {}", userId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
