package com.dongyulong.dogn.core.feign.codec;

import cn.hutool.core.util.XmlUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dongyulong.dogn.core.annotation.Required;
import com.dongyulong.dogn.core.feign.FeignParamsValid;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * openfeign xml 编码器
 * 配合注解{@link Required}参数格式校验
 *
 * @author dongy
 * @date 11:00 2022/1/27
 **/
public class XmlEncoder implements Encoder {

    @SneakyThrows
    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        FeignParamsValid.check(object, bodyType);
        Map<String, Object> targetMap = JSON.parseObject(JSON.toJSONString(object), new TypeReference<Map<String, Object>>() {
        });
        String xml = XmlUtil.mapToXmlStr(targetMap);
        template.body(xml);
    }


}
