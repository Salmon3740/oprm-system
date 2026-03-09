package com.oprm.controller;

import com.oprm.entity.User;
import com.oprm.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users/profile — returns the authenticated user's profile.
     */
    @GetMapping("/profile")
    public ResponseEntity<User> getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    /**
     * GET /api/users/profile/{id} — returns a user by ID (admin use).
     */
    @GetMapping("/profile/{id}")
    public ResponseEntity<User> getUserProfile(@PathVariable Integer id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}