package net.lab1024.sa.admin.module.business.recharge.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_gift_card")
public class GiftCard {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 金额 15 30 50 100
     */
    private Long amount;

    /**
     * 是否有效
     */
    private Boolean validFlag;

    /**
     * 过期时间
     */
    private LocalDateTime expiredTime;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}

