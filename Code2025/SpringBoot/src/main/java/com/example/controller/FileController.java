package com.example.controller;

import cn.hutool.core.io.FileUtil;
import com.example.common.Result;
import com.example.exception.CustomerException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

//处理文件下载相关的接口
@RestController
@RequestMapping("files")
public class FileController {
    /*
     * 文件下载
     * 下载路径 ："http://localhost:9999/files/download/GgsaQsHaQAAfith.jpeg"
     *F:\Java2025\Vue+SpringBoot\代码\Code2025\files\数据库.txt
     * 图片，纯文本，pdf是可以直接下载的，并且支持在线预览
     * */
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse httpServletResponse) throws Exception {
//        找到文件的位置
        String filePath = System.getProperty("user.dir") + "\\files\\";//获取到当前项目的根路径，得到Code2025的绝对路径 F:\Java2025\Vue+SpringBoot\代码\Code2025
        String realPath = filePath + fileName;//  F:\Java2025\Vue+SpringBoot\代码\Code2025\files\GgsaQsHaQAAfith.jpeg
        boolean exist = FileUtil.exist(realPath);
//        System.out.println(realPath);
        if (!exist) {
            throw new CustomerException("文件不存在");
        }
        //读取文件的字节流
        byte[] bytes = FileUtil.readBytes(realPath);
        //写一个响应头，

        ServletOutputStream os = httpServletResponse.getOutputStream();
        //输出流对象，把文件写出到客户端
        os.write(bytes);
        os.flush();
        os.close();
    }


    /*文件上传
     * */
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {

        System.out.println("Content-Type: " + request.getContentType());

        // 打印文件信息
        if (file.isEmpty()) {
            System.out.println("文件为空！");
        } else {
            System.out.println("文件名: " + file.getOriginalFilename());
        }


//        确定文件上传的位置
        String filePath = System.getProperty("user.dir") + "\\files\\";
        if (!FileUtil.isDirectory(filePath)) {
            FileUtil.mkdir(filePath);
            //创建文件夹
        }
//        写入文件
        byte[] bytes = file.getBytes();
//取得原始名称,为了防止上传同名文件，选择取出上传时的毫秒数来保证唯一
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//        String encodeFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);//统一编码
//        写入文件
        FileUtil.writeBytes(bytes, filePath + filename);
        String URL = "http://localhost:9999/files/download/" + filename;
        return Result.success(URL);

    }

    /*富文本编辑器上传的图片接口*/
    @PostMapping("/wang/upload")
    public Map<String, Object> wangEditorUpload(@RequestParam("file") MultipartFile multipartFile) {
        // 参数校验
        if (multipartFile == null || multipartFile.isEmpty()) {
            return Map.of("errno", 1, "message", "请选择要上传的文件");
        }

        try {
            String flag = System.currentTimeMillis() + "";
            String fileName = multipartFile.getOriginalFilename();
            String filePath = System.getProperty("user.dir") + "\\files\\";

            // 确保目录存在
            FileUtil.mkdir(filePath);

            // 保存文件
            String savedFileName = flag + "-" + fileName;
            FileUtil.writeBytes(multipartFile.getBytes(), filePath + savedFileName);

            // 构建返回结果（严格遵循 wangEditor 要求的格式）
            String fileUrl = "http://localhost:9999/files/download/" + savedFileName;
            return Map.of(
                    "errno", 0,  // 必须返回 errno: 0 表示成功
                    "data", Map.of(
                            "url", fileUrl,
                            "alt", fileName,
                            "href", fileUrl
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("errno", 1, "message", "文件上传失败: " + e.getMessage());
        }
    }
}
