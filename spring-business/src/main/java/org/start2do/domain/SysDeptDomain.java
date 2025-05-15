package org.start2do.domain;

import java.util.ArrayList;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.start2do.ebean.util.EntityHook;
import org.start2do.entity.security.SysDept;
import org.start2do.entity.security.query.QSysDept;
import org.start2do.util.TreeUtil;
import org.start2do.util.TreeUtil.TreeBaseDto;

@Component
public class SysDeptDomain implements EntityHook<SysDept> {

  @Cacheable(value = "dept", key = "#id + '_child'")
  public List<String> findAllChildId(String id) {
    List<TreeBaseDto> list =
        TreeUtil.generateTreesNoMiss(
            new QSysDept()
                .findList().stream()
                    .map(t -> new TreeBaseDto(t.getId(), t.getParentId(), new ArrayList<>()))
                    .toList());
    return TreeUtil.findNode(list, id).getAllChildrenId();
  }

  @Override
  public Class<SysDept> getKey() {
    return SysDept.class;
  }
}
