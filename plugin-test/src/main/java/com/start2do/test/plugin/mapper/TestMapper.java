package com.start2do.test.plugin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.start2do.plugin.api.spring.annotation.PluginMapper;

@PluginMapper(dataSourceId = "test")
@Mapper
public interface TestMapper {

    String version();
}
