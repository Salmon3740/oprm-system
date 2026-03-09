package com.oprm.service;

import com.oprm.entity.Professor;
import com.oprm.entity.Student;
import com.oprm.entity.User;
import com.oprm.entity.enums.ProfessorAvailability;
import com.oprm.entity.enums.UserRole;
import com.oprm.repository.ProfessorRepository;
import com.oprm.repository.StudentRepository;
import com.oprm.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
            StudentRepository studentRepository,
            ProfessorRepository professorRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.professorRepository = professorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(User user, String roleSpecificId) {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null");

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered: " + user.getEmail());
        }

        if (roleSpecificId != null && !roleSpecificId.isBlank()) {
            if (user.getRole() == UserRole.STUDENT
                    && studentRepository.findByRegistrationNumber(roleSpecificId).isPresent()) {
                throw new RuntimeException("Registration Number already registered: " + roleSpecificId);
            }
            if (user.getRole() == UserRole.PROFESSOR
                    && professorRepository.findByFacultyId(roleSpecificId).isPresent()) {
                throw new RuntimeException("Faculty ID already registered: " + roleSpecificId);
            }
        }

        // generate unique secret key
        String secretKey = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        user.setSecretKey(secretKey);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        User activeUser = (savedUser != null) ? savedUser : user;

        // Auto-create role-based profile
        if (activeUser.getRole() == UserRole.STUDENT) {
            String rollNo = (roleSpecificId != null && !roleSpecificId.isBlank())
                    ? roleSpecificId
                    : "REG-" + (activeUser.getUserId() != null ? activeUser.getUserId() : "TEMP");
            Student student = Student.builder()
                    .user(activeUser)
                    .registrationNumber(rollNo)
                    .semester(1)
                    .build();
            studentRepository.save(student);
        } else if (activeUser.getRole() == UserRole.PROFESSOR) {
            String facId = (roleSpecificId != null && !roleSpecificId.isBlank())
                    ? roleSpecificId
                    : "FAC-" + (activeUser.getUserId() != null ? activeUser.getUserId() : "TEMP");
            Professor professor = Professor.builder()
                    .user(activeUser)
                    .facultyId(facId)
                    .department(activeUser.getDepartment() != null ? activeUser.getDepartment() : "General")
                    .expertiseDomains(activeUser.getDepartment() != null ? "" : "To be updated")
                    .availability(ProfessorAvailability.Available)
                    .build();
            professorRepository.save(professor);
        }

        return activeUser;
    }

    public Optional<User> login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean validateSecretKey(String email, String secretKey) {
        return userRepository.findByEmail(email)
                .map(user -> secretKey.equals(user.getSecretKey()))
                .orElse(false);
    }

    @Transactional
    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public User getUserByEmail(String identifier) {
        java.util.Optional<com.oprm.entity.Student> studentOpt = studentRepository.findByRegistrationNumber(identifier);
        if (studentOpt.isPresent()) {
            return studentOpt.get().getUser();
        }

        java.util.Optional<com.oprm.entity.Professor> profOpt = professorRepository.findByFacultyId(identifier);
        if (profOpt.isPresent()) {
            return profOpt.get().getUser();
        }

        return userRepository.findByEmail(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));
    }

    public User findByEmail(String identifier) {
        return getUserByEmail(identifier);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    public User updateUser(Integer id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }
}