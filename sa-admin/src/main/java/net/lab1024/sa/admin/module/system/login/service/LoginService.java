package net.lab1024.sa.admin.module.system.login.service;

import cn.hutool.extra.servlet.ServletUtil;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.system.department.domain.vo.DepartmentVO;
import net.lab1024.sa.admin.module.system.department.service.DepartmentService;
import net.lab1024.sa.admin.module.system.employee.domain.entity.EmployeeEntity;
import net.lab1024.sa.admin.module.system.employee.service.EmployeePermissionService;
import net.lab1024.sa.admin.module.system.employee.service.EmployeeService;
import net.lab1024.sa.admin.module.system.login.domain.LoginEmployeeDetail;
import net.lab1024.sa.admin.module.system.login.domain.LoginForm;
import net.lab1024.sa.admin.module.system.menu.domain.vo.MenuVO;
import net.lab1024.sa.common.common.constant.RequestHeaderConst;
import net.lab1024.sa.common.common.constant.StringConst;
import net.lab1024.sa.common.common.domain.RequestUser;
import net.lab1024.sa.common.common.domain.ResponseDTO;
import net.lab1024.sa.common.common.enumeration.UserTypeEnum;
import net.lab1024.sa.common.common.util.SmartBeanUtil;
import net.lab1024.sa.common.common.util.SmartEnumUtil;
import net.lab1024.sa.common.module.support.captcha.CaptchaService;
import net.lab1024.sa.common.module.support.captcha.domain.CaptchaVO;
import net.lab1024.sa.common.module.support.config.ConfigKeyEnum;
import net.lab1024.sa.common.module.support.config.ConfigService;
import net.lab1024.sa.common.module.support.loginlog.LoginLogResultEnum;
import net.lab1024.sa.common.module.support.loginlog.LoginLogService;
import net.lab1024.sa.common.module.support.loginlog.domain.LoginLogEntity;
import net.lab1024.sa.common.module.support.loginlog.domain.LoginLogVO;
import net.lab1024.sa.common.module.support.token.LoginDeviceEnum;
import net.lab1024.sa.common.module.support.token.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * ?????? ????????????
 *
 * @Author 1024???????????????: ??????
 * @Date 2021-12-01 22:56:34
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright 1024??????????????? ??? https://1024lab.net ??????2012-2022
 */
@Slf4j
@Service
public class LoginService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private EmployeePermissionService employeePermissionService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private LoginLogService loginLogService;

    /**
     * ????????????????????????
     */
    private ConcurrentMap<Long, LoginEmployeeDetail> loginUserDetailCache = new ConcurrentLinkedHashMap.Builder<Long, LoginEmployeeDetail>().maximumWeightedCapacity(1000).build();

    /**
     * ???????????????
     *
     * @return
     */
    public ResponseDTO<CaptchaVO> getCaptcha() {
        return ResponseDTO.ok(captchaService.generateCaptcha());
    }

    /**
     * ????????????
     *
     * @param loginForm
     * @return ????????????????????????
     */
    public ResponseDTO<LoginEmployeeDetail> login(LoginForm loginForm, String ip, String userAgent) {
        LoginDeviceEnum loginDeviceEnum = SmartEnumUtil.getEnumByValue(loginForm.getLoginDevice(), LoginDeviceEnum.class);
        if (loginDeviceEnum == null) {
            return ResponseDTO.userErrorParam("???????????????????????????");
        }
        // ?????? ???????????????
        ResponseDTO<String> checkCaptcha = captchaService.checkCaptcha(loginForm);
        if (!checkCaptcha.getOk()) {
            return ResponseDTO.error(checkCaptcha);
        }

        /**
         * ???????????????????????????
         */
        EmployeeEntity employeeEntity = employeeService.getByLoginName(loginForm.getLoginName());
        if (null == employeeEntity) {
            return ResponseDTO.userErrorParam("?????????????????????");
        }

        if (employeeEntity.getDisabledFlag()) {
            saveLoginLog(employeeEntity, ip, userAgent, "???????????????", LoginLogResultEnum.LOGIN_FAIL);
            return ResponseDTO.userErrorParam("????????????????????????,????????????????????????");
        }
        /**
         * ???????????????
         * 1???????????????
         * 2???????????????
         */
        String superPassword = EmployeeService.getEncryptPwd(configService.getConfigValue(ConfigKeyEnum.SUPER_PASSWORD));
        String requestPassword = EmployeeService.getEncryptPwd(loginForm.getPassword());
        if (!(superPassword.equals(requestPassword) || employeeEntity.getLoginPwd().equals(requestPassword))) {
            saveLoginLog(employeeEntity, ip, userAgent, "????????????", LoginLogResultEnum.LOGIN_FAIL);
            return ResponseDTO.userErrorParam("???????????????????????????");
        }

        // ?????? ??????token?????????token
        Boolean superPasswordFlag = superPassword.equals(requestPassword);
        String token = tokenService.generateToken(employeeEntity.getEmployeeId(), employeeEntity.getActualName(), UserTypeEnum.ADMIN_EMPLOYEE, loginDeviceEnum, superPasswordFlag);

        //????????????????????????
        LoginEmployeeDetail loginEmployeeDetail = loadLoginInfo(employeeEntity);
        loginEmployeeDetail.setToken(token);

        // ????????????
        loginUserDetailCache.put(employeeEntity.getEmployeeId(), loginEmployeeDetail);

        //??????????????????
        saveLoginLog(employeeEntity, ip, userAgent, superPasswordFlag ? "??????????????????" : loginDeviceEnum.getDesc(), LoginLogResultEnum.LOGIN_SUCCESS);

        return ResponseDTO.ok(loginEmployeeDetail);
    }


    /**
     * ???????????????????????????
     *
     * @return
     */
    private LoginEmployeeDetail loadLoginInfo(EmployeeEntity employeeEntity) {
        LoginEmployeeDetail loginEmployeeDetail = SmartBeanUtil.copy(employeeEntity, LoginEmployeeDetail.class);
        loginEmployeeDetail.setUserType(UserTypeEnum.ADMIN_EMPLOYEE);

        //????????????
        DepartmentVO department = departmentService.getDepartmentById(employeeEntity.getDepartmentId());
        loginEmployeeDetail.setDepartmentName(null == department ? StringConst.EMPTY : department.getName());

        /**
         * ?????????????????????????????????
         * 1????????????????????????????????????
         * 2?????????????????????????????????
         */
        List<MenuVO> menuAndPointsList = employeePermissionService.getEmployeeMenuAndPointsList(employeeEntity.getEmployeeId(), employeeEntity.getAdministratorFlag());
        //????????????
        loginEmployeeDetail.setMenuList(menuAndPointsList);
        //????????????
        loginEmployeeDetail.setAuthorities(employeePermissionService.buildAuthorities(menuAndPointsList));

        //??????????????????
        LoginLogVO loginLogVO = loginLogService.queryLastByUserId(employeeEntity.getEmployeeId(), UserTypeEnum.ADMIN_EMPLOYEE);
        if (loginLogVO != null) {
            loginEmployeeDetail.setLastLoginIp(loginLogVO.getLoginIp());
            loginEmployeeDetail.setLastLoginTime(loginLogVO.getCreateTime());
            loginEmployeeDetail.setLastLoginUserAgent(loginLogVO.getUserAgent());
        }

        return loginEmployeeDetail;
    }

    /**
     * ??????????????????
     *
     * @param employeeEntity
     * @param ip
     * @param userAgent
     */
    private void saveLoginLog(EmployeeEntity employeeEntity, String ip, String userAgent, String remark, LoginLogResultEnum result) {
        LoginLogEntity loginEntity = LoginLogEntity.builder()
                .userId(employeeEntity.getEmployeeId())
                .userType(UserTypeEnum.ADMIN_EMPLOYEE.getValue())
                .userName(employeeEntity.getActualName())
                .userAgent(userAgent)
                .loginIp(ip)
                .remark(remark)
                .loginResult(result.getValue())
                .createTime(LocalDateTime.now())
                .build();
        loginLogService.log(loginEntity);
    }


    /**
     * ????????????????????????
     *
     * @param requestUserId
     */
    public void removeLoginUserDetailCache(Long requestUserId) {
        loginUserDetailCache.remove(requestUserId);
    }

    /**
     * ????????????token ????????????????????????
     *
     * @param
     * @return
     */
    public LoginEmployeeDetail getLoginUserDetail(String token, HttpServletRequest request) {
        Long requestUserId = tokenService.getUserIdAndValidateToken(token);
        if (requestUserId == null) {
            return null;
        }
        // ??????????????????
        LoginEmployeeDetail loginEmployeeDetail = loginUserDetailCache.get(requestUserId);
        if (loginEmployeeDetail == null) {
            // ??????????????????
            EmployeeEntity employeeEntity = employeeService.getById(requestUserId);
            if (employeeEntity == null) {
                return null;
            }

            loginEmployeeDetail = this.loadLoginInfo(employeeEntity);
            loginEmployeeDetail.setToken(token);
            loginUserDetailCache.put(requestUserId, loginEmployeeDetail);
        }

        //????????????ip???user agent
        loginEmployeeDetail.setUserAgent(ServletUtil.getHeaderIgnoreCase(request, RequestHeaderConst.USER_AGENT));
        loginEmployeeDetail.setIp(ServletUtil.getClientIP(request));

        return loginEmployeeDetail;
    }


    /**
     * ?????????????????????token??????
     *
     * @return
     */
    public ResponseDTO<String> logout(String token, RequestUser requestUser) {
        loginUserDetailCache.remove(requestUser.getUserId());
        tokenService.removeToken(token);
        //??????????????????
        saveLogoutLog(requestUser, requestUser.getIp(), requestUser.getUserAgent());
        return ResponseDTO.ok();
    }

    /**
     * ??????????????????
     */
    private void saveLogoutLog(RequestUser requestUser, String ip, String userAgent) {
        LoginLogEntity loginEntity = LoginLogEntity.builder()
                .userId(requestUser.getUserId())
                .userName(requestUser.getUserName())
                .userAgent(userAgent)
                .loginIp(ip)
                .loginResult(LoginLogResultEnum.LOGIN_OUT.getValue())
                .createTime(LocalDateTime.now())
                .build();
        loginLogService.log(loginEntity);
    }
}
