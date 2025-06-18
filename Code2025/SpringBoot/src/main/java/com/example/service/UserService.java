package com.example.service;

import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.exception.CustomerException;
import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class UserService {

    @Resource
    UserMapper userMapper;

    public void add(Account account) {
        //根据新的账号查询数据库
        User dbUser = userMapper.selectByUserName(account.getUsername());
        if (dbUser != null) {
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
        account.setRole("USER");//设置对应角色
        userMapper.insert(account);
    }

    @Transactional
    public void update(User user) {
        userMapper.updateById(user);
    }

    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }

    public void deleteAll(List<User> list) {//使用循环来删除全选
        for (User user : list) {
            this.deleteById(user.getId());
        }
    }

    public List<User> selectAll(User user) {
        return userMapper.selectAll(user);
    }


    public PageInfo<User> selectPage(Integer pageNum, Integer pageSize, User user) {
        //        开启分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<User> userList = userMapper.selectAll(user);
        return PageInfo.of(userList);
    }


    public void batchInsert(List<User> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (User user : list) {
            this.add(user); // 复用单条插入逻辑
        }
    }

    public void register(Account account) {
        // 注册就是新增嘛
        this.add(account);//调用一下新增
    }

    public User selectById(String userId) {
        return userMapper.selectById(userId);
    }

    public User login(Account account) {
//        第一步先验证账号是否存在
        User dbUser = userMapper.selectByUserName(account.getUsername());
        if (dbUser == null) {
            throw new CustomerException("账号不存在！");
        } else if (!dbUser.getPassword().equals(account.getPassword())) {
//           这是模糊提示，虽然校验的是密码错误，但是不应该提示账号
            throw new CustomerException("账号或密码错误！");
        }

        //创建Token并返回给前端
        String token = TokenUtils.createToken(dbUser.getId() + "-" + "USER", dbUser.getPassword());
        dbUser.setToken(token);
        return dbUser;
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
        User user = userMapper.selectById(currentUser.getId().toString());
        user.setPassword(account.getNewPassword());
        userMapper.updateById(user);
    }
}
