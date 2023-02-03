package net.lab1024.sa.admin.module.business.recharge.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.oa.enterprise.dao.EnterpriseEmployeeDao;
import net.lab1024.sa.admin.module.business.recharge.constant.RechargeKindEnum;
import net.lab1024.sa.admin.module.business.recharge.dao.GiftCardDao;
import net.lab1024.sa.admin.module.business.recharge.dao.RechargeLogDao;
import net.lab1024.sa.admin.module.business.recharge.domain.entity.GiftCard;
import net.lab1024.sa.admin.module.business.recharge.domain.entity.RechargeLog;
import net.lab1024.sa.admin.module.business.recharge.domain.form.GiftCardForm;
import net.lab1024.sa.admin.module.system.employee.dao.EmployeeDao;
import net.lab1024.sa.admin.module.system.employee.domain.vo.EmployeeVO;
import net.lab1024.sa.admin.module.system.login.domain.LoginEmployeeDetail;
import net.lab1024.sa.common.common.code.ErrorCode;
import net.lab1024.sa.common.common.code.UserErrorCode;
import net.lab1024.sa.common.common.domain.PageParam;
import net.lab1024.sa.common.common.domain.PageResult;
import net.lab1024.sa.common.common.domain.RequestUser;
import net.lab1024.sa.common.common.domain.ResponseDTO;
import net.lab1024.sa.common.common.util.SmartPageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RechargeService {

    @Autowired
    private GiftCardDao giftCardDao;

    @Autowired
    private EmployeeDao employeeDao;

    @Autowired
    private RechargeLogDao rechargeLogDao;

    /**
     * 生成礼品卡
     * @param amount
     * @return
     */
    public String generateGiftCard(Long amount) {
        String uuid = IdUtil.randomUUID();

        GiftCard giftCard = new GiftCard();

        giftCard.setAmount(amount);
        giftCard.setCardNo(uuid);
        giftCard.setValidFlag(true);
        giftCard.setExpiredTime(DateUtil.nextMonth().toLocalDateTime());

        giftCardDao.insert(giftCard);

        return uuid;
    }

    /**
     * 使用礼品卡
     * @param cardNo 卡号
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> useGiftCard(String cardNo, RequestUser requestUser) {
        // 1。礼品卡失效
        GiftCard giftCard = giftCardDao.selectByCardNo(cardNo);
        // 错误卡号
        if (giftCard == null) {
            return ResponseDTO.error(UserErrorCode.PARAM_ERROR, "卡号不存在");
        }
        // 卡号失效
        if (!validateGiftCard(giftCard)) return ResponseDTO.error(UserErrorCode.PARAM_ERROR, "礼品卡已失效");
        giftCard.setValidFlag(false);
        // 2。更新用户金额
        Long userId = requestUser.getUserId();
        Long balance = employeeDao.getBalance(requestUser.getUserId()) + giftCard.getAmount();
        // 3. 更新用户的充值记录
        RechargeLog rechargeLog = new RechargeLog();
        rechargeLog.setUserId(userId);
        rechargeLog.setKind(RechargeKindEnum.GIFT_CARD);
        rechargeLog.setAmount(giftCard.getAmount());

        // 4. db update
        employeeDao.updateBalance(userId, balance);
        giftCardDao.updateById(giftCard);
        rechargeLogDao.insert(rechargeLog);

        return ResponseDTO.ok();
    }

    private Boolean validateGiftCard(GiftCard giftCard){
        Boolean isExpired = giftCard.getExpiredTime().isBefore(DateUtil.date().toLocalDateTime());
        Boolean isValid = giftCard.getValidFlag();

        return !isExpired && isValid;
    }

    /**
     * 分页查询礼品卡列表
     * @param giftCardForm
     * @return PageResult<GiftCard>
     */
    public ResponseDTO<PageResult<GiftCard>> queryGiftCard(GiftCardForm giftCardForm) {
        Page page = SmartPageUtil.convert2PageQuery(giftCardForm);

        Page page1 = giftCardDao.selectPage(page, null);

        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(page, page1.getRecords()));
    }

    /**
     * 分页查询当前用户的充值记录
     * @param pageParam 分页参数
     */
    public ResponseDTO<PageResult<RechargeLog>> queryRechargeLog(RequestUser requestUser, PageParam pageParam) {
        Page page = SmartPageUtil.convert2PageQuery(pageParam);
        List<RechargeLog> rechargeLogs = rechargeLogDao.queryByUserId(page, requestUser.getUserId());
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(page, rechargeLogs));
    }
}
