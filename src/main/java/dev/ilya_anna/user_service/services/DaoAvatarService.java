package dev.ilya_anna.user_service.services;

import dev.ilya_anna.user_service.entities.AvatarMetadata;
import dev.ilya_anna.user_service.entities.User;
import dev.ilya_anna.user_service.exceptions.AvatarNotFoundException;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;
import dev.ilya_anna.user_service.repositories.AvatarMetadataRepository;
import dev.ilya_anna.user_service.repositories.UserRepository;
import io.minio.*;
import io.minio.errors.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
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
    private AvatarMetadataRepository avatarMetadataRepository;

    @Autowired
    private MinioClient minioClient;
    private static final String BUCKET_NAME = "avatars";

    public Resource getAvatar(String userId)
            throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException,
            InternalException {

        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("user with id " + userId + " not found"));

        if (user.getAvatarImageId() == null) {
            throw new AvatarNotFoundException("User has no avatar");
        }

        AvatarMetadata avatarMetadata = avatarMetadataRepository.findById(user.getAvatarImageId())
                .orElseThrow(() -> new AvatarNotFoundException("Avatar metadata not found"));

        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(avatarMetadata.getAvatarPath())
                        .build());
        return new InputStreamResource(stream);
    }

    public String updateAvatar(String userId, MultipartFile avatarFile) throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException,
            InternalException {

        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User with id " + userId + " not found"));

        String avatarId = UUID.randomUUID().toString();
        String avatarPath = "users/" + userId + "/avatar/" + avatarId;

        if (user.getAvatarImageId() != null) {
            removeOldAvatar(user.getAvatarImageId());
        }

        try (InputStream inputStream = avatarFile.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(avatarPath)
                            .stream(inputStream, avatarFile.getSize(), -1)
                            .contentType(avatarFile.getContentType())
                            .build());
        }

        AvatarMetadata avatarMetadata = new AvatarMetadata();
        avatarMetadata.setId(avatarId);
        avatarMetadata.setAvatarPath(avatarPath);
        avatarMetadataRepository.save(avatarMetadata);

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

        AvatarMetadata avatarMetadata = avatarMetadataRepository.findById(avatarId)
                .orElseThrow(() -> new AvatarNotFoundException("Avatar metadata not found"));

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(avatarMetadata.getAvatarPath())
                        .build());
    }
}
