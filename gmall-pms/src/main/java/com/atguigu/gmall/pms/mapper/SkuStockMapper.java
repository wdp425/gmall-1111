package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.SkuStock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * sku的库存 Mapper 接口
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
public interface SkuStockMapper extends BaseMapper<SkuStock> {

    SkuStock getSkuStockAndLockStock(Long skuId);

    void lockSkuLockStock(@Param("skuId") Long skuId, @Param("num") Integer num);

    void releaseStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
}
