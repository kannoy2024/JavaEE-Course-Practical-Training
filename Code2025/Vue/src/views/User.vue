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
      <el-input
          v-model="data.name"
          :prefix-icon="Search"
          clearable
          placeholder="请输入名称查询"
          style="width: 200px; margin-right: 5px;"
          @clear="load"
      />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px;">
      <!-- 批量操作按钮 -->
      <el-button type="primary" @click="handleAdd">新增</el-button>
      <el-button type="danger" @click="deleteAll">批量删除</el-button>
      <el-upload
          :on-success="handleImportSuccess"
          :show-file-list="false"
          action="http://localhost:9999/supplier/import"
          style="display: inline-block;margin-right: 10px;margin-left: 10px"
      >
        <el-button type="success">
          批量导入
        </el-button>
      </el-upload>

      <el-button type="info" @click="exportData">批量导出</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px;">
      <!-- 数据表格 -->
      <el-table
          :data="data.tableData"
          :header-cell-style="{ color: '#333' }"
          style="width: 100%;"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"/>
        <el-table-column label="头像">
          <template #default="scope">
            <el-image v-if="scope.row.avatar"
                      :src="scope.row.avatar"
                      :preview-src-list="[scope.row.avatar]"
                      :preview-teleported="true"
                      style="width: 40px ;height:40px;border-radius: 50%;display: block"/>
          </template>
        </el-table-column>
        <el-table-column label="账号" prop="username"/>
        <el-table-column label="名称" prop="name"/>
        <el-table-column label="电话" prop="phone"/>
        <el-table-column label="邮箱" prop="email"/>

        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button circle icon="Edit" type="primary" @click="handleEdit(scope.row)"/>
            <el-button circle icon="Delete" type="danger" @click="del(scope.row.id)"/>
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

    <div>
      <!-- 编辑/新增使用同一个弹窗 -->
      <el-dialog v-model="data.formVisible" destroy-on-close title="普通用户信息" width="500">
        <el-form
            ref="formRef"
            :model="data.form"
            :rules="data.rules"
            label-width="80px"
            style="padding: 20px 30px 20px 0;"
        >
          <el-form-item label="账号" prop="username">
            <el-input v-model="data.form.username" autocomplete="off" placeholder="请输入账号"/>
          </el-form-item>
          <el-form-item label="名称" prop="name">
            <el-input v-model="data.form.name" autocomplete="off" placeholder="请输入名称"/>
          </el-form-item>
          <el-form-item label="电话" prop="phone">
            <el-input v-model="data.form.phone" autocomplete="off" placeholder="请输电话"/>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="data.form.email" autocomplete="off" placeholder="请输入邮箱"/>
          </el-form-item>
          <el-form-item label="头像" prop="avatar">
            <el-upload
                action="http://localhost:9999/files/upload"
                :headers="{token:data.user.token}"
                :on-success="handleFileSuccess"
                list-type="picture"
            >
              <el-button type="primary">上传头像</el-button>
            </el-upload>
          </el-form-item>
        </el-form>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="data.formVisible = false">取消</el-button>
            <el-button type="primary" @click="save">保存</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from "vue";
import {Search} from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus"; // 表格数据和状态管理

// 表格数据和状态管理
const data = reactive({
  user: JSON.parse(localStorage.getItem('CodeUser') || "{}"),
  name: '',
  username: '',
  pageNum: 1,
  pageSize: 5,
  total: 0,
  tableData: [],
  formVisible: false,
  formLabelWidth: "120px",
  form: {},
  rules: {
    username: [
      {required: true, message: "请填写账号", trigger: "blur"},
    ],
    name: [
      {required: true, message: "请填写名称", trigger: "blur"},
    ],
    phone: [
      {required: true, message: "请填写电话", trigger: "blur"},
    ],
    email: [
      {required: true, message: "请填写邮箱", trigger: "blur"},
    ],
  },
  rows: [], // 批量操作选中的行
  ids: []
});

// 加载数据
const load = () => {
  request
      .get("user/selectPage", {
        params: {
          pageNum: data.pageNum,
          pageSize: data.pageSize,
          username: data.username,
          name: data.name,
        },
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

// 初始化加载
load();

// 重置查询条件
const reset = () => {
  data.name = '';
  data.username = '';
  load();
};

// 新增
const handleAdd = () => {
  data.formVisible = true;
  data.form = {};
};

// 表单验证和提交
const userAdd = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      request
          .post("/user/add", data.form)
          .then((res) => {
            if (res.code === "200") {
              ElMessage.success("新增成功");
              load();
              data.formVisible = false;
            } else {
              ElMessage.error(res.msg);
            }
          })
          .catch(() => {
            ElMessage.error("新增，请求失败，请重试");
          });
    }
  });
};

// 表单引用
const formRef = ref();

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row)); // 深度拷贝
  data.formVisible = true;
};

// 更新
const update = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      request
          .put("/user/update", data.form)
          .then((res) => {
            if (res.code === "200") {
              ElMessage.success("修改成功");
              load();
              data.formVisible = false;
            } else {
              ElMessage.error(res.msg);
            }
          })
          .catch(() => {
            ElMessage.error("修改，请求失败，请重试");
          });
    }
  });
};

// 保存（新增或更新）
const save = () => {
  data.form.id ? update() : userAdd();
};

// 删除单条记录
const del = (id) => {
  ElMessageBox.confirm("你确认删除该行数据吗？（无法恢复）", "删除确认", {
    type: "warning",
  })
      .then(() => {
        request
            .delete(`user/delete/${id}`)
            .then((res) => {
              if (res.code === "200") {
                ElMessage.success("删除成功");
                load();
                data.formVisible = false;
              } else {
                ElMessage.error(res.msg);
              }
            })
            .catch(() => {
              ElMessage.error("删除，请求失败，请重试");
            });
      })
      .catch(() => {
      });
};

// 批量选择
const handleSelectionChange = (rows) => {
  data.rows = rows;
  data.ids = data.rows.map((item) => item.id);//把对象的数组转换成一个纯数字的数组[1,2,3]
};

// 批量删除
const deleteAll = () => {
  if (data.rows.length === 0) {
    ElMessage.warning("请选择数据");
    return;
  }
  ElMessageBox.confirm("你确认删除这些数据吗？（无法恢复）", "删除确认", {
    type: "warning",
  })
      .then(() => {
        request
            .delete("user/deleteAll", {data: data.rows})
            .then((res) => {
              if (res.code === "200") {
                ElMessage.success("删除成功");
                load();
                data.formVisible = false;
              } else {
                ElMessage.error(res.msg);
              }
            })
            .catch(() => {
              ElMessage.error("删除全部，请求失败，请重试");
            })
      })
}

const exportData = (res) => {
  let idsStr = data.ids.join(",") //把数组转换成字符串
  let url = `http://localhost:9999/user/export?`
      + `&username=${data.username === null ? '' : data.username}`
      + `&name=${data.name === null ? '' : data.name}`
      + `&ids=${idsStr}`
      + `&token=${data.user.token}`
  window.open(url);
  if (res.code === '200') {
    ElMessage.success("导出请求已发送");
    load()
  } else {
    ElMessage.error(res.msg)
  }
}

const handleImportSuccess = (res) => {
  if (res.code === '200') {
    ElMessage.success('批量导入成功')
    load()
  } else {
    ElMessage.error(res.msg)
  }
}

const handleFileSuccess = (res) => {
  data.form.avatar = res.data
}
</script>

<style>
/* 可在此处添加样式 */
</style>