package com.example.mapper;

import com.example.entity.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {

    List<Category> selectAll(Category category);

    void insert(Category category);

    void deleteById(Integer id);

    void updateById(Category category);

    Category selectById(Integer id);
}
