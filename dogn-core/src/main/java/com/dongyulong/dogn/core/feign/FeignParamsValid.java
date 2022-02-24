package com.dongyulong.dogn.core.feign;

import cn.hutool.core.bean.DynaBean;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import com.dongyulong.dogn.core.annotation.Required;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * feign 参数格式校验
 *
 * @author dongy
 * @date 15:38 2022/1/27
 **/
public class FeignParamsValid {

    private FeignParamsValid() {

    }

    private static final String ERROR_MSG_TEMPLATE = "参数{}.{}格式错误（参数为空或长度过长）";
    private static final String GET_METHOD_TEMPLATE = "get%s";

    public static void check(Object object, Type bodyType) {
        Class<?> clazz = TypeFactory.rawClass(bodyType);
        for (Field field : clazz.getDeclaredFields()) {
            check(field, clazz, object);
        }
    }

    private static void check(Field field, Class<?> clazz, Object object) {
        if (!String.class.equals(field.getType())) {
            return;
        }
        String name = field.getName();
        DynaBean dynaBean = DynaBean.create(object);
        String val = (String) dynaBean.invoke(String.format(GET_METHOD_TEMPLATE, CharSequenceUtil.upperFirst(name)));

        if (!field.isAnnotationPresent(Required.class)) {
            return;
        }
        Required required = field.getAnnotation(Required.class);
        //参数长度不可超限
        Assert.isFalse(StringUtils.length(val) > required.max(), ERROR_MSG_TEMPLATE, clazz.getTypeName(), name);
        //如果为必传参数，则不能为空
        Assert.isFalse(required.must() && StringUtils.isBlank(val), ERROR_MSG_TEMPLATE, clazz.getTypeName(), name);
        //如果不是必传参数，且指定的或参数名不为空时,两个参数不可同时为空
        if (!required.must() && StringUtils.isNotBlank(required.params())) {
            String premiseVal = (String) dynaBean.invoke(String.format(GET_METHOD_TEMPLATE, CharSequenceUtil.upperFirst(required.params())));
            Assert.isFalse(StringUtils.isAllBlank(val, premiseVal), ERROR_MSG_TEMPLATE, clazz.getTypeName(), name);
        }
    }

}
