package com.atguigu.gmall.pms.config;

import com.atguigu.gmall.constant.RoutingKeyConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@EnableRabbit
@Configuration
public class PmsRabbitConfig {

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
