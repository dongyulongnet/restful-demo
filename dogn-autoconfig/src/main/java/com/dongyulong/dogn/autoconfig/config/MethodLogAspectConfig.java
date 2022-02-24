package com.dongyulong.dogn.autoconfig.config;

import com.dongyulong.dogn.core.log.MethodLogAspect;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;

/**
 * @author dongy
 * @date 18:25 2022/2/24
 **/
@AutoConfigureOrder(2)
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class MethodLogAspectConfig {

    public static final String PREFIX = "dida.method.log";

    @Setter
    @Getter
    private Boolean enable;

    @Bean
    @Order(2)
    @ConditionalOnProperty(value = "enable", prefix = MethodLogAspectConfig.PREFIX, havingValue = "true")
    public MethodLogAspect methodLogAspect() {
        return new MethodLogAspect();
    }
}
