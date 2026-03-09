package com.oprm.repository;

import com.oprm.entity.ProjectMilestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectMilestoneRepository extends JpaRepository<ProjectMilestone, Integer> {

    List<ProjectMilestone> findByProjectProjectId(Integer projectId);

}