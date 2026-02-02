package com.example.esm_project.repository;

import com.example.esm_project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Check if username already exists in the system
     * 
     * @param username username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Find user by username for authentication
     * 
     * @param username username to find
     * @return Optional containing user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);
}
