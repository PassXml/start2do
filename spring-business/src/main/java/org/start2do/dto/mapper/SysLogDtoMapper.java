package org.start2do.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.start2do.dto.resp.log.LogPageResp;
import org.start2do.entity.business.SysLog;

@Mapper
public interface SysLogDtoMapper {

    SysLogDtoMapper INSTANCE = Mappers.getMapper(SysLogDtoMapper.class);


    LogPageResp LogPageResp(SysLog sysLog);
}
