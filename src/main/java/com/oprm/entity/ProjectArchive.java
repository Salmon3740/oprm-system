package com.oprm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_archive")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer archiveId;

    @OneToOne
    @JoinColumn(name = "project_id")
    private Project project;

    private String finalReportLink;

    private String githubLink;

    private LocalDateTime archivedAt;
}