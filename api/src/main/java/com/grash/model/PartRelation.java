package com.grash.model;

import com.grash.model.abstracts.CompanyAudit;
import com.grash.model.enums.PartRelationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Schema(description = "Relation between two parts (substitute or related/accessory)")
public class PartRelation extends CompanyAudit {

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Schema(description = "Type of relation between the two parts", requiredMode = Schema.RequiredMode.REQUIRED)
    private PartRelationType relationType = PartRelationType.RELATED;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @Schema(description = "The part the relation originates from")
    private Part sourcePart;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @Schema(description = "The part the relation points to")
    private Part targetPart;
}
