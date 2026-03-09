package com.oprm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String PYTHON_CMD = "python";

    public List<String> classifyDomain(String description) {
        try {
            ProcessBuilder pb = new ProcessBuilder(PYTHON_CMD, "ai_bridge.py", "classify", description);
            pb.directory(new java.io.File("c:\\oprm-system"));
            pb.redirectErrorStream(true);
            Process p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                output.append(line);
            }

            String result = output.toString().trim();
            if (!result.isEmpty() && !result.startsWith("{\"error\"")) {
                return objectMapper.readValue(result, new TypeReference<List<String>>() {
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<Map<String, Object>> suggestMentors(String domains) {
        try {
            ProcessBuilder pb = new ProcessBuilder(PYTHON_CMD, "ai_bridge.py", "mentor", domains);
            pb.directory(new java.io.File("c:\\oprm-system"));
            pb.redirectErrorStream(true);
            Process p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                output.append(line);
            }
            String result = output.toString().trim();
            if (!result.isEmpty() && !result.startsWith("{\"error\"")) {
                return objectMapper.readValue(result, new TypeReference<List<Map<String, Object>>>() {
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Map<String, Object> getUnifiedSuggestions(String description) {
        try {
            ProcessBuilder pb = new ProcessBuilder(PYTHON_CMD, "ai_bridge.py", "suggest_all", description);
            pb.directory(new java.io.File("c:\\oprm-system"));
            pb.redirectErrorStream(true);
            Process p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                output.append(line);
            }
            String result = output.toString().trim();
            if (!result.isEmpty() && !result.startsWith("{\"error\"")) {
                return objectMapper.readValue(result, new TypeReference<Map<String, Object>>() {
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Map.of();
    }
}
