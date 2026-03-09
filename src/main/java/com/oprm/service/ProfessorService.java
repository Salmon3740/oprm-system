package com.oprm.service;

import com.oprm.entity.Professor;
import com.oprm.entity.Project;
import com.oprm.entity.enums.ProfessorAvailability;
import com.oprm.entity.enums.ProjectStatus;
import com.oprm.repository.ProfessorRepository;
import com.oprm.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final ProjectRepository projectRepository;
    private final NotificationService notificationService;
    private final LogService logService;

    public ProfessorService(ProfessorRepository professorRepository,
            ProjectRepository projectRepository,
            NotificationService notificationService,
            LogService logService) {
        this.professorRepository = professorRepository;
        this.projectRepository = projectRepository;
        this.notificationService = notificationService;
        this.logService = logService;
    }

    public Professor saveProfessor(Professor professor) {
        return professorRepository.save(professor);
    }

    public Optional<Professor> getProfessorById(Integer id) {
        return professorRepository.findById(id);
    }

    public List<Professor> getAllProfessors() {
        return professorRepository.findAll();
    }

    public List<Professor> getAvailableProfessors() {
        return professorRepository.findByAvailability(ProfessorAvailability.Available);
    }

    /**
     * Professor accepts mentorship for a project.
     * Assigns professor as mentor, updates project status to MENTOR_ASSIGNED,
     * notifies student.
     */
    @Transactional
    public Project acceptMentorship(Integer professorId, Integer projectId) {
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new RuntimeException("Professor not found with id: " + professorId));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        if (project.getMentor() != null) {
            throw new RuntimeException("Project already has a mentor assigned.");
        }

        project.setMentor(professor);
        project.setStatus(ProjectStatus.MENTOR_ASSIGNED);
        project.setLastUpdated(LocalDateTime.now());
        Project saved = projectRepository.save(project);

        // Notify student about mentor acceptance
        notificationService.createNotification(project.getStudent().getUser(),
                "Professor " + professor.getUser().getName() + " has accepted mentorship for your project '"
                        + project.getTitle() + "'!",
                "MENTOR_ACCEPTED");

        // Notify professor about mentor acceptance
        notificationService.createNotification(professor.getUser(),
                "You have accepted mentorship for project '" + project.getTitle() + "' by "
                        + project.getStudent().getUser().getName() + ".",
                "MENTOR_ACCEPTED");

        logService.logAction("MENTOR_ASSIGNED",
                "Professor " + professor.getUser().getName() + " assigned as mentor for project: " + project.getTitle(),
                professor.getUser());

        return saved;
    }

    /**
     * Update a professor's expertise domains and sync with AI model.
     */
    public Professor updateExpertise(Integer professorId, String expertiseDomains) {
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new RuntimeException("Professor not found with id: " + professorId));
        professor.setExpertiseDomains(expertiseDomains);
        Professor saved = professorRepository.save(professor);
        syncProfessorCsv();
        return saved;
    }

    public void syncProfessorCsv() {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(
                "c:\\oprm-system\\ai_models\\mentor_matching\\professors_dataset.csv"))) {
            writer.println("professor_id,name,department,expertise_domains,availability");
            for (Professor p : professorRepository.findAll()) {
                writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        p.getProfessorId(),
                        p.getUser().getName().replace("\"", "\"\""),
                        p.getDepartment() != null ? p.getDepartment().replace("\"", "\"\"") : "",
                        p.getExpertiseDomains() != null ? p.getExpertiseDomains().replace("\"", "\"\"") : "",
                        p.getAvailability().name());
            }
        } catch (Exception e) {
            // log silently
        }
    }
}