package org.start2do.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.start2do.dto.Page;
import org.start2do.dto.SqlPagePojo;
import org.start2do.entity.HttpLog;

@Mapper
public interface HttpLogMapper {

    List<HttpLog> findAllBodyJson(@Param("pojo") SqlPagePojo pojo, @Param("page") Page page);
    Long count(@Param("pojo") SqlPagePojo pojo);
}
