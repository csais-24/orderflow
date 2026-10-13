package com.cnom.orderflow;

import com.cnom.orderflow.user.controller.UserController;
import com.cnom.orderflow.user.dto.CreateUserRequest;
import com.cnom.orderflow.user.dto.UserResponse;
import com.cnom.orderflow.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
public class UserControllerTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldCreateUserSuccessfully() throws Exception {
        String jsonBody = """
                {
                    "email": "email@gmail.com",
                    "plainTextPassword": "pass1234"
                }
                """;
        UserResponse userResponse = new UserResponse(
                UUID.fromString("12345678-1234-1234-1234-1234567890ab"),
                "email@gmail.com",
                Instant.parse("2026-09-28T10:15:30.00Z"),
                Instant.parse("2026-09-28T10:15:30.00Z"),
                true
        );
        Mockito.when(userService.createUser(Mockito.any(CreateUserRequest.class)))
                        .thenReturn(userResponse);

        mockMvc.perform(
                post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("email@gmail.com"));
    }

    @Test
    public void shouldRejectInvalidEmail() throws Exception {
        String jsonBody = """
                {
                    "email": "not-an-email",
                    "plainTextPassword": "pass1234"
                }
                """;


        mockMvc.perform(
                        post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                )
                .andExpect(status().isBadRequest());

        Mockito.verify(userService, Mockito.never())
                .createUser(Mockito.any(CreateUserRequest.class));

    }

}
