package com.oprm.service;

import com.oprm.entity.ProjectMilestone;
import com.oprm.repository.ProjectMilestoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectMilestoneService {

    private final ProjectMilestoneRepository milestoneRepository;

    public ProjectMilestoneService(ProjectMilestoneRepository milestoneRepository) {
        this.milestoneRepository = milestoneRepository;
    }

    public ProjectMilestone addMilestone(ProjectMilestone milestone) {
        return milestoneRepository.save(milestone);
    }

    public List<ProjectMilestone> getMilestonesByProject(Integer projectId) {
        return milestoneRepository.findByProjectProjectId(projectId);
    }

    public List<ProjectMilestone> getAllMilestones() {
        return milestoneRepository.findAll();
    }
}