package org.start2do.entity.business;

import io.ebean.Model;
import io.ebean.annotation.Cache;
import io.ebean.annotation.DbComment;
import io.ebean.annotation.DbDefault;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "sys_dict_item")
@Cache(enableQueryCache=true)
public class SysDictItem extends Model {

    @DbComment("UUID")
    @Id
    private java.util.UUID id;

    @DbComment("字典项ID")
    @Column(name = "dict_id", nullable = false, length = 128)
    private UUID dictId;
    @JoinColumn(name = "dict_id",insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private SysDict sysDict;

    @DbComment("字典项名称")
    @Column(name = "item_name", length = 128, nullable = false)
    private String itemName;

    @DbComment("字典项数据项")
    @Column(name = "item_data", length = 1024, nullable = false)
    private String itemData;


    @DbComment("字典项标签")
    @Column(name = "item_tag", length = 128)
    private String itemTag;

    @DbComment("字典项备注")
    @Column(name = "item_note", length = 64)
    private String itemNote;

    @DbComment("字典项排序")
    @DbDefault("0")
    @Column(name = "item_sort", nullable = false)
    private Integer itemSort;

    @DbComment("字典项描述")
    @Column(name = "item_desc", length = 1024)
    private String itemDesc;

}
