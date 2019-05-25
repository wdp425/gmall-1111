package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.oms.entity.Order;
import com.atguigu.gmall.oms.entity.OrderItem;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.pms.entity.SkuStock;
import com.atguigu.gmall.pms.mapper.SkuStockMapper;
import com.atguigu.gmall.pms.service.SkuStockService;
import com.atguigu.gmall.vo.mq.OrderMQVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * <p>
 * sku的库存 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
@Slf4j
@Service
@Component
public class SkuStockServiceImpl extends ServiceImpl<SkuStockMapper, SkuStock> implements SkuStockService {


    @Autowired
    SkuStockMapper skuStockMapper;


    /**
     * 订单（java）和库存（php）；
     * 1）、http；
     * 2）、
     * @param skuId
     * @return
     */

    @Override
    public BigDecimal getSkuPriceBySkuId(Long skuId) {



        return skuStockMapper.selectById(skuId).getPrice();
    }


    @Transactional
    @Override
    public boolean lockSkuLockStock(Long skuId, Integer num) {

        SkuStock skuStock = skuStockMapper.getSkuStockAndLockStock(skuId);
        if(skuStock.getStock() + skuStock.getLockStock()>=num ){
            //锁库存
            skuStockMapper.lockSkuLockStock(skuId,num);
            return true;
        }else {
            return false;
        }


    }

    /**
     * 监听订单关闭消息
     */
    @RabbitListener(queues = "stock-order-release-queue")
    public void releaseStock(OrderMQVo mqVo, Message message, Channel channel) throws IOException {
        log.info("库存服务收到已经关闭的订单消息，开始解锁库存：{}",mqVo);
        //订单本身就是一个undo-log；
        //查出这个订单当时买了哪些商品，和这些商品的数量，然后解锁
        List<OrderItem> items = mqVo.getItems();
        for (OrderItem item:items) {
            Long skuId = item.getProductSkuId();
            Integer quantity = item.getProductQuantity();
            skuStockMapper.releaseStock(skuId,quantity);
        }
        //channel.basicQos(1);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }


}
