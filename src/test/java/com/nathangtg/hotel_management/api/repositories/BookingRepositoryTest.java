package com.nathangtg.hotel_management.api.repositories;

import com.nathangtg.hotel_management.api.models.Booking;
import com.nathangtg.hotel_management.api.models.Room;
import com.nathangtg.hotel_management.api.models.User;
import com.nathangtg.hotel_management.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestConfig.class)
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    private User testUser;
    private Room testRoom;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("booker");
        testUser.setPassword("password123");
        testUser.setFirstName("Book");
        testUser.setLastName("Er");
        testUser.setEmail("booker@example.com");
        testUser.setRole("USER");
        testUser = userRepository.save(testUser);

        testRoom = new Room();
        testRoom.setRoomNumber("101");
        testRoom.setRoomType("Standard");
        testRoom.setPricePerNight(BigDecimal.valueOf(100.00));
        testRoom.setIsAvailable(true);
        testRoom = roomRepository.save(testRoom);

        testBooking = new Booking();
        testBooking.setUserId(testUser.getId());
        testBooking.setCheckInDate(LocalDate.now().plusDays(1));
        testBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        testBooking.setTotalPrice(BigDecimal.valueOf(200.00));
        testBooking.setStatus("CONFIRMED");
        testBooking.setUser(testUser);
        testBooking.setRoom(testRoom);
    }

    @Test
    void testSaveBooking() {
        // Act
        Booking savedBooking = bookingRepository.save(testBooking);

        // Assert
        assertNotNull(savedBooking.getId());
        assertEquals("CONFIRMED", savedBooking.getStatus());
        assertEquals(testUser.getId(), savedBooking.getUserId());
        assertEquals(testUser, savedBooking.getUser());
        assertEquals(testRoom, savedBooking.getRoom());
    }

    @Test
    void testFindBookingById() {
        // Arrange
        Booking savedBooking = bookingRepository.save(testBooking);

        // Act
        Optional<Booking> foundBooking = bookingRepository.findById(savedBooking.getId());

        // Assert
        assertTrue(foundBooking.isPresent());
        assertEquals(savedBooking.getId(), foundBooking.get().getId());
        assertEquals("CONFIRMED", foundBooking.get().getStatus());
    }

    @Test
    void testUpdateBookingStatus() {
        // Arrange
        Booking savedBooking = bookingRepository.save(testBooking);

        // Act
        savedBooking.setStatus("CHECKED_IN");
        Booking updatedBooking = bookingRepository.save(savedBooking);

        // Assert
        assertEquals("CHECKED_IN", updatedBooking.getStatus());
    }

    @Test
    void testFindBookingsByUserId() {
        // Arrange
        Booking savedBooking = bookingRepository.save(testBooking);

        // Act
        List<Booking> userBookings = bookingRepository.findByUser_Id(testUser.getId());

        // Assert
        assertFalse(userBookings.isEmpty());
        assertTrue(userBookings.stream().anyMatch(booking -> booking.getUserId().equals(testUser.getId())));
    }

    @Test
    void testFindBookingsByStatus() {
        // Arrange
        bookingRepository.save(testBooking);

        Booking anotherBooking = new Booking();
        anotherBooking.setUserId(testUser.getId());
        anotherBooking.setCheckInDate(LocalDate.now().plusDays(5));
        anotherBooking.setCheckOutDate(LocalDate.now().plusDays(7));
        anotherBooking.setTotalPrice(BigDecimal.valueOf(300.00));
        anotherBooking.setStatus("PENDING"); // Different status
        anotherBooking.setUser(testUser);
        anotherBooking.setRoom(testRoom);
        bookingRepository.save(anotherBooking);

        // Act
        List<Booking> confirmedBookings = bookingRepository.findByStatus("CONFIRMED");

        // Assert
        assertEquals(1, confirmedBookings.size());
        assertEquals("CONFIRMED", confirmedBookings.get(0).getStatus());
    }
}