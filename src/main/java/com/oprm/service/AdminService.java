package com.oprm.service;

import com.oprm.entity.Allocation;
import com.oprm.entity.Project;
import com.oprm.entity.ResourceRequest;
import com.oprm.entity.enums.ProjectStatus;
import com.oprm.entity.enums.RequestStatus;
import com.oprm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

        private final UserRepository userRepository;
        private final ProjectRepository projectRepository;
        private final ResourceRepository resourceRepository;
        private final ResourceRequestRepository resourceRequestRepository;
        private final AllocationRepository allocationRepository;
        private final NotificationService notificationService;
        private final LogService logService;

        public Map<String, Object> getDashboardStats() {
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalUsers", userRepository.count());
                stats.put("totalProjects", projectRepository.count());
                stats.put("totalResources", resourceRepository.count());

                long activeMentorships = projectRepository.findAll().stream()
                                .filter(p -> p.getMentor() != null &&
                                                (p.getStatus() == ProjectStatus.MENTOR_ASSIGNED ||
                                                                p.getStatus() == ProjectStatus.IN_PROGRESS))
                                .count();
                stats.put("activeMentorships", activeMentorships);

                long completedProjects = projectRepository.findAll().stream()
                                .filter(p -> p.getStatus() == ProjectStatus.COMPLETED)
                                .count();
                stats.put("completedProjects", completedProjects);

                long pendingProjects = projectRepository.findAll().stream()
                                .filter(p -> p.getStatus() == ProjectStatus.SUBMITTED ||
                                                p.getStatus() == ProjectStatus.MENTOR_PENDING)
                                .count();
                stats.put("pendingProjects", pendingProjects);

                return stats;
        }

        @Transactional
        public Project approveProject(Integer projectId, String adminEmail) {
                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

                project.setStatus(ProjectStatus.MENTOR_PENDING);
                project.setLastUpdated(LocalDateTime.now());
                Project saved = projectRepository.save(project);

                notificationService.createNotification(project.getStudent().getUser(),
                                "Your project '" + project.getTitle()
                                                + "' has been approved! A mentor will be assigned soon.",
                                "PROJECT_APPROVED");

                logService.logAction("PROJECT_APPROVED",
                                "Admin approved project: " + project.getTitle(),
                                project.getStudent().getUser());

                return saved;
        }

        @Transactional
        public Project rejectProject(Integer projectId, String reason) {
                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

                project.setStatus(ProjectStatus.SUBMITTED);
                project.setLastUpdated(LocalDateTime.now());
                Project saved = projectRepository.save(project);

                String msg = "Your project '" + project.getTitle() + "' was not approved." +
                                (reason != null && !reason.isBlank() ? " Reason: " + reason : "");

                notificationService.createNotification(project.getStudent().getUser(), msg, "PROJECT_REJECTED");

                logService.logAction("PROJECT_REJECTED",
                                "Admin rejected project: " + project.getTitle()
                                                + (reason != null ? " Reason: " + reason : ""),
                                project.getStudent().getUser());

                return saved;
        }

        @Transactional
        public ResourceRequest approveResourceRequest(Integer requestId) {
                ResourceRequest request = resourceRequestRepository.findById(requestId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Resource request not found with id: " + requestId));

                request.setRequestStatus(RequestStatus.APPROVED);
                ResourceRequest saved = resourceRequestRepository.save(request);

                Project project = projectRepository.findAll().stream()
                                .filter(p -> p.getStudent() != null &&
                                                p.getStudent().getUser().getUserId()
                                                                .equals(request.getUser().getUserId())
                                                &&
                                                (p.getStatus() == ProjectStatus.IN_PROGRESS
                                                                || p.getStatus() == ProjectStatus.MENTOR_ASSIGNED))
                                .findFirst().orElse(null);

                if (project != null) {
                        Allocation allocation = Allocation.builder()
                                        .project(project)
                                        .resource(request.getResource())
                                        .allocationDate(LocalDateTime.now())
                                        .build();
                        allocationRepository.save(allocation);
                }

                notificationService.createNotification(request.getUser(),
                                "Your request for resource '" + request.getResource().getName()
                                                + "' has been approved!",
                                "RESOURCE_ALLOCATED");

                logService.logAction("RESOURCE_REQUEST_APPROVED",
                                "Admin approved resource request #" + requestId + " for resource: "
                                                + request.getResource().getName(),
                                request.getUser());

                return saved;
        }

        @Transactional
        public ResourceRequest rejectResourceRequest(Integer requestId, String reason) {
                ResourceRequest request = resourceRequestRepository.findById(requestId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Resource request not found with id: " + requestId));

                request.setRequestStatus(RequestStatus.REJECTED);
                ResourceRequest saved = resourceRequestRepository.save(request);

                String msg = "Your request for resource '" + request.getResource().getName() + "' was rejected." +
                                (reason != null && !reason.isBlank() ? " Reason: " + reason : "");

                notificationService.createNotification(request.getUser(), msg, "RESOURCE_REJECTED");

                logService.logAction("RESOURCE_REQUEST_REJECTED",
                                "Admin rejected resource request #" + requestId,
                                request.getUser());

                return saved;
        }
}
