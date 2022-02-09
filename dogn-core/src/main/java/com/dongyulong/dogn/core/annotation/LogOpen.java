package com.dongyulong.dogn.core.annotation;

import com.dongyulong.dogn.core.log.MethodLogAroundHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来标记方法是否打印开始/结束日志
 *
 * @author dongy/ 和小奇
 * @date 2019/2/22 11:45 AM
 * @see MethodLogAroundHandler
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogOpen {

    /**
     * 业务标识，日志最前侧会出现当前值
     *
     * @return -
     */
    String value() default "";

    /**
     * 私有方法是否开启日志，默认不开启
     *
     * @return -
     */
    boolean open() default false;


}
