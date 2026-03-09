package com.oprm.service;

import com.oprm.entity.FacultyExpertise;
import com.oprm.repository.FacultyExpertiseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacultyExpertiseService {

    private final FacultyExpertiseRepository expertiseRepository;

    public FacultyExpertiseService(FacultyExpertiseRepository expertiseRepository) {
        this.expertiseRepository = expertiseRepository;
    }

    public FacultyExpertise addExpertise(FacultyExpertise expertise) {
        return expertiseRepository.save(expertise);
    }

    public List<FacultyExpertise> getExpertiseByProfessor(Integer professorId) {
        return expertiseRepository.findByProfessorProfessorId(professorId);
    }

    public List<FacultyExpertise> getAllExpertise() {
        return expertiseRepository.findAll();
    }
}