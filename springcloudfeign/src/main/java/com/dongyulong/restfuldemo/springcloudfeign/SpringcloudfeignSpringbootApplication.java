package com.dongyulong.restfuldemo.springcloudfeign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

/**
 * @author dongy
 * @date 10:21 2022/1/26
 **/
@SpringBootApplication
@EnableFeignClients
public class SpringcloudfeignSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringcloudfeignSpringbootApplication.class, args);
    }
}
