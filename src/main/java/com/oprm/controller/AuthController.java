package com.oprm.controller;

import com.oprm.dto.AuthRequest;
import com.oprm.dto.AuthResponse;
import com.oprm.dto.RegisterRequest;
import com.oprm.entity.User;
import com.oprm.entity.enums.UserRole;
import com.oprm.security.JwtUtil;
import com.oprm.service.LogService;
import com.oprm.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private final UserService userService;
        private final JwtUtil jwtUtil;
        private final AuthenticationManager authenticationManager;
        private final LogService logService;

        public AuthController(UserService userService, JwtUtil jwtUtil,
                        AuthenticationManager authenticationManager,
                        LogService logService) {
                this.userService = userService;
                this.jwtUtil = jwtUtil;
                this.authenticationManager = authenticationManager;
                this.logService = logService;
        }

        @PostMapping("/register")
        public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
                User user = User.builder()
                                .name(request.getName())
                                .email(request.getEmail())
                                .password(request.getPassword())
                                .role(request.getRole() != null ? request.getRole() : UserRole.STUDENT)
                                .department(request.getDepartment())
                                .build();

                User saved = userService.registerUser(user, null);

                User responseUser = User.builder()
                                .userId(saved.getUserId())
                                .name(saved.getName())
                                .email(saved.getEmail())
                                .role(saved.getRole())
                                .department(saved.getDepartment())
                                .createdAt(saved.getCreatedAt())
                                .profilePhoto(saved.getProfilePhoto())
                                .secretKey(saved.getSecretKey())
                                .build();

                return ResponseEntity.ok(responseUser);
        }

        @PostMapping("/login")
        public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
                Authentication auth = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

                String token = jwtUtil.generateToken(request.getEmail());
                User user = userService.getUserByEmail(request.getEmail());

                // Log the login action
                logService.logAction("USER_LOGIN", "User logged in: " + user.getEmail(), user);

                AuthResponse response = AuthResponse.builder()
                                .token(token)
                                .userId(user.getUserId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .role(user.getRole() != null ? user.getRole().name() : "STUDENT")
                                .build();

                return ResponseEntity.ok(response);
        }

        @PostMapping("/forgot-password")
        public ResponseEntity<java.util.Map<String, String>> forgotPassword(
                        @RequestBody java.util.Map<String, String> request) {
                String email = request.get("email");
                String secretKey = request.get("secretKey");

                boolean isValid = userService.validateSecretKey(email, secretKey);
                java.util.Map<String, String> response = new java.util.HashMap<>();

                if (isValid) {
                        response.put("status", "success");
                        response.put("message", "Credentials verified. Proceed to reset password.");
                        return ResponseEntity.ok(response);
                } else {
                        response.put("status", "error");
                        response.put("message", "Invalid email or secret key.");
                        return ResponseEntity.badRequest().body(response);
                }
        }

        @PostMapping("/reset-password")
        public ResponseEntity<java.util.Map<String, String>> resetPassword(
                        @RequestBody java.util.Map<String, String> request) {
                String email = request.get("email");
                String newPassword = request.get("newPassword");

                java.util.Map<String, String> response = new java.util.HashMap<>();
                try {
                        userService.updatePassword(email, newPassword);
                        response.put("status", "success");
                        response.put("message", "Password reset successfully.");
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        response.put("status", "error");
                        response.put("message", "Failed to reset password: " + e.getMessage());
                        return ResponseEntity.badRequest().body(response);
                }
        }
}