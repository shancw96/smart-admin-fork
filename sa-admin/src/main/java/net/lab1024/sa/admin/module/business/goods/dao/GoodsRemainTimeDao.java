package net.lab1024.sa.admin.module.business.goods.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.goods.domain.entity.GoodsRemainTimeEntity;
import net.lab1024.sa.admin.module.business.goods.domain.vo.GoodsRemainTimeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Mapper
@Component
public interface GoodsRemainTimeDao extends BaseMapper<GoodsRemainTimeEntity> {
    Optional<GoodsRemainTimeEntity> queryByUserId(@Param("userId") Long userId);

    Optional<GoodsRemainTimeEntity> queryByGoodsId(@Param("goodsId") Long goodsId);

    List<GoodsRemainTimeEntity> queryAllByRoleIds(@Param("roleIds") List<Long> roleIds);

    void batchUpdateExpiredTime(@Param("gtList") List<GoodsRemainTimeEntity> gtList);

    List<GoodsRemainTimeVO> queryAllByUserId(@Param("userId") Long requestUserId);

    List<GoodsRemainTimeEntity> queryAllByUserSecret(@Param("secret") String secret);
}
