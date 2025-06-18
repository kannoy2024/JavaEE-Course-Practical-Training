package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Goods;
import com.example.entity.Record;
import com.example.entity.Supplier;
import com.example.entity.User;
import com.example.mapper.GoodsMapper;
import com.example.mapper.RecordMapper;
import com.example.mapper.SupplierMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Service
public class RecordService {

    @Resource
    RecordMapper recordMapper;

    @Resource
    SupplierMapper supplierMapper;

    @Resource
    UserMapper userMapper;
    @Autowired
    private GoodsMapper goodsMapper;


    public List<Record> selectAll(Record record) {
        return recordMapper.selectAll(record);
    }

    public void add(Record record) {
        recordMapper.insert(record);
    }

    public PageInfo<Record> selectPage(Integer pageNum, Integer pageSize, Record record, Integer userId, Integer supplierId) {
        // 根据角色添加过滤条件
        if (userId != null) {
            record.setUserId(userId); // 普通用户只能查看自己的订单
        }
        if (supplierId != null) {
            record.setSupplierId(supplierId); // 供应商只能查看自己供应的订单
        }

        // 开启分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<Record> records = recordMapper.selectAll(record);

        // 关联查询补充信息
        for (Record dbRecord : records) {
            Integer recordSupplierId = dbRecord.getSupplierId();
            Integer goodId = dbRecord.getGoodId();
            Integer recordUserId = dbRecord.getUserId();

            Supplier supplier = supplierMapper.selectById(recordSupplierId != null ? recordSupplierId.toString() : null);
            Goods goods = goodsMapper.selectById(goodId);
            User user = userMapper.selectById(recordUserId != null ? recordUserId.toString() : null);

            if (ObjectUtil.isNotNull(supplier)) {
                dbRecord.setSupplierName(supplier.getName());
            }
            if (ObjectUtil.isNotNull(goods)) {
                dbRecord.setGoodsName(goods.getGname());
            }
            if (ObjectUtil.isNotNull(user)) {
                dbRecord.setUserName(user.getName());
            }
        }

        return PageInfo.of(records);
    }

    // 生成订单号的方法
    public String generateOrderNo() {
        // 示例: ORD + 时间戳 + 随机数
        return "ORD" + System.currentTimeMillis() + (int) (Math.random() * 1000);
    }

    //多选购买
    public String addBatch(List<Record> records) {
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("订单列表不能为空");
        }

        String orderNo = "ORD" + System.currentTimeMillis();

        records.forEach(record -> {
            record.setOrderNo(orderNo);
            record.setTime(DateUtil.now());

            // 确保价格不为null
            if (record.getUnitPrice() == null) {
                record.setUnitPrice(BigDecimal.ZERO);
            }
            if (record.getTotalPrice() == null) {
                record.setTotalPrice(BigDecimal.ZERO);
            }
        });

        try {
            recordMapper.insertBatch(records);
            return orderNo;
        } catch (Exception e) {
            throw new RuntimeException("批量插入订单失败: " + e.getMessage());
        }
    }


    @Transactional
    public void completeOrder(Integer orderId) {
        // 查询订单 - 使用修正后的方法名和参数
        Record record = recordMapper.selectByOrderId(orderId);
        if (record == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证订单状态
        if (Record.Status.COMPLETED.getValue().equals(record.getStatus())) {
            throw new RuntimeException("订单已完成，无需重复操作");
        }

        // 扣除用户余额
        User user = userMapper.selectById(record.getUserId().toString());
        BigDecimal totalPrice = record.getTotalPrice();
        if (user.getBalance().compareTo(totalPrice) < 0) {
            throw new RuntimeException("用户余额不足");
        }
        user.setBalance(user.getBalance().subtract(totalPrice));
        userMapper.updateBalance(user.getId().toString(), user.getBalance());

        //修正商品库存扣除逻辑
        Goods goods = goodsMapper.selectById(record.getGoodId());
        if (goods.getNum() < record.getBuyQuantity()) {
            throw new RuntimeException("商品库存不足");
        }
        // 修正setter方法调用
        goods.setNum(goods.getNum() - record.getBuyQuantity());
        goodsMapper.updateStock(goods.getId(), goods.getNum());

        // 更新订单状态
        recordMapper.updateStatus(orderId, Record.Status.COMPLETED.getValue());

        // 记录操作日志
        System.out.println("完成订单订单ID: " + orderId);
    }
}