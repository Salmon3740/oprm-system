package com.oprm.repository;

import com.oprm.entity.ProjectArchive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectArchiveRepository extends JpaRepository<ProjectArchive, Integer> {

    Optional<ProjectArchive> findByProjectProjectId(Integer projectId);

}