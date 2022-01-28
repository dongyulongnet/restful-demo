package com.dongyulong.restfuldemo.openfeign.server;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.XmlUtil;
import com.dongyulong.restfuldemo.openfeign.config.XmlEncoder;
import com.dongyulong.restfuldemo.openfeign.entities.WechatServiceQueryRequestDTO;
import com.fasterxml.jackson.databind.type.TypeFactory;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestLine;
import feign.Response;
import feign.Util;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author dongy
 * @date 11:25 2022/1/26
 **/
public interface WechatService {

    String HOST = "https://api.mch.weixin.qq.com";
    String HOST2 = "https://api2.mch.weixin.qq.com";
    String name = "微信";

    /**
     * @param queryRequest -
     * @return -
     */
    @RequestLine("POST /pay/orderquery")
    Map<String, Object> query(WechatServiceQueryRequestDTO queryRequest);

    @Slf4j
    @Configuration
    class Config {

        private static final ThreadLocal<Long> THREAD_LOCAL = ThreadLocal.withInitial(System::currentTimeMillis);

        @Bean
        public Request.Options options() {
            return new Request.Options(3000L, TimeUnit.MILLISECONDS, 2000L, TimeUnit.MILLISECONDS, Boolean.FALSE);
        }

        @Bean
        public RequestInterceptor requestInterceptor() {
            return template -> {
                template.target(WechatService.HOST2);
                THREAD_LOCAL.set(System.currentTimeMillis());
                System.err.println(template);
            };
        }

        @Bean
        public Encoder encoder() {
            return new XmlEncoder();
        }

        @Bean
        public ErrorDecoder errorDecoder() {
            return (methodKey, response) -> {
                log.info("methodKey 耗时：{}ms", System.currentTimeMillis() - THREAD_LOCAL.get());
                THREAD_LOCAL.remove();
                return new RuntimeException("methodKey ErrorDecoder");
            };
        }

        @Bean
        public Decoder decoder() {
            return (response, type) -> {
                log.info("response 耗时：{}ms", System.currentTimeMillis() - THREAD_LOCAL.get());
                THREAD_LOCAL.remove();
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
            };
        }

    }

}
