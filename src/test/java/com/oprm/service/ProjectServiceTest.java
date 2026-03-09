package com.oprm.service;

import com.oprm.dto.ProjectRequest;
import com.oprm.entity.*;
import com.oprm.entity.enums.ProjectStatus;
import com.oprm.entity.enums.UserRole;
import com.oprm.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AIService aiService;
    @Mock
    private LogService logService;
    @Mock
    private ProjectArchiveService archiveService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ProjectMilestoneRepository milestoneRepository;

    @InjectMocks
    private ProjectService projectService;

    private User testUser;
    private Student testStudent;
    private Project testProject;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1)
                .name("Student One")
                .email("student@test.com")
                .role(UserRole.STUDENT)
                .build();

        testStudent = Student.builder()
                .studentId(1)
                .user(testUser)
                .registrationNumber("REG-1")
                .semester(2)
                .build();

        testProject = Project.builder()
                .projectId(1)
                .title("ML Project")
                .description("A machine learning project")
                .student(testStudent)
                .status(ProjectStatus.SUBMITTED)
                .detectedDomains("Machine Learning, Data Science")
                .build();
    }

    @Test
    void createProject_ShouldClassifyDomainAndSave() {
        ProjectRequest request = new ProjectRequest("ML Project", "A machine learning project about neural networks",
                1);
        when(studentRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(aiService.classifyDomain(anyString())).thenReturn(List.of("Machine Learning", "AI", "Data Science"));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        Project result = projectService.createProject(request);

        assertNotNull(result);
        verify(aiService, times(1)).classifyDomain(anyString());
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(logService, times(1)).logAction(eq("PROJECT_CREATED"), anyString(), any(User.class));
        verify(notificationService, times(1)).createNotification(any(User.class), anyString(), anyString());
    }

    @Test
    void createProject_ShouldThrowWhenStudentNotFound() {
        ProjectRequest request = new ProjectRequest("Test", "Test desc", 999);
        when(studentRepository.findById(999)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> projectService.createProject(request));
    }

    @Test
    void submitProject_ShouldChangeStatusToSubmitted() {
        when(projectRepository.findById(1)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        Project result = projectService.submitProject(1);

        assertNotNull(result);
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(logService, times(1)).logAction(eq("PROJECT_SUBMITTED"), anyString(), any(User.class));
    }

    @Test
    void updateProjectStatus_ShouldArchiveWhenCompleted() {
        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        when(projectRepository.findById(1)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        Project result = projectService.updateProjectStatus(1, ProjectStatus.COMPLETED);

        verify(archiveService, times(1)).archiveProject(any(ProjectArchive.class));
        verify(logService, times(1)).logAction(eq("PROJECT_ARCHIVED"), anyString(), any(User.class));
    }

    @Test
    void updateProgress_ShouldUpdatePercentAndCreateMilestone() {
        when(projectRepository.findById(1)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        Project result = projectService.updateProgress(1, 50, "MVP Done", "Completed the MVP");

        verify(projectRepository).save(any(Project.class));
        verify(milestoneRepository, times(1)).save(any(ProjectMilestone.class));
        verify(logService, times(1)).logAction(eq("PROJECT_PROGRESS_UPDATED"), anyString(), any(User.class));
    }
}
