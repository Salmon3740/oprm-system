package com.oprm.entity;

import com.oprm.entity.enums.MilestoneStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_milestones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer milestoneId;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    private String milestoneTitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private MilestoneStatus status;

    private LocalDateTime updatedAt;
}