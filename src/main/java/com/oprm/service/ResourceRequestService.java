package com.oprm.service;

import com.oprm.entity.ResourceRequest;
import com.oprm.entity.enums.RequestStatus;
import com.oprm.repository.ResourceRequestRepository;
import com.oprm.service.LogService;
import com.oprm.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceRequestService {

    private final ResourceRequestRepository requestRepository;
    private final NotificationService notificationService;
    private final LogService logService;

    public ResourceRequestService(ResourceRequestRepository requestRepository,
            NotificationService notificationService,
            LogService logService) {
        this.requestRepository = requestRepository;
        this.notificationService = notificationService;
        this.logService = logService;
    }

    public ResourceRequest createRequest(ResourceRequest request) {
        ResourceRequest saved = requestRepository.save(request);
        notificationService.createNotification(request.getUser(),
                "You requested access to resource: " + request.getResource().getName(), "RESOURCE_REQUESTED");
        logService.logAction("RESOURCE_REQUESTED",
                "Requested access to resource: " + request.getResource().getName(), request.getUser());
        return saved;
    }

    public List<ResourceRequest> getRequestsByUser(Integer userId) {
        return requestRepository.findByUserUserId(userId);
    }

    public List<ResourceRequest> getRequestsByStatus(RequestStatus status) {
        return requestRepository.findByRequestStatus(status);
    }

    public List<ResourceRequest> getAllRequests() {
        return requestRepository.findAll();
    }
}