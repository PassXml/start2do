package org.start2do.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.MappedStatement.Builder;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.scripting.xmltags.XMLScriptBuilder;
import org.apache.ibatis.session.Configuration;
import org.start2do.dto.BusinessException;
import org.start2do.util.BeanUtils.SFunction;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


@Slf4j
@UtilityClass
public class MyBatisUtil {

    private final Configuration configuration = new Configuration();
    private DocumentBuilder documentBuilder;
    private Cache<String, MappedStatement> cache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES)
        .build();

    static {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        try {
            documentBuilder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public BoundSql parseDynamicXML(String xmlSQL, Map<String, Object> parameters) {
        //解析成xml
        MappedStatement ms = null;
        try {
            ms = cache.get("Mybatis:XML:" + xmlSQL.hashCode(), () -> {
                Document doc = parseXMLDocument(xmlSQL);
                if (doc == null) {
                    return null;
                }
                //走mybatis 流程 parse成Xnode
                XNode xNode = new XNode(new XPathParser(doc, false), doc.getFirstChild(), null);
                // 之前的所有步骤 都是为了构建 XMLScriptBuilder 对象,
                XMLScriptBuilder xmlScriptBuilder = new XMLScriptBuilder(configuration, xNode);
                //解析 静态xml 和动态的xml
                SqlSource sqlSource = xmlScriptBuilder.parseScriptNode();
                return new Builder(configuration, UUID.randomUUID().toString(), sqlSource, null).build();
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        //将原始sql 与 参数绑定
        return ms.getBoundSql(parameters);
    }

    private Document parseXMLDocument(String xmlString) {
        if (StringUtils.isBlank(xmlString)) {
            log.error("动态解析的xmlString 不能为空!!");
            throw new BusinessException("动态解析的xmlString 不能为空!!");
        }
        try {
            return documentBuilder.parse(new InputSource(new StringReader(xmlString)));
        } catch (Exception e) {
            log.error("XML解析异常,请检查XML格式是否正确,errMsg:{}", e.getMessage());
            throw new BusinessException(e.getMessage());
        }

    }

    /**
     * xml生成
     */
    public static class XMLBuilder {

        private List<Select> select = new ArrayList<>();
        private String table;
        private List<ICondition> where = new ArrayList<>();
        private List<Select> groupBy = new ArrayList<>();

        public XMLBuilder(String table) {
            this.table = table;
        }

        public XMLBuilder select(String... property) {
            for (String string : property) {
                select.add(new Select(string));
            }
            return this;
        }

        public <T> XMLBuilder select(SFunction<T>... property) {
            for (SFunction<T> function : property) {
                select.add(new Select<T>(function));
            }
            return this;
        }

        public XMLBuilder where(ICondition... condition) {
            where.addAll(Arrays.asList(condition));
            return this;
        }

        public XMLBuilder groupBy(String... property) {
            for (String item : property) {
                boolean found = false;
                for (Select i : select) {
                    if (item.equals(i.getProperty())) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    groupBy.add(new Select(item));
                }
            }
            return this;
        }

        public MyBatisUtil.XMLBuilder groupBy(Select... property) {
            for (Select item : property) {
                boolean found = false;
                for (Select i : select) {
                    if (StringUtils.isNotEmpty(i.getAsLabel())) {
                        if (i.getAsLabel().equals(item.getAsLabel())) {
                            found = true;
                            break;
                        }
                    }
                    if (item.getProperty().equals(i.getProperty())) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    groupBy.add(item);
                }
            }
            return this;
        }

        public String toXMl() {
            StringJoiner joiner = new StringJoiner("");
            joiner.add("<select> select ");

            for (int i = 0; i < select.size(); i++) {
                Select item = select.get(i);
                if (i != 0) {
                    joiner.add(",");
                }
                if (StringUtils.isNotEmpty(item.getAsLabel())) {
                    joiner.add(item.getProperty() + " " + item.getAsLabel());
                } else {
                    joiner.add(item.getProperty());
                }
            }
            joiner.add(" from " + table);
            if (!where.isEmpty()) {
                joiner.add(" <where> ");
                for (ICondition condition : where) {
                    joiner.add(condition.toString());
                }
                joiner.add(" </where> ");
            }
            if (!groupBy.isEmpty()) {
                joiner.add(" group by ");
                for (int i = 0; i < groupBy.size(); i++) {
                    Select item = groupBy.get(i);
                    if (i != 0) {
                        joiner.add(",");
                    }
                    if (StringUtils.isNotEmpty(item.getAsLabel())) {
                        joiner.add(item.getProperty() + " " + item.getAsLabel());
                    } else {
                        joiner.add(item.getProperty());
                    }
                }
            }
            joiner.add(" </select>");
            return joiner.toString();
        }


        public <T> XMLBuilder groupBy(SFunction<T>... sFunctions) {
            for (SFunction<T> sFunction : sFunctions) {
                groupBy(BeanUtils.getFieldName(sFunction));
            }
            return this;
        }
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Select<T> {

        /**
         * 字段名称
         */
        private String property;
        private String asLabel;


        public Select(String property) {
            this.property = property;
        }

        public Select(SFunction<T> property) {
            this.property = BeanUtils.getFieldName(property);
        }

        public Select(String value, String asLabel) {
            this.property = value;
            this.asLabel = asLabel;
        }
    }

    public interface ICondition {

        String toString();
    }

    public static class StringCondition implements ICondition {

        private String str;

        public StringCondition(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return this.str;
        }
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Condition<T, S> implements ICondition {

        private boolean isIf = false;
        @NonNull
        private String property;
        @NonNull
        private String valueName;
        private boolean checkNull = false;
        private boolean checkEmpty = false;
        private ConditionType conditionType = ConditionType.AND;
        @NonNull
        private Type type;

        public Condition(@NonNull BeanUtils.SFunction<T> property, @NonNull BeanUtils.SFunction<S> valueName,
            @NonNull Type type) {
            this.property = BeanUtils.getFieldName(property);
            this.valueName = BeanUtils.getFieldName(valueName);
            this.type = type;
        }

        public Condition(@NonNull String property, @NonNull String valueName, @NonNull Type type) {
            this.property = property;
            this.valueName = valueName;
            this.type = type;
        }

        public enum ConditionType {
            AND, OR
        }

        public enum Type {
            EQ, NE, GE, LE, GT, LT, LIKE, IS_NULL, IS_NOT_NULL
        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner("");
            if (isIf) {
                joiner.add(" <if ");
                if (checkNull && !checkEmpty) {
                    joiner.add("test=\"").add(valueName).add("!=null\"");
                } else if (!checkNull && checkEmpty) {
                    joiner.add("test=\"").add(valueName).add("!=''\"");
                } else if (checkNull && checkEmpty) {
                    joiner.add("test=\"").add(valueName).add("!=null and ").add(property).add("!=''\"");
                }
                joiner.add(">");
            }
            if (conditionType != null) {
                joiner.add(conditionType.name().toLowerCase()).add(" ");
            }
            joiner.add(property);
            switch (type) {
                case EQ -> joiner.add(" = ").add("#{").add(valueName).add("}");
                case NE -> joiner.add(" != ").add("#{").add(valueName).add("}");
                case GE -> joiner.add(" >= ").add("#{").add(valueName).add("}");
                case LE -> joiner.add(" <= ").add("#{").add(valueName).add("}");
                case GT -> joiner.add(" > ").add("#{").add(valueName).add("}");
                case LT -> joiner.add(" < ").add("#{").add(valueName).add("}");
                case LIKE -> joiner.add(" like ").add("concat('%',#{").add(valueName).add("},'%')");
                case IS_NULL -> joiner.add(" is null ");
                case IS_NOT_NULL -> joiner.add(" is not null ");
            }
            if (isIf) {
                joiner.add(" </if> ");
            }
            return joiner.toString();
        }
    }


}
