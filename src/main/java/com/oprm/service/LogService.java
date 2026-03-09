package com.oprm.service;

import com.oprm.entity.Log;
import com.oprm.entity.User;
import com.oprm.repository.LogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void logAction(String action, String details, User user) {
        Log log = Log.builder()
                .action(action)
                .details(details)
                .user(user)
                .timestamp(LocalDateTime.now())
                .build();
        logRepository.save(log);
    }
}
