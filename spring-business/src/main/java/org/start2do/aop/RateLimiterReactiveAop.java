package org.start2do.aop;

import jakarta.validation.constraints.Min;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.start2do.BusinessConfig;
import org.start2do.util.ELUtil;
import org.start2do.util.RateLimitUtil;
import org.start2do.util.StringUtils;

@Aspect
@Slf4j
@Component
@ConditionalOnProperty(prefix = "start2do.business.rate-limit", value = "enable", havingValue = "true")
public class RateLimiterReactiveAop {

    @Getter
    private final RateLimitUtil rateLimitUtil;


    public RateLimiterReactiveAop(BusinessConfig businessConfig) {
        log.info("初始化RateLimiterReactiveAop");
        this.rateLimitUtil = new RateLimitUtil(businessConfig);
    }


    @Around("@annotation(setting)")
    public Object around(ProceedingJoinPoint point, RateLimitSetting setting) throws Throwable {
        List<String> keys = null;
        boolean found = false;
        if (StringUtils.isEmpty(setting.id())) {
            for (Object arg : point.getArgs()) {
                if (IRetaLimitGetterKey.class.isAssignableFrom(arg.getClass())) {
                    keys = rateLimitUtil.getKey(((IRetaLimitGetterKey) arg).getPrefix());
                    found = true;
                    break;
                }
            }
            if (!found) {
                MethodSignature signature = (MethodSignature) point.getSignature();
                Method method = signature.getMethod();
                keys = rateLimitUtil.getKey(
                    method.getDeclaringClass().getName() + "." + method.getName() + "." + method.getParameterCount());
            }
        } else {
            String spelValue = ELUtil.parseSpel(point, setting.id());
            keys = rateLimitUtil.getKey(spelValue);
        }
        if (keys != null) {
            rateLimitUtil.getToken(keys, setting.await(), setting.requested(), setting.capacity(), setting.rate(),
                setting.waitMs(), setting.maxWaitMs(), setting.errorMsg(), setting.maxWaitMsg());
        }
        return point.proceed();
    }

    interface IRetaLimitGetterKey {

        String getPrefix();
    }

    /**
     * 桶容量 / token 速率，即需要多少单位时间（秒）才能填满桶
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RateLimitSetting {

        /**
         * 请求Token数
         */
        @Min(1) int requested() default 1;

        /**
         * 桶容量
         */
        @Min(1) int capacity() default 10000;

        /**
         * 每秒填充数
         */
        @Min(1) int rate() default 200;

        String id() default "";

        boolean await() default false;

        /**
         * 等待1秒
         */
        long waitMs() default 1000;

        /**
         *
         */
        long maxWaitMs() default 1000;

        String maxWaitMsg() default "等待令牌超时";

        String errorMsg() default "获取令牌异常";

    }


}
