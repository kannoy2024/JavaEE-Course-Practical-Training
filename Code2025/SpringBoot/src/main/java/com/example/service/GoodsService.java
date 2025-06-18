package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Account;
import com.example.entity.Goods;
import com.example.entity.Supplier;
import com.example.mapper.GoodsMapper;
import com.example.mapper.SupplierMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    @Resource
    GoodsMapper goodsMapper;

    @Resource
    SupplierMapper supplierMapper;

    public void add(Goods goods) {
        Account account = TokenUtils.getCurrentUser();
        goods.setTime(DateUtil.now());
        goodsMapper.insert(goods);
    }

    public void update(Goods goods) {
        goodsMapper.updateById(goods);
    }

    public void deleteById(Integer id) {
        goodsMapper.deleteById(id);
    }


    public List<Goods> selectAll(Goods goods) {
        return goodsMapper.selectAll(goods);
    }

    public PageInfo<Goods> selectPage(Integer pageNum, Integer pageSize, Goods goods) {
        //        开启分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<Goods> listGoods = goodsMapper.selectAll(goods);
        for (Goods dbGoods : listGoods) {
            Integer supplierId = dbGoods.getSupplierId();

            String supplierStr = supplierId != null ? supplierId.toString() : null;

            Supplier supplier = supplierMapper.selectById(supplierStr);
            if (ObjectUtil.isNotNull(supplier)) {
                dbGoods.setSupplierName(supplier.getName());
            }
        }
        return PageInfo.of(listGoods);
    }


}
