package dev.ilya_anna.user_service.controllers;

import dev.ilya_anna.user_service.dto.UserSettingsDto;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;
import dev.ilya_anna.user_service.services.UserSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/user-settings")
@Tag(name = "User settings controller",
        description = "Controller for managing user settings"
)
public class UserSettingsController {

    @Autowired
    private UserSettingsService userSettingsService;

    @Operation(
            summary = "Gets user settings",
            description = "Gets user settings by user id"
    )
    @GetMapping("/{userId}")
    public ResponseEntity<UserSettingsDto> getUserSettings(@PathVariable String userId) {
        try {
            return ResponseEntity.ok(userSettingsService.getUserSettings(userId));
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(
            summary = "Updates user settings",
            description = "Updates user settings by user id and settings for update," +
                    "returns user settings"
    )
    @PutMapping("/{userId}")
    public ResponseEntity<UserSettingsDto> updateUserSettings(@PathVariable String userId,
                                                              @RequestBody UserSettingsDto userSettingsDto) {
        try {
            return ResponseEntity.ok(userSettingsService.updateUserSettings(userId, userSettingsDto));
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
