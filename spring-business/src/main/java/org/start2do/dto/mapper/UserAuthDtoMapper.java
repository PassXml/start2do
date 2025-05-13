package org.start2do.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.start2do.dto.resp.userauth.UserAuthDetailResp;
import org.start2do.dto.resp.userauth.UserAuthPageResp;
import org.start2do.entity.security.SysUserAuth;

@Mapper
public interface UserAuthDtoMapper {
  UserAuthDtoMapper INSTANCE = Mappers.getMapper(UserAuthDtoMapper.class);

  @Mapping(source = "user.username", target = "username")
  @Mapping(source = "user.realName", target = "realName")
  UserAuthPageResp toUserAuthPageResp(SysUserAuth entity);

  UserAuthDetailResp toUserAuthDetailResp(SysUserAuth entity);
}
