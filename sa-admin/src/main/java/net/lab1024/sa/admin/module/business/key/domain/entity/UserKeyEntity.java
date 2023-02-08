package net.lab1024.sa.admin.module.business.key.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserKeyEntity {

    @TableId(type= IdType.AUTO)
    private Long id;

    private Long userId;

    private String key;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
