<template>
  <div class="card" style="width: 50%;">
    <div style="font-size: 25px"> 个人中心</div>
    <el-form
        ref="formRef"
        :model="data.user"
        :rules="data.rules"
        label-width="80px"
        style="padding: 20px 30px 20px 0;"
    >
      <el-form-item label="账号" prop="username">
        <el-input v-model="data.user.username" autocomplete="off" placeholder="请输入账号"/>
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="data.user.name" autocomplete="off" placeholder="请输入名称"/>
      </el-form-item>
      <el-form-item label="电话" prop="phone">
        <el-input v-model="data.user.phone" autocomplete="off" placeholder="请输电话"/>
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="data.user.email" autocomplete="off" placeholder="请输入邮箱"/>
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
    <div style="text-align: center">
      <el-button type="primary" style="padding: 20px 40px" @click="update">保存</el-button>
    </div>
  </div>
</template>

<script setup>

// 表格数据和状态管理
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";

const data = reactive({
  user: JSON.parse(localStorage.getItem('CodeUser') || "{}"),
});

const handleFileSuccess = (res) => {
  data.user.avatar = res.data
}

const update = () => {
  let url
  if (data.user.role === 'ADMIN') {
    url = 'admin/update'
  }
  if (data.user.role === 'USER') {
    url = 'user/update'
  }
  request.put(url, data.user).then((res) => {
    if (res.code === '200') {
      ElMessage.success('更新成功');
      localStorage.setItem('CodeUser', JSON.stringify(data.user))
      emit('updateUser')
    }
  });
}

const emit = defineEmits(['updateUser'])
</script>

<style scoped>

</style>