package net.lab1024.sa.admin.module.business.recharge.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.lab1024.sa.admin.module.business.recharge.constant.RechargeKindEnum;

import java.time.LocalDateTime;

@Data
@TableName("t_user_recharge_log")
public class RechargeLog {
    @TableId(type= IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 充值金额
     */
    private Long amount;

    /**
     * 充值方式
     */
    private RechargeKindEnum kind;


    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
