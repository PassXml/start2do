package org.start2do.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.start2do.dto.resp.permission.PermissionUserPageResp;
import org.start2do.entity.security.SysPermission;

@Mapper
public interface PermissionDtoMapper {
    PermissionDtoMapper INSTANCE = Mappers.getMapper(PermissionDtoMapper.class);



    PermissionUserPageResp toPermissionPageResp(SysPermission permission);

}
