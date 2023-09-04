import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
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
}
