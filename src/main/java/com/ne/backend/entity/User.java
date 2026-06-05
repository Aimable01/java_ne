package com.ne.backend.entity;

import com.ne.backend.enums.Role;
import com.ne.backend.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Base User entity containing common user information
 * Extended by Customer for utility billing system
 * Uses TABLE_PER_CLASS inheritance strategy - each subclass has its own table
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // Unique user code for identification
    @Column(unique = true)
    private String code;

    // User's first name
    private String firstName;

    // User's last name
    private String lastName;

    // User's email (used as username)
    @Column(unique = true)
    private String email;

    // User's password (hashed)
    private String password;

    // User's phone number
    private String mobile;

    // User's account status
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    // User's roles for authorization
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    // Timestamp when user was created
    private LocalDateTime createdAt;

    // Set creation timestamp before persisting
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
