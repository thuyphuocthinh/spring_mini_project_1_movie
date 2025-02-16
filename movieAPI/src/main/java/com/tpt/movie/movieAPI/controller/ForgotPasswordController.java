package com.tpt.movie.movieAPI.controller;

import com.tpt.movie.movieAPI.auth.entities.User;
import com.tpt.movie.movieAPI.auth.repositories.UserRepository;
import com.tpt.movie.movieAPI.auth.utils.ChangePassword;
import com.tpt.movie.movieAPI.dto.MailBody;
import com.tpt.movie.movieAPI.entities.ForgotPassword;
import com.tpt.movie.movieAPI.repositories.ForgotPasswordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

@RestController
@RequestMapping("/api/v1/forgot-password")
public class ForgotPasswordController {
    private final UserRepository userRepository;
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordController(UserRepository userRepository, ForgotPasswordRepository forgotPasswordRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.forgotPasswordRepository = forgotPasswordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/verify-email/{email}")
    public ResponseEntity<String> forgotPassword(@PathVariable String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int otp = otpGenerator();
        MailBody mailBody = MailBody.builder()
                .to(email)
                .text("This is the OTP for verification: " + otp)
                .subject("Email verification")
                .build();

        ForgotPassword forgotPassword = ForgotPassword.builder()
                .otp(otp)
                .expirationTime(new Date(System.currentTimeMillis() + 70 * 1000))
                .build();

        forgotPasswordRepository.save(forgotPassword);
        return ResponseEntity.ok("OTP was sent to your email. Please check.");
    }

    @PostMapping("/verify-otp/{otp}/{email}")
    public ResponseEntity<String> verifyOtp(@PathVariable Integer otp,
                                            @PathVariable String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ForgotPassword forgotPassword = forgotPasswordRepository.findByOtpAndUser(otp, user)
                .orElseThrow(()-> new RuntimeException("OTP not found"));

        if(forgotPassword.getExpirationTime().before(Date.from(Instant.now()))) {
            forgotPasswordRepository.deleteById(forgotPassword.getForgotPasswordId());
            return new ResponseEntity<>("OTP verification failed", HttpStatus.EXPECTATION_FAILED);
        }

        return ResponseEntity.ok("OTP verification successful");
    }

    @PostMapping("/reset-password/{email}")
    public ResponseEntity<String> resetPassword(@RequestBody ChangePassword changePassword,
                                                @PathVariable String email) {
        if(!Objects.equals(changePassword.password(), changePassword.repeatPassword())) {
            return new ResponseEntity<>("Password not match", HttpStatus.EXPECTATION_FAILED);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String encodedPassword = passwordEncoder.encode(changePassword.password());
        userRepository.updatePassword(email, encodedPassword);

        return ResponseEntity.ok("Password changed successfully");
    }

    private Integer otpGenerator() {
        Random random = new Random();
        return random.nextInt(100_000, 999_999);
    }
}
