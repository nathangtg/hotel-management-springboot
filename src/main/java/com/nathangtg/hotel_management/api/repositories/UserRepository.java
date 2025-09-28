package com.nathangtg.hotel_management.api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nathangtg.hotel_management.api.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByIdAndRole(Long id, String role);
    Optional<User> findByUsernameAndRole(String username, String role);
    Optional<List<User>> findAllByRole(String role);
    Optional<User> findByEmail(String email);
    List<User> findByRoleIn(List<String> roles);
    Optional<User> findByFirstNameAndLastName(String firstName, String lastName);
}