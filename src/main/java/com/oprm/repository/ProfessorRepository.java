package com.oprm.repository;

import com.oprm.entity.Professor;
import com.oprm.entity.enums.ProfessorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfessorRepository extends JpaRepository<Professor, Integer> {

    List<Professor> findByAvailability(ProfessorAvailability availability);

    Optional<Professor> findByFacultyId(String facultyId);

    Optional<Professor> findByUser(com.oprm.entity.User user);

}