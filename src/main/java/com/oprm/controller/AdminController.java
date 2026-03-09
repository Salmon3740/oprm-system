package com.oprm.controller;

import com.oprm.entity.Project;
import com.oprm.entity.Resource;
import com.oprm.entity.ResourceRequest;
import com.oprm.entity.User;
import com.oprm.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ResourceService resourceService;
    private final AdminService adminService;
    private final ProjectService projectService;
    private final ResourceRequestService resourceRequestService;

    public AdminController(UserService userService,
            ResourceService resourceService,
            AdminService adminService,
            ProjectService projectService,
            ResourceRequestService resourceRequestService) {
        this.userService = userService;
        this.resourceService = resourceService;
        this.adminService = adminService;
        this.projectService = projectService;
        this.resourceRequestService = resourceRequestService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(users);
    }

    @GetMapping("/projects")
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @PostMapping("/resources")
    public ResponseEntity<Resource> addResource(@RequestBody Resource resource) {
        return ResponseEntity.ok(resourceService.addResource(resource));
    }

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getReports() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @PutMapping("/projects/{id}/approve")
    public ResponseEntity<Project> approveProject(@PathVariable Integer id, Authentication auth) {
        String adminEmail = auth.getName();
        return ResponseEntity.ok(adminService.approveProject(id, adminEmail));
    }

    @PutMapping("/projects/{id}/reject")
    public ResponseEntity<Project> rejectProject(@PathVariable Integer id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(adminService.rejectProject(id, reason));
    }

    @GetMapping("/resources/requests")
    public ResponseEntity<List<ResourceRequest>> getAllResourceRequests() {
        return ResponseEntity.ok(resourceRequestService.getAllRequests());
    }

    @PutMapping("/resources/requests/{id}/approve")
    public ResponseEntity<ResourceRequest> approveResourceRequest(@PathVariable Integer id) {
        return ResponseEntity.ok(adminService.approveResourceRequest(id));
    }

    @PutMapping("/resources/requests/{id}/reject")
    public ResponseEntity<ResourceRequest> rejectResourceRequest(@PathVariable Integer id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(adminService.rejectResourceRequest(id, reason));
    }
}