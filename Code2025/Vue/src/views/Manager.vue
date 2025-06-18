<template>
  <div>
    <!-- ==================== 头部区域 ==================== -->
    <div class="header-container">
      <!-- Logo区域 -->
      <div class="logo-container">
        <!-- 网站Logo -->
        <img
            alt="网站Logo"
            class="logo-image"
            src="@/assets/images/老君.jpg"
        >
        <span class="rainbow-text logo-text">祖振妙妙屋</span>
      </div>
      <!-- 面包屑导航 -->
      <div class="breadcrumb-container">
        <span
            @click="router.push('/manager/home')"
            class="rainbow-text logo-text;breadcrumb-item"
        >
          首页————>
        </span>
        <!-- 当前页面名称 -->
        <span class="current-page">
          {{ router.currentRoute.value.meta.name }}
        </span>
        <span class="current-page">
          当前角色类型 {{ data.user.role }}
        </span>
      </div>
      <!-- 用户信息区域 -->
      <div class="user-info-container">
        <el-dropdown>
          <!-- 用户信息展示 -->
          <div class="user-info-wrapper">
            <img v-if="data.user?.avatar" style="width: 40px;height: 40px;border-radius: 50%" :src="data.user?.avatar"/>
            <Avatar v-else class="user-avatar"/>
            <span>{{ data.user?.name }}</span>

          </div>
          <!-- 下拉菜单 -->
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/manager/person')">个人信息</el-dropdown-item>
              <el-dropdown-item @click="router.push('/manager/updatePassword')">修改密码</el-dropdown-item>
              <el-dropdown-item @click="loginOut">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    <!-- 头部区域结束 -->


    <!-- ==================== 主内容区域 ==================== -->
    <div class="main-container">
      <!-- 左侧菜单 -->
      <div class="menu-container">
        <el-menu
            :default-active="router.currentRoute.value.path"
            :default-openeds="['1']"
            class="side-menu"
            router
        >
          <el-menu-item index="/manager/home">
            <el-icon>
              <House/>
            </el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/manager/supplier" v-if="data.user.role === 'ADMIN'||data.user.role === 'USER'">
            <el-icon>
              <User/>
            </el-icon>
            <span>供应商信息</span>
          </el-menu-item>
          <el-menu-item index="/manager/goods">
            <el-icon>
              <Position/>
            </el-icon>
            <span>商品信息</span>
          </el-menu-item>
          <el-menu-item index="/manager/order">
            <el-icon>
              <Position/>
            </el-icon>
            <span>订单信息</span>
          </el-menu-item>
          <el-menu-item index="/manager/recharge">
            <el-icon>
              <Position/>
            </el-icon>
            <span>充值中心</span>
          </el-menu-item>
          <el-menu-item index="/manager/admin" v-if="data.user.role === 'ADMIN'">
            <el-icon>
              <Setting/>
            </el-icon>
            <span>
             管理员信息
          </span>
          </el-menu-item>
          <el-menu-item index="/manager/user" v-if="data.user.role === 'ADMIN'">
            <el-icon>
              <User/>
            </el-icon>
            <span>用户信息</span>
          </el-menu-item>

          <el-menu-item index="/manager/notice" v-if="data.user.role === 'ADMIN'">
            <el-icon>
              <Monitor/>
            </el-icon>
            <span>留言板</span>
          </el-menu-item>
          <el-menu-item index="/manager/notice" v-else>
            <el-icon>
              <Monitor/>
            </el-icon>
            <span>公告信息</span>
          </el-menu-item>
          <el-menu-item index="/manager/person">
            <el-icon>
              <Avatar/>
            </el-icon>
            <span>个人中心</span>
          </el-menu-item>
          <el-menu-item index="/manager/updatePassword">
            <el-icon>
              <Lock/>
            </el-icon>
            <span>修改密码</span>
          </el-menu-item>
        </el-menu>
      </div>
      <!-- 菜单区域结束 -->

      <!-- 右侧数据内容区域 -->
      <div class="content-container">
        <RouterView @updateUser="updateUser"/>
      </div>
      <!-- 数据区域结束 -->
    </div>
    <!-- 下方区域结束 -->
  </div>
</template>

<script setup>

import {reactive} from 'vue'
import router from '@/router/index.js'

const data = reactive({
  user: JSON.parse(localStorage.getItem('CodeUser') || '{}')
})

/**
 * 退出登录处理
 */
const loginOut = () => {
  localStorage.removeItem('CodeUser')
  location.href = '/login'
}

// ==================== 登录状态检查 ====================
/**
 * 登录状态验证逻辑
 */
let userStr = localStorage.getItem('CodeUser')
if (userStr) {
  try {
    let user = JSON.parse(userStr)
    if (!user?.id) {
      location.href = '/login'
    }
  } catch (e) {
    console.error('用户数据解析失败:', e)
    location.href = '/login'
  }
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('CodeUser') || '{}')
}
</script>

<style>
/* 动画效果 */
@keyframes rainbow {
  0% {
    color: red;
  }
  14% {
    color: orange;
  }
  28% {
    color: yellow;
  }
  42% {
    color: green;
  }
  57% {
    color: blue;
  }
  71% {
    color: indigo;
  }
  85% {
    color: violet;
  }
  100% {
    color: red;
  }
}

/* 头部样式 */
.header-container {
  height: 60px;
  display: flex;
}

.logo-container {
  width: 200px;
  display: flex;
  align-items: center;
  padding-left: 20px;
  background-color: rgba(8, 192, 216, 0.53);
  border-bottom: 2px solid rgba(5, 0, 0, 0.94);
}

.logo-image {
  width: 40px;
}

.logo-text {
  font-size: 20px;
  font-weight: bold;
  margin-left: 5px;
}

.breadcrumb-container {
  flex: 1;
  border-bottom: 2px solid rgba(5, 0, 0, 0.94);
  display: flex;
  align-items: center;
  padding-left: 20px;
}

.breadcrumb-item {
  margin-right: 5px;
  cursor: pointer;
}

.current-page {
  margin-left: 5px;
}

.user-info-container {
  width: fit-content;
  display: flex;
  align-items: center;
  padding-right: 20px;
  border-bottom: 2px solid rgba(5, 0, 0, 0.94);
}

.user-info-wrapper {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 40px;
  height: 50px;
  margin-right: 8px;
}

/* 主体区域样式 */
.main-container {
  display: flex;
}

.menu-container {
  width: 200px;
}

.side-menu {
  min-height: calc(100vh - 60px);
  background-color: rgba(40, 93, 191, 0.84);
  border: none;
}

.content-container {
  margin: 10px;
  background-color: #eaeaea;
  flex: 1;
  width: 0;
}

/* 左侧下拉菜单项样式 */

.el-menu-item {
  background-color: rgba(40, 93, 191, 0.84);
}

.el-menu-item {
  height: 50px;
}

.el-menu .is-active {
  background-color: rgba(8, 192, 216, 0.53);
  color: rgba(40, 93, 191, 0.84);
}

.el-sub-menu__title:hover {
  background-color: rgba(40, 93, 191, 0.84);
}

.el-menu-item:not(.is-active):hover {
  background-color: rgba(40, 93, 191, 0.84);
  color: black;
}

/* 管理员下拉菜单样式 */
.el-dropdown {
  cursor: pointer;
}

.el-tooltip__trigger {
  outline: none;
}

.el-menu--inline .el-menu-item {
  padding-right: 50px !important;
}

.rainbow-text {
  animation: rainbow 3s linear infinite;
}
</style>