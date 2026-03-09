package com.oprm.controller;

import com.oprm.dto.ProjectRequest;
import com.oprm.entity.Project;
import com.oprm.service.AIService;
import com.oprm.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final AIService aiService;

    public ProjectController(ProjectService projectService, AIService aiService) {
        this.projectService = projectService;
        this.aiService = aiService;
    }

    // POST /api/projects — create project with NLP domain detection
    @PostMapping
    public ResponseEntity<Project> createProject(@Valid @RequestBody ProjectRequest projectRequest) {
        return ResponseEntity.ok(projectService.createProject(projectRequest));
    }

    // GET /api/projects — list all projects
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    // GET /api/projects/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable Integer id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/projects/student/{studentId}
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Project>> getStudentProjects(@PathVariable Integer studentId) {
        return ResponseEntity.ok(projectService.getProjectsByStudent(studentId));
    }

    // GET /api/projects/mentor/{mentorId}
    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<List<Project>> getMentorProjects(@PathVariable Integer mentorId) {
        return ResponseEntity.ok(projectService.getProjectsByMentor(mentorId));
    }

    /**
     * POST /api/projects/classify-domain
     * Body: { "description": "..." }
     * Returns: { "domains": ["Machine Learning", "AI", ...] }
     */
    @PostMapping("/classify-domain")
    public ResponseEntity<Map<String, Object>> classifyDomain(@RequestBody Map<String, String> body) {
        String description = body.get("description");
        if (description == null || description.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Description is required"));
        }
        List<String> domains = aiService.classifyDomain(description);
        return ResponseEntity.ok(Map.of("domains", domains));
    }

    /**
     * PUT /api/projects/{id}/progress
     * Body: { "progressPercent": 50, "milestoneTitle": "MVP done",
     * "milestoneDescription": "..." }
     */
    @PutMapping("/{id}/progress")
    public ResponseEntity<Project> updateProgress(@PathVariable Integer id,
            @RequestBody Map<String, Object> body) {
        Integer progressPercent = body.get("progressPercent") != null
                ? ((Number) body.get("progressPercent")).intValue()
                : null;
        String milestoneTitle = (String) body.get("milestoneTitle");
        String milestoneDescription = (String) body.get("milestoneDescription");
        return ResponseEntity
                .ok(projectService.updateProgress(id, progressPercent, milestoneTitle, milestoneDescription));
    }

    /**
     * POST /api/projects/{id}/submit — student submits completed project
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<Project> submitProject(@PathVariable Integer id) {
        return ResponseEntity.ok(projectService.submitProject(id));
    }
}