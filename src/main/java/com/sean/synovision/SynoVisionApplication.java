package com.sean.synovision;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;


@EnableAspectJAutoProxy(exposeProxy = true) //暴露代理
@EnableScheduling
// 如果需要开启，需要修改部分接口的逻辑，将公共图库 spaceId 定义为 0，否则会出现问题
@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class}) //关闭分库分表的配置
@MapperScan("com.sean.synovision.mapper")
public class SynoVisionApplication {

    public static void main(String[] args) {
        SpringApplication.run(SynoVisionApplication.class, args);
    }
}
