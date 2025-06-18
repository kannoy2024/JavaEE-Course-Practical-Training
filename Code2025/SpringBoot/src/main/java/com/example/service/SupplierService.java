package com.example.service;

import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.Supplier;
import com.example.exception.CustomerException;
import com.example.mapper.SupplierMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class SupplierService {

    @Resource
    SupplierMapper supplierMapper;

    public void add(Account account) {
        //根据新的账号查询数据库
        Supplier dbSupplier = supplierMapper.selectByUserName(account.getUsername());
        if (dbSupplier != null) {
            throw new CustomerException("账号重复");
        }
        //提供默认名称,把账号作为默认名称
        if (StrUtil.isBlank(account.getName())) {
            account.setName(account.getUsername());
        }
        //提供默认密码
        if (StrUtil.isBlank(account.getPassword())) {
            account.setPassword("123");
        }
        account.setRole("SUPPLIER");//设置对应角色
        supplierMapper.insert(account);
    }

    @Transactional
    public void update(Supplier supplier) {
        supplierMapper.updateById(supplier);
    }

    public void deleteById(Integer id) {
        supplierMapper.deleteById(id);
    }

    public void deleteAll(List<Supplier> list) {//使用循环来删除全选
        for (Supplier supplier : list) {
            this.deleteById(supplier.getId());
        }
    }

    public List<Supplier> selectAll(Supplier supplier) {
        return supplierMapper.selectAll(supplier);
    }


    public PageInfo<Supplier> selectPage(Integer pageNum, Integer pageSize, Supplier supplier) {
        //        开启分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<Supplier> supplierList = supplierMapper.selectAll(supplier);
        return PageInfo.of(supplierList);
    }


    public void batchInsert(List<Supplier> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (Supplier supplier : list) {
            this.add(supplier); // 复用单条插入逻辑
        }
    }

    public void register(Account account) {
        // 注册就是新增嘛
        this.add(account);//调用一下新增
    }

    public Supplier selectById(String supplierId) {
        return supplierMapper.selectById(supplierId);
    }

    public Supplier login(Account account) {
//        第一步先验证账号是否存在
        Supplier dbSupplier = supplierMapper.selectByUserName(account.getUsername());
        if (dbSupplier == null) {
            throw new CustomerException("账号不存在！");
        } else if (!dbSupplier.getPassword().equals(account.getPassword())) {
//           这是模糊提示，虽然校验的是密码错误，但是不应该提示账号
            throw new CustomerException("账号或密码错误！");
        }

        //创建Token并返回给前端
        String token = TokenUtils.createToken(dbSupplier.getId() + "-" + "SUPPLIER", dbSupplier.getPassword());
        dbSupplier.setToken(token);
        return dbSupplier;
    }

    public void updatePassword(Account account) {
        //先判断新密码和确认密码是否一致
        if (!account.getNewPassword().equals(account.getNew2Password())) {
            throw new CustomerException("500", "您两次输入的密码不一致");
        }
        //校验当前登录用户的原密码是否正确
        Account currentSupplier = TokenUtils.getCurrentUser();
        if (!account.getPassword().equals(currentSupplier.getPassword())) {
            throw new CustomerException("500", "原密码输入错误");
        }
        //开始更新密码
        Supplier supplier = supplierMapper.selectById(currentSupplier.getId().toString());
        supplier.setPassword(account.getNewPassword());
        supplierMapper.updateById(supplier);
    }
}
