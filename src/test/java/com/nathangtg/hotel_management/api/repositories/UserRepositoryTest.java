package com.nathangtg.hotel_management.api.repositories;

import com.nathangtg.hotel_management.api.models.User;
import com.nathangtg.hotel_management.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("testuser@example.com");
        testUser.setRole("USER");
    }

    @Test
    void testFindByUsername() {
        // Arrange
        userRepository.save(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        assertEquals("testuser@example.com", foundUser.get().getEmail());
    }

    @Test
    void testFindByUsername_NotFound() {
        // Act
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // Assert
        assertTrue(foundUser.isEmpty());
    }

    @Test
    void testFindByIdAndRole() {
        // Arrange
        userRepository.save(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByIdAndRole(testUser.getId(), "USER");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("USER", foundUser.get().getRole());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void testFindByUsernameAndRole() {
        // Arrange
        userRepository.save(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByUsernameAndRole("testuser", "USER");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        assertEquals("USER", foundUser.get().getRole());
    }

    @Test
    void testFindAllByRole() {
        // Arrange
        User user1 = new User();
        user1.setUsername("admin1");
        user1.setPassword("password123");
        user1.setFirstName("Admin");
        user1.setLastName("One");
        user1.setEmail("admin1@example.com");
        user1.setRole("ADMIN");
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("admin2");
        user2.setPassword("password456");
        user2.setFirstName("Admin");
        user2.setLastName("Two");
        user2.setEmail("admin2@example.com");
        user2.setRole("ADMIN");
        userRepository.save(user2);

        User user3 = new User();
        user3.setUsername("user3");
        user3.setPassword("password789");
        user3.setFirstName("Regular");
        user3.setLastName("User");
        user3.setEmail("user3@example.com");
        user3.setRole("USER");
        userRepository.save(user3);

        // Act
        Optional<List<User>> adminUsers = userRepository.findAllByRole("ADMIN");

        // Assert
        assertTrue(adminUsers.isPresent());
        assertEquals(2, adminUsers.get().size());
        adminUsers.get().forEach(user -> assertEquals("ADMIN", user.getRole()));
    }

    @Test
    void testFindByEmail() {
        // Arrange
        userRepository.save(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByEmail("testuser@example.com");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("testuser@example.com", foundUser.get().getEmail());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void testFindByEmail_NotFound() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertTrue(foundUser.isEmpty());
    }

    @Test
    void testFindByRoleIn() {
        // Arrange
        User user1 = new User();
        user1.setUsername("admin1");
        user1.setPassword("password123");
        user1.setFirstName("Admin");
        user1.setLastName("One");
        user1.setEmail("admin1@example.com");
        user1.setRole("ADMIN");
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("mod1");
        user2.setPassword("password456");
        user2.setFirstName("Mod");
        user2.setLastName("erator");
        user2.setEmail("mod1@example.com");
        user2.setRole("MODERATOR");
        userRepository.save(user2);

        User user3 = new User();
        user3.setUsername("user3");
        user3.setPassword("password789");
        user3.setFirstName("Regular");
        user3.setLastName("User");
        user3.setEmail("user3@example.com");
        user3.setRole("USER");
        userRepository.save(user3);

        // Act
        List<User> users = userRepository.findByRoleIn(List.of("ADMIN", "MODERATOR"));

        // Assert
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(user -> user.getRole().equals("ADMIN")));
        assertTrue(users.stream().anyMatch(user -> user.getRole().equals("MODERATOR")));
    }

    @Test
    void testFindByFirstNameAndLastName() {
        // Arrange
        userRepository.save(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByFirstNameAndLastName("Test", "User");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("Test", foundUser.get().getFirstName());
        assertEquals("User", foundUser.get().getLastName());
    }

    @Test
    void testFindByFirstNameAndLastName_NotFound() {
        // Act
        Optional<User> foundUser = userRepository.findByFirstNameAndLastName("Non", "Existent");

        // Assert
        assertTrue(foundUser.isEmpty());
    }
}