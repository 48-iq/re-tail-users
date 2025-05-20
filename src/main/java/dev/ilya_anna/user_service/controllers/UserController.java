package dev.ilya_anna.user_service.controllers;

import dev.ilya_anna.user_service.dto.UpdateUserDto;
import dev.ilya_anna.user_service.dto.UserDto;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;
import dev.ilya_anna.user_service.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User controller",
        description = "Controller for managing users"
)
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(
            summary = "Gets full user info",
            description = "Gets full user info by id"
    )
    @GetMapping("/all-info/{userId}")
    public ResponseEntity<UserDto> getUserAllInfo(
            @PathVariable String userId) {
        try {
            return ResponseEntity.ok(userService.getUserAllInfo(userId));
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(
            summary = "Gets user",
            description = "Gets user by id, " +
                    "returns visible user info"
    )
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable String userId){
        try {
            return ResponseEntity.ok(userService.getUser(userId));
        } catch (UserNotFoundException e) {
            log.error("User {} not found", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(
            summary = "Updates user",
            description = "Updates user by id and info for update, " +
                    "returns full user info"
    )
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable String userId,
                                              @Valid @RequestBody UpdateUserDto updateUserDto) {
        try {
            return ResponseEntity.ok(userService.updateUser(userId, updateUserDto));
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
