package com.atguigu.gmall.oms.config;

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
public class OmsRabbitConfig {

    @Bean
    public MessageConverter messageConverter(){
        return  new Jackson2JsonMessageConverter();
    }


    /**
     * 订单交换机
     * @return
     */
    @Bean
    public Exchange orderExchange(){
        //(String name, boolean durable, boolean autoDelete)
        return new TopicExchange("order-exchange",true,false);
    }

    /**
     * 秒杀单队列
     */
    @Bean
    public Queue secKillQueue(){
        return new Queue("seckill-order-queue",true,false,false);
    }

    @Bean
    public Binding secKillBiding(){

        return new Binding("seckill-order-queue",
                Binding.DestinationType.QUEUE,
                "order-exchange",
                RoutingKeyConstant.ORDER_SECKILL_QUEUE_ROUTING_KEY,null);
    }


    /**
     * 用户服务监听的订单队列
     * @return
     */
    @Bean
    public Queue userServiceQueue(){
        //Queue(String name, boolean durable, boolean exclusive, boolean autoDelete)
        return new Queue("user-order-queue",true,false,false);
    }

    /**
     * 订单的延迟队列
     * @return
     */
    @Bean
    public Queue orderdelayqueue(){
        Map<String, Object> arguments = new HashMap<>();
        //每个消息，发的时候动态设置过期时间
        arguments.put("x-message-ttl",RoutingKeyConstant.MESSAGE_TTL);//这个队列里面所有消息的过期时间
        arguments.put("x-dead-letter-exchange","order-exchange");//消息死了交给那个交换机
        arguments.put("x-dead-letter-routing-key","order.dead");//死信发出去的路由键
        return new Queue("order-delay-queue",true,false,false,arguments);
    }


    /**
     * 库存解锁队列
     * @return
     */
    @Bean
    public Queue releaseStockQueue(){
        return new Queue("stock-order-release-queue",true,false,false);
    }

    @Bean
    public Queue deadOrderQueue(){
        return new Queue("order-dead-queue",true,false,false);
    }


    @Bean
    public Binding userOrderExchangeBinding(){

        //String destination, DestinationType destinationType, String exchange, String routingKey,
        //			Map<String, Object> arguments
        return new Binding("user-order-queue",
                Binding.DestinationType.QUEUE,
                "order-exchange",
                RoutingKeyConstant.USER_ORDER_QUEUE_ROUTING_KEY,null);
    }

    @Bean
    public Binding orderDelayExchangeBinding(){

        return new Binding("order-delay-queue",
                Binding.DestinationType.QUEUE,
                "order-exchange",
                RoutingKeyConstant.USER_ORDER_QUEUE_ROUTING_KEY,null);
    }

    @Bean
    public Binding OrderReleaseBinding(){

        //String destination, DestinationType destinationType, String exchange, String routingKey,
        //			Map<String, Object> arguments
        return new Binding("stock-order-release-queue",
                Binding.DestinationType.QUEUE,
                "order-exchange",
                RoutingKeyConstant.ORDER_RELEASE_QUEUE_ROUTING_KEY,null);
    }

    @Bean
    public Binding deadQueueBinding(){

        //String destination, DestinationType destinationType, String exchange, String routingKey,
        //			Map<String, Object> arguments
        return new Binding("order-dead-queue",
                Binding.DestinationType.QUEUE,
                "order-exchange",
                RoutingKeyConstant.ORDER_DEAD_QUEUE_ROUTING_KEY,null);
    }



}
