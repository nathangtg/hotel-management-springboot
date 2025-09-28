package com.nathangtg.hotel_management.test.config;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.nathangtg.hotel_management.api.repositories.UserRepository;
import com.nathangtg.hotel_management.api.repositories.BookingRepository;
import com.nathangtg.hotel_management.api.repositories.RoomRepository;
import com.nathangtg.hotel_management.api.repositories.HotelRepository;
import com.nathangtg.hotel_management.api.repositories.ManagementRepository;
import com.nathangtg.hotel_management.services.*;
import com.nathangtg.hotel_management.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("controller-test")
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    JpaRepositoriesAutoConfiguration.class,
    SpringDataWebAutoConfiguration.class
})
@ComponentScan(basePackages = {
    "com.nathangtg.hotel_management.api.controllers",
    "com.nathangtg.hotel_management.api.exception",
    "com.nathangtg.hotel_management.security"
})
public class ControllerTestConfiguration {
    
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    @Primary
    public BookingRepository bookingRepository() {
        return Mockito.mock(BookingRepository.class);
    }

    @Bean
    @Primary
    public RoomRepository roomRepository() {
        return Mockito.mock(RoomRepository.class);
    }

    @Bean
    @Primary
    public HotelRepository hotelRepository() {
        return Mockito.mock(HotelRepository.class);
    }

    @Bean
    @Primary
    public ManagementRepository managementRepository() {
        return Mockito.mock(ManagementRepository.class);
    }

    // Mock services
    @Bean
    @Primary
    public UserService userService() {
        return Mockito.mock(UserService.class);
    }

    @Bean
    @Primary
    public BookingService bookingService() {
        return Mockito.mock(BookingService.class);
    }

    @Bean
    @Primary
    public RoomService roomService() {
        return Mockito.mock(RoomService.class);
    }

    @Bean
    @Primary
    public HotelService hotelService() {
        return Mockito.mock(HotelService.class);
    }

    @Bean
    @Primary
    public ManagementService managementService() {
        return Mockito.mock(ManagementService.class);
    }

    // Mock security components
    @Bean
    @Primary
    public JwtUtil jwtUtil() {
        return Mockito.mock(JwtUtil.class);
    }

    @Bean
    @Primary
    public AuthenticationManager authenticationManager() {
        return Mockito.mock(AuthenticationManager.class);
    }
}
