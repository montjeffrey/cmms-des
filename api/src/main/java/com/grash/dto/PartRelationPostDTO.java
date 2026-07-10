package com.grash.dto;

import com.grash.model.enums.PartRelationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Schema(description = "Payload for relating a part to another part")
public class PartRelationPostDTO {

    @Schema(description = "The other part to relate to")
    @NotNull
    private IdDTO targetPart;

    @NotNull
    @Schema(description = "Type of relation", requiredMode = Schema.RequiredMode.REQUIRED)
    private PartRelationType relationType = PartRelationType.RELATED;
}
