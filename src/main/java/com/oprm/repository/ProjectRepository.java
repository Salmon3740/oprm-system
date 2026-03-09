package com.oprm.repository;

import com.oprm.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Integer> {

    List<Project> findByStudentStudentId(Integer studentId);

    List<Project> findByMentorProfessorId(Integer mentorId);

}