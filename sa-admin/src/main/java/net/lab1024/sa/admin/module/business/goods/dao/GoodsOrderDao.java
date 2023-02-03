package net.lab1024.sa.admin.module.business.goods.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.goods.domain.entity.GoodsOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface GoodsOrderDao extends BaseMapper<GoodsOrder> {
    List<GoodsOrder> selectByUserId(Page page, @Param("userId") Long userId);
}
