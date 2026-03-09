package com.oprm.controller;

import com.oprm.entity.ChatMessage;
import com.oprm.entity.User;
import com.oprm.service.ChatService;
import com.oprm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Integer projectId) {
        return ResponseEntity.ok(chatService.getChatHistory(projectId));
    }

    @GetMapping("/my-projects")
    public ResponseEntity<List<com.oprm.entity.Project>> getMyProjects(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        if (user.getRole() == com.oprm.entity.enums.UserRole.PROFESSOR) {
            return ResponseEntity.ok(chatService.getProfessorProjects(user));
        } else {
            return ResponseEntity.ok(chatService.getStudentProjects(user));
        }
    }

    @PostMapping("/project/{projectId}/text")
    public ResponseEntity<ChatMessage> sendText(
            @PathVariable Integer projectId,
            @RequestParam String content,
            Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(chatService.sendTextMessage(projectId, user, content));
    }

    @PostMapping("/project/{projectId}/resource")
    public ResponseEntity<ChatMessage> shareResource(
            @PathVariable Integer projectId,
            @RequestParam Integer resourceId,
            Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(chatService.shareResource(projectId, user, resourceId));
    }

    @PostMapping("/project/{projectId}/file")
    public ResponseEntity<ChatMessage> shareFile(
            @PathVariable Integer projectId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(chatService.shareFile(projectId, user, file));
    }
}
