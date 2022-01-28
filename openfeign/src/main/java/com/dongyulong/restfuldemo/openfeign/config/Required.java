package com.dongyulong.restfuldemo.openfeign.config;

import cn.hutool.core.annotation.Alias;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注意只对String类型有效
 * 配合注解{@link Alias}使用hutool工具类做bean转换时可以指定别名
 *
 * @author dongy
 * @date 11:54 2022/1/26
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Required {

    /**
     * 必填
     *
     * @return -
     */
    boolean must() default true;

    /**
     * 长度限制
     *
     * @return -
     */
    int max() default 64;

    /**
     * 哪些参数为空的前提下当前参数不能为空
     *
     * @return -
     */
    String params() default "";
}
