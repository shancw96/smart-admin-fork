package net.lab1024.sa.admin.module.business.key.controller;

import cn.hutool.extra.servlet.ServletUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.key.Service.UserKeyService;
import net.lab1024.sa.admin.module.business.key.domain.form.KeyUploadForm;
import net.lab1024.sa.common.common.annoation.NoNeedLogin;
import net.lab1024.sa.common.common.domain.ResponseDTO;
import net.lab1024.sa.common.common.util.SmartRequestUtil;
import net.lab1024.sa.common.module.support.operatelog.annoation.OperateLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@OperateLog
@RestController
@Api(tags = AdminSwaggerTagConst.Business.AI_USER_KEY)
public class UserKeyController {
    @Autowired
    UserKeyService userKeyService;


    @NoNeedLogin
    @ApiOperation("用户key校验")
    @PostMapping("/key/check")
    public ResponseDTO<String> validateKey(@RequestBody KeyUploadForm keyForm) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return userKeyService.validate(keyForm, ServletUtil.getClientIP(request));
    }

    @ApiOperation("重新生成用户key")
    @PostMapping("/key/create")
    public ResponseDTO<String> createKey() {
        return userKeyService.create(SmartRequestUtil.getRequestUserId());
    }
}
