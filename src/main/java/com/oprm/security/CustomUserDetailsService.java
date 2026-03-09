package com.oprm.security;

import com.oprm.entity.User;
import com.oprm.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;
        private final com.oprm.repository.StudentRepository studentRepository;
        private final com.oprm.repository.ProfessorRepository professorRepository;

        public CustomUserDetailsService(UserRepository userRepository,
                        com.oprm.repository.StudentRepository studentRepository,
                        com.oprm.repository.ProfessorRepository professorRepository) {
                this.userRepository = userRepository;
                this.studentRepository = studentRepository;
                this.professorRepository = professorRepository;
        }

        /**
         * Spring Security calls this with whatever the user typed in the "username"
         * field.
         * We support login by:
         * 1. registration number (for students)
         * 2. faculty ID (for professors)
         * 3. email — fallback for admins and seeded users
         */
        @Override
        public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
                // Determine user and mapped ID
                User user = null;
                String mappedId = null;

                java.util.Optional<com.oprm.entity.Student> studentOpt = studentRepository
                                .findByRegistrationNumber(identifier);
                if (studentOpt.isPresent()) {
                        user = studentOpt.get().getUser();
                        mappedId = studentOpt.get().getRegistrationNumber();
                } else {
                        java.util.Optional<com.oprm.entity.Professor> profOpt = professorRepository
                                        .findByFacultyId(identifier);
                        if (profOpt.isPresent()) {
                                user = profOpt.get().getUser();
                                mappedId = profOpt.get().getFacultyId();
                        } else {
                                user = userRepository.findByEmail(identifier)
                                                .orElseThrow(() -> new UsernameNotFoundException(
                                                                "User not found with ID or email: " + identifier));
                        }
                }

                return new org.springframework.security.core.userdetails.User(
                                // Use the mapped ID if present, otherwise email (for admin / backward compat)
                                mappedId != null ? mappedId : user.getEmail(),
                                user.getPassword(),
                                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        }
}