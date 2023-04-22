package org.start2do.dto;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.start2do.dto.req.HttpLogPageReq;
import org.start2do.dto.resp.HttpLogPageResp;
import org.start2do.entity.HttpLog;

@Mapper
public interface HttpLogDtoMapper {

    HttpLogDtoMapper INSTANCE = Mappers.getMapper(HttpLogDtoMapper.class);


    SqlPagePojo toSqlPojo(HttpLogPageReq req);

    HttpLogPageResp toHttpLogPageResp(HttpLog httpLog);

}
