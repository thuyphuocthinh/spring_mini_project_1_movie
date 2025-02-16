package com.tpt.movie.movieAPI.auth.repositories;

import com.tpt.movie.movieAPI.auth.entities.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Transactional
    @Modifying
    @Query(
            "UPDATE User u SET u.password = ?2 WHERE u.email = ?1"
    )
    void updatePassword(String email, String password);
}
