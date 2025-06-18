package com.example.common;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.entity.Account;
import com.example.exception.CustomerException;
import com.example.service.AdminService;
import com.example.service.SupplierService;
import com.example.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JWTInterceptor implements HandlerInterceptor {
    //真正用来确认Token的类
    @Resource
    AdminService adminService;
    @Resource
    UserService userService;
    @Resource
    SupplierService supplierService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 获取Token
        String token = request.getParameter("token");
        if (StrUtil.isBlank(token)) {
            token = request.getHeader("token");
        }
        if (StrUtil.isBlank(token)) {
            throw new CustomerException("401", "未提供Token");
        }

        // 2. 解析Token
        Account account = null;
        String role = null;
        try {
            String audience = JWT.decode(token).getAudience().get(0);
            String[] split = audience.split("-");
            if (split.length != 2) {
                throw new CustomerException("401", "Token格式错误");
            }
            String userId = split[0];
            role = split[1];

            // 3. 根据角色查询用户
            switch (role) {
                case "ADMIN":
                    account = adminService.selectById(userId);
                    break;
                case "USER":
                    account = userService.selectById(userId);
                    break;
                case "SUPPLIER":
                    account = supplierService.selectById(userId);
                    break;
                default:
                    throw new CustomerException("401", "无效的用户角色");
            }

            if (account == null || StrUtil.isBlank(account.getPassword())) {
                throw new CustomerException("401", "用户不存在或凭证无效");
            }

            // 4. 验证签名
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(account.getPassword())).build();
            verifier.verify(token);

            return true;
        } catch (JWTDecodeException e) {
            throw new CustomerException("401", "Token解析失败");
        } catch (JWTVerificationException e) {
            throw new CustomerException("401", "Token验证失败");
        } catch (CustomerException e) {
            throw e; // 直接抛出已知异常
        } catch (Exception e) {
            throw new CustomerException("500", "认证系统错误");
        }
    }
}