package mapTest;

import java.util.Optional;
import org.start2do.ebean.dict.DictItems;
import org.start2do.ebean.dict.IDictItem;

public enum Test implements IDictItem {
    V1("0", "v1"),
    V2("11", "v2"),
    ;

    Test(String number, String v2) {
        putItemBean(number, v2);
    }

    public static Test get(String value) {
        return find(value).orElseThrow(() -> new RuntimeException("未知字典值:" + value));
    }

    public static Optional<Test> find(String value) {
        Test result = DictItems.getByValue(Test.class, value);
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(result);
    }
}
