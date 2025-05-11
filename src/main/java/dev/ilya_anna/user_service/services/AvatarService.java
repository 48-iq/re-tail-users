package dev.ilya_anna.user_service.services;

import dev.ilya_anna.user_service.exceptions.UserNotFoundException;
import io.minio.errors.*;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface AvatarService {
    Resource getAvatar(String userId, String authHeader) throws UserNotFoundException, ServerException, InsufficientDataException,
            ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException,
            InternalException;

    String updateAvatar(String userId, MultipartFile avatarFile, String authHeader) throws UserNotFoundException, IOException, ServerException, InsufficientDataException,
            ErrorResponseException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException,
            XmlParserException, InternalException;
}
