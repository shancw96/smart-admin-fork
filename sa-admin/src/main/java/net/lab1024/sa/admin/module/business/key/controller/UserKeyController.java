package net.lab1024.sa.admin.module.business.key.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.key.Service.UserKeyService;
import net.lab1024.sa.admin.module.business.key.domain.form.KeyUploadForm;
import net.lab1024.sa.common.common.domain.ResponseDTO;
import net.lab1024.sa.common.module.support.operatelog.annoation.OperateLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@OperateLog
@RestController
@Api(tags = AdminSwaggerTagConst.Business.AI_USER_KEY)
public class UserKeyController {
    @Autowired
    UserKeyService userKeyService;


    @ApiOperation("用户key校验")
    @PostMapping("/login/key/check")
    public ResponseDTO<String> validateKey(@RequestBody KeyUploadForm keyForm) {
        return userKeyService.validate(keyForm);
    }

    @ApiOperation("重新生成用户key")
    @PostMapping("/key/create")
    public ResponseDTO<String> createKey() {
        return userKeyService.create();
    }
}
