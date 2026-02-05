package com.example.esm_project.repository;

import com.example.esm_project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    java.util.List<User> findByRole(String role);

    Page<User> findByRole(String role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchManagers(String role, String search, Pageable pageable);
}
