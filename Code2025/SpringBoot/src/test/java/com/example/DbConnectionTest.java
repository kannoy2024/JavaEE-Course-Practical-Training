package com.example;// 确保此文件存储在项目的 `src/test/java/com/example/` 目录中。

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DbConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDataSourceExists() {
        assertNotNull(dataSource, "DataSource未被注入！检查Spring配置");
        System.out.println("DataSource实现类: " + dataSource.getClass().getName());
    }

    @Test
    void testConnection() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            // 检查admin表是否存在
            ResultSet rs = meta.getTables(null, null, "admin", new String[]{"TABLE"});
            assertTrue(rs.next(), "admin表不存在");

            // 可选：打印表结构验证
            System.out.println("数据库连接成功，admin表存在");
        }
    }
}