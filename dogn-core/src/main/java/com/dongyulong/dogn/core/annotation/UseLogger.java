package com.dongyulong.dogn.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记使用的Log对象
 *
 * @author dongy / 和小奇
 * @date 2019/2/22 5:48 PM
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseLogger {

    String value() default "";
}

