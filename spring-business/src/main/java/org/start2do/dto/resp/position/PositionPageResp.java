package org.start2do.dto.resp.position;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.start2do.ebean.dto.EnableType;
import org.start2do.util.EnumUtil;
import org.start2do.util.TreeUtil.TreeNode;

@Data
public class PositionPageResp implements TreeNode<PositionPageResp>, Cloneable {
  private String id;
  private String parentId;
  private String name;
  private String code;
  private EnableType status;
  private Integer sort;
  private String sourceType;
  private String sourceId;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private List<PositionPageResp> children;

  @Override
  public String getTreeNodeId() {
    return id;
  }

  @Override
  public void setTreeNodeId(String id) {
    this.id = id;
  }

  @Override
  public void setChildren(List<PositionPageResp> children) {
    this.children = children;
  }

  @Override
  public List<PositionPageResp> getChildren() {
    if (children == null) {
      this.children = new ArrayList<PositionPageResp>();
    }
    return this.children;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public String getStatusStr() {
    return EnumUtil.toStr(this.status);
  }
}
