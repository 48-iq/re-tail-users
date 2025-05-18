package dev.ilya_anna.user_service.services;

import dev.ilya_anna.user_service.dto.UserDto;
import dev.ilya_anna.user_service.exceptions.AvatarNotFoundException;
import dev.ilya_anna.user_service.exceptions.InvalidImageFormatException;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface AvatarService {
    Resource getAvatar(String userId) throws AvatarNotFoundException;

    UserDto updateAvatar(String userId, MultipartFile avatarFile) throws UserNotFoundException, InvalidImageFormatException;
}
