package com.oprm.controller;

import com.oprm.entity.Professor;
import com.oprm.entity.Project;
import com.oprm.service.ProfessorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/professors")
public class ProfessorController {

    private final ProfessorService professorService;

    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @PostMapping
    public ResponseEntity<Professor> createProfessor(@RequestBody Professor professor) {
        return ResponseEntity.ok(professorService.saveProfessor(professor));
    }

    @GetMapping
    public ResponseEntity<List<Professor>> getAllProfessors() {
        return ResponseEntity.ok(professorService.getAllProfessors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Professor> getProfessor(@PathVariable Integer id) {
        return professorService.getProfessorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/available")
    public ResponseEntity<List<Professor>> getAvailableProfessors() {
        return ResponseEntity.ok(professorService.getAvailableProfessors());
    }

    /**
     * PUT /api/professors/{professorId}/accept-mentorship/{projectId}
     * Professor accepts mentorship for a project.
     */
    @PutMapping("/{professorId}/accept-mentorship/{projectId}")
    public ResponseEntity<Project> acceptMentorship(
            @PathVariable Integer professorId,
            @PathVariable Integer projectId) {
        return ResponseEntity.ok(professorService.acceptMentorship(professorId, projectId));
    }

    /**
     * PUT /api/professors/{id}/expertise
     * Update professor expertise domains.
     * Body: { "expertiseDomains": "Machine Learning, AI, Data Science" }
     */
    @PutMapping("/{id}/expertise")
    public ResponseEntity<Professor> updateExpertise(@PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        String domains = body.get("expertiseDomains");
        if (domains == null || domains.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(professorService.updateExpertise(id, domains));
    }
}