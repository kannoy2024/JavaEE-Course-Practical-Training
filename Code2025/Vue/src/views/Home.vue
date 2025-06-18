<template>
  <div class="card" style="display: flex;">
    <div class="card" style="margin-top: 10px;width: 50%">
      <div style="margin-bottom: 20px;font-size: 30px">留言</div>
      <el-timeline style="max-width: 600px">
        <el-timeline-item :timestamp="item.time" color="#0bbd87" placement="top" v-for="item in data.noticeData"
                          :key="item.id">
          <h4>{{ item.title }}</h4>
          <p>{{ item.content }}</p>
        </el-timeline-item>
      </el-timeline>
    </div>
    <div class="card" style="margin-top: 10px;width: 50%">
      <div class="demo-collapse">
        <el-collapse v-model="activeName" accordion>
          <el-collapse-item
              :title="item.title"
              :name="item.id"
              v-for="item in data.noticeData"
              :key="item.id"
          >
            <div>{{ item.content }}</div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue'
import request from '@/utils/request'
import {ElMessage} from "element-plus"

const activeName = ref('') // 定义折叠面板当前激活项
const data = reactive({
  user: JSON.parse(localStorage.getItem('CodeUser') || "{}"),
  noticeData: []
})

const loadNotice = () => {
  request.get('/notice/selectAll').then(res => {
    if (res.code === '200') {
      data.noticeData = res.data
      if (data.noticeData.length > 4) {
        data.noticeData = data.noticeData.slice(-4)
      }
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadNotice()
</script>

<style scoped>

</style>