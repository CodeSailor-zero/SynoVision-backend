package com.sean.synovision;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@EnableAspectJAutoProxy(exposeProxy = true) //暴露代理
@SpringBootApplication
@MapperScan("com.sean.synovision.mapper")
public class SynoVisionApplication {

    public static void main(String[] args) {
        SpringApplication.run(SynoVisionApplication.class, args);
    }

}
