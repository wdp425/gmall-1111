package com.atguigu.gmall.oms.service;

import com.atguigu.gmall.oms.entity.Order;
import com.atguigu.gmall.vo.order.OrderConfirmVo;
import com.atguigu.gmall.vo.order.OrderCreateVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
public interface OrderService extends IService<Order> {


    /**
     * 订单确认
     * @param id
     * @return
     */
    OrderConfirmVo orderConfirm(Long id);

    OrderCreateVo createOrder(BigDecimal totalPrice, Long addressId, String note);



}
