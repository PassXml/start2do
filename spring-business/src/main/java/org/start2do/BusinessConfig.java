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
    private RateLimitConfig rateLimit;
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
        private boolean setting = true;
        private boolean customDict = false;
        private boolean mock = false;

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
        private String luaScript = """
            redis.replicate_commands()
            
            local tokens_key = KEYS[1]
            local timestamp_key = KEYS[2]
            --redis.log(redis.LOG_WARNING, "tokens_key " .. tokens_key)
            
            local rate = tonumber(ARGV[1])
            local capacity = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local requested = tonumber(ARGV[4])
            
            local fill_time = capacity / rate
            local ttl = math.floor(fill_time * 2)
            
            -- for testing, it should use redis system time in production
            if now == nil then
              now = redis.call('TIME')[1]
            end
            
            --redis.log(redis.LOG_WARNING, "rate " .. ARGV[1])
            --redis.log(redis.LOG_WARNING, "capacity " .. ARGV[2])
            --redis.log(redis.LOG_WARNING, "now " .. now)
            --redis.log(redis.LOG_WARNING, "requested " .. ARGV[4])
            --redis.log(redis.LOG_WARNING, "filltime " .. fill_time)
            --redis.log(redis.LOG_WARNING, "ttl " .. ttl)
            
            local last_tokens = tonumber(redis.call("get", tokens_key))
            if last_tokens == nil then
              last_tokens = capacity
            end
            --redis.log(redis.LOG_WARNING, "last_tokens " .. last_tokens)
            
            local last_refreshed = tonumber(redis.call("get", timestamp_key))
            if last_refreshed == nil then
              last_refreshed = 0
            end
            --redis.log(redis.LOG_WARNING, "last_refreshed " .. last_refreshed)
            
            local delta = math.max(0, now-last_refreshed)
            local filled_tokens = math.min(capacity, last_tokens+(delta*rate))
            local allowed = filled_tokens >= requested
            local new_tokens = filled_tokens
            local allowed_num = 0
            if allowed then
              new_tokens = filled_tokens - requested
              allowed_num = 1
            end
            
            --redis.log(redis.LOG_WARNING, "delta " .. delta)
            --redis.log(redis.LOG_WARNING, "filled_tokens " .. filled_tokens)
            --redis.log(redis.LOG_WARNING, "allowed_num " .. allowed_num)
            --redis.log(redis.LOG_WARNING, "new_tokens " .. new_tokens)
            
            if ttl > 0 then
              redis.call("setex", tokens_key, ttl, new_tokens)
              redis.call("setex", timestamp_key, ttl, now)
            end
            
            -- return { allowed_num, new_tokens, capacity, filled_tokens, requested, new_tokens }
            return { allowed_num, new_tokens }
            """;

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

        public String getUploadDir() {
            if (StringUtils.isEmpty(uploadDir)) {
                return System.getProperty("java.io.tmpdir");
            }
            return uploadDir;
        }
    }


    public enum FileSettingType {
        local, qn
    }
}
