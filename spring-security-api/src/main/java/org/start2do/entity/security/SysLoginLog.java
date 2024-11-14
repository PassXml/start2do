package org.start2do.entity.security;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.ebean.id_generators.SnowflakeStrGenerator;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "sys_login_log")
public class SysLoginLog extends Model {

    @Id
    @GeneratedValue(generator = SnowflakeStrGenerator.KEY)
    private String id;
    private String username;
    private String ip;
    private String userAgent;
    @WhenCreated
    private LocalDateTime createTime;
    /**
     * 所属者标志
     */
    private String owner;

    public static final String RedisLockKey = "Cache:LOGIN_LOCK:";

    public static String getRedisLockKey(String username) {
        return RedisLockKey + username;
    }

    public SysLoginLog(String username, String ip, String userAgent, String owner) {
        this.username = username;
        this.ip = ip;
        this.userAgent = userAgent;
        this.owner = owner;
    }
}
