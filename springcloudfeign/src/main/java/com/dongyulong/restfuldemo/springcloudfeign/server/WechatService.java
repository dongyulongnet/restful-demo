package com.dongyulong.restfuldemo.springcloudfeign.server;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.XmlUtil;
import com.dongyulong.restfuldemo.springcloudfeign.entities.WechatServiceQueryRequestDTO;
import com.fasterxml.jackson.databind.type.TypeFactory;
import feign.Request;
import feign.RequestInterceptor;
import feign.Response;
import feign.Util;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * @author dongy
 * @date 11:25 2022/1/26
 **/
@FeignClient(name = "wechatService", url = WechatService.HOST, configuration = WechatService.Config.class)
public interface WechatService {

    String HOST = "https://api.mch.weixin.qq.com";
    String HOST2 = "https://api2.mch.weixin.qq.com";

    /**
     * @param queryRequest -
     * @return -
     */
    @RequestMapping(method = RequestMethod.POST, value = "/pay/orderquery")
    Map<String, Object> query(@RequestBody WechatServiceQueryRequestDTO queryRequest);

    @Slf4j
    @Configuration
    class Config {

        private static final ThreadLocal<Long> threadLocal = ThreadLocal.withInitial(System::currentTimeMillis);

        @Bean
        public Request.Options options() {
            return new Request.Options(3, 2);
        }

        @Bean
        public RequestInterceptor requestInterceptor() {
            return template -> {
                threadLocal.set(System.currentTimeMillis());
                System.err.println(template);
            };
        }

        @Bean
        public Encoder encoder() {
            return (object, bodyType, template) -> {
                Map<String, Object> targetMap = new TreeMap<>(String::compareTo);
                BeanUtil.beanToMap(object, targetMap, CopyOptions.create().ignoreNullValue());
                String xml = XmlUtil.mapToXmlStr(targetMap);
                template.body(xml);
            };
        }

        @Bean
        public ErrorDecoder errorDecoder() {
            return (methodKey, response) -> {
                log.info("methodKey 耗时：{}ms", System.currentTimeMillis() - threadLocal.get());
                threadLocal.remove();
                return new RuntimeException("methodKey ErrorDecoder");
            };
        }

        @Bean
        public Decoder decoder() {
            return (response, type) -> {
                log.info("response 耗时：{}ms", System.currentTimeMillis() - threadLocal.get());
                threadLocal.remove();
                Response.Body body = response.body();
                if (body == null) {
                    return null;
                }
                String xmlStr = Util.toString(body.asReader());
                if (String.class.equals(type)) {
                    return xmlStr;
                }
                Map<String, Object> map = XmlUtil.xmlToMap(xmlStr);
                if (Map.class.equals(type)) {
                    return map;
                }
                Class<?> clazz = TypeFactory.rawClass(type);
                return BeanUtil.mapToBean(map, clazz, Boolean.FALSE, CopyOptions.create());
            };
        }

    }

}
