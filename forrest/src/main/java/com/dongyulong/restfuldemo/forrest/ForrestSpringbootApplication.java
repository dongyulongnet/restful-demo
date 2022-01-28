package com.dongyulong.restfuldemo.forrest;

import com.dtflys.forest.springboot.annotation.ForestScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author dongy
 * @date 10:21 2022/1/26
 **/
@SpringBootApplication(scanBasePackages = "com.dongyulong")
@ForestScan(basePackages = "com.dongyulong.restfuldemo.forrest.server")
public class ForrestSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForrestSpringbootApplication.class, args);
    }
}
