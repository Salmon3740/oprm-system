package com.oprm.entity;

import com.oprm.entity.enums.ResourceType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resourceId;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    private String domain;

    @Column(columnDefinition = "TEXT")
    private String link;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;
}