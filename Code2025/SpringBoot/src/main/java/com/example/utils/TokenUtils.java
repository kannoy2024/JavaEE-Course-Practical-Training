package com.example.utils;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.entity.Account;
import com.example.exception.CustomerException;
import com.example.service.AdminService;
import com.example.service.SupplierService;
import com.example.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Component
public class TokenUtils {
    //通过复杂的迂回在工具类里面拿到Bean
    @Resource
    AdminService adminService;
    @Resource
    UserService userService;
    @Resource
    SupplierService supplierService;

    static AdminService staticAdminService;
    static UserService staticUserService;
    static SupplierService staticSupplierService;

    //把容器的内容变成静态变量
    @PostConstruct
    public void init() {
        staticAdminService = adminService;
        staticUserService = userService;
        staticSupplierService = supplierService;
    }

    //生成token
    public static String createToken(String data, String sign) {
        if (data == null || data.contains("null")) {
            throw new IllegalArgumentException("生成Token时用户ID不能为null");
        }
        return JWT.create().withAudience(data)//将userId-role保存到token里面作为载荷
                .withExpiresAt(DateUtil.offsetDay(new Date(), 1))//一天后token过期
                .sign(Algorithm.HMAC256(sign));//以password作为token的秘钥，用HMAC256算法加密
    }

    /*
     * 获取当前登录的用户信息*/
    public static Account getCurrentUser() {

        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = httpServletRequest.getHeader("token");
        if (StrUtil.isBlank(token)) {
            token = httpServletRequest.getParameter("token");
        }

        //拿到token载荷的数据
        String audience = JWT.decode(token).getAudience().get(0);
        String[] split = audience.split("-");
        String userId = split[0];
        String role = split[1];
        //根据token解析出来的数据来查询对应的表
        if ("ADMIN".equals(role)) {
            return staticAdminService.selectById(userId);
        } else if ("USER".equals(role)) {
            return staticUserService.selectById(userId);
        } else if ("SUPPLIER".equals(role)) {
            return staticSupplierService.selectById(userId);
        } else {
            throw new CustomerException("401", "获取当前登录的用户信息。TokenUtils。。。角色类型错误");
        }
    }
}
