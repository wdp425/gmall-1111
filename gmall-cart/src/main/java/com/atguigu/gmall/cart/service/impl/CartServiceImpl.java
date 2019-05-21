package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.component.MemberComponent;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.cart.vo.CartItem;
import com.atguigu.gmall.cart.vo.CartResponse;
import com.atguigu.gmall.constant.CartConstant;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.ums.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@Component
public class CartServiceImpl implements CartService {


    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberComponent memberComponent;


    @Override
    public CartResponse addToCart(Long skuId, String cartKey, String accessToken) {
        //0、根据accessToken获取用户的id
        Member member = memberComponent.getMemberByAccessToken(accessToken);

        if(member!=null&&!StringUtils.isEmpty(cartKey)){
            //先去合并再说
            mergeCart(cartKey,member.getId());
        }


        String finalCartKey = "";

        //1、这个人登录了，购物车就用他的在线购物车；cart:user:1
        if(member != null){
            //用户登录了。
            finalCartKey = CartConstant.USER_CART_KEY_PREFIX+member.getId();
            /**
             * 1、按照skuId找到sku的真正信息
             * 2、给指定的购物车添加记录
             *    如果已经有了这个skuId只是count的增加；
             *
             * skuId,cartKey
             */
            CartItem cartItem = addItemToCart(skuId,finalCartKey);

            CartResponse cartResponse = new CartResponse();
            cartResponse.setCartItem(cartItem);
            return cartResponse;
        }


        //2、这个人没登录，用离线（临时）购物车；cart:temp:cartKey
        if(!StringUtils.isEmpty(cartKey)){
            finalCartKey = CartConstant.TEMP_CART_KEY_PREFIX+cartKey;

            //添加到购物车
            CartItem cartItem = addItemToCart(skuId,finalCartKey);

            CartResponse cartResponse = new CartResponse();
            cartResponse.setCartItem(cartItem);
            return cartResponse;
        }

        //3、如果以上都没有，说明刚来，分配一个临时购物车
        String newCartKey = UUID.randomUUID().toString().replace("-", "");
        finalCartKey = CartConstant.TEMP_CART_KEY_PREFIX+newCartKey;

        //添加到购物车
        CartItem cartItem = addItemToCart(skuId,finalCartKey);

        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartItem(cartItem);
        cartResponse.setCartKey(newCartKey);
        return cartResponse;
    }

    /**
     * 添加商品到购物车
     * @param skuId
     * @param finalCartKey
     * @return
     */
    private CartItem addItemToCart(Long skuId, String finalCartKey) {

        return null;
    }

    /**
     *
     * @param cartKey  老购物车
     * @param id   用户id
     */
    private void mergeCart(String cartKey, Long id){

    }

}
