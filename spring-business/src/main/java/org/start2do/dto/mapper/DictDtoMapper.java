package org.start2do.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.start2do.dto.req.dict.DictAddReq;
import org.start2do.dto.req.dict.DictUpdateReq;
import org.start2do.dto.req.dict.item.DictItemAddReq;
import org.start2do.dto.req.dict.item.DictItemUpdateReq;
import org.start2do.dto.resp.dict.DictAllResp;
import org.start2do.dto.resp.dict.DictDetailResp;
import org.start2do.dto.resp.dict.DictPageResp;
import org.start2do.dto.resp.dict.item.DictItemDetailResp;
import org.start2do.dto.resp.dict.item.DictItemPageResp;
import org.start2do.ebean.pojo.IgnoreBaseModel2;
import org.start2do.ebean.pojo.IgnoreId;
import org.start2do.entity.business.SysDict;
import org.start2do.entity.business.SysDict.Type;
import org.start2do.entity.business.SysDictItem;

@Mapper(imports = {Type.class})

public interface DictDtoMapper {

    DictDtoMapper INSTANCE = Mappers.getMapper(DictDtoMapper.class);


    @Mapping(source = "dictType.value", target = "dictType")
    @Mapping(source = "dictType.label", target = "dictTypeStr")
    DictPageResp toDictPageResp(SysDict sysDict);

    DictItemPageResp toDictPageItemResp(SysDictItem sysDictItem);

    @IgnoreId
    @Mapping(target = "sysDict", ignore = true)
    SysDictItem toDictItem(DictItemAddReq req);

    @IgnoreId
    @Mapping(target = "sysDict", ignore = true)
    void dictItemUpdate(@MappingTarget SysDictItem item, DictItemUpdateReq req);

    @IgnoreBaseModel2
    @IgnoreId
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "dictType", expression = "java(Type.find(req.getDictType()))")
    SysDict toSysDict(DictAddReq req);

    @IgnoreBaseModel2
    @IgnoreId
    @Mapping(target = "items", ignore = true)
    @Mapping(expression = "java(Type.find(req.getDictType()))", target = "dictType")
    void updateSysDict(@MappingTarget SysDict dict, DictUpdateReq req);

    @Mapping(target = "items", ignore = true)
    DictAllResp toDictAllResp(SysDict sysDict);

    DictItemDetailResp toDictItemDetailResp(SysDictItem item);


    @Mapping(source = "dictType.value", target = "dictType")
    @Mapping(source = "dictType.label", target = "dictTypeStr")
    DictDetailResp toDictDetailResp(SysDict dict);
}
