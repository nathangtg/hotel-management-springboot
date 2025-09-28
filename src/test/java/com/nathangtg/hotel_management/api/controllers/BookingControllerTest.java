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

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.nathangtg.hotel_management.api.models.Booking;
import com.nathangtg.hotel_management.api.models.Room;
import com.nathangtg.hotel_management.api.models.User;
import com.nathangtg.hotel_management.services.BookingService;
import com.nathangtg.hotel_management.services.UserService;
import com.nathangtg.hotel_management.test.config.ControllerTestConfiguration;

@SpringBootTest(classes = ControllerTestConfiguration.class, 
               properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@ActiveProfiles("controller-test")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private BookingService bookingService;
    
    @MockitoBean
    private UserService userService;
    
    private User testUser;
    private User adminUser;
    private Room testRoom;
    private Booking testBooking;

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
        
        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setRoomNumber("101");
        testRoom.setRoomType("Single");
        testRoom.setCapacity(1);
        testRoom.setPricePerNight(new BigDecimal("100.00"));
        testRoom.setIsAvailable(true);
        
        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setCheckInDate(LocalDate.now().plusDays(1));
        testBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        testBooking.setTotalPrice(new BigDecimal("200.00"));
        testBooking.setStatus("PENDING");
        testBooking.setUser(testUser);
        testBooking.setRoom(testRoom);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetAllBookings_AsUser() throws Exception {
        // Arrange
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingService.getBookingsByUserId(1L)).thenReturn(bookings);
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/bookings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L));
            
        verify(bookingService, times(1)).getBookingsByUserId(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllBookings_AsAdmin() throws Exception {
        // Arrange
        List<Booking> allBookings = Arrays.asList(testBooking);
        when(bookingService.getAllBookings()).thenReturn(allBookings);
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(get("/api/bookings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllBookings_ByUserId_AsAdmin() throws Exception {
        // Arrange
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingService.getBookingsByUserId(1L)).thenReturn(bookings);
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(get("/api/bookings").param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].userId").value(1L));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllBookings_ByRoomId_AsAdmin() throws Exception {
        // Arrange
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingService.getBookingsByRoomId(1L)).thenReturn(bookings);

        // Act & Assert
        mockMvc.perform(get("/api/bookings").param("roomId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllBookings_ByStatus_AsAdmin() throws Exception {
        // Arrange
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingService.getBookingsByStatus("PENDING")).thenReturn(bookings);

        // Act & Assert
        mockMvc.perform(get("/api/bookings").param("status", "PENDING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllBookings_ByDateRange_AsAdmin() throws Exception {
        // Arrange
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingService.getBookingsByDateRange(
            LocalDate.of(2023, 6, 1), 
            LocalDate.of(2023, 6, 10)
        )).thenReturn(bookings);

        // Act & Assert
        mockMvc.perform(get("/api/bookings")
                .param("startDate", "2023-06-01")
                .param("endDate", "2023-06-10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetBookingById_AsOwner() throws Exception {
        // Arrange
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/bookings/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetBookingById_AsAdmin() throws Exception {
        // Arrange
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(get("/api/bookings/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "otheruser", roles = {"USER"})
    void testGetBookingById_AsNonOwner_ShouldReturnNotFound() throws Exception {
        // Arrange
        Booking otherBooking = new Booking();
        otherBooking.setId(1L);
        otherBooking.setUserId(999L); // Different user
        otherBooking.setCheckInDate(LocalDate.now().plusDays(1));
        otherBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        otherBooking.setTotalPrice(new BigDecimal("200.00"));
        otherBooking.setStatus("PENDING");
        
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(otherBooking));
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
        mockMvc.perform(get("/api/bookings/1"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateBooking() throws Exception {
        // Arrange
        Booking newBooking = new Booking();
        newBooking.setCheckInDate(LocalDate.now().plusDays(1));
        newBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        newBooking.setTotalPrice(new BigDecimal("200.00"));
        newBooking.setUser(testUser);
        newBooking.setRoom(testRoom);
        
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(bookingService.createBooking(any(Booking.class))).thenReturn(newBooking);

        // Act & Assert
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBooking)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.checkInDate").value(newBooking.getCheckInDate().toString()));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateBooking_AsOwner() throws Exception {
        // Arrange
        Booking updatedBooking = new Booking();
        updatedBooking.setCheckInDate(LocalDate.now().plusDays(1));
        updatedBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        updatedBooking.setStatus("CONFIRMED");
        
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(bookingService.updateBooking(eq(1L), any(Booking.class))).thenReturn(updatedBooking);

        // Act & Assert
        mockMvc.perform(put("/api/bookings/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedBooking)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateBooking_AsAdmin() throws Exception {
        // Arrange
        Booking updatedBooking = new Booking();
        updatedBooking.setCheckInDate(LocalDate.now().plusDays(1));
        updatedBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        updatedBooking.setStatus("CONFIRMED");
        
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        when(userService.findByUsername("admin")).thenReturn(adminUser);
        when(bookingService.updateBooking(eq(1L), any(Booking.class))).thenReturn(updatedBooking);

        // Act & Assert
        mockMvc.perform(put("/api/bookings/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedBooking)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(username = "otheruser", roles = {"USER"})
    void testUpdateBooking_AsNonOwner_ShouldReturnForbidden() throws Exception {
        // Arrange
        Booking otherBooking = new Booking();
        otherBooking.setId(1L);
        otherBooking.setUserId(999L); // Different user
        otherBooking.setCheckInDate(LocalDate.now().plusDays(1));
        otherBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        otherBooking.setTotalPrice(new BigDecimal("200.00"));
        otherBooking.setStatus("PENDING");
        
        Booking updatedBooking = new Booking();
        updatedBooking.setStatus("CONFIRMED");
        
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(otherBooking));
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
        mockMvc.perform(put("/api/bookings/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedBooking)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCancelBooking_AsOwner() throws Exception {
        // Arrange
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(put("/api/bookings/1/cancel"))
            .andExpect(status().isOk());
            
        verify(bookingService, times(1)).cancelBooking(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCancelBooking_AsAdmin() throws Exception {
        // Arrange
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(put("/api/bookings/1/cancel"))
            .andExpect(status().isOk());
            
        verify(bookingService, times(1)).cancelBooking(1L);
    }

    @Test
    @WithMockUser(username = "otheruser", roles = {"USER"})
    void testCancelBooking_AsNonOwner_ShouldReturnForbidden() throws Exception {
        // Arrange
        Booking otherBooking = new Booking();
        otherBooking.setId(1L);
        otherBooking.setUserId(999L); // Different user
        otherBooking.setCheckInDate(LocalDate.now().plusDays(1));
        otherBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        otherBooking.setTotalPrice(new BigDecimal("200.00"));
        otherBooking.setStatus("PENDING");
        
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(otherBooking));
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
        mockMvc.perform(put("/api/bookings/1/cancel"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeleteBooking_AsOwner() throws Exception {
        // Arrange
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(delete("/api/bookings/1"))
            .andExpect(status().isOk());
            
        verify(bookingService, times(1)).deleteBooking(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteBooking_AsAdmin() throws Exception {
        // Arrange
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(delete("/api/bookings/1"))
            .andExpect(status().isOk());
            
        verify(bookingService, times(1)).deleteBooking(1L);
    }

    @Test
    @WithMockUser(username = "otheruser", roles = {"USER"})
    void testDeleteBooking_AsNonOwner_ShouldReturnForbidden() throws Exception {
        // Arrange
        Booking otherBooking = new Booking();
        otherBooking.setId(1L);
        otherBooking.setUserId(999L); // Different user
        otherBooking.setCheckInDate(LocalDate.now().plusDays(1));
        otherBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        otherBooking.setTotalPrice(new BigDecimal("200.00"));
        otherBooking.setStatus("PENDING");
        
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(otherBooking));
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
        mockMvc.perform(delete("/api/bookings/1"))
            .andExpect(status().isForbidden());
    }
}