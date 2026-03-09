package com.oprm.service;

import com.oprm.dto.ProjectRequest;
import com.oprm.entity.Project;
import com.oprm.entity.ProjectMilestone;
import com.oprm.entity.Student;
import com.oprm.entity.enums.MilestoneStatus;
import com.oprm.entity.enums.ProjectStatus;
import com.oprm.repository.ProfessorRepository;
import com.oprm.repository.ProjectMilestoneRepository;
import com.oprm.repository.ProjectRepository;
import com.oprm.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProjectService {

        private final ProjectRepository projectRepository;
        private final StudentRepository studentRepository;
        private final AIService aiService;
        private final LogService logService;
        private final ProjectArchiveService archiveService;
        private final NotificationService notificationService;
        private final ProjectMilestoneRepository milestoneRepository;
        private final ProfessorRepository professorRepository; // Added dependency

        public ProjectService(ProjectRepository projectRepository,
                        StudentRepository studentRepository,
                        AIService aiService,
                        LogService logService,
                        ProjectArchiveService archiveService,
                        NotificationService notificationService,
                        ProjectMilestoneRepository milestoneRepository,
                        ProfessorRepository professorRepository) { // Added to constructor
                this.projectRepository = projectRepository;
                this.studentRepository = studentRepository;
                this.aiService = aiService;
                this.logService = logService;
                this.archiveService = archiveService;
                this.notificationService = notificationService;
                this.milestoneRepository = milestoneRepository;
                this.professorRepository = professorRepository; // Initialized
        }

        public Project createProject(ProjectRequest request) {
                Student student = studentRepository.findById(request.getStudentId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Student not found with id: " + request.getStudentId()));

                List<String> domains = aiService.classifyDomain(request.getDescription());

                Project project = Project.builder()
                                .title(request.getTitle())
                                .description(request.getDescription())
                                .student(student)
                                .detectedDomains(String.join(", ", domains))
                                .status(ProjectStatus.SUBMITTED)
                                .submissionDate(LocalDateTime.now())
                                .progressPercent(0)
                                .lastUpdated(LocalDateTime.now())
                                .build();

                Project saved = projectRepository.save(project);
                logService.logAction("PROJECT_CREATED", "New project created: " + project.getTitle(),
                                student.getUser());

                // Notify student that project was submitted
                notificationService.createNotification(student.getUser(),
                                "Your project '" + project.getTitle() + "' has been submitted for review.",
                                "PROJECT_SUBMITTED");

                return saved;
        }

        public Optional<Project> getProjectById(Integer id) {
                return projectRepository.findById(id);
        }

        public List<Project> getAllProjects() {
                return projectRepository.findAll();
        }

        public List<Map<String, Object>> getMentorSuggestions(String description) {
                List<String> domains = aiService.classifyDomain(description);
                return aiService.suggestMentors(String.join(", ", domains));
        }

        public List<Project> getProjectsByStudent(Integer studentId) {
                return projectRepository.findByStudentStudentId(studentId);
        }

        public List<Project> getProjectsByMentor(Integer mentorId) {
                return projectRepository.findByMentorProfessorId(mentorId);
        }

        public Map<String, Object> getMentorAndDomainSuggestions(String description) {
                return aiService.getUnifiedSuggestions(description);
        }

        /**
         * Get mentor suggestions for a specific project (by projectId).
         */
        public Map<String, Object> getMentorSuggestionsForProject(Integer projectId) {
                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
                String domains = project.getDetectedDomains();
                if (domains == null || domains.isBlank()) {
                        // Re-classify if domains not yet set
                        List<String> detected = aiService.classifyDomain(project.getDescription());
                        domains = String.join(", ", detected);
                        project.setDetectedDomains(domains);
                        projectRepository.save(project);
                }
                return aiService.getUnifiedSuggestions(project.getDescription());
        }

        public Project updateProjectStatus(Integer projectId, ProjectStatus status) {
                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

                project.setStatus(status);
                project.setLastUpdated(LocalDateTime.now());
                Project updated = projectRepository.save(project);

                logService.logAction("PROJECT_STATUS_UPDATED",
                                "Project '" + project.getTitle() + "' status updated to " + status,
                                project.getStudent().getUser());

                notificationService.createNotification(project.getStudent().getUser(),
                                "Your project '" + project.getTitle() + "' status has been updated to: " + status,
                                "PROJECT_UPDATE");

                if (project.getMentor() != null) {
                        notificationService.createNotification(project.getMentor().getUser(),
                                        "Project '" + project.getTitle() + "' status has been updated to: " + status,
                                        "PROJECT_UPDATE");
                }

                if (status == ProjectStatus.COMPLETED) {
                        com.oprm.entity.ProjectArchive archive = com.oprm.entity.ProjectArchive.builder()
                                        .project(project)
                                        .archivedAt(LocalDateTime.now())
                                        .build();
                        archiveService.archiveProject(archive);
                        logService.logAction("PROJECT_ARCHIVED",
                                        "Project '" + project.getTitle() + "' has been archived.",
                                        project.getStudent().getUser());
                }

                return updated;
        }

        /**
         * POST /api/projects/{id}/submit — student explicitly submits the project.
         */
        public Project submitProject(Integer projectId) {
                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

                project.setStatus(ProjectStatus.SUBMITTED);
                project.setSubmissionDate(LocalDateTime.now());
                project.setLastUpdated(LocalDateTime.now());
                Project saved = projectRepository.save(project);

                logService.logAction("PROJECT_SUBMITTED", "Project '" + project.getTitle() + "' submitted by student.",
                                project.getStudent().getUser());
                notificationService.createNotification(project.getStudent().getUser(),
                                "Project '" + project.getTitle() + "' has been submitted.", "PROJECT_SUBMITTED");

                if (project.getMentor() != null) {
                        notificationService.createNotification(project.getMentor().getUser(),
                                        "Project '" + project.getTitle() + "' has been submitted by "
                                                        + project.getStudent().getUser().getName() + ".",
                                        "PROJECT_SUBMITTED");
                }

                return saved;
        }

        public Project requestMentor(Integer projectId, Integer professorId) {
                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));
                com.oprm.entity.Professor professor = professorRepository.findById(professorId)
                                .orElseThrow(() -> new RuntimeException("Professor not found"));

                project.getRequestedMentors().add(professor);
                project.setStatus(ProjectStatus.MENTOR_PENDING);
                project.setLastUpdated(LocalDateTime.now());

                logService.logAction("MENTOR_REQUESTED", "Requested mentor " + professor.getUser().getName(),
                                project.getStudent().getUser());
                notificationService.createNotification(project.getStudent().getUser(),
                                "You requested mentorship from " + professor.getUser().getName(), "INFO");
                notificationService.createNotification(professor.getUser(), project.getStudent().getUser().getName()
                                + " requested you as a mentor for project: " + project.getTitle(), "ACTION_REQUIRED");

                return projectRepository.save(project);
        }

        public void deleteProject(Integer projectId) {
                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));
                projectRepository.delete(project);

                notificationService.createNotification(project.getStudent().getUser(),
                                "You deleted project: " + project.getTitle(), "PROJECT_DELETED");
                logService.logAction("PROJECT_DELETED", "Project deleted by student.", project.getStudent().getUser());
        }

        /**
         * PUT /api/projects/{id}/progress — update the progress percentage.
         */
        public Project updateProgress(Integer projectId, Integer progressPercent, String milestoneTitle,
                        String milestoneDescription) {
                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

                if (progressPercent != null) {
                        project.setProgressPercent(progressPercent);
                }
                project.setLastUpdated(LocalDateTime.now());
                Project saved = projectRepository.save(project);

                // Optionally create a milestone entry
                if (milestoneTitle != null && !milestoneTitle.isBlank()) {
                        ProjectMilestone milestone = ProjectMilestone.builder()
                                        .project(project)
                                        .milestoneTitle(milestoneTitle)
                                        .description(milestoneDescription)
                                        .status(MilestoneStatus.COMPLETED)
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                        milestoneRepository.save(milestone);
                }

                logService.logAction("PROJECT_PROGRESS_UPDATED",
                                "Project '" + project.getTitle() + "' progress updated to " + progressPercent + "%.",
                                project.getStudent().getUser());

                notificationService.createNotification(project.getStudent().getUser(),
                                "You updated progress for project '" + project.getTitle() + "' to " + progressPercent
                                                + "%.",
                                "PROGRESS_UPDATED");

                if (project.getMentor() != null) {
                        notificationService.createNotification(project.getMentor().getUser(),
                                        "Student " + project.getStudent().getUser().getName()
                                                        + " updated progress for project '" + project.getTitle()
                                                        + "' to " + progressPercent + "%.",
                                        "PROGRESS_UPDATED");
                }

                return saved;
        }

        public Project updateProject(Project project) {
                return projectRepository.save(project);
        }
}