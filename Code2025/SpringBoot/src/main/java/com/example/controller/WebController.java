package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.service.AdminService;
import com.example.service.SupplierService;
import com.example.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebController {
    @Resource
    AdminService adminService;
    @Resource
    UserService userService;
    @Autowired
    private SupplierService supplierService;

    //表示这是一个 get请求的接口
    @GetMapping("/")//接口路径,全局唯一
    public Result hello() {
        return Result.success("我是斜杠");
    }

    @PostMapping("/login")
    public Result login(@RequestBody Account account) {

        System.out.println("Received role: " + account.getRole());  // 调试日志
        Account dbAccount = null;

        if ("ADMIN".equals(account.getRole())) {
            dbAccount = adminService.login(account);
        } else if ("USER".equals(account.getRole())) {
            dbAccount = userService.login(account);
        } else if ("SUPPLIER".equals(account.getRole())) {
            dbAccount = supplierService.login(account);
        }
        return Result.success(dbAccount);
    }

    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        userService.register(user);
        return Result.success();
    }

    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        if ("ADMIN".equals(account.getRole())) {
            adminService.updatePassword(account);
        }
        if ("USER".equals(account.getRole())) {
            userService.updatePassword(account);
        }
        if ("SUPPLIER".equals(account.getRole())) {
            supplierService.updatePassword(account);
        }
        return Result.success();
    }
}
