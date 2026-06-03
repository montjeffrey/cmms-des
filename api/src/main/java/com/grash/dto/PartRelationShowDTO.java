package com.grash.dto;

import com.grash.model.enums.PartRelationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "A relation between two parts")
public class PartRelationShowDTO extends AuditShowDTO {

    @Schema(description = "Type of relation")
    private PartRelationType relationType;

    @Schema(description = "The part the relation originates from")
    private PartMiniDTO sourcePart;

    @Schema(description = "The part the relation points to")
    private PartMiniDTO targetPart;
}
