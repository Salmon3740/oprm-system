package com.oprm.config;

import com.oprm.entity.*;
import com.oprm.entity.enums.*;
import com.oprm.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import com.oprm.service.ProfessorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final ProjectRepository projectRepository;
    private final AllocationRepository allocationRepository;
    private final NotificationRepository notificationRepository;
    private final LogRepository logRepository;
    private final FacultyExpertiseRepository facultyExpertiseRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceRequestRepository resourceRequestRepository;
    private final ProjectArchiveRepository projectArchiveRepository;
    private final ProjectMilestoneRepository projectMilestoneRepository;
    private final ProfessorService professorService;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting database seeding with DataFaker...");
        Faker faker = new Faker();
        Random random = new Random();
        String defaultPassword = passwordEncoder.encode("password123");

        // 1. Ensure at least 1 Admin
        if (userRepository.findByRole(UserRole.ADMIN).isEmpty()) {
            User admin = User.builder()
                    .name(faker.name().fullName())
                    .email("admin_" + java.util.UUID.randomUUID().toString().substring(0, 8) + "@oprm.edu")
                    .password(defaultPassword)
                    .role(UserRole.ADMIN)
                    .department("Administration")
                    .secretKey(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase())
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(admin);
        }

        // 2. Ensure at least 10 Professors
        if (professorRepository.count() < 10) {
            log.info("Seeding Professors...");
            String[] itDomains = { "Machine Learning", "Artificial Intelligence", "Web Development", "Data Science",
                    "Cybersecurity", "Cloud Computing", "Natural Language Processing", "Internet of Things",
                    "Blockchain", "Computer Vision" };
            for (int i = 0; i < 10; i++) {
                User pUser = User.builder()
                        .name(faker.name().fullName())
                        .email(faker.internet().emailAddress(faker.name().firstName().toLowerCase() + ".prof"))
                        .password(defaultPassword)
                        .role(UserRole.PROFESSOR)
                        .department("Computer Science")
                        .secretKey(
                                java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase())
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(pUser);
                professorRepository.save(Professor.builder()
                        .user(pUser)
                        .facultyId("FAC-" + pUser.getUserId() + "-" + faker.number().digits(4))
                        .availability(ProfessorAvailability.Available)
                        .expertiseDomains(itDomains[random.nextInt(itDomains.length)] + ", "
                                + itDomains[random.nextInt(itDomains.length)])
                        .department(pUser.getDepartment())
                        .build());
            }
        }

        // 3. Ensure at least 20 Students
        if (studentRepository.count() < 20) {
            log.info("Seeding Students...");
            for (int i = 0; i < 20; i++) {
                User sUser = User.builder()
                        .name(faker.name().fullName())
                        .email(faker.internet().emailAddress(faker.name().firstName().toLowerCase() + ".stu"))
                        .password(defaultPassword)
                        .role(UserRole.STUDENT)
                        .department(faker.educator().course())
                        .secretKey(
                                java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase())
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(sUser);
                studentRepository.save(Student.builder()
                        .user(sUser)
                        .registrationNumber("REG" + faker.number().digits(6) + random.nextInt(10000))
                        .semester(random.nextInt(8) + 1)
                        .build());
            }
        }

        List<User> allUsers = userRepository.findAll();
        List<Student> allStudents = studentRepository.findAll();
        List<Professor> allProfessors = professorRepository.findAll();

        // 4. Ensure at least 30 Projects
        if (projectRepository.count() < 30 && !allStudents.isEmpty() && !allProfessors.isEmpty()) {
            log.info("Seeding Projects...");
            for (int i = 0; i < 30; i++) {
                Student student = allStudents.get(random.nextInt(allStudents.size()));
                Professor mentor = allProfessors.get(random.nextInt(allProfessors.size()));

                projectRepository.save(Project.builder()
                        .title(faker.company().catchPhrase())
                        .description(faker.lorem().paragraph())
                        .detectedDomains(faker.job().field())
                        .student(student)
                        .mentor(mentor)
                        .status(ProjectStatus.values()[random.nextInt(ProjectStatus.values().length)])
                        .submissionDate(LocalDateTime.now().plusDays(random.nextInt(30)))
                        .progressPercent(random.nextInt(101))
                        .lastUpdated(LocalDateTime.now().minusDays(random.nextInt(10)))
                        .build());
            }
        }

        List<Project> allProjects = projectRepository.findAll();

        // 5. Ensure at least 20 Allocations
        if (allocationRepository.count() < 20 && !allProjects.isEmpty()) {
            log.info("Seeding Allocations...");
            for (Project project : allProjects) {
                if (random.nextBoolean()) {
                    allocationRepository.save(Allocation.builder()
                            .project(project)
                            .resource(null)
                            .allocationDate(LocalDateTime.now().minusDays(random.nextInt(20)))
                            .build());
                }
            }
        }

        // 6. Ensure at least 20 Resources
        if (resourceRepository.count() < 20) {
            log.info("Seeding Resources...");
            for (int i = 0; i < 20; i++) {
                resourceRepository.save(Resource.builder()
                        .name(faker.commerce().productName())
                        .description(faker.commerce().material())
                        .resourceType(ResourceType.values()[random.nextInt(ResourceType.values().length)])
                        .domain(faker.job().field())
                        .link("https://example.com/resource/" + i)
                        .uploadedBy(allUsers.get(random.nextInt(allUsers.size())))
                        .build());
            }
        }

        List<Resource> allResources = resourceRepository.findAll();

        // 7. Ensure at least 20 Resource Requests
        if (resourceRequestRepository.count() < 20 && !allUsers.isEmpty() && !allResources.isEmpty()) {
            log.info("Seeding Resource Requests...");
            for (int i = 0; i < 20; i++) {
                User randomUser = allUsers.get(random.nextInt(allUsers.size()));
                Resource randomResource = allResources.get(random.nextInt(allResources.size()));
                resourceRequestRepository.save(ResourceRequest.builder()
                        .user(randomUser)
                        .resource(randomResource)
                        .requestStatus(RequestStatus.values()[random.nextInt(RequestStatus.values().length)])
                        .requestDate(LocalDateTime.now().minusDays(random.nextInt(15)))
                        .build());
            }
        }

        // 8. Ensure at least 30 Milestones
        if (projectMilestoneRepository.count() < 30 && !allProjects.isEmpty()) {
            log.info("Seeding Milestones...");
            for (Project project : allProjects) {
                projectMilestoneRepository.save(ProjectMilestone.builder()
                        .project(project)
                        .milestoneTitle(faker.company().buzzword() + " Phase")
                        .description(faker.lorem().sentence())
                        .status(MilestoneStatus.values()[random.nextInt(MilestoneStatus.values().length)])
                        .updatedAt(LocalDateTime.now().minusDays(random.nextInt(10)))
                        .build());
            }
        }

        // 9. Ensure at least 30 Notifications
        if (notificationRepository.count() < 30 && !allUsers.isEmpty()) {
            log.info("Seeding Notifications...");
            for (User user : allUsers) {
                notificationRepository.save(Notification.builder()
                        .user(user)
                        .message(faker.lorem().sentence())
                        .isRead(random.nextBoolean())
                        .type(random.nextBoolean() ? "INFO" : "ALERT")
                        .createdAt(LocalDateTime.now().minusDays(random.nextInt(10)))
                        .build());
            }
        }

        // 10. Ensure at least 30 Logs
        if (logRepository.count() < 30 && !allUsers.isEmpty()) {
            log.info("Seeding Logs...");
            for (int i = 0; i < 30; i++) {
                logRepository.save(Log.builder()
                        .action(faker.hacker().verb() + " " + faker.hacker().noun())
                        .details(faker.lorem().sentence())
                        .timestamp(LocalDateTime.now().minusHours(random.nextInt(100)))
                        .user(allUsers.get(random.nextInt(allUsers.size())))
                        .build());
            }
        }

        List<Professor> existingProfessors = professorRepository.findAll();
        String[] itDomains = { "Machine Learning", "Artificial Intelligence", "Web Development", "Data Science",
                "Cybersecurity", "Cloud Computing", "Natural Language Processing", "Internet of Things", "Blockchain",
                "Computer Vision" };
        for (Professor p : existingProfessors) {
            p.setExpertiseDomains(
                    itDomains[random.nextInt(itDomains.length)] + ", " + itDomains[random.nextInt(itDomains.length)]);
            if (p.getFacultyId() == null || p.getFacultyId().isBlank()) {
                p.setFacultyId("FAC-" + p.getUser().getUserId() + "-" + random.nextInt(10000));
            }
            professorRepository.save(p);
        }

        log.info("Syncing Professor Dataset for AI...");
        professorService.syncProfessorCsv();

        log.info("Database seeding verification completed successfully.");
    }
}
