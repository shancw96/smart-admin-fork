package net.lab1024.sa.admin.module.business.recharge.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.recharge.domain.entity.RechargeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface RechargeLogDao extends BaseMapper<RechargeLog> {
    List<RechargeLog> queryByUserId(Page page, @Param("userId") Long userId);
}
