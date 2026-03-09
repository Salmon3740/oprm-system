package com.oprm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "faculty_expertise")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultyExpertise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer expertiseId;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;

    private String domain;
}