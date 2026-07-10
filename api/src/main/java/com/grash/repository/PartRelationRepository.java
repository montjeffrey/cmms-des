package com.grash.repository;

import com.grash.model.PartRelation;
import com.grash.model.enums.PartRelationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface PartRelationRepository extends JpaRepository<PartRelation, Long> {

    Optional<PartRelation> findByIdAndCompany_Id(Long id, Long companyId);

    /**
     * All relations involving a part: every directional edge it starts (sourcePart),
     * plus every SUBSTITUTE edge that points at it (targetPart) since substitution is
     * bidirectional and stored as a single row.
     */
    @Query("SELECT pr FROM PartRelation pr WHERE pr.company.id = :companyId " +
            "AND (pr.sourcePart.id = :partId " +
            "OR (pr.targetPart.id = :partId " +
            "AND pr.relationType = com.grash.model.enums.PartRelationType.SUBSTITUTE))")
    Collection<PartRelation> findForPart(@Param("partId") Long partId, @Param("companyId") Long companyId);

    Optional<PartRelation> findBySourcePart_IdAndTargetPart_IdAndRelationTypeAndCompany_Id(
            Long sourcePartId, Long targetPartId, PartRelationType relationType, Long companyId);
}
