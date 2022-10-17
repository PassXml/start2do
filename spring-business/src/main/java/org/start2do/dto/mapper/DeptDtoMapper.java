package org.start2do.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.start2do.dto.req.dept.DeptAddReq;
import org.start2do.dto.req.dept.DeptUpdateReq;
import org.start2do.dto.resp.dept.DeptDetailResp;
import org.start2do.dto.resp.dept.DeptPageResp;
import org.start2do.ebean.pojo.IgnoreBaseModel2;
import org.start2do.ebean.pojo.IgnoreId;
import org.start2do.entity.security.SysDept;

@Mapper

public interface DeptDtoMapper {

    DeptDtoMapper INSTANCE = Mappers.getMapper(DeptDtoMapper.class);


    DeptPageResp toDeptPageResp(SysDept sysDept);

    @IgnoreId
    @IgnoreBaseModel2
    @Mapping(target = "parent", ignore = true)
    SysDept toEntity(DeptAddReq req);

    @IgnoreId
    @IgnoreBaseModel2
    @Mapping(target = "parent", ignore = true)
    void update(@MappingTarget SysDept dept, DeptUpdateReq req);

    DeptDetailResp toDeptDetailResp(SysDept byId);
}
