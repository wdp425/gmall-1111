package com.atguigu.gmall.portal.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.to.CommonResult;
import com.atguigu.gmall.ums.entity.Member;
import com.atguigu.gmall.vo.order.OrderConfirmVo;
import com.atguigu.gmall.vo.order.OrderCreateVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Api(tags = "订单服务")
@RequestMapping("/order")
@RestController
public class OrderController {


    @Autowired
    StringRedisTemplate redisTemplate;

    @Reference
    OrderService orderService;


    /**
     * 当信息确认完成以后下一步要提交订单。我们必须做防重复验证【接口幂等性设计】；
     * 1）、以前利用防重的令牌以后。
     * 接口幂等性设计：
     *    select：
     *    insert/delete/update【幂等性设计】；
     *
     * 2）、数据层面怎么幂等？可用数据库的锁机制保证在数据库层面多次请求幂等
     *    insert();【如果id不是自增，传入id】
     *    delete();【在数据层如果带id删除，幂等操作】
     *    update();【乐观锁】  update set stock=stock-1,version=version+1 where skuId=1 and version=1
     *
     * 3）、业务层面；
     *    分布式锁_【令牌防重】。 order:member:1;
     *    分布式锁并发下单；
     *
     * 4）、相同的数据就插入一次，没有任何id-或者唯一索引约束。
     *     比较巧妙的防重：把提交来的数据-->对象-->json-->MD5-->redis（set）；
     *
     *
     *
     * 订单：
     *      1）、确认订单：（查询商品的实时价格）库存系统（wms【ware manage system】）；
     *          库存系统：
     *              高并发情况下的查价，查库存，改库存；
     *
     *          高并发查库存：
     *              精确查（下定单要精确到一件），和模糊查（只提示有货无货）；
     *              模糊查：
     *                  库存进入缓存；
     *                  缓存与数据库的不一致性；
     *                      redis：sku:1-18件  sku:2-22件;
     *          缓存模式：Cache-Aside
     *              查库存；
     *              SkuStock stock = getStocktFromRedis(skuId-1);
     *              Lock lock = getLock();
     *              if(stock){
     *                  lock.lock();
     *                  stock = getStocktFromDB(skuId-1);
     *                  saveToRedis(stock);
     *                  lock.unlock();
     *              }
     *
     *              改库存；（模糊查）【加购物车，查商品详情用这个】
     *                  时机：
     *                  1）、下单的时候，sku7-5，锁库存；lockstock=lockstock+5 where skuId=7
     *                          stock：当前的库存   lockstock：锁住的库存；
     *                          真正的库存；stock-lockstock；
     *
     *                       改数据库，改缓存；超卖了；加了锁，保证了业务，但是丢失了性能；
     *                       // lock.lock();
     *                          lockstock=lockstock+5 where skuId=7
     *                          saveToRedis(stock);
     *                       // lock.unlock();
     *
     *              改库存：（精确查）
     *                   2）、查库存，
     *                      下单能成，就是锁库存成；
     *
     *                   3）、扣库存；拿出了货，出库了。真正的扣库存；
     *                   4）、订单超时，释放锁住的库存；
     *                   5）、退货等，还要加库存；
     *
     *
     *
     *
     *
     *      2）、下订单；（验证无货下不了单）精确查库存；
     *
     *          下订单：锁库存；
     *          订单超时：释放库存；
     *
     *          订单系统和库存系统是同一个系统最简单；
     *          数据库层使用查询悲观锁机制；
     *              begin transaction();
     *                  SELECT stock,lock_stock FROM `pms_sku_stock` WHERE id = 98 FOR UPDATE;
     *                  insert into order();
     *                  insert into orderitem();
     *                  insert into orderitem();
     *              commit();
     *
     *
     *               begin transaction();
     *                      SELECT stock,lock_stock FROM `pms_sku_stock` WHERE id = 98 FOR UPDATE;
     *                      insert into order();
     *                      insert into orderitem();
     *                      insert into orderitem();
     *               commit();
     *          这个事务不结，其他事务不能运行；
     *
     *          最终一致性；
     *              订单系统，下订单；--->使用消息队列--->库存收到订单进行扣库存（库存扣除成功，订单说明ok）；
     *              可以去支付。库存系统扣除失败。下单失败；
     *
     *          最终设计：
     *              订单在下单的时候（库存系统先去查询并且锁库存，库存锁住以后，再去下单），
     *
     *
     *
     *
     *
     *
     *
     * @param accessToken
     * @return
     */
    @ApiOperation("订单确认")
    @GetMapping("/confirm")
    public CommonResult confirmOrder(@RequestParam("accessToken") String accessToken){
        
        //0、检查用户是否存在
        String memberJson = redisTemplate.opsForValue().get(SysCacheConstant.LOGIN_MEMBER + accessToken);
        if(StringUtils.isEmpty(accessToken)||StringUtils.isEmpty(memberJson)){
            CommonResult failed = new CommonResult().failed();
            failed.setMessage("用户未登录，请先登录");
            //用户未登录
            return failed;
        }

        //1、登录的用户
        Member member = JSON.parseObject(memberJson, Member.class);
        /**
         * 返回如下数据；
         * 1、当前用户的可选地址列表
         * 2、当前购物车选中的商品信息
         * 3、可用的优惠卷信息
         * 4、支付、配送、发票方式信息
         *
         *
         */
        //dubbo的RPC隐式传参；setAttachment保存一下下一个远程服务需要的参数
        RpcContext.getContext().setAttachment("accessToken",accessToken);
        //调用下一个远程服务
        OrderConfirmVo confirm = orderService.orderConfirm(member.getId());


        return new CommonResult().success(confirm);
    }


    /**
     * 创建订单的时候必须用到确认订单的那些数据
     * @param totalPrice  为了比价；
     * @param accessToken
     * @return
     */
    @ApiOperation("下单")
    @PostMapping("/create")
    public CommonResult createOrder(@RequestParam("totalPrice") BigDecimal totalPrice,
                              @RequestParam("accessToken") String accessToken,
                              @RequestParam("addressId") Long addressId,
                              @RequestParam(value = "note",required = false) String note,
                                    @RequestParam("orderToken") String orderToken){


        RpcContext.getContext().setAttachment("accessToken", accessToken);
        RpcContext.getContext().setAttachment("orderToken", orderToken);
        //1、创建订单要生成订单（总额）和订单项（购物车中的商品）；

        //防重复
        OrderCreateVo orderCreateVo = orderService.createOrder(totalPrice,addressId,note);

        if(!StringUtils.isEmpty(orderCreateVo.getToken())){
            CommonResult result = new CommonResult().failed();
            result.setMessage(orderCreateVo.getToken());
            result.setData(orderCreateVo);
            return result;
        }
        //
        return new CommonResult().success(orderCreateVo);
    }


    /**
     * 去支付
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/pay",produces = {"text/html"})
    public String pay(@RequestParam("orderSn") String orderSn,
                      @RequestParam("accessToken") String accessToken){
        String string = orderService.pay(orderSn,accessToken);
        return string;
    }

    /**
     * 接收支付宝异步通知
     */
    @ResponseBody
    @RequestMapping("/pay/success/async")
    public String paySuccess(HttpServletRequest request) throws UnsupportedEncodingException {

        //封装支付宝数据
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        log.debug("订单【{}】===支付宝支付异步通知进来....",params.get("out_trade_no"));

        String result = orderService.resolvePayResult(params);
        return result;
    }
}
