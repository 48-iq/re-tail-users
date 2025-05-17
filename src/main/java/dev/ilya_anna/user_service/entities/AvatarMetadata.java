package dev.ilya_anna.user_service.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "avatars_metadata")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AvatarMetadata {
    @Id
    private String Id;
    private String avatarPath;
}
