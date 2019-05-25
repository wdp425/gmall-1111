package com.atguigu.gmall.constant;

public class RoutingKeyConstant {

    public static final String USER_ORDER_QUEUE_ROUTING_KEY = "order";
    public static final String ORDER_RELEASE_QUEUE_ROUTING_KEY = "order.release";
    public static final String ORDER_DEAD_QUEUE_ROUTING_KEY = "order.dead";
    public static final String ORDER_SECKILL_QUEUE_ROUTING_KEY = "order.seckill";

    public static final Long MESSAGE_TTL = 1000*60*30L;
}
