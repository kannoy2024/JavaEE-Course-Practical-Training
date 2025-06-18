package com.example.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.example.common.Result;
import com.example.entity.Admin;
import com.example.service.AdminService;
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
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    @PostMapping("/add")
    public Result add(@RequestBody Admin admin) {
        adminService.add(admin);
        return Result.success();
    }


    @PutMapping("/update")
    public Result update(@RequestBody Admin admin) {
        adminService.update(admin);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {

        adminService.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/deleteAll")
    public Result deleteAll(@RequestBody List<Admin> list) {
        // 调用服务层批量删除管理员
        adminService.deleteAll(list);
        return Result.success();
    }

    @GetMapping("/selectAll")
    public Result selectAll(Admin admin) {
        // 调用服务层查询所有管理员
        List<Admin> adminList = adminService.selectAll(admin);
        return Result.success(adminList);
    }

    @GetMapping("/selectPage")
    public Result selectPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Admin admin) {
        // 调用服务层分页查询管理员
        PageInfo<Admin> pageInfo = adminService.selectPage(pageNum, pageSize, admin);
        return Result.success(pageInfo);
    }


    @GetMapping("/export")
    public void exportData(Admin admin, HttpServletResponse httpServletResponse) throws Exception {
//        根据传入的id进行导出
        String ids = admin.getIds();
        if (StrUtil.isNotBlank(ids)) {//如果有传入的选中id才进行转化
            String[] idsArr = ids.split(",");
            admin.setIdsArr(idsArr);
        }
//         拿到全部数据
        List<Admin> adminList = adminService.selectAll(admin);
//         构建writer对象
        ExcelWriter excelWriter = ExcelUtil.getWriter(true);
//         设置中文的表头
        excelWriter.addHeaderAlias("username", "账号");
        excelWriter.addHeaderAlias("name", "名称");
        excelWriter.addHeaderAlias("phone", "电话");
        excelWriter.addHeaderAlias("email", "邮箱");
//        调用特定的方法排除没有设置别名的字段，防止打印密码
        excelWriter.setOnlyAlias(true);
//         写出数据writer
        excelWriter.write(adminList);
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
        reader.addHeaderAlias("账号", "username");
        reader.addHeaderAlias("名称", "name");
        reader.addHeaderAlias("电话", "phone");
        reader.addHeaderAlias("邮箱", "email");
        // 5. 转换为Java对象列表
        List<Admin> adminList = reader.readAll(Admin.class);
        // 6. 批量保存到数据库
        adminService.batchInsert(adminList);
        return Result.success("成功导入 " + adminList.size() + " 条数据");
    }
}