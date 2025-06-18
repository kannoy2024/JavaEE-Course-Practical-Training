<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
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
        <el-table-column label="订单状态" prop="status" align="center" width="120">
          <template #default="scope">
            <el-tag
                :type="scope.row.status === '已完成' ? 'success' :
                      (scope.row.status === '已取消' ? 'danger' : 'warning')">
              {{ scope.row.status || '处理中' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" align="center" v-if="data.user.role === 'ADMIN'">
          <template #default="scope">
            <el-button
                v-if="scope.row.status !== '已完成'"
                type="primary"
                size="small"
                @click="completeOrder(scope.row.id)">
              完成订单
            </el-button>
            <span v-else>已完成</span>
          </template>
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
import {ElMessage, ElMessageBox} from 'element-plus';
import request from "@/utils/request.js";

const data = reactive({
  user: JSON.parse(localStorage.getItem('CodeUser') || "{}"),
  pageNum: 1,
  pageSize: 5,
  total: 0,
  tableData: [],
  username: '',
  name: ''
});

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

  request.get("/record/selectPage", {params})
      .then((res) => {
        if (res.code === "200") {
          data.tableData = res.data.list.map(item => {
            item.status = item.status || '处理中';
            return item;
          });
          data.total = res.data.total;
        } else {
          ElMessage.error(res.msg);
        }
      });
};

const reset = () => {
  data.name = '';
  data.username = '';
  load();
};

const completeOrder = (id) => {
  ElMessageBox.confirm('确认要完成此订单吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.post(`/record/complete/${id}`)
        .then(res => {
          if (res.code === '200') {
            ElMessage.success('订单已完成');
            load();
          } else {
            ElMessage.error(res.msg);
          }
        })
        .catch(err => {
          ElMessage.error('操作失败: ' + (err.response?.data?.msg || err.message));
        });
  }).catch(() => {
  });
};

load();
</script>

<style>

</style>