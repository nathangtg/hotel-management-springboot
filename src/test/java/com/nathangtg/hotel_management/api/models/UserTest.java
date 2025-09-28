package com.nathangtg.hotel_management.api.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testGetFullName() {
        // Arrange
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");

        // Act
        String fullName = user.getFullName();

        // Assert
        assertEquals("John Doe", fullName);
    }

    @Test
    void testUserConstructor() {
        // Test no-args constructor
        User user = new User();
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getEmail());
        assertNull(user.getRole());

        // Test all-args constructor - need to construct with all fields in correct order
        User userWithArgs = new User(1L, "testuser", "encodedpassword", "Test", "User", 
                                   "test@example.com", "1234567890", "123 Test St", "USER",
                                   null, null); // managements and bookings (List fields) are initialized as null in this call

        assertEquals(1L, userWithArgs.getId());
        assertEquals("testuser", userWithArgs.getUsername());
        assertEquals("encodedpassword", userWithArgs.getPassword());
        assertEquals("Test", userWithArgs.getFirstName());
        assertEquals("User", userWithArgs.getLastName());
        assertEquals("test@example.com", userWithArgs.getEmail());
        assertEquals("1234567890", userWithArgs.getPhone());
        assertEquals("123 Test St", userWithArgs.getAddress());
        assertEquals("USER", userWithArgs.getRole());
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        User user = new User();
        
        // Act
        user.setId(1L);
        user.setUsername("newuser");
        user.setPassword("newpassword");
        user.setFirstName("New");
        user.setLastName("User");
        user.setEmail("new@example.com");
        user.setPhone("0987654321");
        user.setAddress("456 New St");
        user.setRole("ADMIN");

        // Assert
        assertEquals(1L, user.getId());
        assertEquals("newuser", user.getUsername());
        assertEquals("newpassword", user.getPassword());
        assertEquals("New", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("0987654321", user.getPhone());
        assertEquals("456 New St", user.getAddress());
        assertEquals("ADMIN", user.getRole());
    }
}