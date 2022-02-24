package com.dongyulong.dogn.core.feign.codec;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.XmlUtil;
import com.fasterxml.jackson.databind.type.TypeFactory;
import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.Decoder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * openfeign xml 解码器
 *
 * @author dongy
 * @date 15:13 2022/1/27
 **/
public class XmlDecoder implements Decoder {

    @Override
    public Object decode(Response response, Type type) throws IOException, FeignException {
        Response.Body body = response.body();
        if (body == null) {
            return null;
        }
        String xmlStr = Util.toString(body.asReader(StandardCharsets.UTF_8));
        if (String.class.equals(type)) {
            return xmlStr;
        }
        Map<String, Object> map = XmlUtil.xmlToMap(xmlStr);
        if (Map.class.equals(type)) {
            return map;
        }
        Class<?> clazz = TypeFactory.rawClass(type);
        return BeanUtil.mapToBean(map, clazz, Boolean.FALSE, CopyOptions.create());
    }
}
