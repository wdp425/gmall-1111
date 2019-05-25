package com.atguigu.gmall.oms;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * 1、开启定时任务功能 @EnableScheduling
 * 2、标定时任务注解
 */
@EnableScheduling
@EnableDubbo
@MapperScan(basePackages = "com.atguigu.gmall.oms.mapper")
@SpringBootApplication
public class GmallOmsApplication {

    public static void main(String[] args) {
       SpringApplication.run(GmallOmsApplication.class, args);



    }

}
