package com.dongyulong.restfuldemo.openfeign.config;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.DynaBean;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.type.TypeFactory;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author dongy
 * @date 11:00 2022/1/27
 **/
@Data
public class XmlEncoder implements Encoder {

    @SneakyThrows
    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        Class<?> clazz = TypeFactory.rawClass(bodyType);
        for (Field field : clazz.getDeclaredFields()) {
            isBlank(field, clazz, object);
        }
        Map<String, Object> targetMap = JSON.parseObject(JSON.toJSONString(object), new TypeReference<Map<String, Object>>() {
        });
        String xml = XmlUtil.mapToXmlStr(targetMap);
        template.body(xml);
    }

    private void isBlank(Field field, Class<?> clazz, Object object) {
        String errorMsgTemplate = "参数{}.{}格式错误（参数为空或长度过长）";
        String getMethodTemplate = "get%s";
        if (!String.class.equals(field.getType())) {
            return;
        }
        String name = field.getName();
        DynaBean dynaBean = DynaBean.create(object);
        String val = (String) dynaBean.invoke(String.format(getMethodTemplate, CharSequenceUtil.upperFirst(name)));

        if (!field.isAnnotationPresent(Required.class)) {
            return;
        }
        Required required = field.getAnnotation(Required.class);
        //参数长度不可超限
        Assert.isFalse(StringUtils.length(val) > required.max(), errorMsgTemplate, clazz.getTypeName(), name);
        //如果为必传参数，则不能为空
        Assert.isFalse(required.must() && StringUtils.isBlank(val), errorMsgTemplate, clazz.getTypeName(), name);
        //如果不是必传参数，且指定的或参数名不为空时,两个参数不可同时为空
        if (!required.must() && StringUtils.isNotBlank(required.params())) {
            String premiseVal = (String) dynaBean.invoke(String.format(getMethodTemplate, CharSequenceUtil.upperFirst(required.params())));
            Assert.isFalse(StringUtils.isAllBlank(val, premiseVal), errorMsgTemplate, clazz.getTypeName(), name);
        }
    }

}
