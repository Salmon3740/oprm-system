package com.oprm.repository;

import com.oprm.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    Optional<Student> findByRegistrationNumber(String registrationNumber);

}