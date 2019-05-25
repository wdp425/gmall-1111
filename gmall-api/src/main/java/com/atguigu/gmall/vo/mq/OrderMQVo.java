package com.atguigu.gmall.vo.mq;

import com.atguigu.gmall.oms.entity.Order;
import com.atguigu.gmall.oms.entity.OrderItem;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OrderMQVo implements Serializable {

    private Order order;
    private List<OrderItem> items;
}
