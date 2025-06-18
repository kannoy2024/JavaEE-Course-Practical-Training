package com.example.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.example.common.Result;
import com.example.entity.Supplier;
import com.example.service.SupplierService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;


/**
 * 管理员控制器
 * 提供管理员相关的增删改查接口
 */
@RestController
@RequestMapping("/supplier")
public class SupplierController {

    @Resource
    private SupplierService supplierService;

    /**
     * 添加管理员
     *
     * @param supplier 管理员实体，通过@RequestBody接收前端传来的JSON数据
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody Supplier supplier) {
        // 调用服务层添加管理员
        supplierService.add(supplier);
        return Result.success();
    }

    /**
     * 更新管理员信息
     *
     * @param supplier 管理员实体，包含id字段用于标识要更新的记录
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result update(@RequestBody Supplier supplier) {
        // 调用服务层更新管理员信息
        supplierService.update(supplier);
        return Result.success();
    }

    /**
     * 删除单个管理员
     *
     * @param id 要删除的管理员ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        // 调用服务层删除管理员
        supplierService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除管理员
     *
     * @param list 要删除的管理员ID列表
     * @return 操作结果
     */
    @DeleteMapping("/deleteAll")
    public Result deleteAll(@RequestBody List<Supplier> list) {
        // 调用服务层批量删除管理员
        supplierService.deleteAll(list);
        return Result.success();
    }

    /**
     * 查询所有管理员
     *
     * @return 管理员列表
     */
    @GetMapping("/selectAll")
    public Result selectAll(Supplier supplier) {
        // 调用服务层查询所有管理员
        List<Supplier> supplierList = supplierService.selectAll(supplier);
        return Result.success(supplierList);
    }

    /**
     * 分页查询管理员
     *
     * @param pageNum  当前页码，默认为1
     * @param pageSize 每页显示数量，默认为10
     * @param supplier    查询条件（可包含suppliername、name等字段）
     * @return 分页结果
     */
    @GetMapping("/selectPage")
    public Result selectPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Supplier supplier) {
        // 调用服务层分页查询管理员
        PageInfo<Supplier> pageInfo = supplierService.selectPage(pageNum, pageSize, supplier);
        return Result.success(pageInfo);
    }

    /*
     * 数据导出，使用流的方式，输出流
     * ids:1,2,3,4,5,6
     * */
    @GetMapping("/export")
    public void exportData(Supplier supplier, HttpServletResponse httpServletResponse) throws Exception {
//        根据传入的id进行导出
        String ids = supplier.getIds();
        if (StrUtil.isNotBlank(ids)) {//如果有传入的选中id才进行转化
            String[] idsArr = ids.split(",");
            supplier.setIdsArr(idsArr);
        }
//         拿到全部数据
        List<Supplier> supplierList = supplierService.selectAll(supplier);
//         构建writer对象
        ExcelWriter excelWriter = ExcelUtil.getWriter(true);
//         设置中文的表头
        excelWriter.addHeaderAlias("suppliername", "账号");
        excelWriter.addHeaderAlias("name", "名称");
        excelWriter.addHeaderAlias("phone", "电话");
        excelWriter.addHeaderAlias("email", "邮箱");
//        调用特定的方法排除没有设置别名的字段，防止打印密码
        excelWriter.setOnlyAlias(true);
//         写出数据writer
        excelWriter.write(supplierList);
//         设置输出文件的名称和输出流的头信息
        String fileName = URLEncoder.encode("管理员信息", StandardCharsets.UTF_8);
//        固定的内容
        httpServletResponse.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
//        在响应头里面写进名字,最后的名字就是“管理员信息.xlsx”
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
//        设置写出的输出流，并关闭httpServletResponse
        ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
        excelWriter.flush(servletOutputStream);
        excelWriter.close();
        servletOutputStream.close();
    }

    @PostMapping("/import")
    public Result importData(@RequestParam("file") MultipartFile file) throws Exception {
        // 1. 基础校验：检查是否上传了文件
        if (file == null || file.isEmpty()) {
            throw new Exception("请选择要上传的文件");
        }
        // 2. 检查文件格式是否为Excel
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.endsWith(".xlsx")) {
            throw new Exception("只支持.xlsx格式的文件");
        }
        // 3. 读取Excel数据
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 4. 设置Excel列名对应关系（Excel列名 → Java字段）
        reader.addHeaderAlias("账号", "suppliername");
        reader.addHeaderAlias("名称", "name");
        reader.addHeaderAlias("电话", "phone");
        reader.addHeaderAlias("邮箱", "email");
        // 5. 转换为Java对象列表
        List<Supplier> supplierList = reader.readAll(Supplier.class);
        // 6. 批量保存到数据库
        supplierService.batchInsert(supplierList);
        return Result.success("成功导入 " + supplierList.size() + " 条数据");
    }
}