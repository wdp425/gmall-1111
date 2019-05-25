package com.atguigu.gmall.vo.sec;

import lombok.Data;

@Data
public class SecKillOrderVo {

    private String accessToken;
    private Long skuId;
    private Long addressId;//用用户默认
    private String orderSn;//秒杀生成的订单号
    private String message;
    private Boolean success;
}
