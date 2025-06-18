<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <!-- 用户信息展示 -->
      <div class="card" style="margin-bottom: 20px;">
        <div style="display: flex; align-items: center;">
          <div style="margin-right: 20px;">
            <img v-if="data.user?.avatar" style="width: 40px;height: 40px;border-radius: 50%" :src="data.user?.avatar"/>
            <Avatar v-else class="user-avatar"/>
          </div>
          <div>
            <h3>{{ data.user.name || '用户' }}</h3>
            <p>用户ID: {{ data.user.id }}</p>
            <p>用户角色: {{ data.user.role }}</p>
            <p>账户余额: ¥{{ data.user.balance?.toFixed(2) || '0.00' }}</p>
          </div>
        </div>
      </div>


      <!-- 充值入口 -->
      <div class="card" style="margin-bottom: 20px;">
        <h3 style="margin-bottom: 15px;">充值中心</h3>
        <div style="margin-bottom: 15px;">
          <el-radio-group v-model="data.rechargeAmount">
            <el-radio-button :label="100">¥100</el-radio-button>
            <el-radio-button :label="500">¥500</el-radio-button>
            <el-radio-button :label="1000">¥1000</el-radio-button>
            <el-radio-button :label="2000">¥2000</el-radio-button>
          </el-radio-group>
        </div>
        <div style="margin-bottom: 15px;">
          <el-input
              v-model="data.customAmount"
              placeholder="自定义金额"
              style="width: 200px;"
              @input="data.rechargeAmount = null">
            <template #append>元</template>
          </el-input>
        </div>
        <el-button
            type="primary"
            @click="submitRecharge"
            :disabled="!(data.rechargeAmount || data.customAmount)">
          立即充值
        </el-button>
      </div>
    </div>
    <!--下半部分-->
    <div class="card" style="margin-bottom: 5px;">
      <!-- 充值记录表格 -->
      <div class="card" style="margin-bottom: 20px;">
        <div style="display: flex; justify-content: space-between; margin-bottom: 15px;">
          <h3>充值记录</h3>
          <el-input
              v-model="data.searchQuery"
              placeholder="搜索充值记录"
              clearable
              style="width: 200px;"
              @clear="loadRechargeRecords"
              @keyup.enter="loadRechargeRecords">
            <template #prefix>
              <el-icon>
                <search/>
              </el-icon>
            </template>
          </el-input>
        </div>

        <el-table
            :data="data.rechargeRecords"
            style="width: 100%"
            empty-text="暂无充值记录">
          <el-table-column prop="id" label="订单号" width="180"/>
          <el-table-column prop="amount" label="充值金额">
            <template #default="{row}">
              ¥{{ row.amount.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="time" label="充值时间"/>
          <el-table-column prop="status" label="订单状态" width="120" align="center">
            <template #default="{row}">
              <el-tag :type="row.status === '已完成' ? 'success' : 'warning'">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="150" align="center" v-if="data.user.role === 'ADMIN'">
            <template #default="{row}">
              <el-button
                  v-if="row.status !== '已完成'"
                  type="primary"
                  size="small"
                  @click="completeRecharge(row.id)">
                完成订单
              </el-button>
              <span v-else>已完成</span>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
            v-model:current-page="data.pageNum"
            v-model:page-size="data.pageSize"
            :page-sizes="[5, 10, 15]"
            :total="data.totalRecords"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="loadRechargeRecords"
            @size-change="loadRechargeRecords"
            style="margin-top: 20px;"
        />
      </div>
    </div>
  </div>
</template>
<script setup>
import {onMounted, reactive} from 'vue'
import {Search} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import request from "@/utils/request.js"

const data = reactive({
  user: JSON.parse(localStorage.getItem('CodeUser') || '{}'),
  rechargeAmount: null,
  customAmount: '',
  searchQuery: '',
  rechargeRecords: [],
  pageNum: 1,
  pageSize: 5,
  totalRecords: 0
})

// 加载用户信息和充值记录
onMounted(() => {
  loadUserInfo()
  loadRechargeRecords()
})

// 加载用户信息
const loadUserInfo = () => {
  // 直接从localStorage获取或请求后端
  data.user = JSON.parse(localStorage.getItem('CodeUser') || '{}')
}

// 加载充值记录
const loadRechargeRecords = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    search: data.searchQuery
  }

  if (data.user.role === 'USER') {
    params.user_id = data.user.id
  }

  request.get('/recharge/selectPage', {params}).then(res => {
    if (res.code === '200') {
      data.rechargeRecords = res.data.list
      data.totalRecords = res.data.total
    }
  })
}
// 提交充值
const submitRecharge = () => {
  const amount = data.rechargeAmount || parseFloat(data.customAmount);

  if (!amount || amount <= 0) {
    ElMessage.error('请输入有效的充值金额');
    return;
  }
  if (data.user.role !== 'USER') {
    ElMessage.error('只有用户角色能够充值');
    return;
  }
  ElMessageBox.confirm(`确定充值 ¥${amount.toFixed(2)} 元吗？`, '确认充值', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 构建完整的充值对象
    const rechargeData = {
      userId: data.user.id,
      amount: amount,
      status: "处理中",     // 添加状态
      time: new Date().toISOString()  // 添加当前时间
    };

    request.post('/recharge/add', rechargeData).then(res => {
      if (res.code === '200') {
        ElMessage.success('充值申请已提交');
        loadRechargeRecords();
        data.rechargeAmount = null;
        data.customAmount = '';
      } else {
        ElMessage.error(res.msg);
      }
    });
  }).catch(() => {
    // 用户取消操作
  });
}


// 完成充值（管理员操作）
const completeRecharge = (id) => {
  ElMessageBox.confirm('确认已完成此充值订单?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.post('/recharge/complete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('充值已完成');
        loadRechargeRecords();
      } else {
        ElMessage.error(res.msg);
      }
    });
  }).catch(() => {
  });
}
</script>

<style scoped>

</style>