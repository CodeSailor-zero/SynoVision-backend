package com.sean.synovision;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sean.synovision.mapper.PictureMapper;
import com.sean.synovision.model.entity.Picture;
import com.sean.synovision.service.PictureService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;


@EnableAspectJAutoProxy(exposeProxy = true) //暴露代理
@EnableScheduling
@SpringBootApplication
@MapperScan("com.sean.synovision.mapper")
public class SynoVisionApplication {

    public static void main(String[] args) {
        SpringApplication.run(SynoVisionApplication.class, args);
    }
}
