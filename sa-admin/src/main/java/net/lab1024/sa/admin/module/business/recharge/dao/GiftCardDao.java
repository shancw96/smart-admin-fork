package net.lab1024.sa.admin.module.business.recharge.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.recharge.domain.entity.GiftCard;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface GiftCardDao extends BaseMapper<GiftCard> {

    GiftCard selectByCardNo(String cardNo);
}
