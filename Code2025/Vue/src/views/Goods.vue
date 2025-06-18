<template>
  <div>
    <!--第一行的查询和下单-->
    <div class="card" style="margin-bottom: 5px;">
      <el-input
          v-model="data.gname"
          :prefix-icon="Search"
          clearable
          placeholder="请输入商品名称查询"
          style="width: 200px; margin-right: 5px;"
          @clear="load"
      />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="primary" @click="handleAdd" v-if="data.user.role === 'ADMIN'||data.user.role === 'SUPPLIER'">
        新增
      </el-button>
      <el-button type="primary" @click="handlePurchase">下单</el-button>
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
        <el-table-column label="商品名称" prop="gname"/>
        <el-table-column label="供应商名称" prop="supplierName"/>
        <el-table-column label="商品图片">
          <template #default="scope">
            <el-image v-if="scope.row.img"
                      :src="scope.row.img"
                      :preview-src-list="[scope.row.img]"
                      :preview-teleported="true"
                      style="max-width: 200px; max-height: 200px; border-radius: 5px; display: block"
                      fit="contain"
            />
          </template>
        </el-table-column>
        <el-table-column label="库存余量" prop="num"/>
        <el-table-column label="商品详情" prop="content">
          <template v-slot="scope">
            <el-button type="primary" @click="viewContent(scope.row.content)">点击查看</el-button>
          </template>
        </el-table-column>
        <el-table-column label="商品价格" prop="price"/>
        <el-table-column label="发布时间" prop="time"/>

        <el-table-column label="操作" width="100" v-if="data.user.role === 'ADMIN'||data.user.role === 'SUPPLIER '">
          <template #default="scope">
            <el-button circle icon="Edit" type="primary" @click="handleEdit(scope.row)"/>
            <el-button circle icon="Delete" type="danger" @click="del(scope.row.id)"/>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页组件 -->
    <div class="card" style="margin-bottom: 5px;">
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
      <el-dialog v-model="data.formVisible" destroy-on-close title="商品详情" width="60%">
        <el-form
            ref="formRef"
            :model="data.form"
            label-width="100px"
            style="padding: 20px 30px 20px 0;"
        >
          <el-form-item label="商品名称" prop="gname">
            <el-input v-model="data.form.gname" autocomplete="off" placeholder="请输入商品名称"/>
          </el-form-item>
          <el-form-item label="库存余量" prop="num">
            <el-input v-model="data.form.num" autocomplete="off" placeholder="请输入库存余量"/>
          </el-form-item>
          <el-form-item label="商品价格" prop="price">
            <el-input v-model="data.form.price" autocomplete="off" placeholder="请输入商品价格"/>
          </el-form-item>
          <el-form-item label="商品供应商" prop="supplierId">
            <el-select
                v-model="data.form.supplierId"
                placeholder="请选择供应商"
                style="width: 100%"
            >
              <el-option
                  v-for="item in data.supplierData"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="商品详情" prop="content">
            <div style="border: 1px solid #ccc;width: 100%">
              <Toolbar
                  style="border-bottom: 1px solid #ccc"
                  :editor="editorRef"
                  :mode="mode"
              />
              <Editor
                  style="height: 500px; overflow-y: hidden;"
                  v-model="data.form.content"
                  :defaultConfig="editorConfig"
                  :mode="mode"
                  @onCreated="handleCreated"
              />
            </div>
          </el-form-item>
          <el-form-item label="图片" prop="img">
            <el-upload
                action="http://localhost:9999/files/upload"
                :headers="{token:data.user.token}"
                :on-success="handleFileSuccess"
                list-type="picture"
            >
              <el-button type="primary">上传图片</el-button>
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

      <!--点击查看详情的对话框-->
      <el-dialog v-model="data.viewVisible" destroy-on-close title="商品详情" width="60%">
        <div v-html="data.content"></div>
      </el-dialog>
    </div>
    <!--下单弹出的页面    -->
    <div>
      <!-- 下单对话框 -->
      <el-dialog v-model="data.Purchasevisible" destroy-on-close title="订单确认" width="70%">
        <el-table :data="data.selectedGoods" border style="width: 100%">
          <!-- 表格列定义 -->
          <el-table-column prop="gname" label="商品名称"/>
          <el-table-column prop="supplierName" label="供应商"/>
          <el-table-column label="单价(元)">
            <template #default="{row}">{{ row.price.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="数量" width="200">
            <template #default="{row}">
              <el-input-number
                  v-model="row.quantity"
                  :min="1"
                  :max="row.num"
                  @change="calculateTotal"
              />
            </template>
          </el-table-column>
          <el-table-column label="小计(元)">
            <template #default="{row}">
              {{ (row.price * row.quantity).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>

        <div class="order-summary">
          商品总数: {{ totalQuantity }} 件 |
          订单总计: <span class="total-price">{{ data.totalprice.toFixed(2) }}</span> 元
        </div>

        <template #footer>
          <el-button @click="data.Purchasevisible = false">取消</el-button>
          <el-button type="primary" @click="confirmOrder">确认下单</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import '@wangeditor/editor/dist/css/style.css'
import {Editor, Toolbar} from '@wangeditor/editor-for-vue'
import {Search} from "@element-plus/icons-vue";
import {computed, onBeforeMount, reactive, ref, shallowRef} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";

const formRef = ref()

// 处理下单点击
const handlePurchase = () => {
  if (data.selectedGoods.length === 0) {
    ElMessage.warning('请至少选择一件商品')
    return
  }
  if (data.user.role !== 'USER') {
    ElMessage.error('只有用户角色能够下单')
    return
  }
  // 初始化数量
  data.selectedGoods.forEach(item => {
    item.quantity = 1
  })

  calculateTotal()
  data.Purchasevisible = true
}

// 计算总数量和总价
const calculateTotal = () => {
  data.totalprice = data.selectedGoods.reduce((total, item) => {
    return total + (item.price * (item.quantity || 1))
  }, 0)
}


// 获取总数量
const totalQuantity = computed(() => {
  return data.selectedGoods.reduce((total, item) => {
    return total + (item.quantity || 1)
  }, 0)
})

// 处理表格选择变化
const handleSelectionChange = (rows) => {
  data.selectedGoods = rows.map(row => ({...row}))
}

// 确认下单
const confirmOrder = async () => {
  const userId = data.user.id
  const currentTime = new Date().toISOString()
  const orderNo = 'ORD' + Date.now() // 生成订单号

  // 构造订单数据
  const orderItems = data.selectedGoods.map(item => ({
    orderNo: orderNo,
    userId: userId,
    goodId: item.id,
    supplierId: item.supplierId,
    buyQuantity: item.quantity,
    unitPrice: item.price,
    totalPrice: (item.price * item.quantity).toFixed(2),
    time: currentTime
  }))

  try {
    const res = await request.post('/record/addBatch', orderItems)
    if (res.code === '200') {
      ElMessage.success('下单成功')
      data.Purchasevisible = false
      load() // 刷新商品列表
    } else {
      ElMessage.error(res.msg || '下单失败')
    }
  } catch (error) {
    ElMessage.error('下单请求失败')
  }
}

const data = reactive({
  user: JSON.parse(localStorage.getItem('CodeUser') || "{}"),
  pageNum: 1,
  pageSize: 5,
  total: 0,
  tableData: [],
  GoodsData: [],
  phpData: {},
  gname: null,
  form: {},
  rows: [],
  ids: [],
  totalprice: 0,
  formVisible: false,
  content: null,
  viewVisible: false,
  supplierData: [],
  selectedGoods: [], // 存储选中的商品
  Purchasevisible: false,
  totalprice: 0
})

/*富文本编辑的内容*/
const editorRef = shallowRef()
const mode = 'default'
const editorConfig = {MENU_CONF: {}}
//图片上传的配置
editorConfig.MENU_CONF['uploadImage'] = {
  fieldName: 'file',  // 必须与后端@RequestParam("file")一致
  server: "http://localhost:9999/files/wang/upload",
  headers: {
    token: data.user.token  // 确保传递了token
  },
  maxFileSize: 10 * 1024 * 1024, // 10M
  allowedFileTypes: ['image/*'],
}
//组件销毁时也要及时销毁编译器
onBeforeMount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})
//记录editor实例
const handleCreated = (editor) => {
  editorRef.value = editor
}
//wangEditor初始化结束


const load = () => {
  let params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    gname: data.gname
  };

  // 添加供应商ID过滤条件
  if (data.user.role === 'SUPPLIER') {
    params.supplierId = data.user.id;
  }

  request.get('/goods/selectPage', {
    params: params
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
  data.gname = null
  load();
};

const handleAdd = () => {
  data.form = {}
  // 如果是供应商角色，自动设置供应商ID
  if (data.user.role === 'SUPPLIER') {
    data.form.supplierId = data.user.id
  }
  data.formVisible = true
}

const handleFileSuccess = (res) => {
  data.form.img = res.data
}

const add = () => {
  request.post('/goods/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('新增成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  request.put('/goods/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('更新成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
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

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const del = (id) => {
  ElMessageBox.confirm("你确认删除该行数据吗？（无法恢复）", "删除确认", {type: "warning",}).then(() => {
    request.delete('/goods/delete/' + id).then((res) => {
      if (res.code === "200") {
        ElMessage.success("删除成功");
        data.formVisible = false;
        load();
      } else {
        ElMessage.error(res.msg);
      }
    }).catch(() => {
      ElMessage.error("删除，请求失败，请重试");
    });
  }).catch(() => {
  });
}

const viewContent = (content) => {
  data.content = content
  data.viewVisible = true
}

const loadSupplier = () => {
  request.get('/supplier/selectAll').then(res => {
    if (res.code === '200') {
      data.supplierData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadSupplier()
</script>

<style scoped>
.order-summary {
  margin-top: 20px;
  text-align: right;
  font-size: 16px;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 4px;
}

.total-price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}
</style>