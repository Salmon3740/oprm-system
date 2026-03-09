package com.oprm.controller;

import com.oprm.service.MentorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;

    @GetMapping("/suggest")
    public ResponseEntity<Map<String, Object>> getSuggest(@RequestParam("description") String description) {
        return ResponseEntity.ok(mentorService.getRecommendedMentors(description));
    }
}