package com.grash.service;

import com.grash.model.Part;
import com.grash.model.PartRelation;
import com.grash.model.User;
import com.grash.model.enums.PartRelationType;
import com.grash.repository.PartRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.util.Collection;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PartRelationService {

    private final PartRelationRepository partRelationRepository;
    private final EntityManager em;

    public PartRelation create(PartRelation partRelation) {
        PartRelation saved = partRelationRepository.saveAndFlush(partRelation);
        em.refresh(saved);
        return saved;
    }

    public PartRelation createForPart(Part sourcePart, Part targetPart, PartRelationType relationType, User user) {
        PartRelation partRelation = PartRelation.builder()
                .sourcePart(sourcePart)
                .targetPart(targetPart)
                .relationType(relationType)
                .build();
        partRelation.setCompany(user.getCompany());
        return create(partRelation);
    }

    public void delete(Long id) {
        partRelationRepository.deleteById(id);
    }

    public Optional<PartRelation> findById(Long id) {
        return partRelationRepository.findById(id);
    }

    public Optional<PartRelation> findByIdAndCompany(Long id, Long companyId) {
        return partRelationRepository.findByIdAndCompany_Id(id, companyId);
    }

    public Collection<PartRelation> findForPart(Long partId, Long companyId) {
        return partRelationRepository.findForPart(partId, companyId);
    }

    /**
     * Whether a relation of the given type already links the two parts. SUBSTITUTE is
     * symmetric, so a reverse (target -> source) SUBSTITUTE row counts as the same edge;
     * RELATED is directional and only the exact (source -> target) edge is considered.
     */
    public boolean relationExists(Long sourcePartId, Long targetPartId, PartRelationType relationType, Long companyId) {
        boolean direct = partRelationRepository
                .findBySourcePart_IdAndTargetPart_IdAndRelationTypeAndCompany_Id(
                        sourcePartId, targetPartId, relationType, companyId)
                .isPresent();
        if (direct) return true;
        if (relationType == PartRelationType.SUBSTITUTE) {
            return partRelationRepository
                    .findBySourcePart_IdAndTargetPart_IdAndRelationTypeAndCompany_Id(
                            targetPartId, sourcePartId, relationType, companyId)
                    .isPresent();
        }
        return false;
    }
}
