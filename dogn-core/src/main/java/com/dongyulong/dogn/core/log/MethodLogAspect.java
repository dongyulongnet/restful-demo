package com.dongyulong.dogn.core.log;

import com.dongyulong.dogn.core.annotation.LogOpen;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;


/**
 * 日志切面
 *
 * @author dongy
 * @date 11:42 2022/2/9
 **/
@Aspect
public class MethodLogAspect {

    private final MethodLogAroundHandler methodLogAroundHandler = new MethodLogAroundHandler();

    @Around(value = "@annotation(logOpen)")
    @SneakyThrows
    public Object around(final ProceedingJoinPoint pjp, LogOpen logOpen) {
        return methodLogAroundHandler.around(pjp);
    }
}
