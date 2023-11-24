package org.start2do;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.start2do.util.StringUtils;

@Setter
@Getter
@Accessors(chain = true)
@ConfigurationProperties(prefix = "start2do.business")
@NoArgsConstructor
public class BusinessConfig {

    private Boolean enable;
    private SysLogConfig sysLog;
    private Controller controller = new Controller();

    private String dateTimePattern = "yyyy-MM-dd HH:mm:ss";
    private String datePattern = "yyyy-MM-dd";

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Controller {

        private boolean user = true;
        private boolean role = true;
        private boolean dept = true;
        private boolean log = true;
        private boolean menu = true;
        private boolean file = true;

    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class SysLogConfig {

        private boolean enable;

    }

    private FileSetting fileSetting;

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class FileSetting {

        private FileSettingType type = FileSettingType.local;
        private String uploadDir;
        private String host;

        public String getUploadDir() {
            if (StringUtils.isEmpty(uploadDir)) {
                return System.getProperty("java.io.tmpdir");
            }
            return uploadDir;
        }
    }

    public enum FileSettingType {
        local
    }
}
