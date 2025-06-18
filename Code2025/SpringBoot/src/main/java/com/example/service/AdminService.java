package com.example.service;

import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.exception.CustomerException;
import com.example.mapper.AdminMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class AdminService {

    @Resource
    AdminMapper adminMapper;

    public void add(Admin admin) {
        //根据新的账号查询数据库
        Admin dbAdmin = adminMapper.selectByUserName(admin.getUsername());
        if (dbAdmin != null) {
            throw new CustomerException("账号重复");
        }
        //提供默认密码
        if (StrUtil.isBlank(admin.getPassword())) {
            admin.setPassword("admin");
        }
        admin.setRole("ADMIN");
        adminMapper.insert(admin);
    }

    @Transactional
    public void update(Admin admin) {
        adminMapper.updateById(admin);
    }

    public void deleteById(Integer id) {
        adminMapper.deleteById(id);
    }

    public void deleteAll(List<Admin> list) {//使用循环来删除全选
        for (Admin admin : list) {
            this.deleteById(admin.getId());
        }
    }

    public Admin selectById(String userId) {
        return adminMapper.selectById(userId);
    }

    public List<Admin> selectAll(Admin admin) {
        return adminMapper.selectAll(admin);
    }


    public PageInfo<Admin> selectPage(Integer pageNum, Integer pageSize, Admin admin) {
        //        开启分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<Admin> adminList = adminMapper.selectAll(admin);
        return PageInfo.of(adminList);
    }


    public void batchInsert(List<Admin> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (Admin admin : list) {
            this.add(admin); // 复用单条插入逻辑
        }
    }


    public Admin login(Account account) {
        //第一步先验证账号是否存在
        Admin dbAdmin = adminMapper.selectByUserName(account.getUsername());
        if (dbAdmin == null) {
            throw new CustomerException("账号不存在！");
        }
        if (!dbAdmin.getPassword().equals(account.getPassword())) {
            //这是模糊提示，虽然校验的是密码错误，但是不应该提示账号
            throw new CustomerException("账号或密码错误！");
        }
        if (StrUtil.isBlank(dbAdmin.getRole())) {
            throw new CustomerException("用户角色无效");
        }

        //创建Token并返回给前端
        String token = TokenUtils.createToken(dbAdmin.getId() + "-" + "ADMIN", dbAdmin.getPassword());
        dbAdmin.setToken(token);
        return dbAdmin;
    }


    public void updatePassword(Account account) {
        //先判断新密码和确认密码是否一致
        if (!account.getNewPassword().equals(account.getNew2Password())) {
            throw new CustomerException("500", "您两次输入的密码不一致");
        }
        //校验当前登录用户的原密码是否正确
        Account currentUser = TokenUtils.getCurrentUser();
        if (!account.getPassword().equals(currentUser.getPassword())) {
            throw new CustomerException("500", "原密码输入错误");
        }
        //开始更新密码
        Admin admin = adminMapper.selectById(currentUser.getId().toString());
        admin.setPassword(account.getNewPassword());
        adminMapper.updateById(admin);
    }
}

