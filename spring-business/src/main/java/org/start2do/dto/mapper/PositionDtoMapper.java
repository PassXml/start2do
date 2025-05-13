package org.start2do.dto.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import org.start2do.dto.req.position.PositionAddReq;
import org.start2do.dto.req.position.PositionUpdateReq;
import org.start2do.dto.resp.position.PositionDetailResp;
import org.start2do.dto.resp.position.PositionPageResp;
import org.start2do.entity.security.SysPositionEntity;

@Mapper(componentModel = "spring")
public interface PositionDtoMapper {
    PositionDtoMapper INSTANCE = Mappers.getMapper(PositionDtoMapper.class);

    SysPositionEntity toEntity(PositionAddReq req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget SysPositionEntity entity, PositionUpdateReq req);

    PositionPageResp toPositionPageResp(SysPositionEntity entity);

    PositionDetailResp toPositionDetailResp(SysPositionEntity entity);
}
