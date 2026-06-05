package com.ne.backend.service;

import com.ne.backend.entity.User;
import com.ne.backend.exception.ResourceNotFoundException;
import com.ne.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<User> getAll(Pageable pageable) {
        log.info("Fetching all users with pagination");
        return userRepository.findAll(pageable);
    }

    public Page<User> searchUsers(String firstName, String lastName, String email, Pageable pageable) {
        log.info("Searching users with filters - firstName: {}, lastName: {}, email: {}", firstName, lastName, email);
        return userRepository.searchUsers(firstName, lastName, email, pageable);
    }

    public User getById(Long id) {
        log.info("Fetching user by ID: {}", id);

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    public User create(User user) {
        log.info("Creating new user with email: {}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        User saved = userRepository.save(user);
        log.info("User created successfully with ID: {}", saved.getId());
        return saved;
    }

    public User update(Long id, User user) {
        log.info("Updating user with ID: {}", id);

        User existingUser = getById(id);

        if (user.getFirstName() != null) {
            existingUser.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null) {
            existingUser.setLastName(user.getLastName());
        }
        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            existingUser.setEmail(user.getEmail());
        }
        if (user.getMobile() != null) {
            existingUser.setMobile(user.getMobile());
        }
        if (user.getStatus() != null) {
            existingUser.setStatus(user.getStatus());
        }
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            existingUser.setRoles(user.getRoles());
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        User updated = userRepository.save(existingUser);
        log.info("User updated successfully with ID: {}", id);
        return updated;
    }

    public void delete(Long id) {
        log.info("Deleting user with ID: {}", id);

        User user = getById(id);

        userRepository.delete(user);

        log.info("User deleted successfully with ID: {}", id);
    }
}