package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Recharge;
import com.example.entity.User;
import com.example.mapper.RechargeMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
public class RechargeService {

    @Resource
    RechargeMapper rechargeMapper;

    @Resource
    UserMapper userMapper;

    public List<Recharge> selectAll(Recharge recharge) {
        return rechargeMapper.selectAll(recharge);
    }

    public void add(Recharge recharge) {
        // 确保必要字段不为空
        if (recharge.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (recharge.getAmount() == null) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }
        // 设置默认值
        if (recharge.getStatus() == null) {
            recharge.setStatus("处理中");
        }
        if (recharge.getTime() == null) {
            recharge.setTime(DateUtil.now());
        }

        rechargeMapper.insert(recharge);
    }

    public PageInfo<Recharge> selectPage(Integer pageNum, Integer pageSize, Recharge recharge, Integer userId) {
        // 根据角色添加过滤条件
        if (userId != null) {
            recharge.setUserId(userId); // 普通用户只能查看自己的订单
        }
        // 开启分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<Recharge> recharges = rechargeMapper.selectAll(recharge);

        // 关联查询补充信息
        for (Recharge dbRecharge : recharges) {
            Integer rechargeUserId = dbRecharge.getUserId();
            User user = userMapper.selectById(rechargeUserId != null ? rechargeUserId.toString() : null);

            if (ObjectUtil.isNotNull(user)) {
                dbRecharge.setUserName(user.getName());
            }
        }
        return PageInfo.of(recharges);
    }


    public void updateStatus(Integer id, String status) {
        // 1. 更新订单状态
        rechargeMapper.updateStatus(id, status);

        // 2. 如果是完成状态，更新用户余额
        if ("已完成".equals(status)) {
            Recharge recharge = rechargeMapper.selectByOrderId(id);
            if (recharge != null && recharge.getUserId() != null) {
                try {
                    String userIdStr = recharge.getUserId().toString();
                    BigDecimal amount = recharge.getAmount() != null ?
                            new BigDecimal(recharge.getAmount().toString()) : BigDecimal.ZERO;

                    // 3. 更新用户余额
                    userMapper.updateBalance(userIdStr, amount);
                } catch (Exception e) {
                    throw new RuntimeException("更新用户余额失败");
                }
            }
        }
    }
}