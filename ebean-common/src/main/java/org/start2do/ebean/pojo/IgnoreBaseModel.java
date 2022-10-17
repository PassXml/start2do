package org.start2do.ebean.pojo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Retention(RetentionPolicy.CLASS)
@Mappings(value = {
    @Mapping(target = "createTime", ignore = true),
    @Mapping(target = "createPerson", ignore = true),
    @Mapping(target = "updateTime", ignore = true),
    @Mapping(target = "updatePerson", ignore = true),
    @Mapping(target = "version", ignore = true),
    @Mapping(target = "isDelete", ignore = true)
})
public @interface IgnoreBaseModel {

}
