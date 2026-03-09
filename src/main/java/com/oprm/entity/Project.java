package com.oprm.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.oprm.entity.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer projectId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String detectedDomains;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({ "user", "rollNumber", "semester" })
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mentor_id")
    @JsonIgnoreProperties({ "user", "expertiseDomains", "department" })
    private Professor mentor;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "project_requested_mentors", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "professor_id"))
    @JsonIgnoreProperties({ "user", "expertiseDomains", "department" })
    private java.util.Set<Professor> requestedMentors = new java.util.HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ProjectStatus status;

    private LocalDateTime submissionDate;

    private Integer progressPercent;

    private LocalDateTime lastUpdated;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnoreProperties("project")
    private List<ProjectMilestone> milestones = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnoreProperties("project")
    private List<Allocation> allocations = new ArrayList<>();
}