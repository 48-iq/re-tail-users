package dev.ilya_anna.user_service.services;

import dev.ilya_anna.user_service.dto.UserDto;
import dev.ilya_anna.user_service.entities.AvatarMetadata;
import dev.ilya_anna.user_service.entities.User;
import dev.ilya_anna.user_service.exceptions.AvatarNotFoundException;
import dev.ilya_anna.user_service.exceptions.InvalidImageFormatException;
import dev.ilya_anna.user_service.exceptions.UserNotFoundException;
import dev.ilya_anna.user_service.repositories.AvatarMetadataRepository;
import dev.ilya_anna.user_service.repositories.UserRepository;
import io.minio.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class DaoAvatarService implements AvatarService{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AvatarMetadataRepository avatarMetadataRepository;

    @Autowired
    private MinioClient minioClient;

    @Autowired UuidService uuidService;
    private static final String BUCKET_NAME = "avatars";
    private static final String[] ALLOWED_EXTENSIONS = {"png", "jpeg", "jpg"};

    public Resource getAvatar(String avatarId){

        AvatarMetadata avatarMetadata = avatarMetadataRepository.findById(avatarId).orElseThrow(
                () -> new AvatarNotFoundException("avatar with id " + avatarId + " not found"));

        try{
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(avatarMetadata.getAvatarPath())
                            .build());
            return new InputStreamResource(stream);
        }
        catch (Exception e){
            throw new RuntimeException("Failed to get avatar with id " + avatarId);
        }
    }

    public UserDto updateAvatar(String userId, MultipartFile avatarFile){
        String originalFilename = avatarFile.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);

        if (!isValidExtension(fileExtension)) {
            throw new InvalidImageFormatException("Only PNG and JPEG/JPG images are allowed");
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User with id " + userId + " not found"));

        String avatarId = uuidService.generate();
        String avatarPath = String.format("users/%s/avatar/%s.%s", userId, avatarId, fileExtension);

        if (user.getAvatarImageId() != null) {
            removeOldAvatar(user.getAvatarImageId());
        }

        try {
            InputStream inputStream = avatarFile.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(avatarPath)
                            .stream(inputStream, avatarFile.getSize(), -1)
                            .contentType(avatarFile.getContentType())
                            .build());
        }
        catch (Exception e){
            throw new RuntimeException("Failed to upload avatar for user with id " + userId);
        }

        AvatarMetadata avatarMetadata = new AvatarMetadata();
        avatarMetadata.setId(avatarId);
        avatarMetadata.setAvatarPath(avatarPath);
        avatarMetadataRepository.save(avatarMetadata);

        user.setAvatarImageId(avatarId);
        userRepository.save(user);

        return UserDto.builder()
                .name(user.getName())
                .surname(user.getSurname())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .registeredAt(user.getRegisteredAt())
                .announcementsCount(userService.getAnnouncementsCount(userId))
                .about(user.getAbout())
                .avatarImageId(user.getAvatarImageId())
                .build();
    }


    private void removeOldAvatar(String avatarId){

        AvatarMetadata avatarMetadata = avatarMetadataRepository.findById(avatarId)
                .orElseThrow(() -> new AvatarNotFoundException("Avatar metadata not found"));

        try{
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(avatarMetadata.getAvatarPath())
                            .build());
        }
        catch (Exception e){
            throw new RuntimeException("Failed to delete avatar with id " + avatarId);
        }

    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isValidExtension(String extension) {
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}
