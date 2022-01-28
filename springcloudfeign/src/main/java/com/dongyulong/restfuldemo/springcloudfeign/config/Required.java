package com.dongyulong.restfuldemo.springcloudfeign.config;

/**
 * @author dongy
 * @date 11:54 2022/1/26
 **/
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
    String[] params() default {};
}
