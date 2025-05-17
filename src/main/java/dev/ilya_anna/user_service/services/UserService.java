package dev.ilya_anna.user_service.services;

import dev.ilya_anna.user_service.dto.UpdateUserDto;
import dev.ilya_anna.user_service.dto.UserAllInfoDto;
import dev.ilya_anna.user_service.dto.UserDto;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;

public interface UserService {
    UserAllInfoDto getUserAllInfo(String userId) throws UserNotFoundException;

    UserDto getUser(String userId) throws UserNotFoundException;

    UserAllInfoDto updateUser(String userId, UpdateUserDto updateUserDto) throws UserNotFoundException;
}
