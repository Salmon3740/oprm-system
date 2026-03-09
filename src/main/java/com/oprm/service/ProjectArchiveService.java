package com.oprm.service;

import com.oprm.entity.ProjectArchive;
import com.oprm.repository.ProjectArchiveRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectArchiveService {

    private final ProjectArchiveRepository archiveRepository;

    public ProjectArchiveService(ProjectArchiveRepository archiveRepository) {
        this.archiveRepository = archiveRepository;
    }

    public ProjectArchive archiveProject(ProjectArchive archive) {
        if (archive.getArchivedAt() == null) {
            archive.setArchivedAt(java.time.LocalDateTime.now());
        }
        return archiveRepository.save(archive);
    }

    public Optional<ProjectArchive> getArchiveByProject(Integer projectId) {
        return archiveRepository.findByProjectProjectId(projectId);
    }

    public List<ProjectArchive> getAllArchivedProjects() {
        return archiveRepository.findAll();
    }

    public List<ProjectArchive> getArchivedProjectsByDomain(String domain) {
        return archiveRepository.findAll().stream()
                .filter(a -> a.getProject().getDetectedDomains() != null &&
                        a.getProject().getDetectedDomains().toLowerCase().contains(domain.toLowerCase()))
                .collect(Collectors.toList());
    }
}