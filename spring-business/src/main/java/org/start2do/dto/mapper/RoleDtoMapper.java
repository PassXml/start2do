package org.start2do.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.start2do.dto.req.role.RoleAddReq;
import org.start2do.dto.req.role.RoleUpdateReq;
import org.start2do.dto.resp.role.RoleDetailResp;
import org.start2do.dto.resp.role.RolePageResp;
import org.start2do.ebean.pojo.IgnoreBaseModel2;
import org.start2do.ebean.pojo.IgnoreId;
import org.start2do.entity.security.SysRole;

@Mapper
public interface RoleDtoMapper {

    RoleDtoMapper INSTANCE = Mappers.getMapper(RoleDtoMapper.class);

    RolePageResp toRolePageResp(SysRole sysRole);

    @IgnoreBaseModel2
    @IgnoreId
    @Mapping(target = "menus", ignore = true)
    @Mapping(target = "users", ignore = true)
    SysRole toEntity(RoleAddReq req);

    @IgnoreId
    @IgnoreBaseModel2
    @Mapping(target = "menus", ignore = true)
    @Mapping(target = "users", ignore = true)
    void update(@MappingTarget SysRole role, RoleUpdateReq req);


    RoleDetailResp toRoleDetailResp(SysRole role);
}
