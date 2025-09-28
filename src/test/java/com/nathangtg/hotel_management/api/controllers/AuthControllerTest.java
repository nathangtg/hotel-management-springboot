package com.nathangtg.hotel_management.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathangtg.hotel_management.api.controllers.AuthController.LoginRequest;
import com.nathangtg.hotel_management.api.controllers.AuthController.RegisterRequest;
import com.nathangtg.hotel_management.api.models.User;
import com.nathangtg.hotel_management.security.JwtUtil;
import com.nathangtg.hotel_management.services.UserService;
import com.nathangtg.hotel_management.test.config.ControllerTestConfiguration;

@SpringBootTest(classes = ControllerTestConfiguration.class, 
               properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@ActiveProfiles("controller-test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private AuthenticationManager authenticationManager;
    
    @MockitoBean
    private UserService userService;
    
    @MockitoBean
    private JwtUtil jwtUtil;
    
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setRole("USER");
    }

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(jwtUtil.generateToken("testuser", 1L, "USER")).thenReturn("mocked-jwt-token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
            .andExpect(jsonPath("$.message").value("Login successful"))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.userId").value("1"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void testRegister_Success() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        registerRequest.setPassword("newpassword123");
        registerRequest.setRole("USER");
        
        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setRole("USER");
        newUser.setPassword("encodedPassword123");
        
        when(userService.findByUsername("newuser")).thenReturn(null);
        when(userService.findByEmail("new@example.com")).thenReturn(null);
        when(passwordEncoder.encode("newpassword123")).thenReturn("encodedPassword123");
        when(userService.createUser(any(User.class))).thenReturn(newUser);
        when(jwtUtil.generateToken("newuser", 2L, "USER")).thenReturn("mocked-jwt-token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
            .andExpect(jsonPath("$.message").value("Registration successful"))
            .andExpect(jsonPath("$.username").value("newuser"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.userId").value("2"));
    }

    @Test
    void testRegister_UsernameAlreadyExists() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("existinguser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        registerRequest.setPassword("newpassword123");
        
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("existinguser");
        existingUser.setPassword("password123");
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setEmail("existing@example.com");
        existingUser.setRole("USER");
        
        when(userService.findByUsername("existinguser")).thenReturn(existingUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    @Test
    void testRegister_EmailAlreadyExists() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("existing@example.com");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        registerRequest.setPassword("newpassword123");
        
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("existinguser");
        existingUser.setPassword("password123");
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setEmail("existing@example.com");
        existingUser.setRole("USER");
        
        when(userService.findByUsername("newuser")).thenReturn(null);
        when(userService.findByEmail("existing@example.com")).thenReturn(existingUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Email already exists"));
    }

    @Test
    void testRegister_ExceptionHandling() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        registerRequest.setPassword("newpassword123");
        
        when(userService.findByUsername("newuser")).thenReturn(null);
        when(userService.findByEmail("new@example.com")).thenReturn(null);
        doThrow(new RuntimeException("User creation failed")).when(userService).createUser(any(User.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Registration failed: User creation failed"));
    }
}