package org.start2do.dto.mapper;

import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.start2do.dto.req.user.UserAddReq;
import org.start2do.dto.req.user.UserUpdateReq;
import org.start2do.dto.resp.user.UserDetailResp;
import org.start2do.dto.resp.user.UserDetailResp.Item;
import org.start2do.dto.resp.user.UserPageResp;
import org.start2do.ebean.pojo.IgnoreBaseModel2;
import org.start2do.entity.security.SysDept;
import org.start2do.entity.security.SysUser;

@org.mapstruct.Mapper(imports = {SysUser.Status.class})
public interface UserDtoMapper {

    UserDtoMapper INSTANCE = Mappers.getMapper(UserDtoMapper.class);

    @Mapping(target = "statusStr", source = "status.label")
    @Mapping(target = "status", source = "status.value")
    @Mapping(target = "deptId", source = "mainDept.id")
    @Mapping(target = "deptName", source = "mainDept.name")
    UserPageResp toUserPageResp(SysUser sysUser);

    @IgnoreBaseModel2
    @Mapping(target = "status", expression = "java(Status.find(req.getStatus()))")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "dept", ignore = true)
    @Mapping(target = "menus", ignore = true)
    @Mapping(target = "id", ignore = true)
    SysUser  toEntity(UserAddReq req);

    @IgnoreBaseModel2
    @Mapping(target = "status", expression = "java(Status.find(req.getStatus()))")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "dept", ignore = true)
    @Mapping(target = "menus", ignore = true)
    @Mapping(target = "password", ignore = true)
    void update(@MappingTarget SysUser user, UserUpdateReq req);

    @Mapping(target = "menus", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(source = "status.label", target = "statusStr")
    @Mapping(source = "status.value", target = "status")
    @Mapping(target = "rolesInfo", ignore = true)
    @Mapping(source = "mainDept.id", target = "deptId")
    @Mapping(source = "mainDept.name", target = "deptName")
    @Mapping(expression = "java(user.getDept().stream().filter(t->!t.getId().equals(user.getMainDept().getId())).map(UserDtoMapper.INSTANCE::toItem).toList())", target = "depts")
    UserDetailResp toUserDetailResp(SysUser user);

    default UserDetailResp.Item toItem(SysDept dept) {
        return new Item(dept.getId(), dept.getName());
    }
}
