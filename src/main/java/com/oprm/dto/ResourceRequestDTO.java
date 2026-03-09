package com.oprm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceRequestDTO {

    @NotNull(message = "Resource ID is required")
    private Integer resourceId;

    @NotNull(message = "User ID is required")
    private Integer userId;
}
