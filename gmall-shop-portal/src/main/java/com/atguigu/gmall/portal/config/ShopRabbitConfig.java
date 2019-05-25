package com.atguigu.gmall.portal.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class ShopRabbitConfig {

    @Bean
    public MessageConverter messageConverter(){
        return  new Jackson2JsonMessageConverter();
    }



    /**
     * 库存解锁队列
     * @return
     */
    @Bean
    public Queue releaseStockQueue(){
        return new Queue("stock-order-release-queue",true,false,false);
    }





}
