package com.oprm.repository;

import com.oprm.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Integer> {
    List<Allocation> findByProjectProjectId(Integer projectId);
}
