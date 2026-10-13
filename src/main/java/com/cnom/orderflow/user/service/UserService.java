package com.cnom.orderflow.user.service;

import com.cnom.orderflow.user.dto.CreateUserRequest;
import com.cnom.orderflow.user.dto.UserResponse;

import java.util.Optional;

public interface UserService {
    UserResponse createUser(CreateUserRequest createUserRequest);
    UserResponse getUserByEmail(String userEmail);
}
