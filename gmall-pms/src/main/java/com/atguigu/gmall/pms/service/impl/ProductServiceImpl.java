package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.mapper.*;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.vo.PageInfoVo;
import com.atguigu.gmall.vo.product.PmsProductParam;
import com.atguigu.gmall.vo.product.PmsProductQueryParam;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品信息 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
@Slf4j
@Service
@Component
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {



    @Autowired
    ProductMapper productMapper;

    @Autowired
    ProductAttributeValueMapper productAttributeValueMapper;

    @Autowired
    ProductFullReductionMapper productFullReductionMapper;

    @Autowired
    ProductLadderMapper productLadderMapper;

    @Autowired
    SkuStockMapper skuStockMapper;

    //当前线程共享同样的数据
    ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    //ThreadLocal的原理
    private Map<Thread,Long> map = new HashMap<>();



    @Override
    public PageInfoVo productPageInfo(PmsProductQueryParam param) {


        QueryWrapper<Product> wrapper = new QueryWrapper<>();

        if(param.getBrandId()!=null){
            //前端传了
            wrapper.eq("brand_id",param.getBrandId());
        }

        if(!StringUtils.isEmpty(param.getKeyword())){
            wrapper.like("name",param.getKeyword());
        }

        if(param.getProductCategoryId()!=null){
            wrapper.eq("product_category_id",param.getProductCategoryId());
        }

        if(!StringUtils.isEmpty(param.getProductSn())){
            wrapper.like("product_sn",param.getProductSn());
        }

        if(param.getPublishStatus()!=null){
            wrapper.eq("publish_status",param.getPublishStatus());
        }

        if(param.getVerifyStatus()!=null){
            wrapper.eq("verify_status",param.getVerifyStatus());
        }



        IPage<Product> page = productMapper.selectPage(new Page<Product>(param.getPageNum(), param.getPageSize()), wrapper);

        PageInfoVo pageInfoVo = new PageInfoVo(page.getTotal(),page.getPages(),param.getPageSize(),
                page.getRecords(),page.getCurrent());
        return pageInfoVo;
    }


    /**
     * 大保存...
     * @param productParam
     *
     * 考虑事务....
     * 1）、哪些东西是一定要回滚的、哪些即使出错了不必要回滚的。
     *      商品的核心信息（基本数据、sku）保存的时候，不要受到别的无关信息的影响。
     *      无关信息出问题，核心信息也不用回滚的。
     *
     * 2）、事务的传播行为
     *
     *
     * 如何让某些可以不回滚
     *
     *
     * 复习：事务传播行为，隔离级别
     */
    @Transactional
    @Override
    public void saveProduct(PmsProductParam productParam) {
        //1）、pms_product：保存商品基本信息
        saveBaseInfo(productParam);

        //5）、pms_sku_stock：sku_库存表
        saveSkuStock(productParam);

        //2）、pms_product_attribute_value：保存这个商品对应的所有属性的值
        saveProductAttributeValue(productParam);

        //3）、pms_product_full_reduction：保存商品的满减信息
        saveFullReduction(productParam);

        //4）、pms_product_ladder：满减表
        saveProductLadder(productParam);


    }


    //@Transactional
    public void saveSkuStock(PmsProductParam productParam) {
        List<SkuStock> skuStockList = productParam.getSkuStockList();
        for (int i = 1; i<=skuStockList.size(); i++) {
            SkuStock skuStock = skuStockList.get(i-1);
            if(StringUtils.isEmpty(skuStock.getSkuCode())){
                //skuCode必须有  1_1  1_2 1_3 1_4
                //生成规则  商品id_sku自增id
                skuStock.setSkuCode(threadLocal.get()+"_"+i);
            }
            skuStock.setProductId(threadLocal.get());
            skuStockMapper.insert(skuStock);
        }
        int i = 10/0;

        log.debug("当前线程....{}-->{}",Thread.currentThread().getId(),Thread.currentThread().getName());
    }

    //@Transactional
    public void saveProductLadder(PmsProductParam productParam) {
        List<ProductLadder> productLadderList = productParam.getProductLadderList();
        productLadderList.forEach((productLadder)->{
            productLadder.setProductId(threadLocal.get());
            productLadderMapper.insert(productLadder);

        });

        log.debug("当前线程....{}-->{}",Thread.currentThread().getId(),Thread.currentThread().getName());
    }

    //@Transactional
    public void saveFullReduction(PmsProductParam productParam) {
        List<ProductFullReduction> fullReductionList = productParam.getProductFullReductionList();
        fullReductionList.forEach((reduction)->{
            reduction.setProductId(threadLocal.get());
            productFullReductionMapper.insert(reduction);
        });

        log.debug("当前线程....{}-->{}",Thread.currentThread().getId(),Thread.currentThread().getName());
    }

    /**
     * 保存商品基础信息
     */
    //@Transactional
    public void saveBaseInfo(PmsProductParam productParam){
        //1）、pms_product：保存商品基本信息
        Product product = new Product();
        BeanUtils.copyProperties(productParam,product);
        productMapper.insert(product);
        //mybatis-plus能自动获取到刚才这个数据的自增id
        log.debug("刚才的商品的id：{}",product.getId());
        threadLocal.set(product.getId());

        map.put(Thread.currentThread(),product.getId());
        log.debug("当前线程....{}-->{}",Thread.currentThread().getId(),Thread.currentThread().getName());

    }
    //2）、pms_product_attribute_value：保存这个商品对应的所有属性的值
    //@Transactional
    public void saveProductAttributeValue(PmsProductParam productParam){
        List<ProductAttributeValue> valueList = productParam.getProductAttributeValueList();
        valueList.forEach((item)->{
            Long aLong = map.get(Thread.currentThread());
            System.out.println("利用map存储数据"+aLong);
            item.setProductId(threadLocal.get());
            productAttributeValueMapper.insert(item);

        });

        log.debug("当前线程....{}-->{}",Thread.currentThread().getId(),Thread.currentThread().getName());
    }
}
