package start2do;

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;
import org.start2do.util.spring.UtilAutoConfig;

public class OMTest {


    @Test
    public void test() throws JsonProcessingException {
        TestItem item = new TestItem("123", new TestItem("456"));
        System.out.println(UtilAutoConfig.jacksonOM().writeValueAsString(item));
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class TestItem {

        private String id;
        @ManyToOne
        private TestItem testItem;

        public TestItem(String id) {
            this.id = id;
        }

        public TestItem(String id, TestItem testItem) {
            this.id = id;
            this.testItem = testItem;
        }

    }

}
