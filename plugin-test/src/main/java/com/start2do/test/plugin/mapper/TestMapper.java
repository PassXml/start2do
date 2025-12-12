package com.start2do.test.plugin.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestMapper {

    String version();
}
