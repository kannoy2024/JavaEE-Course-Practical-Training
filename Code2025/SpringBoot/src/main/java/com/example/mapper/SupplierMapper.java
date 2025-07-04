package com.example.mapper;

import com.example.entity.Account;
import com.example.entity.Supplier;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SupplierMapper {
    void updateById(Supplier supplier);

    List<Supplier> selectAll(Supplier supplier);

    void insert(Account account);

    Supplier selectByUserName(String username);

    void deleteById(Integer id);

    Supplier selectById(String supplierId);


}