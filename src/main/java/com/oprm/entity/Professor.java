package com.oprm.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.oprm.entity.enums.ProfessorAvailability;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "professors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Professor {

    @Id
    private Integer professorId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "professor_id")
    @JsonIgnoreProperties({ "password", "createdAt" })
    private User user;

    @Column(unique = true)
    private String facultyId;

    @Enumerated(EnumType.STRING)
    private ProfessorAvailability availability;

    @Column(columnDefinition = "TEXT")
    private String expertiseDomains;

    private String department;
}