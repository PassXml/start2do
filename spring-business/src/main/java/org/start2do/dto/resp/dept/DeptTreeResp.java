package org.start2do.dto.resp.dept;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.start2do.util.TreeUtil;

@Setter
@Getter
@NoArgsConstructor
public class DeptTreeResp implements TreeUtil.TreeNode<DeptTreeResp> {

  private String id;
  private String parentId;
  private Integer sort;
  private String name;
  private String deptCode;
  private List<DeptTreeResp> children;

  @Override
  public String getTreeNodeId() {
    return id;
  }

  @Override
  public void setTreeNodeId(String id) {
    this.id = id;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
