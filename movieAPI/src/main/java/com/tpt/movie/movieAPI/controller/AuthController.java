package com.tpt.movie.movieAPI.controller;

import com.tpt.movie.movieAPI.auth.entities.RefreshToken;
import com.tpt.movie.movieAPI.auth.entities.User;
import com.tpt.movie.movieAPI.auth.service.AuthService;
import com.tpt.movie.movieAPI.auth.service.JwtService;
import com.tpt.movie.movieAPI.auth.service.RefreshTokenService;
import com.tpt.movie.movieAPI.auth.utils.AuthResponse;
import com.tpt.movie.movieAPI.auth.utils.LoginRequest;
import com.tpt.movie.movieAPI.auth.utils.RefreshTokenRequest;
import com.tpt.movie.movieAPI.auth.utils.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService, JwtService jwtService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenRequest.getRefreshToken());
        User user = refreshToken.getUser();

        String accesToken = jwtService.generateToken(user);
        return ResponseEntity.ok(
                AuthResponse.builder().accessToken(accesToken).refreshToken(refreshToken.getRefreshToken()).build()
        );
    }
}
