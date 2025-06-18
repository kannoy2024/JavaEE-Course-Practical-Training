package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Category;
import com.example.entity.Introduction;
import com.example.mapper.CategoryMapper;
import com.example.mapper.IntroductionMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class IntroductionService {

    @Resource
    IntroductionMapper introductionMapper;

    @Resource
    CategoryMapper categoryMapper;

    public void add(Introduction introduction) {
        introduction.setTime(DateUtil.now());
        introductionMapper.insert(introduction);
    }

    public void update(Introduction introduction) {
        introductionMapper.updateById(introduction);
    }

    public void deleteById(Integer id) {
        introductionMapper.deleteById(id);
    }


    public List<Introduction> selectAll(Introduction introduction) {
        return introductionMapper.selectAll(introduction);
    }

    public PageInfo<Introduction> selectPage(Integer pageNum, Integer pageSize, Introduction introduction) {
        //        开启分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<Introduction> introductions = introductionMapper.selectAll(introduction);
        for (Introduction dbIntroductions : introductions) {
            //先拿到CategoryId
            Integer categoryId = dbIntroductions.getCategoryId();
//            通过CategoryId去Category表里面查到对应的分类数据
            Category category = categoryMapper.selectById(categoryId);
            if (ObjectUtil.isNotNull(category)) {
//                把查到的title赋值给CategoryTitle
                dbIntroductions.setCategoryTitle(category.getTitle());
            }
        }
        return PageInfo.of(introductions);
    }


}
