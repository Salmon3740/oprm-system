package com.oprm.repository;

import com.oprm.entity.Notification;
import com.oprm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserUserId(Integer userId);

    List<Notification> findByUserOrderByCreatedAtDesc(User user);
}