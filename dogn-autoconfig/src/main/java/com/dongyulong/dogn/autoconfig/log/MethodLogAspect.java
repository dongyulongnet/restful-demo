package com.dongyulong.dogn.autoconfig.log;

import com.dongyulong.dogn.core.annotation.LogOpen;
import com.dongyulong.dogn.core.log.MethodLogAroundHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;


/**
 * 日志切面
 *
 * @author dongy
 * @date 11:42 2022/2/9
 **/
@Slf4j
@Aspect
@Order(30)
@Configuration
@ConditionalOnProperty(value = "enable", prefix = MethodLogAspect.PREFIX, havingValue = "true")
public class MethodLogAspect {

    public static final String PREFIX = "dida.method.log";

    private final MethodLogAroundHandler methodLogAroundHandler = new MethodLogAroundHandler();

    @Around(value = "@annotation(logOpen)")
    @SneakyThrows
    public Object around(final ProceedingJoinPoint pjp, LogOpen logOpen) {
        return methodLogAroundHandler.around(pjp);
    }
}
