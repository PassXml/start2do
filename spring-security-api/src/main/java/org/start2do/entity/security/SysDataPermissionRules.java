package org.start2do.entity.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.ebean.annotation.DbComment;
import io.ebean.annotation.DbDefault;
import java.io.Serializable;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.constant.DBConstant;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.dict.DictItems;
import org.start2do.ebean.dict.IDictItem;
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.entity.BaseModel2;
import org.start2do.ebean.id_generators.SnowflakeStrGenerator;
import org.start2do.util.StringUtils;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "sys_data_permission_rules")
public class SysDataPermissionRules extends BaseModel2 implements Serializable {

    @Id
    @GeneratedValue(generator = SnowflakeStrGenerator.KEY)
    private String id;
    @Column(length = DBConstant.TITLE_LENGTH)
    private String ruleName;
    private Type type;
    @Column(length = DBConstant.ADDRESS_LENGTH)
    private String tableName;
    @DbComment("字段列表(逗号分隔)")
    @Column(length = DBConstant.NOTE_LENGTH)
    private String columnList;
    @Lob
    @Column
    @DbComment("条件表达式")
    private String conditionExpression;
    @DbDefault("1")
    private EnableType enable;

    public enum Type implements IDictItem {
        Table("1", "表"), Row("2", "行"), Column("3", "列"),
        ;

        Type(String value, String label) {
            putItemBean(value, label);
        }

        @JsonCreator
        public static Type get(String value) {
            if (StringUtils.isEmpty(value)) {
                return null;
            }
            return find(value).orElseThrow(() -> new BusinessException("未知字典值:" + value));
        }

        public static Optional<Type> find(String value) {
            Type result = DictItems.getByValue(Type.class, value);
            if (result == null) {
                return Optional.empty();
            }
            return Optional.of(result);
        }
    }


}
