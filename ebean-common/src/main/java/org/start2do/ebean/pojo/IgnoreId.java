package org.start2do.ebean.pojo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Retention(RetentionPolicy.CLASS)
@Mappings(value = {
    @Mapping(target = "id", ignore = true),
})
public @interface IgnoreId {

}
