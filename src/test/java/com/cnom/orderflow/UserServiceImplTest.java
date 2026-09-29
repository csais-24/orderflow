package com.cnom.orderflow;

import com.cnom.orderflow.exception.DuplicateEmailException;
import com.cnom.orderflow.user.dto.CreateUserRequest;
import com.cnom.orderflow.user.dto.UserResponse;
import com.cnom.orderflow.user.entity.User;
import com.cnom.orderflow.user.repository.UserRepository;
import com.cnom.orderflow.user.service.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;


public class UserServiceImplTest {

    private UserServiceImpl userService;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp(){
        userRepository = Mockito.mock();
        passwordEncoder = Mockito.mock();
        userService = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    public void shouldCreateUserSuccessfully(){
        // Arrange
        String cleanEmail = "test@gmail.com";
        String plainTextPassword = "pass123";
        String hashedPassword = "pass123-encoded";
        CreateUserRequest createUserRequest = new CreateUserRequest(
          cleanEmail, plainTextPassword
        );
        Mockito.when(passwordEncoder.encode(plainTextPassword))
                        .thenReturn(hashedPassword);
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(new User(cleanEmail, hashedPassword));
        // Act
        UserResponse userResponse = userService.createUser(createUserRequest);
        // Assert
        Assertions.assertEquals(cleanEmail, userResponse.email());
        Assertions.assertTrue(userResponse.active());
        Mockito.verify(passwordEncoder).encode(plainTextPassword);
        Mockito.verify(userRepository).save(Mockito.any(User.class));
    }

    @Test
    public void shouldNormalizeEmailBeforeCreatingUser(){
        String dirtyEmail = "   tesT@gmail.com    ";
        String cleanEmail = "test@gmail.com";
        String plainTextPassword = "pass123";
        String hashedPassword = "pass123-encoded";
        ArgumentCaptor<User> userArg = ArgumentCaptor.forClass(User.class);
        CreateUserRequest createUserRequest = new CreateUserRequest(
                dirtyEmail, plainTextPassword
        );
        Mockito.when(passwordEncoder.encode(plainTextPassword))
                .thenReturn(hashedPassword);
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(new User(cleanEmail, hashedPassword));
        // Act
        UserResponse userResponse = userService.createUser(createUserRequest);
        // Assert
        Assertions.assertEquals(cleanEmail, userResponse.email());
        Assertions.assertTrue(userResponse.active());
        Mockito.verify(passwordEncoder).encode(plainTextPassword);
        Mockito.verify(userRepository).save(userArg.capture());
        Assertions.assertEquals(cleanEmail, userArg.getValue().getEmail());
    }

    @Test
    public void shouldThrowExceptionWhenEmailAlreadyExists() {
        String email = "test@gmail.com";
        String password = "pass123";
        CreateUserRequest createUserRequest = new CreateUserRequest(
                email, password
        );
        Mockito.when(userRepository.existsByEmail(email))
                .thenReturn(true);

        Assertions.assertThrows(DuplicateEmailException.class, () -> userService.createUser(createUserRequest));
        Mockito.verify(userRepository).existsByEmail(email);
        Mockito.verifyNoInteractions(passwordEncoder);
        Mockito.verify(userRepository, Mockito.never())
                .save(Mockito.any(User.class));
    }

}
