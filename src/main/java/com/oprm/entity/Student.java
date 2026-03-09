package com.oprm.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Student {

    @Id
    private Integer studentId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({ "password", "createdAt" })
    private User user;

    @Column(unique = true)
    private String registrationNumber;

    private Integer semester;
}