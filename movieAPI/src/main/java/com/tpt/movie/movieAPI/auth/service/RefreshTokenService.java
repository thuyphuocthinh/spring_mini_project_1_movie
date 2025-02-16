package com.tpt.movie.movieAPI.auth.service;

import com.tpt.movie.movieAPI.auth.entities.RefreshToken;
import com.tpt.movie.movieAPI.auth.entities.User;
import com.tpt.movie.movieAPI.auth.repositories.RefreshTokenRepository;
import com.tpt.movie.movieAPI.auth.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        RefreshToken refreshToken = user.getRefreshToken();

        if(refreshToken == null) {
            long refreshTokenValidity = 5*60*60*10000;
            refreshToken = RefreshToken.builder().
                    refreshToken(UUID.randomUUID().toString())
                    .expiresAt(Instant.now().plusMillis(refreshTokenValidity))
                    .user(user)
                    .build();
            refreshTokenRepository.save(refreshToken);
        }

        return refreshToken;
    }

    public RefreshToken verifyRefreshToken(String refreshToken) {
        RefreshToken refreshTokenObj = refreshTokenRepository.findByRefreshToken(refreshToken).orElseThrow(() -> new RuntimeException("Refresh token not found"));
        if(refreshTokenObj.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshTokenObj);
            throw new RuntimeException("Refresh token expired");
        }
        return refreshTokenObj;
    }
}
