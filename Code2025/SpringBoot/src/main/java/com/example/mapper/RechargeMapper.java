package com.example.mapper;

import com.example.entity.Recharge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RechargeMapper {
    List<Recharge> selectAll(Recharge recharge);

    void insert(Recharge recharge);

    void insertBatch(@Param("list") List<Recharge> recharges);

    //更新订单状态
    void updateStatus(@Param("id") Integer id, @Param("status") String status);

    Recharge selectByOrderId(@Param("id") Integer id);
}
