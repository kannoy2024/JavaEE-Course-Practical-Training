package com.example.mapper;

import com.example.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticeMapper {

    List<Notice> selectAll(Notice notice);

    void insert(Notice notice);

    void deleteById(Integer id);

    void updateById(Notice notice);
}
