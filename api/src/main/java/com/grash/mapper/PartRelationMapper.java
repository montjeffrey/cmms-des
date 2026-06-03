package com.grash.mapper;

import com.grash.dto.PartRelationShowDTO;
import com.grash.model.PartRelation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PartMapper.class})
public interface PartRelationMapper {
    PartRelationShowDTO toShowDto(PartRelation model);
}
