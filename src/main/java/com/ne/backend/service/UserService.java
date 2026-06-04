package com.ne.backend.service;

import com.ne.backend.entity.User;
import com.ne.backend.exception.ResourceNotFoundException;
import com.ne.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAll() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }

    public User getById(Long id) {
        log.info("Fetching user by ID: {}", id);

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    public void delete(Long id) {
        log.info("Deleting user with ID: {}", id);

        User user = getById(id);

        userRepository.delete(user);

        log.info("User deleted successfully with ID: {}", id);
    }
}