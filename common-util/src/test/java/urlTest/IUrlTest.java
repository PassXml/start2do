package urlTest;

import org.junit.jupiter.api.Test;
import org.start2do.default_impl.IUrl;

public class IUrlTest {

    @Test
    public void test() {
        System.out.println(IUrl.getUrl("/", "/baiud/", "/abc", "/1.jpg"));
    }
}
