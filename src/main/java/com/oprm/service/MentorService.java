package com.oprm.service;

import com.oprm.entity.Professor;
import com.oprm.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorService {

    private final AIService aiService;
    private final ProfessorRepository professorRepository;

    public Map<String, Object> getRecommendedMentors(String description) {
        return aiService.getUnifiedSuggestions(description);
    }

    public List<Professor> getMentorsByDomains(List<String> domains) {
        // Fallback or manual matching if AI bridge is offline
        List<Professor> allProfessors = professorRepository.findAll();
        return allProfessors.stream()
                .filter(p -> {
                    for (String domain : domains) {
                        if (p.getExpertiseDomains().toLowerCase().contains(domain.toLowerCase())) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }
}
