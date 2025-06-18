package com.example.mapper;

import com.example.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodsMapper {

    List<Goods> selectAll(Goods goods);

    void insert(Goods goods);

    void deleteById(Integer id);

    void updateById(Goods goods);

    Goods selectById(Integer goodsId);

    void updateStock(@Param("id") Integer id, @Param("num") Integer num);
}
