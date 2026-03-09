package com.oprm.controller;

import com.oprm.entity.ProjectArchive;
import com.oprm.service.ProjectArchiveService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/archive")
public class ArchiveController {

    private final ProjectArchiveService archiveService;

    public ArchiveController(ProjectArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @GetMapping("/projects")
    public List<ProjectArchive> getAllArchivedProjects() {
        return archiveService.getAllArchivedProjects();
    }

    @GetMapping("/projects/search")
    public List<ProjectArchive> searchByDomain(@RequestParam String domain) {
        return archiveService.getArchivedProjectsByDomain(domain);
    }
}
