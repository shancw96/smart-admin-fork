package net.lab1024.sa.admin.module.business.goods.domain.vo;

import lombok.Data;
import net.lab1024.sa.admin.module.business.goods.domain.entity.GoodsRemainTimeEntity;

import java.math.BigDecimal;

@Data
public class GoodsRemainTimeVO extends GoodsRemainTimeEntity {
    /**
     * 套餐是否过期
     */
    private Boolean isValid;

    /**
     * 剩余时长
     */
    private Long duration;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品单价
     */
    private BigDecimal price;
}
