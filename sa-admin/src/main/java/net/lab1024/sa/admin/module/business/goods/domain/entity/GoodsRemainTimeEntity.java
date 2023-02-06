package net.lab1024.sa.admin.module.business.goods.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user_goods_time")
public class GoodsRemainTimeEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long goodsId;

    private Long userId;

    private LocalDateTime expiredTime;


    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
