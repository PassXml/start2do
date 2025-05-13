package org.start2do.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.util.TreeUtil;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class MenuTreeResp implements Cloneable, TreeUtil.TreeNode<MenuTreeResp> {

  private String label;
  private Object value;
  private String id;
  private String parentId;
  private List<MenuTreeResp> children;

  public MenuTreeResp(String id, String parentId, String label, Object value) {
    this.id = id;
    this.parentId = parentId;
    this.label = label;
    this.value = value;
    this.children = new ArrayList<>();
  }

  @Override
  public String getTreeNodeId() {
    return this.id;
  }

  @Override
  public String getParentId() {
    return this.parentId;
  }

  @Override
  public void setParentId(String id) {
    this.parentId = id;
  }

  @Override
  public void setTreeNodeId(String id) {
    this.id = id;
  }

  @Override
  public void setChildren(List<MenuTreeResp> children) {
    this.children = children;
  }

  @Override
  public List<MenuTreeResp> getChildren() {
    return this.children;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
