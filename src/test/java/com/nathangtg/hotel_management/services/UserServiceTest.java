package com.nathangtg.hotel_management.services;

import com.nathangtg.hotel_management.api.models.User;
import com.nathangtg.hotel_management.api.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private PasswordEncoder passwordEncoder;
    private User testUser;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService.passwordEncoder = passwordEncoder;
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setRole("USER");
    }

    @Test
    void testCreateUser() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setRole("USER");

        User expectedSavedUser = new User();
        expectedSavedUser.setId(1L);
        expectedSavedUser.setUsername("testuser");
        expectedSavedUser.setPassword(passwordEncoder.encode("password123"));
        expectedSavedUser.setFirstName("Test");
        expectedSavedUser.setLastName("User");
        expectedSavedUser.setEmail("test@example.com");
        expectedSavedUser.setRole("USER");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(expectedSavedUser);

        // Act
        User savedUser = userService.createUser(user);

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
        assertEquals("USER", savedUser.getRole());
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_UsernameAlreadyExists_ThrowsException() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setUsername("existinguser");
        existingUser.setPassword("password123");
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setEmail("existing@example.com");
        existingUser.setRole("USER");

        User newUser = new User();
        newUser.setUsername("existinguser"); // Same username
        newUser.setPassword("password456");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setEmail("new@example.com");
        newUser.setRole("USER");

        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(newUser);
        });
        assertEquals("Username already exists: existinguser", exception.getMessage());
        verify(userRepository).findByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_EmailAlreadyExists_ThrowsException() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setUsername("existinguser");
        existingUser.setPassword("password123");
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setEmail("existing@example.com");
        existingUser.setRole("USER");

        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password456");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setEmail("existing@example.com"); // Same email
        newUser.setRole("USER");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(newUser);
        });
        assertEquals("Email already exists: existing@example.com", exception.getMessage());
        verify(userRepository).findByUsername("newuser");
        verify(userRepository).findByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testFindAllUsers() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setPassword("password123");
        user1.setFirstName("First");
        user1.setLastName("User");
        user1.setEmail("user1@example.com");
        user1.setRole("USER");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setPassword("password456");
        user2.setFirstName("Second");
        user2.setLastName("User");
        user2.setEmail("user2@example.com");
        user2.setRole("USER");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // Act
        List<User> users = userService.findAll();

        // Assert
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("user1")));
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("user2")));
        verify(userRepository).findAll();
    }

    @Test
    void testFindUserById() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> foundUser = userService.findById(1L);

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void testFindUserById_NotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<User> foundUser = userService.findById(999L);

        // Assert
        assertTrue(foundUser.isEmpty());
        verify(userRepository).findById(999L);
    }

    @Test
    void testFindByUsername() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // Act & Assert
        User foundUser = userService.findByUsername("testuser");
        assertEquals("testuser", foundUser.getUsername());
        assertEquals("test@example.com", foundUser.getEmail());
        
        // Test exception when user not found
        assertThrows(RuntimeException.class, () -> {
            userService.findByUsername("nonexistentuser");
        });
        
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByUsername("nonexistentuser");
    }

    @Test
    void testFindByEmail() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        User foundUser = userService.findByEmail("test@example.com");
        assertEquals("test@example.com", foundUser.getEmail());
        assertEquals("testuser", foundUser.getUsername());
        
        // Test exception when user not found
        assertThrows(RuntimeException.class, () -> {
            userService.findByEmail("nonexistent@example.com");
        });
        
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void testUpdateUser() {
        // Arrange
        User updateUser = new User();
        updateUser.setUsername("updateduser");
        updateUser.setFirstName("Updated");
        updateUser.setLastName("User");
        updateUser.setEmail("updated@example.com");
        updateUser.setRole("ADMIN");
        updateUser.setPassword("newpassword123");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updateduser");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setRole("ADMIN");
        updatedUser.setPassword(passwordEncoder.encode("newpassword123"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("updateduser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("updated@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = userService.updateUser(1L, updateUser);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("updateduser", result.getUsername());
        assertEquals("Updated", result.getFirstName());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("ADMIN", result.getRole());
        // Password should be encoded
        assertTrue(passwordEncoder.matches("newpassword123", result.getPassword()));
        
        verify(userRepository).findById(1L);
        verify(userRepository).findByUsername("updateduser");
        verify(userRepository).findByEmail("updated@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUser_UsernameAlreadyExists_ThrowsException() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setUsername("existinguser");
        existingUser.setPassword("password123");
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setEmail("existing@example.com");
        existingUser.setRole("USER");

        User updateUser = new User();
        updateUser.setUsername("existinguser"); // Trying to use existing username
        updateUser.setFirstName("Updated");
        updateUser.setLastName("User");
        updateUser.setEmail("updated@example.com");
        updateUser.setRole("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(1L, updateUser);
        });
        assertEquals("Username already exists: existinguser", exception.getMessage());
        
        verify(userRepository).findById(1L);
        verify(userRepository).findByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUser() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void testDeleteUser_NotFound_ThrowsException() {
        // Arrange
        when(userRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(999L);
        });
        assertEquals("User not found with id: 999", exception.getMessage());
        
        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void testFindAllByRole() {
        // Arrange
        User admin1 = new User();
        admin1.setId(1L);
        admin1.setUsername("admin1");
        admin1.setPassword("password123");
        admin1.setFirstName("Admin");
        admin1.setLastName("One");
        admin1.setEmail("admin1@example.com");
        admin1.setRole("ADMIN");

        User admin2 = new User();
        admin2.setId(2L);
        admin2.setUsername("admin2");
        admin2.setPassword("password456");
        admin2.setFirstName("Admin");
        admin2.setLastName("Two");
        admin2.setEmail("admin2@example.com");
        admin2.setRole("ADMIN");

        when(userRepository.findAllByRole("ADMIN")).thenReturn(Optional.of(Arrays.asList(admin1, admin2)));

        // Act
        List<User> admins = userService.findAllByRole("ADMIN");

        // Assert
        assertEquals(2, admins.size());
        admins.forEach(admin -> assertEquals("ADMIN", admin.getRole()));
        assertTrue(admins.stream().anyMatch(u -> u.getUsername().equals("admin1")));
        assertTrue(admins.stream().anyMatch(u -> u.getUsername().equals("admin2")));
        
        verify(userRepository).findAllByRole("ADMIN");
    }

    @Test
    void testFindAllByRole_NoUsersFound_ThrowsException() {
        // Arrange
        when(userRepository.findAllByRole("SUPER_ADMIN")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.findAllByRole("SUPER_ADMIN"); // Role that doesn't exist
        });
        assertEquals("No users found with role: SUPER_ADMIN", exception.getMessage());
        
        verify(userRepository).findAllByRole("SUPER_ADMIN");
    }
}