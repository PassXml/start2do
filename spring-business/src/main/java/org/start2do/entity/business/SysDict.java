package org.start2do.entity.business;

import io.ebean.annotation.Cache;
import io.ebean.annotation.DbComment;
import io.ebean.annotation.DbEnumValue;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.entity.BaseModel2;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "sys_dict")
@Cache(enableQueryCache=true)
public class SysDict extends BaseModel2 {

    @Id
    @DbComment("UUID")
    private java.util.UUID id;

    @NotNull
    @DbComment("字典名称")
    private String dictName;

    @DbComment("字典描述")
    private String dictDesc;

    @NotNull
    @DbComment("字典类型")
    private Type dictType;

    @DbComment("备注信息")
    private String dictNote;
    @OneToMany(mappedBy = "sysDict", fetch = FetchType.LAZY)
    private List<SysDictItem> items;

    public enum Type {
        SYSTEM("0", "系统"), BUSINESS("1", "业务");
        private String value;
        private String label;

        Type(String value, String label) {
            this.value = value;
            this.label = label;
        }

        @DbEnumValue(length = 2)
        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public static Type find(String s) {
            for (Type value : values()) {
                if (value.getValue().equals(s)) {
                    return value;
                }
            }
            throw new BusinessException(String.format("%s未知字典值:%s", "Type", s));


        }

        @Override
        public String toString() {
            return String.join("", label, "(", value, ")");
        }
    }
}
