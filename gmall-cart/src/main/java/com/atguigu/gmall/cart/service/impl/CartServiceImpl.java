package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.component.MemberComponent;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.cart.vo.Cart;
import com.atguigu.gmall.cart.vo.CartItem;
import com.atguigu.gmall.cart.vo.CartResponse;
import com.atguigu.gmall.cart.vo.UserCartKey;
import com.atguigu.gmall.constant.CartConstant;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.pms.entity.Product;
import com.atguigu.gmall.pms.entity.SkuStock;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.pms.service.SkuStockService;
import com.atguigu.gmall.ums.entity.Member;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Component
public class CartServiceImpl implements CartService {


    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberComponent memberComponent;

    @Autowired
    RedissonClient redissonClient;


    @Reference
    SkuStockService skuStockService;

    @Reference
    ProductService productService;



    @Override
    public CartResponse addToCart(Long skuId, Integer num,String cartKey, String accessToken) throws ExecutionException, InterruptedException {
        //0、根据accessToken获取用户的id
        Member member = memberComponent.getMemberByAccessToken(accessToken);


        if(member!=null&&!StringUtils.isEmpty(cartKey)){
            //先去合并再说
            System.out.println("合并购物车...");
            mergeCart(cartKey,member.getId());
        }

        //1、获取到用户真正能使用的购物车
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        String finalCartKey = userCartKey.getFinalCartKey();
        CartItem cartItem = addItemToCart(skuId,num,finalCartKey);
        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartItem(cartItem);
        //设置临时购物车用户的cartKey
        cartResponse.setCartKey(userCartKey.getTempCartKey());
        //返回整个购物车方便操作....
        cartResponse.setCart(listCart(cartKey, accessToken).getCart());
        return cartResponse;
    }

    @Override
    public CartResponse updateCartItem(Long skuId, Integer num, String cartKey, String accessToken) {
        //1、判断购物车用哪个key
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        String finalCartKey = userCartKey.getFinalCartKey();
        RMap<String, String> map = redissonClient.getMap(finalCartKey);

        String json = map.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(json, CartItem.class);
        cartItem.setCount(num);

        String jsonString = JSON.toJSONString(cartItem);
        map.put(skuId.toString(),jsonString);
        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartItem(cartItem);
        return cartResponse;
    }

    @Override
    public CartResponse listCart(String cartKey, String accessToken) {
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);

        //1、查询用户的购物车的时候需要判断购物车是否需要合并；
        if(userCartKey.isLogin()){
            //用户登录了，就需要合并购物车
            mergeCart(cartKey,userCartKey.getUserId());
        }

        //查询出购物车的数据
        String finalCartKey = userCartKey.getFinalCartKey();
        //自动续期
        //redisTemplate.expire(finalCartKey,30L, TimeUnit.DAYS);

        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        Cart cart = new Cart();

        List<CartItem> cartItems = new ArrayList<>();
        CartResponse cartResponse = new CartResponse();
        
        if(map!=null&&!map.isEmpty()){
            map.entrySet().forEach((item)->{
                String value = item.getValue();
                CartItem cartItem = JSON.parseObject(value, CartItem.class);
                cartItems.add(cartItem);
            });
            cart.setCartItems(cartItems);
        }else {
            //用户没有购物车。新建一个购物车

            cartResponse.setCartKey(userCartKey.getTempCartKey());
        }

        cartResponse.setCart(cart);

        return cartResponse;
    }

    @Override
    public CartResponse delCartItem(Long skuId, String cartKey, String accessToken) {

        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);

        String finalCartKey = userCartKey.getFinalCartKey();

        //获取购物车删除购物项
        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        map.remove(skuId.toString());


        //整个购物车再返回出去
        CartResponse cartResponse = listCart(cartKey, accessToken);
        return cartResponse;
    }

    @Override
    public CartResponse clearCart(String cartKey, String accessToken) {
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);

        String finalCartKey = userCartKey.getFinalCartKey();

        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        map.clear();

        CartResponse cartResponse = new CartResponse();
        return cartResponse;
    }

    @Override
    public CartResponse checkCartItems(String skuIds, Integer ops, String cartKey, String accessToken) {

        //1、找到每个skuId对应的购物车中的json，把状态check改为 ops对应的值
        //2、为了快速找到那个被选中了，我们单独维护了数组  数组在map中用的key 是  checked 值是Set集合最好

        return null;
    }

    /**
     * 给指定购物车添加商品到购物车
     * @param skuId
     * @param finalCartKey
     * @param num 商品个数
     * @return
     */
    private CartItem addItemToCart(Long skuId,Integer num, String finalCartKey) throws ExecutionException, InterruptedException {
        CartItem newCartItem = new CartItem();

        /**
         * 1、只接受上一步的结果
         * thenAccept(r){
         *     r:上一步的结果
         *
         * }
         *
         * 2、thenApply(r){
         *     r：把上一步的结果拿来进行修改再返回，
         * }
         *
         * 3、thenAccpet(){} 上一步结果1s+本次处理2s=3s
         *
         * 4、thenAccpetAsync(){} 上一步1s+异步2s = 最多等2s
         */

        // 3s
        CompletableFuture<Void> skuFuture = CompletableFuture.supplyAsync(() -> {
            //1s
            SkuStock skuStock = skuStockService.getById(skuId);
            return skuStock;
        }).thenAcceptAsync((stock)->{
            //2s
            //拿到上一步的商品id
            Long productId = stock.getProductId();
            Product product = productService.getById(productId);

            //拿到上一步结果整体封装
            BeanUtils.copyProperties(stock,newCartItem);
            newCartItem.setSkuId(stock.getId());
            newCartItem.setName(product.getName());
            newCartItem.setCount(num);
        });





        //0、查出skuId在数据库对应的最新详情,远程查询


        /**
         * 购物车集合 k[skuId]是str  v[购物项]是str（json）
         *
         * k[checked]  v[1,2,3]
         *
         * 1-json
         * 1-
         *
         */
        RMap<String, String> map = redissonClient.getMap(finalCartKey);

        //获取购物车中这个skuId对应的购物项
        String itemJson = map.get(skuId.toString());

        skuFuture.get();//在线等结果
        //检查购物车中是否已经存在这个购物项
        if(!StringUtils.isEmpty(itemJson)){
            //只是数量叠加,购物车老item获取到数量，给新的cartItem里面添加信息
            CartItem oldItem = JSON.parseObject(itemJson, CartItem.class);
            Integer count = oldItem.getCount();
            //等到异步任务完成了,newCartItem才能用
            newCartItem.setCount(count+newCartItem.getCount());
            String string = JSON.toJSONString(newCartItem);
            //老数据覆盖成新数据
            map.put(skuId.toString(),string);

        }else {
            //新增购物项
            String string = JSON.toJSONString(newCartItem);
            map.put(skuId.toString(),string);
        }
        return newCartItem;
    }

    /**
     *
     * @param cartKey  老购物车
     * @param id   用户id
     */
    private void mergeCart(String cartKey, Long id){

        String oldCartKey = CartConstant.TEMP_CART_KEY_PREFIX+cartKey;
        String userCartKey = CartConstant.USER_CART_KEY_PREFIX+id.toString();
        
        
        //获取到老购物车的数据
        RMap<String, String> map = redissonClient.getMap(oldCartKey);

        if(map!=null&&!map.isEmpty()){
            //map不是null，而且里面有数据才需要合并
            map.entrySet().forEach((item)->{
                //skuId
                String key = item.getKey();
                //购物项的json数据
                String value = item.getValue();
                CartItem cartItem = JSON.parseObject(value, CartItem.class);

                try {
                    addItemToCart(Long.parseLong(key),cartItem.getCount(),userCartKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            map.clear();
        }

        



    }

}
