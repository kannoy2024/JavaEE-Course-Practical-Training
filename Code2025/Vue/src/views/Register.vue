
<template>
  <div class="bg">
    <div style="width: 350px;background-color: #eaeaea;opacity: 89% ;border-radius: 5px;box-shadow: 0 0 10px black;padding:40px 20px">
      <el-form ref="formRef" :model="data.form" :rules="data.rules" status-icon >
        <div class="rainbow-text" style="margin-bottom: 40px;text-align: center;font-weight: bold;font-size: 24px">欢迎注册</div>
        <el-form-item  prop="username">
          <el-input size="large" v-model="data.form.username" autocomplete="off" prefix-icon="User" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item  prop="password">
          <el-input size="large" show-password v-model="data.form.password" autocomplete="off" prefix-icon="Lock" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item  prop="confirmPassword">
          <el-input size="large" show-password v-model="data.form.confirmPassword" autocomplete="off" prefix-icon="Lock" placeholder="请再次输入密码" />
        </el-form-item>
        <div style="margin-bottom: 20px">
          <el-button style="width: 100% ;background-color: lime ; border-color: lime" type="primary" size="large" @click="register">注册</el-button>
        </div>
        <div style="text-align: right">
          已有账号账号？<a style="color: lime" href="/login">请登录</a>
        </div>
      </el-form>
    </div>
  </div>
</template>


<script setup>
import {reactive,ref} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const formRef = ref();


const validatePass= (rule, value, callback) => {
  if (value !== data.form.password) {
    callback(new Error("两次密码不一致！"))
  } else {
    callback()
  }
}


const data = reactive({
  form:{},
  rules: {
    username: [
      {required: true, message: "请填写账号", trigger: "blur"},
      {min:3,message: '账号最少三位',trigger: "blur"},
    ],
    password: [
      {required: true, message: "请填写密码", trigger: "blur"},
    ],
    confirmPassword:[
      {required: true, message: "请再次输入密码", trigger: "blur"},
      {validator: validatePass, trigger: "blur"}
    ]
  }
})

const register=()=>{
  formRef.value.validate((valid) => {
    if(valid) {
      request.post('/register', data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success("注册成功了")
          router.push("/login")
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}
</script>


<style scoped>
.bg {
  height: 100vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url("@/assets/images/背景2.jpg");
  background-size: cover;
}

.rainbow-text {
  background: linear-gradient(to right,
  #ff0000, #ff7f00, #ffff00, #00ff00, #0000ff, #4b0082, #9400d3, #ff0000 ,#ff0000, #ff7f00, #ffff00, #00ff00, #0000ff, #4b0082, #9400d3, #ff0000);
  background-size: 400% 100%;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  animation: rainbow-animation 4s linear infinite;
  transform: translateZ(0); /* 硬件加速 */
}

@keyframes rainbow-animation {
  0% { background-position: 0% 50%; }
  100% { background-position: 100% 50%; }
}
</style>