package com.oprm.repository;

import com.oprm.entity.Resource;
import com.oprm.entity.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Integer> {

    List<Resource> findByDomain(String domain);

    List<Resource> findByResourceType(ResourceType resourceType);

}