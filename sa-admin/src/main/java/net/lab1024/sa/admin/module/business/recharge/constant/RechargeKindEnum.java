package net.lab1024.sa.admin.module.business.recharge.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RechargeKindEnum {

    ALIPAY(1, "支付宝"),

    WECHAT(2, "微信"),

    GIFT_CARD(3, "礼品卡"),

    ;

    private final Integer value;

    private final String desc;
}
