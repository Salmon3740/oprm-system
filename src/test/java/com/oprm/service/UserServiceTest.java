package com.oprm.service;

import com.oprm.entity.User;
import com.oprm.entity.enums.UserRole;
import com.oprm.repository.ProfessorRepository;
import com.oprm.repository.StudentRepository;
import com.oprm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private ProfessorRepository professorRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1)
                .name("Test Student")
                .email("student@test.com")
                .password("rawpassword")
                .role(UserRole.STUDENT)
                .build();
    }

    @Test
    void registerUser_ShouldEncodePasswordAndSave() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("rawpassword")).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.registerUser(testUser, null);

        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode("rawpassword");
        verify(userRepository, times(1)).save(any(User.class));
        // Student profile should be auto-created
        verify(studentRepository, times(1)).save(any());
    }

    @Test
    void registerUser_ShouldThrowWhenUserIsNull() {
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(null, null));
    }

    @Test
    void registerUser_ShouldThrowWhenEmailAlreadyExists() {
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(testUser));
        assertThrows(RuntimeException.class, () -> userService.registerUser(testUser, null));
    }

    @Test
    void getUserByEmail_ShouldReturnUser() {
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(testUser));
        User result = userService.getUserByEmail("student@test.com");
        assertEquals("Test Student", result.getName());
    }

    @Test
    void getUserByEmail_ShouldThrowWhenNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> userService.getUserByEmail("unknown@test.com"));
    }

    @Test
    void login_ShouldReturnUserWhenPasswordMatches() {
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("rawpassword", testUser.getPassword())).thenReturn(true);

        Optional<User> result = userService.login("student@test.com", "rawpassword");
        assertTrue(result.isPresent());
    }

    @Test
    void login_ShouldReturnEmptyWhenPasswordWrong() {
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        Optional<User> result = userService.login("student@test.com", "wrongpassword");
        assertFalse(result.isPresent());
    }
}
