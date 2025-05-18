package dev.ilya_anna.user_service.services;

import dev.ilya_anna.user_service.dto.UpdateUserDto;
import dev.ilya_anna.user_service.dto.UserDto;
import dev.ilya_anna.user_service.events.UserCreatedEvent;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;

public interface UserService {
    UserDto getUserAllInfo(String userId) throws UserNotFoundException;

    UserDto getUser(String userId) throws UserNotFoundException;

    UserDto updateUser(String userId, UpdateUserDto updateUserDto) throws UserNotFoundException;

    void createUser(UserCreatedEvent userCreatedEvent);
}
