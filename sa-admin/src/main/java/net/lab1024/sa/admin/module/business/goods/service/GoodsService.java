package net.lab1024.sa.admin.module.business.goods.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.category.constant.CategoryTypeEnum;
import net.lab1024.sa.admin.module.business.category.domain.entity.CategoryEntity;
import net.lab1024.sa.admin.module.business.category.service.CategoryQueryService;
import net.lab1024.sa.admin.module.business.goods.constant.GoodsStatusEnum;
import net.lab1024.sa.admin.module.business.goods.dao.GoodsDao;
import net.lab1024.sa.admin.module.business.goods.dao.GoodsOrderDao;
import net.lab1024.sa.admin.module.business.goods.dao.GoodsRemainTimeDao;
import net.lab1024.sa.admin.module.business.goods.domain.entity.GoodsEntity;
import net.lab1024.sa.admin.module.business.goods.domain.entity.GoodsOrder;
import net.lab1024.sa.admin.module.business.goods.domain.entity.GoodsRemainTimeEntity;
import net.lab1024.sa.admin.module.business.goods.domain.form.GoodsAddForm;
import net.lab1024.sa.admin.module.business.goods.domain.form.GoodsQueryForm;
import net.lab1024.sa.admin.module.business.goods.domain.form.GoodsUpdateForm;
import net.lab1024.sa.admin.module.business.goods.domain.vo.GoodsRemainTimeVO;
import net.lab1024.sa.admin.module.business.goods.domain.vo.GoodsVO;
import net.lab1024.sa.admin.module.business.goods.manager.GoodsManager;
import net.lab1024.sa.admin.module.business.recharge.dao.RechargeLogDao;
import net.lab1024.sa.admin.module.business.recharge.domain.entity.RechargeLog;
import net.lab1024.sa.admin.module.system.employee.dao.EmployeeDao;
import net.lab1024.sa.admin.module.system.employee.domain.entity.EmployeeEntity;
import net.lab1024.sa.admin.module.system.role.dao.RoleDao;
import net.lab1024.sa.admin.module.system.role.domain.entity.RoleEntity;
import net.lab1024.sa.common.common.code.UserErrorCode;
import net.lab1024.sa.common.common.domain.PageParam;
import net.lab1024.sa.common.common.domain.PageResult;
import net.lab1024.sa.common.common.domain.RequestUser;
import net.lab1024.sa.common.common.domain.ResponseDTO;
import net.lab1024.sa.common.common.util.SmartBeanUtil;
import net.lab1024.sa.common.common.util.SmartPageUtil;
import net.lab1024.sa.common.module.support.datatracer.constant.DataTracerTypeEnum;
import net.lab1024.sa.common.module.support.datatracer.service.DataTracerService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ??????
 *
 * @Author 1024???????????????: ??????
 * @Date 2021-10-25 20:26:54
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright 1024??????????????? ??? https://1024lab.net ??????2012-2022
 */
@Slf4j
@Service
public class GoodsService extends ServiceImpl<GoodsRemainTimeDao, GoodsRemainTimeEntity> {
    @Autowired
    private GoodsDao goodsDao;

    @Autowired
    private CategoryQueryService categoryQueryService;

    @Autowired
    private DataTracerService dataTracerService;

    @Autowired
    private EmployeeDao employeeDao;

    @Autowired
    private GoodsOrderDao goodsOrderDao;

    @Autowired
    private GoodsRemainTimeDao goodsRemainTimeDao;

    @Autowired
    private RoleDao roleDao;

    /**
     * ????????????
     *
     * @param addForm
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> add(GoodsAddForm addForm) {
        // ????????????
        ResponseDTO<String> res = this.checkGoods(addForm, null);
        if (!res.getOk()) {
            return res;
        }
        GoodsEntity goodsEntity = SmartBeanUtil.copy(addForm, GoodsEntity.class);
        goodsEntity.setDeletedFlag(Boolean.FALSE);
        goodsDao.insert(goodsEntity);
        dataTracerService.insert(goodsEntity.getGoodsId(), DataTracerTypeEnum.GOODS);
        return ResponseDTO.ok();
    }

    /**
     * ????????????
     *
     * @param updateForm
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> update(GoodsUpdateForm updateForm) {
        // ????????????
        ResponseDTO<String> res = this.checkGoods(updateForm, updateForm.getGoodsId());
        if (!res.getOk()) {
            return res;
        }
        GoodsEntity originEntity = goodsDao.selectById(updateForm.getGoodsId());
        GoodsEntity goodsEntity = SmartBeanUtil.copy(updateForm, GoodsEntity.class);
        goodsDao.updateById(goodsEntity);
        dataTracerService.update(updateForm.getGoodsId(), DataTracerTypeEnum.GOODS, originEntity, goodsEntity);
        return ResponseDTO.ok();
    }

    /**
     * ??????/?????? ????????????
     *
     * @param addForm
     * @param goodsId ????????? ??????????????????
     * @return
     */
    private ResponseDTO<String> checkGoods(GoodsAddForm addForm, Long goodsId) {
        // ????????????id
        Long categoryId = addForm.getCategoryId();
        Optional<CategoryEntity> optional = categoryQueryService.queryCategory(categoryId);
        if (!optional.isPresent() || !CategoryTypeEnum.GOODS.equalsValue(optional.get().getCategoryType())) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST, "?????????????????????~");
        }

        return ResponseDTO.ok();
    }

    /**
     * ??????
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> delete(Long goodsId) {
        GoodsEntity goodsEntity = goodsDao.selectById(goodsId);
        if (goodsEntity == null) {
            return ResponseDTO.userErrorParam("???????????????");
        }

        if (!goodsEntity.getGoodsStatus().equals(GoodsStatusEnum.SELL_OUT.getValue())) {
            return ResponseDTO.userErrorParam("????????????????????????????????????");
        }

        batchDelete(Arrays.asList(goodsId));
        dataTracerService.batchDelete(Arrays.asList(goodsId), DataTracerTypeEnum.GOODS);
        return ResponseDTO.ok();
    }

    /**
     * ????????????
     */
    public ResponseDTO<String> batchDelete(List<Long> goodsIdList) {
        if (CollectionUtils.isEmpty(goodsIdList)) {
            return ResponseDTO.ok();
        }

        goodsDao.batchUpdateDeleted(goodsIdList, Boolean.TRUE);
        return ResponseDTO.ok();
    }


    /**
     * ????????????
     *
     * @param queryForm
     * @return
     */
    public ResponseDTO<PageResult<GoodsVO>> query(GoodsQueryForm queryForm) {
        queryForm.setDeletedFlag(false);
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<GoodsVO> list = goodsDao.query(page, queryForm);
        PageResult<GoodsVO> pageResult = SmartPageUtil.convert2PageResult(page, list);
        if (pageResult.getEmptyFlag()) {
            return ResponseDTO.ok(pageResult);
        }
        // ??????????????????
        List<Long> categoryIdList = list.stream().map(GoodsVO::getCategoryId).distinct().collect(Collectors.toList());
        Map<Long, CategoryEntity> categoryMap = categoryQueryService.queryCategoryList(categoryIdList);
        list.forEach(e -> {
            CategoryEntity categoryEntity = categoryMap.get(e.getCategoryId());
            if (categoryEntity != null) {
                e.setCategoryName(categoryEntity.getCategoryName());
            }
        });
        return ResponseDTO.ok(pageResult);
    }


    /**
     * ????????????
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO buy(Long userId, Long goodsId) {

        GoodsEntity goodsEntity = goodsDao.selectById(goodsId);
        EmployeeEntity employeeEntity = employeeDao.selectById(userId);

        if (goodsEntity == null) {
            return ResponseDTO.userErrorParam("???????????????");
        }

        if(employeeEntity.getBalance().compareTo(goodsEntity.getPrice()) < 0){
            return ResponseDTO.userErrorParam("??????????????????????????????");
        }

        // ??????????????????
        GoodsOrder goodsOrder = new GoodsOrder();
        goodsOrder.setOrderNo(IdUtil.randomUUID());
        goodsOrder.setGoodsId(goodsId);
        goodsOrder.setUserId(userId);
        goodsOrder.setCost(goodsEntity.getPrice());
        goodsOrderDao.insert(goodsOrder);

        // ??????????????????
        employeeDao.updateBalance(userId, employeeEntity.getBalance().subtract(goodsDao.selectById(goodsId).getPrice()));

        // ????????????????????????
        GoodsRemainTimeEntity goodsRemainTimeEntity1 = goodsRemainTimeDao.queryByGoodsId(goodsId).orElseGet(() -> {
            GoodsRemainTimeEntity goodsRemainTimeEntity = new GoodsRemainTimeEntity();
            goodsRemainTimeEntity.setUserId(userId);
            goodsRemainTimeEntity.setGoodsId(goodsId);
            goodsRemainTimeEntity.setExpiredTime(LocalDateTime.now().minusDays(1));
            return goodsRemainTimeEntity;
        });
        // ???????????????????????????????????????expired_time ?????????today
        if (goodsRemainTimeEntity1.getExpiredTime().compareTo(LocalDateTime.now()) < 0) {
            goodsRemainTimeEntity1.setExpiredTime(LocalDateTime.now());
        }
        // ???????????????????????? goodsEntity.getDuration() ???
        goodsRemainTimeEntity1.setExpiredTime(goodsRemainTimeEntity1.getExpiredTime().plusDays(goodsEntity.getDuration()));

        // ??????????????????
        if (goodsRemainTimeEntity1.getId() == null) {
            goodsRemainTimeDao.insert(goodsRemainTimeEntity1);
        } else {
            goodsRemainTimeDao.updateById(goodsRemainTimeEntity1);
        }

        return ResponseDTO.ok();
    }

    /**
     * ##### ?????? #####
     * ?????????????????????0??? ??????????????????????????????expiredTime ??? ??????????????????????????? expiredTime < currentTime ?????????????????????true
     */
//    @Scheduled(cron = "59 59 23 * * ?")
    public void updateGoodsRemainTime() {
        log.info("???????????? updateGoodsRemainTime start");
        String excludeRole = "??????";
        // ????????????id??????????????????????????? userIds
        List<RoleEntity> roleEntityList = roleDao.selectList(null);
        List<Long> roleIds = roleEntityList
                .stream()
                .filter(roleEntity -> !roleEntity.getRoleName().equals(excludeRole))
                .map(roleEntity -> roleEntity.getRoleId())
                .collect(Collectors.toList());

        // ??????roleIds ??????????????? List<GoodsRemainTimeEntity> gtList
        // ??????gtList ?????? expired_time??????????????? - 1
        List<GoodsRemainTimeEntity> gtList = goodsRemainTimeDao
                .queryAllByRoleIds(roleIds)
                .stream()
                .filter(gt -> gt.getExpiredTime().compareTo(LocalDateTime.now()) > 0)
                .map(gt -> {
                    gt.setExpiredTime(gt.getExpiredTime().minusDays(1));
                    return gt;
                })
                .collect(Collectors.toList());
        // ??????gtList?????????????????????
        if (gtList.size() > 0) {
            goodsRemainTimeDao.batchUpdateExpiredTime(gtList);
        }
    }

    /**
     * ???????????????????????????????????????
     */
    public ResponseDTO<PageResult<GoodsOrder>> orderHistory(Long userId, PageParam pageParam) {
        Page page = SmartPageUtil.convert2PageQuery(pageParam);
        List<GoodsOrder> goodsOrders = goodsOrderDao.selectByUserId(page, userId);
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(page, goodsOrders));
    }

    /**
     * ?????????????????????????????????????????????????????????
     */
    public ResponseDTO<List<GoodsRemainTimeVO>> comboList(Long requestUserId) {
        List<GoodsRemainTimeVO> goodsRemainTimeEntities = goodsRemainTimeDao.queryAllByUserId(requestUserId);
        List<GoodsRemainTimeVO> goodsRemainTimeVOS = goodsRemainTimeEntities
                .stream()
                .map(vo -> {
                    vo.setIsValid(vo.getExpiredTime().compareTo(LocalDateTime.now()) > 0);
                    vo.setDuration(
                            vo.getExpiredTime().compareTo(LocalDateTime.now()) > 0 ?
                            vo.getExpiredTime().toLocalDate().toEpochDay() - LocalDateTime.now().toLocalDate().toEpochDay() : 0);
                    return vo;
                })
                .collect(Collectors.toList());
        return ResponseDTO.ok(goodsRemainTimeVOS);
    }
}
