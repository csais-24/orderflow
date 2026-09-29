package com.cnom.orderflow.user.service;

import com.cnom.orderflow.exception.DuplicateEmailException;
import com.cnom.orderflow.user.dto.CreateUserRequest;
import com.cnom.orderflow.user.dto.UserResponse;
import com.cnom.orderflow.user.entity.User;
import com.cnom.orderflow.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        String normalizedUserEmail = createUserRequest.email().strip().toLowerCase();
        if (userRepository.existsByEmail(normalizedUserEmail)) {
            throw new DuplicateEmailException("Email already registered");
        }

        String hashedPassword = passwordEncoder.encode(createUserRequest.plainTextPassword());
        User savedUser = userRepository.save(new User(normalizedUserEmail, hashedPassword));
        return new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt(),
                savedUser.isActive()
        );
    }
}
