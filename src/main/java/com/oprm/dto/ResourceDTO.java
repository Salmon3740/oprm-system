package com.oprm.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceDTO {
    private String name;
    private String description;
    private String resourceType;
    private String domain;
    private String link;
}
