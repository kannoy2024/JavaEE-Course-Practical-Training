package com.example.mapper;

import com.example.entity.Record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecordMapper {
    List<Record> selectAll(Record record);

    void insert(Record record);

    void insertBatch(@Param("list") List<Record> records);
}
