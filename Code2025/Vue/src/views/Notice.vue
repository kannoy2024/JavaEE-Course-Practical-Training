<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input
          v-model="data.title"
          :prefix-icon="Search"
          clearable
          placeholder="请输入标题查询"
          style="width: 200px; margin-right: 5px;"
          @clear="load"
      />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="primary" @click="handleAdd" v-if="data.user.role === 'ADMIN'">新增</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px;">
      <!-- 数据表格 -->
      <el-table
          :data="data.tableData"
          :header-cell-style="{ color: '#333' }"
          style="width: 100%;"
      >
        <el-table-column label="标题" prop="title"/>
        <el-table-column label="留言内容" prop="content">
          <template v-slot="scope">
            <span >{{ scope.row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" prop="time"/>

        <el-table-column label="操作" width="100">
          <template #default="scope">
<!--            <el-button circle icon="Edit" type="primary" @click="handleEdit(scope.row)"/>-->
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
      <el-dialog v-model="data.formVisible" destroy-on-close title="留言信息" width="500">
        <el-form
            ref="formRef"
            :model="data.form"
            :rules="data.rules"
            label-width="80px"
            style="padding: 20px 30px 20px 0;"
        >
          <el-form-item label="留言标题" prop="title">
            <el-input v-model="data.form.title" autocomplete="off" placeholder="请输入留言标题"/>
          </el-form-item>
          <el-form-item label="留言内容" prop="content">
            <el-input type="textarea" :rows="3" v-model="data.form.content" autocomplete="off"
                      placeholder="请输入留言内容"/>
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

import {Search} from "@element-plus/icons-vue";
import {reactive, ref} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";

const formRef = ref()

const data = reactive({
  user: JSON.parse(localStorage.getItem('CodeUser') || "{}"),
  title: null,
  pageNum: 1,
  pageSize: 5,
  total: 0,
  tableData: [],
  form: {},
  formVisible: false,
  rules: {
    title: [
      {required: true, message: "请填写公告标题", trigger: "blur"},
    ],
    content: [
      {required: true, message: "请填写公告内容", trigger: "blur"},
    ]
  }
})

const load = () => {
  request.get('/notice/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list
      data.total = res.data?.total
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  })
}
load()

const reset = () => {
  data.title = null
  load();
};

const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const add = () => {
  request.post('/notice/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('新增成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg || '新增失败')
    }
  })
}


const update = () => {
  request.put('/notice/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('更新成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg || '更新失败')
    }
  })
}

const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      data.form.id ? update() : add()
    }
  })
}

const del = (id) => {
  ElMessageBox.confirm("你确认删除该行数据吗？（无法恢复）", "删除确认", {type: "warning",}).then(() => {
    request.delete('/notice/delete/' + id).then((res) => {
      if (res.code === "200") {
        ElMessage.success("删除成功");
        load();
        data.formVisible = false;
      } else {
        ElMessage.error(res.msg);
      }
    }).catch(() => {
      ElMessage.error("删除，请求失败，请重试");
    });
  }).catch(() => {
  });
}
</script>

<style scoped>

</style>