package com.example.mapper;

import com.example.entity.Account;
import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserMapper {
    void updateById(User user);

    List<User> selectAll(User user);

    void insert(Account account);

    User selectByUserName(String username);

    void deleteById(Integer id);

    User selectById(String userId);

    void updateBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);
}