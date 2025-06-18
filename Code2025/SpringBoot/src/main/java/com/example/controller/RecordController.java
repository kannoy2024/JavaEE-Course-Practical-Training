package com.example.controller;

import com.example.common.Result;
import com.example.entity.Record;
import com.example.service.RecordService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/record")
public class RecordController {

    @Resource
    RecordService recordService;


    @GetMapping("/selectAll")
    public Result selectAll(Record record) {
        // 调用服务层查询所有管理员
        List<Record> recordList = recordService.selectAll(record);
        return Result.success(recordList);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Record record) {
        recordService.add(record);
        return Result.success();
    }

    @PostMapping("/addBatch")
    public Result addBatch(@RequestBody List<Record> records) {
        try {
            String orderNo = recordService.addBatch(records);
            return Result.success();
        } catch (Exception e) {
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }

    @GetMapping("/selectPage")
    public Result selectPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Record record,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer supplierId) {
        PageInfo<Record> pageInfo = recordService.selectPage(pageNum, pageSize, record, userId, supplierId);
        return Result.success(pageInfo);
    }
}
