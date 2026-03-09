package com.oprm.repository;

import com.oprm.entity.ResourceRequest;
import com.oprm.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRequestRepository extends JpaRepository<ResourceRequest, Integer> {

    List<ResourceRequest> findByUserUserId(Integer userId);

    List<ResourceRequest> findByRequestStatus(RequestStatus status);

}