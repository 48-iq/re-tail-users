package dev.ilya_anna.user_service.services;

import dev.ilya_anna.user_service.exceptions.AvatarNotFoundException;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;
import io.minio.errors.*;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface AvatarService {
    Resource getAvatar(String userId) throws UserNotFoundException, AvatarNotFoundException, ServerException, InsufficientDataException,
            ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException,
            InternalException;

    String updateAvatar(String userId, MultipartFile avatarFile) throws UserNotFoundException, AvatarNotFoundException, IOException, ServerException, InsufficientDataException,
            ErrorResponseException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException,
            XmlParserException, InternalException;
}
