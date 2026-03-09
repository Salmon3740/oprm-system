package com.oprm.controller;

import com.oprm.dto.ResourceRequestDTO;
import com.oprm.entity.Resource;
import com.oprm.entity.ResourceRequest;
import com.oprm.entity.User;
import com.oprm.entity.enums.RequestStatus;
import com.oprm.repository.ResourceRepository;
import com.oprm.repository.UserRepository;
import com.oprm.service.LogService;
import com.oprm.service.ResourceRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/resources/request")
public class ResourceRequestController {

    private final ResourceRequestService requestService;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final LogService logService;

    public ResourceRequestController(ResourceRequestService requestService,
            ResourceRepository resourceRepository,
            UserRepository userRepository,
            LogService logService) {
        this.requestService = requestService;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.logService = logService;
    }

    /**
     * POST /api/resources/request
     * Body: { "resourceId": 1, "userId": 2 }
     */
    @PostMapping
    public ResponseEntity<ResourceRequest> requestResource(@Valid @RequestBody ResourceRequestDTO dto,
            Authentication auth) {
        Resource resource = resourceRepository.findById(dto.getResourceId())
                .orElseThrow(() -> new RuntimeException("Resource not found with id: " + dto.getResourceId()));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

        ResourceRequest request = ResourceRequest.builder()
                .resource(resource)
                .user(user)
                .requestStatus(RequestStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .build();

        ResourceRequest saved = requestService.createRequest(request);

        logService.logAction("RESOURCE_REQUESTED",
                "User " + user.getEmail() + " requested resource: " + resource.getName(), user);

        return ResponseEntity.ok(saved);
    }

    /**
     * GET /api/resources/request — list all requests (admin/professor)
     */
    @GetMapping
    public ResponseEntity<List<ResourceRequest>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    /**
     * GET /api/resources/request/user/{userId} — requests by user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ResourceRequest>> getUserRequests(@PathVariable Integer userId) {
        return ResponseEntity.ok(requestService.getRequestsByUser(userId));
    }
}