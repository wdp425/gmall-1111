package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.vo.CartItem;
import com.atguigu.gmall.cart.vo.CartResponse;

/**
 * 购物车服务
 */
public interface CartService {

    /**
     * 添加商品区购物车
     * @param skuId
     * @param cartKey
     * @param accessToken
     * @return
     */
    CartResponse addToCart(Long skuId, String cartKey, String accessToken);
}
