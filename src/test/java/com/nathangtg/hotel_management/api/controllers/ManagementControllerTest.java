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
import com.nathangtg.hotel_management.api.models.Hotel;
import com.nathangtg.hotel_management.api.models.Management;
import com.nathangtg.hotel_management.api.models.User;
import com.nathangtg.hotel_management.services.ManagementService;
import com.nathangtg.hotel_management.services.UserService;
import com.nathangtg.hotel_management.test.config.ControllerTestConfiguration;

@SpringBootTest(classes = ControllerTestConfiguration.class, 
               properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@ActiveProfiles("controller-test")
class ManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private ManagementService managementService;
    
    @MockitoBean
    private UserService userService;
    
    private User testUser;
    private User adminUser;
    private Hotel testHotel;
    private User managedUser;
    private Management testManagement;

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
        
        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");
        testHotel.setAddress("123 Test Street");
        testHotel.setPhone("555-1234");
        testHotel.setEmail("test@hotel.com");
        
        managedUser = new User();
        managedUser.setId(3L);
        managedUser.setUsername("manageduser");
        managedUser.setPassword("password123");
        managedUser.setFirstName("Managed");
        managedUser.setLastName("User");
        managedUser.setEmail("managed@example.com");
        managedUser.setRole("STAFF");
        
        testManagement = new Management();
        testManagement.setId(1L);
        testManagement.setHotel(testHotel);
        testManagement.setUser(managedUser);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllManagements_AsAdmin() throws Exception {
        // Arrange
        List<Management> managements = Arrays.asList(testManagement);
        when(managementService.getAllManagements()).thenReturn(managements);
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(get("/api/managements"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].hotel.id").value(1L))
            .andExpect(jsonPath("$[0].user.id").value(3L));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetAllManagements_AsUser_ShouldReturnEmpty() throws Exception {
        // Arrange
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/managements"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetManagementById_AsAdmin() throws Exception {
        // Arrange
        when(managementService.getManagementById(1L)).thenReturn(Optional.of(testManagement));
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(get("/api/managements/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hotel.id").value(1L))
            .andExpect(jsonPath("$.user.id").value(3L));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetManagementById_AsUser_ShouldReturnForbidden() throws Exception {
        // Arrange
        when(managementService.getManagementById(1L)).thenReturn(Optional.of(testManagement));
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/managements/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetManagementById_NotFound() throws Exception {
        // Arrange
        when(managementService.getManagementById(999L)).thenReturn(Optional.empty());
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(get("/api/managements/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateManagement_AsAdmin() throws Exception {
        // Arrange
        Management newManagement = new Management();
        newManagement.setHotel(testHotel);
        newManagement.setUser(managedUser);
        
        when(userService.findByUsername("admin")).thenReturn(adminUser);
        when(managementService.createManagement(any(Management.class))).thenReturn(newManagement);

        // Act & Assert
        mockMvc.perform(post("/api/managements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newManagement)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hotel.id").value(1L))
            .andExpect(jsonPath("$.user.id").value(3L));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateManagement_AsUser_ShouldReturnForbidden() throws Exception {
        // Arrange
        Management newManagement = new Management();
        newManagement.setHotel(testHotel);
        newManagement.setUser(managedUser);
        
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/managements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newManagement)))
            .andExpect(status().isForbidden()) // Access denied should return 403
            .andExpect(jsonPath("$.error").value("Access denied: Only admin can create management entries"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateManagement_AsAdmin() throws Exception {
        // Arrange
        Management updatedManagement = new Management();
        updatedManagement.setHotel(testHotel);
        updatedManagement.setUser(managedUser);
        
        when(managementService.getManagementById(1L)).thenReturn(Optional.of(testManagement));
        when(userService.findByUsername("admin")).thenReturn(adminUser);
        when(managementService.updateManagement(eq(1L), any(Management.class))).thenReturn(updatedManagement);

        // Act & Assert
        mockMvc.perform(put("/api/managements/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedManagement)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hotel.id").value(1L));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateManagement_AsUser_ShouldReturnForbidden() throws Exception {
        // Arrange
        Management updatedManagement = new Management();
        updatedManagement.setHotel(testHotel);
        updatedManagement.setUser(managedUser);
        
        when(managementService.getManagementById(1L)).thenReturn(Optional.of(testManagement));
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(put("/api/managements/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedManagement)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteManagement_AsAdmin() throws Exception {
        // Arrange
        when(managementService.getManagementById(1L)).thenReturn(Optional.of(testManagement));
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(delete("/api/managements/1"))
            .andExpect(status().isOk());
            
        verify(managementService, times(1)).deleteManagement(1L);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeleteManagement_AsUser_ShouldReturnForbidden() throws Exception {
        // Arrange
        when(managementService.getManagementById(1L)).thenReturn(Optional.of(testManagement));
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(delete("/api/managements/1"))
            .andExpect(status().isForbidden());
    }
}