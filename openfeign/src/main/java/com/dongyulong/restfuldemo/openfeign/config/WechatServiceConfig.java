package com.dongyulong.restfuldemo.openfeign.config;

import com.didapinche.agaue.core.http.HttpClient;
import com.dongyulong.restfuldemo.openfeign.server.WechatService;
import feign.ExceptionPropagationPolicy;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dongy
 * @date 13:49 2022/1/26
 **/
@Slf4j
@Configuration
public class WechatServiceConfig {

    @Bean
    public WechatService wechatService(Request.Options options,
                                       RequestInterceptor requestInterceptor,
                                       Encoder encoder,
                                       ErrorDecoder errorDecoder,
                                       Decoder decoder) {
        return Feign.builder()
                .logger(new Slf4jLogger())
                .logLevel(Logger.Level.FULL)
                .client(new OkHttpClient(new HttpClient.Builder(WechatService.NAME).build().getOkHttpClient()))
                .requestInterceptor(requestInterceptor)
                .encoder(encoder)
                .errorDecoder(errorDecoder)
                .decoder(decoder)
                .options(options)
                .exceptionPropagationPolicy(ExceptionPropagationPolicy.UNWRAP)
                .retryer(Retryer.NEVER_RETRY)
                .target(WechatService.class, WechatService.HOST);
    }


}
