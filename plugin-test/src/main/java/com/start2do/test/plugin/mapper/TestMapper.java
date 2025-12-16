package com.start2do.test.plugin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.start2do.plugin.api.spring.annotation.PluginMapper;

@Mapper
@PluginMapper(dataSourceId = "ds1")
public interface TestMapper {

    String version();
}
