package com.oprm.repository;

import com.oprm.entity.ChatMessage;
import com.oprm.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    List<ChatMessage> findByProjectOrderByTimestampAsc(Project project);
}
