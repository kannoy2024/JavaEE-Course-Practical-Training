package com.example.mapper;

import com.example.entity.Introduction;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IntroductionMapper {
    List<Introduction> selectAll(Introduction introduction);

    void insert(Introduction introduction);

    void deleteById(Integer id);

    void updateById(Introduction introduction);
}
