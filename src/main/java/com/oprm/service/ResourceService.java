package com.oprm.service;

import com.oprm.entity.Resource;
import com.oprm.entity.enums.ResourceType;
import com.oprm.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public Resource addResource(Resource resource) {
        return resourceRepository.save(resource);
    }

    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    public List<Resource> getResourcesByDomain(String domain) {
        return resourceRepository.findByDomain(domain);
    }

    public List<Resource> getResourcesByType(ResourceType type) {
        return resourceRepository.findByResourceType(type);
    }
}