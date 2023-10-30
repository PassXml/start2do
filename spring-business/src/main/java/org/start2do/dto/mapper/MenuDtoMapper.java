package org.start2do.dto.mapper;

import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.start2do.dto.req.menu.MenuAddReq;
import org.start2do.dto.req.menu.MenuUpdateReq;
import org.start2do.dto.resp.menu.MenuDetailResp;
import org.start2do.dto.resp.menu.MenuPageResp;
import org.start2do.ebean.pojo.IgnoreBaseModel2;
import org.start2do.ebean.pojo.IgnoreId;
import org.start2do.entity.security.SysMenu;
import org.start2do.entity.security.SysUser;

@org.mapstruct.Mapper(imports = {SysUser.Status.class, SysMenu.Type.class})
public interface MenuDtoMapper {

    MenuDtoMapper INSTANCE = Mappers.getMapper(MenuDtoMapper.class);

    @Mapping(source = "type.value", target = "type")
    @Mapping(source = "type.label", target = "typeStr")
    @Mapping(source = "status.label", target = "statusStr")
    @Mapping(source = "status.value", target = "status")
    @Mapping(source = "isShow.value", target = "isShow")
    @Mapping(source = "isShow.label", target = "isShowStr")
    MenuPageResp toMenuPageResp(SysMenu sysMenu);

    @IgnoreBaseModel2
    @IgnoreId
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "type", expression = "java(Type.find(req.getType()))")
    @Mapping(target = "roles", ignore = true)
    SysMenu toEntity(MenuAddReq req);

    @IgnoreId
    @IgnoreBaseModel2
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "type", expression = "java(Type.find(req.getType()))")
    void update(@MappingTarget SysMenu menu, MenuUpdateReq req);

    @Mapping(source = "type.value", target = "type")
    @Mapping(source = "type.label", target = "typeStr")
    @Mapping(source = "status.label", target = "statusStr")
    @Mapping(source = "status.value", target = "status")
    @Mapping(source = "isShow.value", target = "isShow")
    @Mapping(source = "isShow.label", target = "isShowStr")
    MenuDetailResp toMenuDetailResp(SysMenu menu);

}
