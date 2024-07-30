import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.start2do.util.StringUtils;

public class StringUtilTest {

    @Test
    public void test() {
        String input = "someString?nodeId=12345&anotherParam=value";
        String prefix = "nodeId=";
        String suffix = "&";
        String nodeId = StringUtils.extractNodeId(input, prefix, suffix);
        Assertions.assertEquals("12345", nodeId);
    }

    @Test
    public void test2() {
        String input = "http://192.168.30.172:9000/api/video/gb28181/control/ptz?serial=34020000001320000001&code=34020000001320000001&command=stop&speed=129&nodeId=4";
        String prefix = "nodeId=";
        String suffix = "&";
        String nodeId = StringUtils.extractNodeId(input, prefix, suffix);
        Assertions.assertEquals("4", nodeId);
    }
}
