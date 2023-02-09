package net.lab1024.sa.admin.module.system.login.controller;

import cn.hutool.extra.servlet.ServletUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.key.Service.UserKeyService;
import net.lab1024.sa.admin.module.business.key.dao.UserKeyDao;
import net.lab1024.sa.admin.module.business.key.domain.entity.UserKeyEntity;
import net.lab1024.sa.admin.module.system.employee.dao.EmployeeDao;
import net.lab1024.sa.admin.module.system.login.domain.LoginEmployeeDetail;
import net.lab1024.sa.admin.module.system.login.domain.LoginForm;
import net.lab1024.sa.admin.module.system.login.service.LoginService;
import net.lab1024.sa.common.common.annoation.NoNeedLogin;
import net.lab1024.sa.common.common.code.UserErrorCode;
import net.lab1024.sa.common.common.constant.RequestHeaderConst;
import net.lab1024.sa.common.common.domain.ResponseDTO;
import net.lab1024.sa.common.common.util.SmartRequestUtil;
import net.lab1024.sa.common.module.support.captcha.domain.CaptchaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 员工登录
 *
 * @Author 1024创新实验室-主任:卓大
 * @Date 2021-12-15 21:05:46
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright 1024创新实验室 （ https://1024lab.net ），2012-2022
 */
@RestController
@Api(tags = {AdminSwaggerTagConst.System.SYSTEM_LOGIN})
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private EmployeeDao employeeDao;

    @Autowired
    private UserKeyDao userKeyDao;

    @Autowired
    private UserKeyService userKeyService;

    @NoNeedLogin
    @PostMapping("/login")
    @ApiOperation("登录 @author 卓大")
    public ResponseDTO<LoginEmployeeDetail> login(@Valid @RequestBody LoginForm loginForm) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = ServletUtil.getClientIP(request);
        String userAgent = ServletUtil.getHeaderIgnoreCase(request, RequestHeaderConst.USER_AGENT);
        return loginService.login(loginForm, ip, userAgent);
    }

    @GetMapping("/login/refresh")
    @ApiOperation("刷新用户信息（包含用户基础信息、权限信息等等）  @author 卓大")
    public ResponseDTO<String> refresh() {
        loginService.removeLoginUserDetailCache(SmartRequestUtil.getRequestUserId());
        return ResponseDTO.ok();
    }

    @GetMapping("/login/getLoginInfo")
    @ApiOperation("获取登录结果信息  @author 卓大")
    public ResponseDTO<LoginEmployeeDetail> getLoginInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseDTO.error(UserErrorCode.LOGIN_STATE_INVALID);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof LoginEmployeeDetail)) {
            return ResponseDTO.error(UserErrorCode.LOGIN_STATE_INVALID);
        }

        LoginEmployeeDetail loginEmployeeDetail = (LoginEmployeeDetail) authentication.getPrincipal();
        loginEmployeeDetail.setLoginPassword(null);

        // 获取用户余额 - 实时获取
        loginEmployeeDetail.setBalance(employeeDao.getBalance(loginEmployeeDetail.getUserId()));

        // 设置用户secret
        String s = userKeyDao.selectKeyByUserId(loginEmployeeDetail.getUserId());
        if(s == null){
            UserKeyEntity userKeyEntity = userKeyService.create(loginEmployeeDetail.getUserId());
            s = userKeyEntity.getSecret();
        }
        loginEmployeeDetail.setSecretKey(s);

        return ResponseDTO.ok(loginEmployeeDetail);
    }

    @ApiOperation("退出登陆  @author 卓大")
    @GetMapping("/login/logout")
    public ResponseDTO<String> logout(@RequestHeader(value = RequestHeaderConst.TOKEN, required = false) String token) {
        return loginService.logout(token, SmartRequestUtil.getRequestUser());
    }

    @ApiOperation("获取验证码  @author 卓大")
    @GetMapping("/login/getCaptcha")
    @NoNeedLogin
    public ResponseDTO<CaptchaVO> getCaptcha() {
        return loginService.getCaptcha();
    }

}
