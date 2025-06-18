package com.example.mapper;

import com.example.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminMapper {
    void updateById(Admin admin);

    List<Admin> selectAll(Admin admin);

    void insert(Admin admin);

    Admin selectByUserName(String username);

    void deleteById(Integer id);

    Admin selectById(String userId);
}