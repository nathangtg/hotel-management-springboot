package com.nathangtg.hotel_management.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathangtg.hotel_management.api.models.User;
import com.nathangtg.hotel_management.services.UserService;
import com.nathangtg.hotel_management.test.config.ControllerTestConfiguration;

@SpringBootTest(classes = ControllerTestConfiguration.class, 
               properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@ActiveProfiles("controller-test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private UserService userService;
    
    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setRole("USER");
        
        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setPassword("admin123");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole("ADMIN");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetAllUsers_AsUser() throws Exception {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userService.findAll()).thenReturn(users);
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].username").value("testuser"));
            
        verify(userService, times(1)).findByUsername("testuser");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllUsers_AsAdmin() throws Exception {
        // Arrange
        List<User> users = Arrays.asList(testUser, adminUser);
        when(userService.findAll()).thenReturn(users);
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllUsers_ByRole_AsAdmin() throws Exception {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userService.findAllByRole("USER")).thenReturn(users);
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(get("/api/users").param("role", "USER"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllUsers_ByEmail_AsAdmin() throws Exception {
        // Arrange
        when(userService.findByEmail("test@example.com")).thenReturn(testUser);
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(get("/api/users").param("email", "test@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].email").value("test@example.com"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetUserById_AsOwner() throws Exception {
        // Arrange
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetUserById_AsAdmin() throws Exception {
        // Arrange
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "otheruser", roles = {"USER"})
    void testGetUserById_AsNonOwner_ShouldReturnForbidden() throws Exception {
        // Arrange
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setUsername("otheruser");
        otherUser.setPassword("password");
        otherUser.setFirstName("Other");
        otherUser.setLastName("User");
        otherUser.setEmail("other@example.com");
        otherUser.setRole("USER");
        when(userService.findByUsername("otheruser")).thenReturn(otherUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    void testGetUserById_Unauthenticated_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateUser() throws Exception {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("newpassword123");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setEmail("new@example.com");
        newUser.setRole("USER");
        
        when(userService.createUser(any(User.class))).thenReturn(newUser);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateUser_AsOwner() throws Exception {
        // Arrange
        User updatedUser = new User();
        updatedUser.setUsername("updateduser");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setRole("ADMIN");
        
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("updateduser"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUser_AsAdmin() throws Exception {
        // Arrange
        User updatedUser = new User();
        updatedUser.setUsername("updateduser");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setRole("ADMIN");
        
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(userService.findByUsername("admin")).thenReturn(adminUser);
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("updateduser"));
    }

    @Test
    @WithMockUser(username = "otheruser", roles = {"USER"})
    void testUpdateUser_AsNonOwner_ShouldReturnForbidden() throws Exception {
        // Arrange
        User updatedUser = new User();
        updatedUser.setUsername("updateduser");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setRole("ADMIN");
        
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setUsername("otheruser");
        otherUser.setPassword("password");
        otherUser.setFirstName("Other");
        otherUser.setLastName("User");
        otherUser.setEmail("other@example.com");
        otherUser.setRole("USER");
        when(userService.findByUsername("otheruser")).thenReturn(otherUser);

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeleteUser_AsOwner() throws Exception {
        // Arrange
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(delete("/api/users/1"))
            .andExpect(status().isOk());
            
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteUser_AsAdmin() throws Exception {
        // Arrange
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(delete("/api/users/1"))
            .andExpect(status().isOk());
            
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @WithMockUser(username = "otheruser", roles = {"USER"})
    void testDeleteUser_AsNonOwner_ShouldReturnForbidden() throws Exception {
        // Arrange
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setUsername("otheruser");
        otherUser.setPassword("password");
        otherUser.setFirstName("Other");
        otherUser.setLastName("User");
        otherUser.setEmail("other@example.com");
        otherUser.setRole("USER");
        when(userService.findByUsername("otheruser")).thenReturn(otherUser);

        // Act & Assert
        mockMvc.perform(delete("/api/users/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSearchUsers_AsAdmin_ByFirstNameAndLastName() throws Exception {
        // Arrange
        when(userService.findByFirstNameAndLastName("Test", "User")).thenReturn(testUser);
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/search")
                .param("firstName", "Test")
                .param("lastName", "User"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].firstName").value("Test"))
            .andExpect(jsonPath("$[0].lastName").value("User"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSearchUsers_AsAdmin_ByRole() throws Exception {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userService.findAllByRole("USER")).thenReturn(users);
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/search")
                .param("role", "USER"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].role").value("USER"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testSearchUsers_AsUser_ShouldReturnEmpty() throws Exception {
        // Arrange
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/search")
                .param("role", "ADMIN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }
}