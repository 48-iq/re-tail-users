package dev.ilya_anna.user_service.services;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.ilya_anna.user_service.entities.User;
import dev.ilya_anna.user_service.exceptions.JwtAuthenticationException;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;
import dev.ilya_anna.user_service.repositories.UserRepository;
import io.minio.*;
import io.minio.errors.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class DaoAvatarService implements AvatarService{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private JwtService jwtService;
    private static final String BUCKET_NAME = "avatars";

    public Resource getAvatar(String userId, String authHeader)
            throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException,
            InternalException {

        if (authHeader == null) {
            throw new JwtAuthenticationException("Missing or invalid Authorization header");
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("user with id " + userId + " not found"));
        String avatarId = user.getAvatarImageId();
        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(avatarId)
                        .build());
        return new InputStreamResource(stream);
    }

    public String updateAvatar(String userId, MultipartFile avatarFile, String authHeader) throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException,
            InternalException {

        validateAuthorization(userId, authHeader);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User with id " + userId + " not found"));

        String avatarId = UUID.randomUUID().toString();

        if (user.getAvatarImageId() != null) {
            removeOldAvatar(user.getAvatarImageId());
        }

        try (InputStream inputStream = avatarFile.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(avatarId)
                            .stream(inputStream, avatarFile.getSize(), -1)
                            .contentType(avatarFile.getContentType())
                            .build());
        }

        user.setAvatarImageId(avatarId);
        userRepository.save(user);

        return avatarId;
    }


    private void removeOldAvatar(String avatarId)
            throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException,
            InternalException {

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(avatarId)
                        .build());
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
