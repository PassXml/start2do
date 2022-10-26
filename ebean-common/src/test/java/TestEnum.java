import java.util.Optional;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.dict.DictItems;
import org.start2do.ebean.dict.IDictItem;

public enum TestEnum implements IDictItem {
    T1("1", "test"), T2("2", "Test");

    TestEnum(String value, String label) {
        putItemBean(value, label);
    }

    public static TestEnum get(String value) {
        return find(value).orElseThrow(() -> new BusinessException("未知字典值:" + value));
    }

    public static Optional<TestEnum> find(String value) {
        TestEnum result = DictItems.getByValue(TestEnum.class, value);
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(result);
    }
}
