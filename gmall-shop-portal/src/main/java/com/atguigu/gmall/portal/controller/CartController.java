package com.atguigu.gmall.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.cart.vo.CartItem;
import com.atguigu.gmall.to.CommonResult;
import org.springframework.web.bind.annotation.*;

/**
 * 购物车
 */
@RequestMapping("/cart")
@RestController
public class CartController {


    @Reference
    CartService cartService;

    /**
     * 返回当前添加的购物项的详细信息
     * @param skuId
     * @param cartKey
     * @param accessToken
     * @return
     */
    @PostMapping("/add")
    public CommonResult addToCart(@RequestParam("skuId") Long skuId,
                                  @RequestParam(value = "cartKey",required = false) String cartKey,
                                  @RequestParam(value = "accessToken",required = false) String accessToken){

        CartItem cartItem = cartService.addToCart(skuId,cartKey,accessToken);
        return new CommonResult().success(cartItem);
    }
}
