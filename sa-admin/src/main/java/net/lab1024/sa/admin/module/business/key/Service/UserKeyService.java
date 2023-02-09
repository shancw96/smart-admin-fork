package net.lab1024.sa.admin.module.business.key.Service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.goods.dao.GoodsRemainTimeDao;
import net.lab1024.sa.admin.module.business.goods.domain.entity.GoodsRemainTimeEntity;
import net.lab1024.sa.admin.module.business.goods.domain.vo.GoodsRemainTimeVO;
import net.lab1024.sa.admin.module.business.key.dao.UserKeyDao;
import net.lab1024.sa.admin.module.business.key.domain.entity.UserKeyEntity;
import net.lab1024.sa.admin.module.business.key.domain.form.KeyUploadForm;
import net.lab1024.sa.admin.module.system.login.domain.LoginEmployeeDetail;
import net.lab1024.sa.common.common.code.UserErrorCode;
import net.lab1024.sa.common.common.domain.ResponseDTO;
import net.lab1024.sa.common.constant.RedisKeyConst;
import net.lab1024.sa.common.module.support.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;


@Slf4j
@Service
public class UserKeyService {

    /**
     * 过期时间： 40MIN
     */
    private static final long EXPIRED_MINUTE = 40;

    private final byte[] secretKey = new byte[]{'T', 'h', 'i', 's', 'I', 's', 'A', 'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y'};

    /**
     * 登录信息二级缓存
     */
    private ConcurrentMap<Long, UserKeyEntity> userVerifyCache = new ConcurrentLinkedHashMap.Builder<Long, UserKeyEntity>().maximumWeightedCapacity(1000).build();


    @Autowired
    private UserKeyDao userKeyDao;


    @Autowired
    private GoodsRemainTimeDao goodsRemainTimeDao;

    @Autowired
    private RedisService redisService;


    /**
     * 客户端每隔一段时间会进行一次验证，验证通过返回加密后的goodsId
     *
     * @param form
     * @return
     */
    public ResponseDTO<String> validate(KeyUploadForm form, String ip) {
        // 从redis中获取secretKey
        String redisSecretKey = redisService.generateRedisKey(RedisKeyConst.Support.USER_SECRET_KEY, form.getSecret());
        String lastIp = redisService.get(redisSecretKey);
        log.info("lastIp:{}", lastIp);
        // 如果从来没有验证过，或者上次验证过的ip和当前ip一致
        if (lastIp == null || lastIp.equals(ip)) {
            // set secret <-> ip to redis
            redisService.set(redisSecretKey, ip, EXPIRED_MINUTE * 60);
            // -------------- 对权限进行加密处理 --------------
            SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, secretKey);
            List<GoodsRemainTimeEntity> goodsRemainTimeList = goodsRemainTimeDao.queryAllByUserSecret(form.getSecret());
            // 获取所有未过期(expiredTime LocalDateTime)的goodsRemainTimeEntity
            List<Long> goodsId = goodsRemainTimeList.stream()
                    .filter(entity -> entity.getExpiredTime().compareTo(LocalDateTime.now()) > 0)
                    .map(entity -> entity.getGoodsId()).collect(Collectors.toList());

            // goodsId join with , and encrypt
            String goodIdStr = goodsId.stream().map(String::valueOf).collect(Collectors.joining(","));
            // 加上时间混淆
            String goodsIdTime = goodIdStr.concat(",").concat(DateUtil.formatDateTime(new DateTime()));
            //加密
            byte[] encrypt = aes.encrypt(goodsIdTime);
            return ResponseDTO.ok(new String(encrypt));
        } else {
            return ResponseDTO.error(UserErrorCode.USER_STATUS_ERROR, "当前已有用户登录，ip为".concat(lastIp).concat("请30min后再试"));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public UserKeyEntity create(Long userId) {
        //生成的是不带-的字符串，类似于：b17f24ff026d40949c85a24f4f375d42
        String simpleUUID = IdUtil.simpleUUID();

        UserKeyEntity keyEntity = new UserKeyEntity();
        keyEntity.setUserId(userId);
        keyEntity.setSecret(simpleUUID);

        userKeyDao.insert(keyEntity);
        return keyEntity;
    }

}
