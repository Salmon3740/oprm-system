package com.oprm.repository;

import com.oprm.entity.FacultyExpertise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacultyExpertiseRepository extends JpaRepository<FacultyExpertise, Integer> {

    List<FacultyExpertise> findByProfessorProfessorId(Integer professorId);

}