package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    void updateById(User user);

    List<User> selectAll(User user);

    void insert(User user);

    User selectByUserName(String username);

    void deleteById(Integer id);

    User selectById(String userId);
}