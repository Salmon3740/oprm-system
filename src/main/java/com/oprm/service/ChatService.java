package com.oprm.service;

import com.oprm.entity.ChatMessage;
import com.oprm.entity.Project;
import com.oprm.entity.Resource;
import com.oprm.entity.User;
import com.oprm.entity.enums.MessageType;
import com.oprm.repository.ChatMessageRepository;
import com.oprm.repository.ProjectRepository;
import com.oprm.repository.ResourceRepository;
import com.oprm.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

        private final ChatMessageRepository chatMessageRepository;
        private final ProjectRepository projectRepository;
        private final ResourceRepository resourceRepository;
        private final ProfessorRepository professorRepository;
        private final NotificationService notificationService;

        private final String UPLOAD_DIR = "c:/oprm-system/src/main/resources/static/uploads/chat/";

        public List<ChatMessage> getChatHistory(Integer projectId) {
                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Project not found"));
                return chatMessageRepository.findByProjectOrderByTimestampAsc(project);
        }

        public ChatMessage sendTextMessage(Integer projectId, User sender, String content) {
                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Project not found"));

                ChatMessage message = ChatMessage.builder()
                                .project(project)
                                .sender(sender)
                                .content(content)
                                .type(MessageType.TEXT)
                                .build();

                ChatMessage savedMessage = chatMessageRepository.save(message);
                notifyRecipient(project, sender, "New message: " + content);
                return savedMessage;
        }

        public ChatMessage shareResource(Integer projectId, User sender, Integer resourceId) {
                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Project not found"));
                Resource resource = resourceRepository.findById(resourceId)
                                .orElseThrow(() -> new RuntimeException("Resource not found"));

                ChatMessage message = ChatMessage.builder()
                                .project(project)
                                .sender(sender)
                                .content("Shared a resource: " + resource.getName())
                                .type(MessageType.RESOURCE)
                                .resource(resource)
                                .build();

                ChatMessage savedMessage = chatMessageRepository.save(message);
                notifyRecipient(project, sender, "Shared a resource: " + resource.getName());
                return savedMessage;
        }

        public ChatMessage shareFile(Integer projectId, User sender, MultipartFile file) {
                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Project not found"));

                try {
                        Files.createDirectories(Paths.get(UPLOAD_DIR));
                        String originalFilename = file.getOriginalFilename();
                        String filename = UUID.randomUUID().toString() + "_" + originalFilename;
                        Path dest = Paths.get(UPLOAD_DIR + filename);
                        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

                        ChatMessage message = ChatMessage.builder()
                                        .project(project)
                                        .sender(sender)
                                        .content("Shared a file: " + originalFilename)
                                        .type(MessageType.FILE)
                                        .attachmentPath("/uploads/chat/" + filename)
                                        .build();

                        ChatMessage savedMessage = chatMessageRepository.save(message);
                        notifyRecipient(project, sender, "Shared a file: " + originalFilename);
                        return savedMessage;
                } catch (Exception e) {
                        throw new RuntimeException("Failed to upload file", e);
                }
        }

        private void notifyRecipient(Project project, User sender, String content) {
                User recipient = null;
                User studentUser = project.getStudent().getUser();
                User mentorUser = (project.getMentor() != null) ? project.getMentor().getUser() : null;

                if (studentUser.getUserId().equals(sender.getUserId())) {
                        recipient = mentorUser;
                } else if (mentorUser != null && mentorUser.getUserId().equals(sender.getUserId())) {
                        recipient = studentUser;
                }

                if (recipient != null) {
                        notificationService.createNotification(
                                        recipient,
                                        "New message from " + sender.getName() + " in project '" + project.getTitle()
                                                        + "': " + content,
                                        "NEW_MESSAGE");
                }
        }

        public List<Project> getProfessorProjects(User professorUser) {
                return professorRepository.findByUser(professorUser)
                                .map(prof -> projectRepository.findByMentorProfessorId(prof.getProfessorId()))
                                .orElse(Collections.emptyList());
        }

        public List<Project> getStudentProjects(User studentUser) {
                return projectRepository.findAll().stream()
                                .filter(p -> p.getStudent().getUser().getUserId().equals(studentUser.getUserId()))
                                .toList();
        }
}
