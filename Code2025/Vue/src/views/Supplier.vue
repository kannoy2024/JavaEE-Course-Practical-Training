<template>
  <div>
    <div class="card" style="margin-bottom: 5px;box-shadow: 0 0 6px rgb(57,148,255,0.9);">
      <el-input clearable @clear="load " style="width: 300px;margin-right: 10px" v-model="data.username"
                placeholder="请输入账号查询" :prefix-icon="Search"></el-input>
      <el-input clearable @clear="load " style="width: 300px;margin-right: 10px" v-model="data.name"
                placeholder="请输入名称查询" :prefix-icon="Search"></el-input>

      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="primary" @click="reset">重置</el-button>
    </div>
    <div class="card" style="margin-bottom: 5px;box-shadow: 0 0 6px rgba(255,57,186,0.9);">
      <el-button type="danger" @click="deleteBatch">批量删除</el-button>
      <el-button type="primary" @click="handleAdd">新增</el-button>
      <el-button type="info" @click="exportData">批量导出</el-button>
      <el-upload
          style="display:inline-block;margin-left:11px;"
          :show-file-list="false"
          action="http://localhost:9999/supplier/import"
          :on-success="handleImportSuccess">
        <el-button type="success">批量导入</el-button>
      </el-upload>
    </div>
    <div class="card" style="margin-bottom: 5px;box-shadow: 0 0 6px rgba(159,57,255,0.9);">
      <el-table :data="data.tableData" style="width: 100%"
                :header-cell-style="{color:'#f64ee0',backgroundColor:'#b2e0fa'}"
                :cell-style="{color:'#0a77f6',fontSize:'15px',fontStyle:'italic',}"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column label="头像" width="100">
          <template #default="scope">
            <el-image v-if="scope.row.avatar" :src="scope.row.avatar" :preview-src-list="[scope.row.avatar]"
                      :preview-teleported="true"
                      style="width: 40px;height:40px;border-radius: 50%;display: block"/>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="账号"/>
        <el-table-column prop="name" label="名称"/>
        <el-table-column prop="phone" label="电话"/>
        <el-table-column prop="email" label="邮箱"/>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button icon="Edit" type="primary" circle @click="handleEdit(scope.row)"></el-button>
            <el-button icon="Delete" type="danger" circle @click="del(scope.row.id)"></el-button>
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


    <el-dialog title="供应商信息" v-model="data.formVisible" width="500" style="text-align: center" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding: 20px 30px 0px 0">
        <el-form-item prop="username" label="账号" :label-width="formLabelWidth">
          <el-input v-model="data.form.username" autocomplete="off" placeholder="请输入账号"/>
        </el-form-item>
        <el-form-item prop="name" label="名称" :label-width="formLabelWidth">
          <el-input v-model="data.form.name" autocomplete="off" placeholder="请输入名称"/>
        </el-form-item>
        <el-form-item prop="phone" label="电话" :label-width="formLabelWidth">
          <el-input v-model="data.form.phone" autocomplete="off" placeholder="请输入电话"/>
        </el-form-item>
        <el-form-item prop="email" label="邮箱" :label-width="formLabelWidth">
          <el-input v-model="data.form.email" autocomplete="off" placeholder="请输入邮箱"/>
        </el-form-item>

        <el-form-item prop="avatar" label="头像">
          <el-upload
              action="http://localhost:9999/files/upload"
              :headers="{token:data.user.token}"
              :on-success="handleFileSuccess"
              list-type="picture">
            <el-button type="primary">上传头像</el-button>
          </el-upload>
        </el-form-item>

      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">保 存</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>
<script setup>
import {reactive, ref} from "vue";
import {Search} from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";


const formRef = ref()

const data = reactive({
  user: JSON.parse(localStorage.getItem('CodeUser') || '{}'),
  name: null,
  pageSize: 8,
  pageNum: 1,
  total: null,
  tableData: [],
  username: null,
  phone: null,
  email: null,
  commodity: null,
  formVisible: false,
  form: {},
  rows: [],
  ids: [],
  rules: {
    username: [
      {required: true, message: '请填写账号', trigger: 'blur'}
    ],
    name: [
      {required: true, message: '请填写名称', trigger: 'blur'}
    ],
    phone: [
      {required: true, message: '请填写电话', trigger: 'blur'}
    ],
    email: [
      {required: true, message: '请填写邮箱', trigger: 'blur'}
    ],
    commodity: [
      {required: true, message: '请填写供应商品', trigger: 'blur'}
    ]
  }
})

// 分页加载逻辑
const load = () => {
  request
      .get("supplier/selectPage", {
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
load()

const reset = () => {
  data.name = null,
      data.username = null
  load()
}

const handleAdd = () => {
  data.formVisible = true
  data.form = {}
}

const add = () => {
  //formRef是表单的引用
  formRef.value.validate((valid) => {
    if (valid) {
      request.post("/supplier/add", data.form).then(res => {
        if (res.code === '200') {
          console.log(res.data)
          ElMessage.success(res.msg)
          load()
          data.formVisible = false
        } else {
          console.log(res.data)
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))//深度拷贝数据
  data.formVisible = true
}

const update = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      request.put("/supplier/update", data.form).then(res => {
            if (res.code === '200') {
              console.log(res.data)
              ElMessage.success(res.msg)
              load()
              data.formVisible = false
            } else {
              console.log(res.data)
              ElMessage.error(res.msg)
            }
          }
      )
    }
  })
}

const save = () => {
  data.form.id ? update() : add()


}

const del = (id) => {
  ElMessageBox.confirm('请确认是否删除，删除后无法恢复', '删除确认', {type: 'warning'}).then(res => {
    request.delete('/supplier/delete/' + id).then(res => {
      if (res.code === '200') {
        console.log(res.data)
        ElMessage.success(res.msg)
        load()
      } else {
        console.log(res.data)
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
  })
}

const handleSelectionChange = (rows) => {   //rows 就是实际选择的数组
  data.rows = rows
  data.ids = data.rows.map(v => v.id)//map可以把一个存放对象的数组，转换成一个存放每个对象的id属性对应的数值，比如说：对象Test1对应的id属性的数值为13，则把13存入数组ids中
}

const deleteBatch = () => {
  if (data.rows.length === 0) {
    ElMessage.warning("请选择数据")
    return
  }
  ElMessageBox.confirm('请确认是否删除，删除后无法恢复', '删除确认', {type: 'warning'}).then(res => {
    request.delete('/supplier/deleteAll', {data: data.rows}).then(res => {
      if (res.code === '200') {
        console.log(res.data)
        ElMessage.success(res.msg)
        load()
      } else {
        console.log(res.data)
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
  })
}

const exportData = () => {
  let idsStr = data.ids.join(",")//把数组转换成字符串  [1,2,3] =>"1,2,3"
  let url = `http://localhost:9999/supplier/export?username=${data.username === null ? '' : data.username}`
      + `&name=${data.name === null ? '' : data.name}`
      + `&ids=${idsStr}`
      + `&token=${data.user.token}`
  window.open(url)
}

const handleImportSuccess = (response) => {
  if (response.code === '200') {
    ElMessage.success(response.msg)
    load()
  } else {
    ElMessage.error(response.msg)

  }
}

const handleFileSuccess = (res) => {
  data.form.avatar = res.data;
}
</script>

<style itemscope>
.el-table .el-table__body tr:hover {
  background-color: #9ff6f6; /* 设置悬停时的背景颜色 */
}

.el-dialog__title {
  font-size: 20px;
  font-weight: bold;
  color: #0564f4;
}
</style>