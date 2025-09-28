package com.nathangtg.hotel_management.api.repositories;

import com.nathangtg.hotel_management.api.models.Hotel;
import com.nathangtg.hotel_management.api.models.Room;
import com.nathangtg.hotel_management.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestConfig.class)
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    private Hotel testHotel;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        testHotel = new Hotel();
        testHotel.setName("Test Hotel");
        testHotel.setAddress("123 Test Street");
        testHotel.setPhone("123-456-7890");
        testHotel.setEmail("testhotel@example.com");
        testHotel = hotelRepository.save(testHotel);

        testRoom = new Room();
        testRoom.setRoomNumber("201");
        testRoom.setRoomType("Deluxe");
        testRoom.setCapacity(2);
        testRoom.setPricePerNight(BigDecimal.valueOf(150.00));
        testRoom.setIsAvailable(true);
        testRoom.setHotel(testHotel);
    }

    @Test
    void testSaveRoom() {
        // Act
        Room savedRoom = roomRepository.save(testRoom);

        // Assert
        assertNotNull(savedRoom.getId());
        assertEquals("201", savedRoom.getRoomNumber());
        assertEquals("Deluxe", savedRoom.getRoomType());
        assertEquals(Integer.valueOf(2), savedRoom.getCapacity());
        assertEquals(BigDecimal.valueOf(150.00), savedRoom.getPricePerNight());
        assertTrue(savedRoom.getIsAvailable());
        assertEquals(testHotel, savedRoom.getHotel());
    }

    @Test
    void testFindByHotelId() {
        // Arrange
        Room savedRoom = roomRepository.save(testRoom);

        // Act
        List<Room> roomsInHotel = roomRepository.findByHotelId(testHotel.getId());

        // Assert
        assertEquals(1, roomsInHotel.size());
        assertEquals(savedRoom.getId(), roomsInHotel.get(0).getId());
        assertEquals(testHotel.getId(), roomsInHotel.get(0).getHotel().getId());
    }

    @Test
    void testFindByIsAvailableTrue() {
        // Arrange
        roomRepository.save(testRoom);

        Room unavailableRoom = new Room();
        unavailableRoom.setRoomNumber("202");
        unavailableRoom.setRoomType("Standard");
        unavailableRoom.setCapacity(2);
        unavailableRoom.setPricePerNight(BigDecimal.valueOf(100.00));
        unavailableRoom.setIsAvailable(false); // Not available
        unavailableRoom.setHotel(testHotel);
        roomRepository.save(unavailableRoom);

        // Act
        List<Room> availableRooms = roomRepository.findByIsAvailableTrue();

        // Assert
        assertEquals(1, availableRooms.size());
        assertTrue(availableRooms.get(0).getIsAvailable());
        assertEquals("201", availableRooms.get(0).getRoomNumber());
    }

    @Test
    void testFindByRoomType() {
        // Arrange
        roomRepository.save(testRoom);

        Room anotherRoom = new Room();
        anotherRoom.setRoomNumber("202");
        anotherRoom.setRoomType("Standard"); // Different type
        anotherRoom.setCapacity(2);
        anotherRoom.setPricePerNight(BigDecimal.valueOf(100.00));
        anotherRoom.setIsAvailable(true);
        anotherRoom.setHotel(testHotel);
        roomRepository.save(anotherRoom);

        // Act
        List<Room> deluxeRooms = roomRepository.findByRoomType("Deluxe");

        // Assert
        assertEquals(1, deluxeRooms.size());
        assertEquals("Deluxe", deluxeRooms.get(0).getRoomType());
        assertEquals("201", deluxeRooms.get(0).getRoomNumber());
    }

    @Test
    void testUpdateRoomAvailability() {
        // Arrange
        Room savedRoom = roomRepository.save(testRoom);

        // Act
        savedRoom.setIsAvailable(false);
        Room updatedRoom = roomRepository.save(savedRoom);

        // Assert
        assertFalse(updatedRoom.getIsAvailable());
    }
}