package net.lab1024.sa.admin.module.business.key.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.key.domain.entity.UserKeyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface UserKeyDao extends BaseMapper<UserKeyEntity> {

}
