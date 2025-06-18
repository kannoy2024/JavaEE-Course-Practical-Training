package com.example.controller;

import cn.hutool.core.date.DateUtil;
import com.example.common.Result;
import com.example.entity.Recharge;
import com.example.service.RechargeService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/recharge")
public class RechargeController {

    @Resource
    RechargeService rechargeService;


    @GetMapping("/selectAll")
    public Result selectAll(Recharge recharge) {
        List<Recharge> rechargeList = rechargeService.selectAll(recharge);
        return Result.success(rechargeList);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Recharge recharge) {
        // 设置默认值
        if (recharge.getStatus() == null) {
            recharge.setStatus("处理中");
        }
        if (recharge.getTime() == null) {
            recharge.setTime(DateUtil.now());
        }
        rechargeService.add(recharge);
        return Result.success();
    }


    @GetMapping("/selectPage")
    public Result selectPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Recharge recharge,
            @RequestParam(required = false) Integer userId) {
        PageInfo<Recharge> pageInfo = rechargeService.selectPage(pageNum, pageSize, recharge, userId);
        return Result.success(pageInfo);
    }

    // 完成充值接口
    @PostMapping("/complete/{id}")
    public Result complete(@PathVariable Integer id) {
        rechargeService.updateStatus(id, "已完成");
        return Result.success();
    }

    // 创建充值订单接口
    @PostMapping("/create")
    public Result create(@RequestBody Recharge recharge) {
        recharge.setStatus("处理中");
        rechargeService.add(recharge);
        return Result.success();
    }
}
