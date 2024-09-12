import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;
import org.start2do.util.BeanValidatorUtil;
import org.start2do.util.validator.list.ValidListItem;
import org.start2do.util.validator.notPattern.NotHasEmoji;

public class Test1 {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestPojo {

        @NotEmpty
//        @InArray(value = {"1", "2"})
//        @NotHasPattern(checkNull = false, value = "[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]")
        @NotHasEmoji(ignoreNull = true)
        private String name;

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void test() {
//        BeanValidatorUtil.validate(new TestPojo());
    }

    @Test
    public void test2() {
//        System.out.println("111");
//        TestPojo pojo = new TestPojo();
//        pojo.setName("\uD83D\uDE19");
//        try {
//            BeanValidatorUtil.validate(pojo);
//        } catch (ValidateException e) {
//            Assertions.assertTrue(false);
//            return;
//        }
//        Assertions.assertTrue(true);

//        String reviewerName = "😀😁😂😃😄😅😆8888";
//        String replaceAll = reviewerName.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "");
//        System.out.println("repalceAll"+replaceAll);
    }

    @Test
    void test3() {
        Item item = new Item().setName("ok").setList(List.of(new Item2()));
        BeanValidatorUtil.validate(item);

    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    static class Item {

        private String name;
        @ValidListItem(message = "list字段")
        private List<Item2> list;
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    static class Item2 {

        @NotEmpty
        private String name;
    }
}
