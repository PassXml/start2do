import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.start2do.util.EnumUtil;
import org.start2do.util.EnumUtil.IEnum;

class EnumUtilTest {

    @org.junit.jupiter.api.Test
    public void test() {
        List<Test> enums = List.of(
            Test.H1, Test.H2
        );
        String string = EnumUtil.enum2code("000", enums);
        Assertions.assertEquals(string, "110");
    }

    @Getter
    @AllArgsConstructor
    enum Test implements IEnum {
        H1("010"), H2("100"), H3("001");

        private String code;

    }


}
