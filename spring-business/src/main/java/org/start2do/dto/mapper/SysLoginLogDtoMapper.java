package org.start2do.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.start2do.dto.resp.log.SysLogPageResp;
import org.start2do.ebean.dict.IDictItem;
import org.start2do.entity.security.SysLoginLog;

@Mapper
public interface SysLoginLogDtoMapper {

    SysLoginLogDtoMapper INSTANCE = Mappers.getMapper(SysLoginLogDtoMapper.class);

    default String toString(IDictItem item) {
        return item.value();
    }


    SysLogPageResp toSysLogPageResp(SysLoginLog sysLoginLog);

}
