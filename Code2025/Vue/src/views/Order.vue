<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <!-- 角色信息提示 -->
      <div>
        当前角色类型 {{ data.user.role }}
      </div>
      <!-- 查询条件 -->
      <el-input
          v-model="data.username"
          :prefix-icon="Search"
          clearable
          placeholder="请输入账号查询"
          style="width: 200px; margin-right: 5px;"
          @clear="load"
      />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px;">
      <!-- 数据表格 -->
      <el-table
          :data="data.tableData"
          :header-cell-style="{ color: '#333' }"
          style="width: 100%;"
      >
        <el-table-column label="商品名称" prop="goodsName"/>
        <el-table-column label="用户名称" prop="userName"/>
        <el-table-column label="供应商名称" prop="supplierName"/>
        <el-table-column label="购买数量" prop="buyQuantity"/>
        <el-table-column label="商品单价" prop="unitPrice"/>
        <el-table-column label="商品总价" prop="totalPrice"/>
        <el-table-column label="订单时间" prop="time"/>
        <el-table-column label="订单状态" prop="" align="center" width="120">
        </el-table-column>
      </el-table>
    </div>

    <div class="card" style="margin-bottom: 5px;">
      <!-- 分页组件 -->
      <el-pagination
          v-model:current-page="data.pageNum"
          v-model:page-size="data.pageSize"
          :page-size="data.pageSize"
          :page-sizes="[5, 10, 15]"
          :total="data.total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="load"
          @size-change="load"
      />
    </div>
  </div>
</template>

<script setup>
import {reactive} from "vue";
import {Search} from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus"; // 表格数据和状态管理

// 表格数据和状态管理
const data = reactive({
  user: JSON.parse(localStorage.getItem('CodeUser') || "{}"),
  pageNum: 1,
  pageSize: 5,
  total: 0,
  tableData: [],
  formVisible: false,
  formLabelWidth: "120px",
  form: {},
  rows: [], // 批量操作选中的行
  ids: []
});

// 加载数据
const load = () => {
  let params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    username: data.username,
    name: data.name,
  };

  if (data.user.role === 'SUPPLIER') {
    params.supplierId = data.user.id;
  } else if (data.user.role === 'USER') {
    params.userId = data.user.id;
  }

  request
      .get("/record/selectPage", {
        params: params
      })
      .then((res) => {
        if (res.code === "200") {
          data.tableData = res.data.list;
          data.total = res.data.total;
        } else {
          ElMessage.error(res.msg);
        }
      });
};
load();

// 重置查询条件
const reset = () => {
  data.name = '';
  data.username = '';
  load();
};
</script>

<style>
/* 可在此处添加样式 */
</style>