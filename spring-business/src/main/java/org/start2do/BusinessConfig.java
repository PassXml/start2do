package org.start2do;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.start2do.constant.Constant;
import org.start2do.ebean.util.SysSettingUtil;
import org.start2do.util.StringUtils;

@Setter
@Getter
@Accessors(chain = true)
@ConfigurationProperties(prefix = "start2do.business")
@NoArgsConstructor
public class BusinessConfig {

  private Boolean enable;
  private boolean enableDictConvert = false;
  private SysLogConfig sysLog;
  private RateLimitConfig rateLimit;
  private Controller controller = new Controller();
  private Service service = new Service();
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
    private boolean loginLog = true;
    private boolean menu = true;
    private boolean file = true;
    private boolean setting = true;
    private boolean dict = true;

    private boolean customDict = false;
    private boolean mock = false;
  }

  @Setter
  @Getter
  @Accessors(chain = true)
  @NoArgsConstructor
  public static class Service {

    private boolean user = true;
    private boolean role = true;
    private boolean dept = true;
    private boolean log = true;
    private boolean dict = true;
    private boolean loginLog = true;
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

  @Setter
  @Getter
  @Accessors(chain = true)
  @NoArgsConstructor
  public static class RateLimitConfig {

    private boolean enable;

    /**
     *
     *
     * <pre>
     * ARGV[1]：rate，表示每秒生成 token 数量，即 token 生成速度
     * ARGV[2]：capacity，桶容量
     * ARGV[3]：now，当前请求令牌的时间戳
     * ARGV[4]：requested，当前请求 token 数量
     * KEYS[1]：访问资源的标识
     * KEYS[2]：保存上一次访问的刷新时间戳
     * fill_time：桶容量 / token 速率，即需要多少单位时间（秒）才能填满桶
     * ttl：ttl 为填满时间的 2 倍
     * last_tokens：当前时刻桶容量
     *  </pre>
     */
    private String luaScript =
        new StringBuilder()
            .append("            redis.replicate_commands()\n")
            .append("            \n")
            .append("            local tokens_key = KEYS[1]\n")
            .append("            local timestamp_key = KEYS[2]\n")
            .append("            --redis.log(redis.LOG_WARNING, \"tokens_key \" .. tokens_key)\n")
            .append("            \n")
            .append("            local rate = tonumber(ARGV[1])\n")
            .append("            local capacity = tonumber(ARGV[2])\n")
            .append("            local now = tonumber(ARGV[3])\n")
            .append("            local requested = tonumber(ARGV[4])\n")
            .append("            \n")
            .append("            local fill_time = capacity / rate\n")
            .append("            local ttl = math.floor(fill_time * 2)\n")
            .append("            \n")
            .append("            -- for testing, it should use redis system time in production\n")
            .append("            if now == nil then\n")
            .append("              now = redis.call('TIME')[1]\n")
            .append("            end\n")
            .append("            \n")
            .append("            --redis.log(redis.LOG_WARNING, \"rate \" .. ARGV[1])\n")
            .append("            --redis.log(redis.LOG_WARNING, \"capacity \" .. ARGV[2])\n")
            .append("            --redis.log(redis.LOG_WARNING, \"now \" .. now)\n")
            .append("            --redis.log(redis.LOG_WARNING, \"requested \" .. ARGV[4])\n")
            .append("            --redis.log(redis.LOG_WARNING, \"filltime \" .. fill_time)\n")
            .append("            --redis.log(redis.LOG_WARNING, \"ttl \" .. ttl)\n")
            .append("            \n")
            .append("            local last_tokens = tonumber(redis.call(\"get\", tokens_key))\n")
            .append("            if last_tokens == nil then\n")
            .append("              last_tokens = capacity\n")
            .append("            end\n")
            .append("            --redis.log(redis.LOG_WARNING, \"last_tokens \" .. last_tokens)\n")
            .append("            \n")
            .append(
                "            local last_refreshed = tonumber(redis.call(\"get\", timestamp_key))\n")
            .append("            if last_refreshed == nil then\n")
            .append("              last_refreshed = 0\n")
            .append("            end\n")
            .append(
                "            --redis.log(redis.LOG_WARNING, \"last_refreshed \" .. last_refreshed)\n")
            .append("            \n")
            .append("            local delta = math.max(0, now-last_refreshed)\n")
            .append(
                "            local filled_tokens = math.min(capacity, last_tokens+(delta*rate))\n")
            .append("            local allowed = filled_tokens >= requested\n")
            .append("            local new_tokens = filled_tokens\n")
            .append("            local allowed_num = 0\n")
            .append("            if allowed then\n")
            .append("              new_tokens = filled_tokens - requested\n")
            .append("              allowed_num = 1\n")
            .append("            end\n")
            .append("            \n")
            .append("            --redis.log(redis.LOG_WARNING, \"delta \" .. delta)\n")
            .append(
                "            --redis.log(redis.LOG_WARNING, \"filled_tokens \" .. filled_tokens)\n")
            .append("            --redis.log(redis.LOG_WARNING, \"allowed_num \" .. allowed_num)\n")
            .append("            --redis.log(redis.LOG_WARNING, \"new_tokens \" .. new_tokens)\n")
            .append("            \n")
            .append("            if ttl > 0 then\n")
            .append("              redis.call(\"setex\", tokens_key, ttl, new_tokens)\n")
            .append("              redis.call(\"setex\", timestamp_key, ttl, now)\n")
            .append("            end\n")
            .append("            \n")
            .append(
                "            -- return { allowed_num, new_tokens, capacity, filled_tokens, requested, new_tokens }\n")
            .append("            return { allowed_num, new_tokens }\n")
            .toString();
  }

  private FileSetting fileSetting = new FileSetting();

  @Setter
  @Getter
  @Accessors(chain = true)
  @NoArgsConstructor
  public static class FileSetting {

    private FileSettingType type = FileSettingType.local;
    private String uploadDir;
    private String host;

    public String getHost() {
      return SysSettingUtil.getLabel(
          Constant.TYPE_SYSTEM_SETTING, Constant.KEY_FILE_DOWNLOAD_HOST, host);
    }

    public String getUploadDir() {
      if (StringUtils.isEmpty(uploadDir)) {
        return System.getProperty("java.io.tmpdir");
      }
      return uploadDir;
    }
  }

  public enum FileSettingType {
    local,
    qn
  }

  public List<DefaultDataItem> defaultData;

  @Setter
  @Getter
  @Accessors(chain = true)
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class DefaultDataItem {
    private boolean isDict;
    private String key;
    private List<String> value;
    private String desc;

      public DefaultDataItem(boolean isDict, String key, List<String> value, String desc) {
          this.isDict = isDict;
          this.key = key;
          this.value = value;
          this.desc = desc;
      }
  }
}
