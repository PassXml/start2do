package org.start2do.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.start2do.dto.req.setting.SettingAddReq;
import org.start2do.dto.req.setting.SettingUpdateReq;
import org.start2do.dto.resp.setting.SettingDetailResp;
import org.start2do.dto.resp.setting.SettingMenuResp;
import org.start2do.dto.resp.setting.SettingPageResp;
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.entity.SysSetting;
import org.start2do.ebean.pojo.IgnoreBaseModel2;
import org.start2do.ebean.pojo.IgnoreId;

@Mapper(imports = EnableType.class)
public interface SettingDtoMapper {

    SettingDtoMapper INSTANCE = Mappers.getMapper(SettingDtoMapper.class);

    @Mapping(target = "enable", source = "enable.value")
    @Mapping(target = "enableStr", source = "enable.label")
    SettingPageResp toSettingResp(SysSetting sysSetting);

    @IgnoreBaseModel2
    @IgnoreId
    SysSetting toEntity(SettingAddReq req);

    @IgnoreBaseModel2
    @IgnoreId

    void update(@MappingTarget SysSetting setting, SettingUpdateReq req);

    @Mapping(target = "enableStr", source = "enable.label")
    @Mapping(target = "enable", source = "enable.value")
    SettingDetailResp toDetail(SysSetting req);

    SettingMenuResp toSettingMenuResp(SysSetting sysSetting);

}
