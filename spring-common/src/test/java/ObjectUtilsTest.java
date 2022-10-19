import java.util.Arrays;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.start2do.util.ObjectUtils;
import org.start2do.util.ObjectUtils.Pairs;

public class ObjectUtilsTest {

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public class C1 {

        private Integer integer;

        public C1(Integer integer) {
            this.integer = integer;
        }
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public class C2 {

        private String string;

        public C2(String string) {
            this.string = string;
        }
    }

    @Test
    public void test1() {
        C1 c1 = new C1(1);
        C1 c2 = new C1(1);
        Boolean eq = ObjectUtils.eq(c1, c2, true, Arrays.asList(
                new Pairs<>(C1::getInteger, C1::getInteger, Integer::equals)
            )
        );
        Assertions.assertTrue(eq);
    }

    @Test
    public void test2() {
        C1 c1 = new C1(1);
        C2 c2 = new C2("1");
        Boolean eq = ObjectUtils.eq(c1, c2, true, Arrays.asList(
                new Pairs<>(C1::getInteger, C2::getString, (v1, v2) -> v1.toString().equals(v2))
            )
        );
        Assertions.assertTrue(eq);
    }

    @Test
    public void test3() {
        C1 c1 = new C1(1);
        Boolean eq = ObjectUtils.eq(c1, null, true, Arrays.asList(
                new Pairs<>(C1::getInteger, C2::getString, (v1, v2) -> v1.toString().equals(v2))
            )
        );
        Assertions.assertFalse(eq);
    }

    @Test
    public void test4() {
        Boolean eq = ObjectUtils.eq(null, null, true, Arrays.asList(
                new Pairs<>(C1::getInteger, C2::getString, (v1, v2) -> v1.toString().equals(v2))
            )
        );
        Assertions.assertTrue(eq);
    }

    @Test
    public void test5() {
        C1 c1 = new C1(1);
        Boolean eq = ObjectUtils.eq(c1, null, false, Arrays.asList(
                new Pairs<>(C1::getInteger, C2::getString, (v1, v2) -> v1.toString().equals(v2))
            )
        );
        Assertions.assertTrue(eq);
    }

    @Test
    public void test6() {
        C1 c1 = new C1(1);
        Boolean eq = ObjectUtils.eq(null, null, false, Arrays.asList(
                new Pairs<>(C1::getInteger, C2::getString, (v1, v2) -> v1.toString().equals(v2))
            )
        );
        Assertions.assertTrue(eq);
    }

    @Test
    public void test7() {
        C2 c1 = new C2("1");
        Boolean eq = ObjectUtils.eq(null, c1, false, Arrays.asList(
                new Pairs<>(C1::getInteger, C2::getString, (v1, v2) -> v1.toString().equals(v2))
            )
        );
        Assertions.assertTrue(eq);
    }
}
