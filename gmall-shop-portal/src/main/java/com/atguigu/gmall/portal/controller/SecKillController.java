package com.atguigu.gmall.portal.controller;

import com.atguigu.gmall.constant.RoutingKeyConstant;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.vo.sec.SecKillOrderVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
public class SecKillController {


    @Autowired
    StringRedisTemplate redisTemplate;



    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sec/kill")
    public SecKillOrderVo secKill(@RequestParam("accessToken") String accessToken,
                          @RequestParam("skuId") Long skuId) throws InterruptedException {

        SecKillOrderVo orderVo = new SecKillOrderVo();
        //检查redis中是否存在这个键
        Boolean hasKey = redisTemplate.hasKey(SysCacheConstant.LOGIN_MEMBER + accessToken);
        if(!hasKey){
            //用户没登录了
            orderVo.setSuccess(false);
            orderVo.setMessage("请先登录");
            return orderVo;
        }

        RSemaphore semaphore = redissonClient.getSemaphore("sec:kill:sku:" + 206);
        //1、MD5 防重；
        String data = accessToken+skuId;
        String s = DigestUtils.md5DigestAsHex(data.getBytes());

        Boolean aBoolean = false;//redisTemplate.opsForSet().isMember("sec:token", s);
        if(aBoolean){
            //redis中有，之前已经发过请求
            orderVo.setSuccess(false);
            orderVo.setMessage("你已经秒杀过了");
            return orderVo;
        }else {
            //第一次进来，放防重
            redisTemplate.opsForSet().add("sec:token",s);
            //秒杀。去redis中减库存
            boolean acquire = semaphore.tryAcquire();

            if(acquire){
                //信号量-1
                orderVo.setAccessToken(accessToken);
                orderVo.setSkuId(skuId);
                //生成订单号
                orderVo.setOrderSn(IdWorker.getTimeId());
                orderVo.setSuccess(true);
                orderVo.setMessage("秒杀成功...");

                rabbitTemplate.convertAndSend("order-exchange", RoutingKeyConstant.ORDER_SECKILL_QUEUE_ROUTING_KEY,orderVo);
                return  orderVo;
            }else {
                orderVo.setSuccess(false);
                orderVo.setMessage("运气不好，稍后再试...");
                return orderVo;
            }
        }




    }


    /**
     * 定时任务整点上线需要秒杀的商品；
     */
    public void task(){
        redisTemplate.opsForValue().set("sec:kill:sku:"+206,new String("1000"));
    }
}
