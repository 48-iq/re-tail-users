package dev.ilya_anna.user_service.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users_settings")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserSettings {
    @Id
    private String id;
    private boolean nameVisibility;
    private boolean surnameVisibility;
    private boolean emailVisibility;
    private boolean phoneVisibility;
    private boolean addressVisibility;
    private boolean avatarVisibility;
    @OneToOne
    @JoinColumn(name="user_id", referencedColumnName = "id")
    private User user;
}
