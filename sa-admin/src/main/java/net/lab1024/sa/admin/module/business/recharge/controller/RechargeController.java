package net.lab1024.sa.admin.module.business.recharge.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.lab1024.sa.admin.common.AdminBaseController;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.recharge.domain.entity.GiftCard;
import net.lab1024.sa.admin.module.business.recharge.domain.entity.RechargeLog;
import net.lab1024.sa.admin.module.business.recharge.domain.form.GiftCardForm;
import net.lab1024.sa.admin.module.business.recharge.service.RechargeService;
import net.lab1024.sa.common.common.domain.PageParam;
import net.lab1024.sa.common.common.domain.PageResult;
import net.lab1024.sa.common.common.domain.RequestUser;
import net.lab1024.sa.common.common.domain.ResponseDTO;
import net.lab1024.sa.common.common.util.SmartRequestUtil;
import net.lab1024.sa.common.module.support.operatelog.annoation.OperateLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@OperateLog
@RestController
@Api(tags = AdminSwaggerTagConst.Business.AI_RECHARGE)
public class RechargeController extends AdminBaseController {

    @Autowired
    RechargeService rechargeService;

    @ApiOperation("查看充值历史记录")
    @GetMapping("/recharge/history")
    public ResponseDTO<PageResult<RechargeLog>> history(@RequestParam("pageSize") Integer pageSize,
                                                        @RequestParam("pageNum") Integer pageNum) {
        RequestUser requestUser = SmartRequestUtil.getRequestUser();
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(pageNum);
        pageParam.setPageSize(pageSize);
        return rechargeService.queryRechargeLog(requestUser, pageParam);
    }

    @ApiOperation("生成礼品卡")
    @PostMapping("/recharge/gift-card/generate/{amount}")
    public ResponseDTO<String> generateGiftCard(@PathVariable BigDecimal amount) {
        String s = rechargeService.generateGiftCard(amount);
        return ResponseDTO.ok(s);
    }

    @ApiOperation("进行充值-礼品卡(使用礼品卡)")
    @GetMapping("/recharge/gift-card/use")
    public ResponseDTO<String> useGiftCard(@RequestParam String giftCard) {
        RequestUser requestUser = SmartRequestUtil.getRequestUser();
        return rechargeService.useGiftCard(giftCard, requestUser);
    }

    @ApiOperation("查看礼品卡列表")
    @GetMapping("/recharge/gift-card-list")
    public ResponseDTO<PageResult<GiftCard>> getGiftCardList(@RequestBody GiftCardForm giftCardForm) {
        return rechargeService.queryGiftCard(giftCardForm);
    }

}
