package com.grash.controller;

import com.grash.dto.PartRelationPostDTO;
import com.grash.dto.PartRelationShowDTO;
import com.grash.dto.SuccessResponse;
import com.grash.exception.CustomException;
import com.grash.mapper.PartRelationMapper;
import com.grash.model.Part;
import com.grash.model.PartRelation;
import com.grash.model.User;
import com.grash.model.enums.PartRelationType;
import com.grash.model.enums.PermissionEntity;
import com.grash.service.PartRelationService;
import com.grash.service.PartService;
import com.grash.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/parts/{partId}/relations")
@Tag(name = "Part Relations", description = "Operations on relations between parts")
@RequiredArgsConstructor
public class PartRelationController {

    private final PartRelationService partRelationService;
    private final PartService partService;
    private final PartRelationMapper partRelationMapper;
    private final UserService userService;

    @GetMapping("")
    @PreAuthorize("permitAll()")
    public Collection<PartRelationShowDTO> getByPart(@Parameter(description = "Part ID") @PathVariable("partId") Long partId,
                                                     HttpServletRequest req) {
        User user = userService.whoami(req);
        Part part = partService.findByIdAndCompany(partId, user.getCompany().getId())
                .orElseThrow(() -> new CustomException("Part not found", HttpStatus.NOT_FOUND));
        if (user.getRole().getViewPermissions().contains(PermissionEntity.PARTS_AND_MULTIPARTS) &&
                (user.getRole().getViewOtherPermissions().contains(PermissionEntity.PARTS_AND_MULTIPARTS)
                        || part.getCreatedBy().equals(user.getId()))) {
            return partRelationService.findForPart(partId, user.getCompany().getId()).stream()
                    .map(partRelationMapper::toShowDto).collect(Collectors.toList());
        } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public PartRelationShowDTO create(@Parameter(description = "Part ID") @PathVariable("partId") Long partId,
                                      @Parameter(description = "Relation to create") @Valid @RequestBody PartRelationPostDTO partRelationReq,
                                      HttpServletRequest req) {
        User user = userService.whoami(req);
        if (!user.getRole().getCreatePermissions().contains(PermissionEntity.PARTS_AND_MULTIPARTS))
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);

        if (partRelationReq.getTargetPart() == null || partRelationReq.getTargetPart().getId() == null)
            throw new CustomException("Target part is required", HttpStatus.NOT_ACCEPTABLE);
        Long targetPartId = partRelationReq.getTargetPart().getId();

        if (partId.equals(targetPartId))
            throw new CustomException("A part cannot be related to itself", HttpStatus.NOT_ACCEPTABLE);

        Long companyId = user.getCompany().getId();
        Part sourcePart = partService.findByIdAndCompany(partId, companyId)
                .orElseThrow(() -> new CustomException("Part not found", HttpStatus.NOT_FOUND));
        Part targetPart = partService.findByIdAndCompany(targetPartId, companyId)
                .orElseThrow(() -> new CustomException("Part not found", HttpStatus.NOT_FOUND));

        PartRelationType relationType = partRelationReq.getRelationType();
        if (partRelationService.relationExists(partId, targetPartId, relationType, companyId))
            throw new CustomException("There already is a relation between these 2 parts", HttpStatus.NOT_ACCEPTABLE);

        PartRelation saved = partRelationService.createForPart(sourcePart, targetPart, relationType, user);
        return partRelationMapper.toShowDto(saved);
    }

    @DeleteMapping("/{relationId}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public ResponseEntity<SuccessResponse> delete(@Parameter(description = "Part ID") @PathVariable("partId") Long partId,
                                                   @Parameter(description = "Relation ID") @PathVariable("relationId") Long relationId,
                                                   HttpServletRequest req) {
        User user = userService.whoami(req);
        Long companyId = user.getCompany().getId();
        Part part = partService.findByIdAndCompany(partId, companyId)
                .orElseThrow(() -> new CustomException("Part not found", HttpStatus.NOT_FOUND));
        PartRelation relation = partRelationService.findByIdAndCompany(relationId, companyId)
                .orElseThrow(() -> new CustomException("Relation not found", HttpStatus.NOT_FOUND));
        boolean involvesThisPart = relation.getSourcePart().getId().equals(partId)
                || relation.getTargetPart().getId().equals(partId);
        if (!involvesThisPart)
            throw new CustomException("Relation not found", HttpStatus.NOT_FOUND);
        if (user.getRole().getDeleteOtherPermissions().contains(PermissionEntity.PARTS_AND_MULTIPARTS)
                || part.getCreatedBy().equals(user.getId())) {
            partRelationService.delete(relation.getId());
            return new ResponseEntity<>(new SuccessResponse(true, "Deleted successfully"), HttpStatus.OK);
        } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
    }
}
