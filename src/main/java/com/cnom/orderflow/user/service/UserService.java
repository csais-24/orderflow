package com.cnom.orderflow.user.service;

import com.cnom.orderflow.user.dto.CreateUserRequest;
import com.cnom.orderflow.user.dto.UserResponse;

public interface UserService {
    UserResponse createUser(CreateUserRequest createUserRequest);

}
