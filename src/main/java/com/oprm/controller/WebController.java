package com.oprm.controller;

import com.oprm.dto.ProjectRequest;
import com.oprm.entity.*;
import com.oprm.entity.enums.UserRole;
import com.oprm.repository.NotificationRepository;
import com.oprm.repository.ProjectRepository;
import com.oprm.repository.ResourceRepository;
import com.oprm.repository.UserRepository;
import com.oprm.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.web.multipart.MultipartFile;

import com.oprm.repository.StudentRepository;

@Controller
public class WebController {

    private final UserService userService;
    private final ProjectService projectService;
    private final StudentService studentService;
    private final ProfessorService professorService;
    private final AdminService adminService;
    private final NotificationRepository notificationRepository;
    private final ProjectRepository projectRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final AIService aiService;
    private final ResourceRequestService resourceRequestService;
    private final StudentRepository studentRepository;

    public WebController(UserService userService,
            ProjectService projectService,
            StudentService studentService,
            ProfessorService professorService,
            AdminService adminService,
            NotificationRepository notificationRepository,
            ProjectRepository projectRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository,
            AIService aiService,
            ResourceRequestService resourceRequestService,
            StudentRepository studentRepository) {
        this.userService = userService;
        this.projectService = projectService;
        this.studentService = studentService;
        this.professorService = professorService;
        this.adminService = adminService;
        this.notificationRepository = notificationRepository;
        this.projectRepository = projectRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
        this.resourceRequestService = resourceRequestService;
        this.studentRepository = studentRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            return null;
        return userService.getUserByEmail(auth.getName());
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(name = "email", required = false) String email,
            org.springframework.ui.Model model) {
        if (email != null) {
            model.addAttribute("email", email);
        }
        return "reset-password";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("allDomains", ALL_IT_DOMAINS);
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("role") UserRole role,
            @RequestParam(value = "loginId", required = false) String loginId,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "domains", required = false) java.util.List<String> domains,
            Model model) {
        try {
            User user = User.builder()
                    .name(name)
                    .email(email)
                    .password(password)
                    .role(role)
                    .department(department)
                    .build();
            User saved = userService.registerUser(user,
                    loginId != null && !loginId.isBlank() ? loginId.trim().toUpperCase() : null);

            // If professor and domains were selected, save them now
            if (role == UserRole.PROFESSOR && domains != null && !domains.isEmpty()) {
                Professor prof = professorService.getAllProfessors().stream()
                        .filter(p -> p.getUser().getUserId().equals(saved.getUserId()))
                        .findFirst().orElse(null);
                if (prof != null) {
                    prof.setExpertiseDomains(String.join(", ", domains));
                    professorService.saveProfessor(prof);
                    // Centralized sync
                    professorService.syncProfessorCsv();
                }
            }

            model.addAttribute("message",
                    "Registration successful! Login with your " +
                            (role == UserRole.STUDENT ? "Registration Number" : "Faculty ID") + ".");
            model.addAttribute("secretKey", saved.getSecretKey());
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("allDomains", ALL_IT_DOMAINS);
            return "register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = getCurrentUser();
        if (user == null)
            return "redirect:/login";

        if (user.getRole() == UserRole.ADMIN) {
            model.addAllAttributes(adminService.getDashboardStats());
            model.addAttribute("pendingProjects", projectRepository.findAll().stream()
                    .filter(p -> p.getMentor() == null).toList());
            model.addAttribute("recentNotifications", notificationRepository.findByUserOrderByCreatedAtDesc(user));
            return "admin-dashboard";
        } else if (user.getRole() == UserRole.PROFESSOR) {
            Professor prof = professorService.getAllProfessors().stream()
                    .filter(p -> p.getUser().getUserId().equals(user.getUserId()))
                    .findFirst().orElse(null);
            if (prof != null) {
                model.addAttribute("pendingRequests", projectRepository.findAll().stream()
                        .filter(p -> p.getRequestedMentors() != null
                                && p.getRequestedMentors().stream()
                                        .anyMatch(rm -> rm.getProfessorId().equals(prof.getProfessorId()))
                                && p.getMentor() == null)
                        .toList());
                model.addAttribute("requestCount", projectRepository.count()); // Simplified
                model.addAttribute("menteeCount",
                        projectRepository.findByMentorProfessorId(prof.getProfessorId()).size());
            }
            return "professor-dashboard";
        } else {
            Student student = studentService.getAllStudents().stream()
                    .filter(s -> s.getUser().getUserId().equals(user.getUserId()))
                    .findFirst().orElse(null);
            if (student != null) {
                List<Project> studentProjects = projectService.getProjectsByStudent(student.getStudentId());
                model.addAttribute("projects", studentProjects);
                model.addAttribute("projectCount", studentProjects.size());
                model.addAttribute("activeProjects",
                        studentProjects.stream().filter(p -> p.getProgressPercent() < 100).count());
                model.addAttribute("notificationCount",
                        notificationRepository.findByUserOrderByCreatedAtDesc(user).size());
            }
            return "student-dashboard";
        }
    }

    @GetMapping("/projects/submit")
    public String submitProjectPage() {
        return "submit-project";
    }

    @PostMapping("/projects/submit")
    public String submitProject(@RequestParam("title") String title, @RequestParam("description") String description,
            Model model) {
        User user = getCurrentUser();
        Student student = studentService.getAllStudents().stream()
                .filter(s -> s.getUser().getUserId().equals(user.getUserId()))
                .findFirst().orElse(null);

        if (student == null) {
            model.addAttribute("error", "Only students can submit projects.");
            return "submit-project";
        }

        ProjectRequest request = new ProjectRequest(title, description, student.getStudentId());
        Project project = projectService.createProject(request);
        return "redirect:/projects/" + project.getProjectId() + "/suggestions";
    }

    @GetMapping("/projects/{id}")
    public String projectDetails(@org.springframework.web.bind.annotation.PathVariable("id") Integer id, Model model) {
        Project project = projectService.getProjectById(id).orElseThrow();
        model.addAttribute("project", project);
        model.addAttribute("milestones", projectRepository.findById(id).get().getMilestones());
        return "project-details";
    }

    @PostMapping("/projects/{id}/update")
    public String updateProgress(@org.springframework.web.bind.annotation.PathVariable("id") Integer id,
            @RequestParam("progressPercent") Integer progressPercent,
            @RequestParam("milestoneTitle") String milestoneTitle,
            @RequestParam("milestoneDescription") String milestoneDescription) {
        projectService.updateProgress(id, progressPercent, milestoneTitle, milestoneDescription);
        return "redirect:/projects/" + id;
    }

    @PostMapping("/projects/{id}/request-mentor/{mentorId}")
    public String requestMentor(@org.springframework.web.bind.annotation.PathVariable("id") Integer id,
            @org.springframework.web.bind.annotation.PathVariable("mentorId") Integer mentorId,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {
        projectService.requestMentor(id, mentorId);
        redirectAttrs.addFlashAttribute("successMessage", "Mentorship requested successfully!");
        return "redirect:/projects/" + id;
    }

    @PostMapping("/projects/{id}/delete")
    public String deleteProject(@org.springframework.web.bind.annotation.PathVariable("id") Integer id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {
        projectService.deleteProject(id);
        redirectAttrs.addFlashAttribute("successMessage", "Project deleted successfully.");
        return "redirect:/projects/my";
    }

    @GetMapping("/projects/{id}/suggestions")
    public String projectSuggestions(@org.springframework.web.bind.annotation.PathVariable("id") Integer id,
            Model model) {
        Project project = projectService.getProjectById(id).orElseThrow();
        model.addAttribute("project", project);

        // Split detected domains string into list for Thymeleaf
        List<String> domains = project.getDetectedDomains() != null
                ? List.of(project.getDetectedDomains().split(",\\s*"))
                : List.of();
        model.addAttribute("domains", domains);

        // Get AI recommendations
        List<Map<String, Object>> mentors = aiService.suggestMentors(project.getDetectedDomains());
        List<Map<String, Object>> mentorsWithDetails = new java.util.ArrayList<>();
        for (Map<String, Object> aiMentor : mentors) {
            Integer profId = (Integer) aiMentor.get("professorId");
            if (profId != null) {
                Professor prof = professorService.getProfessorById(profId).orElse(null);
                if (prof != null) {
                    Map<String, Object> map = new java.util.HashMap<>(aiMentor);
                    map.put("professor", prof);
                    map.put("menteeCount", projectRepository.findByMentorProfessorId(profId).size());
                    mentorsWithDetails.add(map);
                }
            }
        }
        model.addAttribute("mentors", mentorsWithDetails);

        return "mentor-suggestions";
    }

    @GetMapping("/mentors/suggestions")
    public String generalSuggestions(Model model) {
        User user = getCurrentUser();
        if (user == null)
            return "redirect:/login";

        Student student = studentService.getAllStudents().stream()
                .filter(s -> s.getUser().getUserId().equals(user.getUserId()))
                .findFirst().orElse(null);

        if (student != null) {
            List<Project> projects = projectService.getProjectsByStudent(student.getStudentId());
            if (!projects.isEmpty()) {
                model.addAttribute("projects", projects);
                return "mentor-project-select";
            }
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/resources")
    public String resourceLibrary(Model model) {
        model.addAttribute("resources", resourceRepository.findAll());
        model.addAttribute("domains", List.of("AI", "Web", "Mobile", "Security", "Cloud")); // Simplified
        return "resource-list";
    }

    @PostMapping("/resources/{id}/request")
    public String requestResource(@org.springframework.web.bind.annotation.PathVariable("id") Integer id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {
        User user = getCurrentUser();
        if (user != null) {
            com.oprm.entity.ResourceRequest req = new com.oprm.entity.ResourceRequest();
            req.setResource(resourceRepository.findById(id).orElseThrow());
            req.setUser(user);
            req.setRequestStatus(com.oprm.entity.enums.RequestStatus.PENDING);
            req.setRequestDate(java.time.LocalDateTime.now());
            resourceRequestService.createRequest(req);
        }
        redirectAttrs.addFlashAttribute("successMessage", "Resource access requested successfully!");
        return "redirect:/resources";
    }

    @PostMapping("/resources")
    public String addResource(@RequestParam("name") String name,
            @RequestParam("resourceType") com.oprm.entity.enums.ResourceType resourceType,
            @RequestParam("domain") String domain,
            @RequestParam("link") String link,
            @RequestParam(value = "description", required = false) String description,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {
        User user = getCurrentUser();
        if (user != null && (user.getRole() == UserRole.PROFESSOR || user.getRole() == UserRole.ADMIN)) {
            Resource res = new Resource();
            res.setName(name);
            res.setResourceType(resourceType);
            res.setDomain(domain);
            res.setLink(link);
            res.setDescription(description != null ? description : "Added by " + user.getName());
            res.setUploadedBy(user);
            resourceRepository.save(res);
            redirectAttrs.addFlashAttribute("successMessage", "Resource added successfully!");
        } else {
            redirectAttrs.addFlashAttribute("errorMessage", "You lack permission to add resources.");
        }
        // Redirect back to admin resources if admin, else generic resources
        if (user != null && user.getRole() == UserRole.ADMIN) {
            return "redirect:/admin/resources";
        }
        return "redirect:/resources";
    }

    // --- Professor Views ---

    @GetMapping("/professor/projects")
    public String professorProjects(Model model) {
        User user = getCurrentUser();
        if (user == null || user.getRole() != UserRole.PROFESSOR)
            return "redirect:/login";
        Professor prof = professorService.getAllProfessors().stream()
                .filter(p -> p.getUser().getUserId().equals(user.getUserId()))
                .findFirst().orElseThrow();

        model.addAttribute("pendingRequests", projectRepository.findAll().stream()
                .filter(p -> p.getRequestedMentors() != null
                        && p.getRequestedMentors().stream()
                                .anyMatch(rm -> rm.getProfessorId().equals(prof.getProfessorId()))
                        && p.getMentor() == null)
                .toList());
        return "professor-projects";
    }

    @GetMapping("/professor/mentees")
    public String professorMentees(Model model) {
        User user = getCurrentUser();
        if (user == null || user.getRole() != UserRole.PROFESSOR)
            return "redirect:/login";
        Professor prof = professorService.getAllProfessors().stream()
                .filter(p -> p.getUser().getUserId().equals(user.getUserId()))
                .findFirst().orElseThrow();
        model.addAttribute("mentees", projectRepository.findByMentorProfessorId(prof.getProfessorId()));
        return "professor-mentees";
    }

    @PostMapping("/professor/accept-mentorship/{projectId}")
    public String acceptMentorship(
            @org.springframework.web.bind.annotation.PathVariable("projectId") Integer projectId) {
        User user = getCurrentUser();
        Professor prof = professorService.getAllProfessors().stream()
                .filter(p -> p.getUser().getUserId().equals(user.getUserId()))
                .findFirst().orElseThrow();
        professorService.acceptMentorship(prof.getProfessorId(), projectId);
        return "redirect:/professor/mentees";
    }

    // --- Admin Views ---

    @GetMapping("/admin/users")
    public String adminUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin-user-list";
    }

    @GetMapping("/admin/projects")
    public String adminProjects(Model model) {
        model.addAttribute("projects", projectRepository.findAll());
        return "admin-project-list";
    }

    @GetMapping("/admin/resources")
    public String adminResources(Model model) {
        model.addAttribute("resources", resourceRepository.findAll());
        return "admin-resource-list";
    }

    @PostMapping("/admin/projects/{id}/approve")
    public String approveProject(@org.springframework.web.bind.annotation.PathVariable("id") Integer id) {
        adminService.approveProject(id, getCurrentUser().getEmail());
        return "redirect:/admin/projects";
    }

    // --- Student Views ---

    @GetMapping("/projects/my")
    public String myProjects(Model model) {
        User user = getCurrentUser();
        Student student = studentService.getAllStudents().stream()
                .filter(s -> s.getUser().getUserId().equals(user.getUserId()))
                .findFirst().orElse(null);
        if (student != null) {
            model.addAttribute("projects", projectService.getProjectsByStudent(student.getStudentId()));
        }
        return "student-project-list";
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        User user = getCurrentUser();
        model.addAttribute("notifications", notificationRepository.findByUserOrderByCreatedAtDesc(user));
        return "notification-list";
    }

    // ─── PROFILE ───────────────────────────────────────────────────────────────

    private static final List<String> ALL_IT_DOMAINS = Arrays.asList(
            "Machine Learning", "Artificial Intelligence", "Web Development",
            "Data Science", "Cybersecurity", "Cloud Computing",
            "Natural Language Processing", "Internet of Things",
            "Blockchain", "Computer Vision", "DevOps", "Mobile Development",
            "Database Systems", "Computer Networks", "Software Engineering",
            "Human-Computer Interaction", "Robotics", "Big Data",
            "Augmented Reality", "Quantum Computing");

    @GetMapping("/profile")
    public String profilePage(Model model) {
        User user = getCurrentUser();
        if (user == null)
            return "redirect:/login";
        model.addAttribute("user", user);
        model.addAttribute("allDomains", ALL_IT_DOMAINS);
        if (user.getRole() == UserRole.PROFESSOR) {
            Professor prof = professorService.getAllProfessors().stream()
                    .filter(p -> p.getUser().getUserId().equals(user.getUserId()))
                    .findFirst().orElse(null);
            model.addAttribute("professor", prof);
            if (prof != null && prof.getExpertiseDomains() != null) {
                List<String> selected = Arrays.asList(prof.getExpertiseDomains().split(",\\s*"));
                model.addAttribute("selectedDomains", selected);
            }
        } else if (user.getRole() == UserRole.STUDENT) {
            Student stu = studentService.getAllStudents().stream()
                    .filter(s -> s.getUser().getUserId().equals(user.getUserId()))
                    .findFirst().orElse(null);
            model.addAttribute("student", stu);
        }
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam("name") String name,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "domains", required = false) List<String> domains,
            @RequestParam(value = "semester", required = false) Integer semester,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            Model model) {
        User user = getCurrentUser();
        if (user == null)
            return "redirect:/login";

        // Update common user fields
        user.setName(name);
        if (department != null && !department.isBlank())
            user.setDepartment(department);

        // Handle optional profile photo
        if (photo != null && !photo.isEmpty()) {
            try {
                String uploadDir = "c:/oprm-system/src/main/resources/static/uploads/";
                Files.createDirectories(Paths.get(uploadDir));
                String filename = "user_" + user.getUserId() + "_" + photo.getOriginalFilename();
                Path dest = Paths.get(uploadDir + filename);
                Files.copy(photo.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
                user.setProfilePhoto("/uploads/" + filename);
            } catch (Exception e) {
                model.addAttribute("error", "Photo upload failed: " + e.getMessage());
            }
        }
        userRepository.save(user);

        // Professor-specific: update domains + re-sync CSV
        if (user.getRole() == UserRole.PROFESSOR) {
            Professor prof = professorService.getAllProfessors().stream()
                    .filter(p -> p.getUser().getUserId().equals(user.getUserId()))
                    .findFirst().orElse(null);
            if (prof != null) {
                String domainsStr = (domains != null && !domains.isEmpty())
                        ? String.join(", ", domains)
                        : "";
                prof.setExpertiseDomains(domainsStr);
                professorService.saveProfessor(prof);
                // Centralized sync
                professorService.syncProfessorCsv();
            }
        } else if (user.getRole() == UserRole.STUDENT) {
            Student stu = studentService.getAllStudents().stream()
                    .filter(s -> s.getUser().getUserId().equals(user.getUserId()))
                    .findFirst().orElse(null);
            if (stu != null && semester != null) {
                stu.setSemester(semester);
                studentRepository.save(stu);
            }
        }

        model.addAttribute("success", "Profile updated successfully!");
        return "redirect:/profile?updated";
    }

}
