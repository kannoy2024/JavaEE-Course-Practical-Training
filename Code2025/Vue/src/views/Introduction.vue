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
        <el-table-column label="攻略图片">
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
        <el-table-column label="攻略分类" prop="categoryTitle"/>
        <el-table-column label="攻略内容" prop="content">
          <template v-slot="scope">
            <el-button type="primary" @click="viewContent(scope.row.content)">点击查看</el-button>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" prop="time"/>

        <el-table-column label="操作" width="100" v-if="data.user.role === 'ADMIN'">
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
      <el-dialog v-model="data.formVisible" destroy-on-close title="攻略信息" width="60%">
        <el-form
            ref="formRef"
            :model="data.form"
            label-width="80px"
            style="padding: 20px 30px 20px 0;"
        >
          <el-form-item label="攻略标题" prop="title">
            <el-input v-model="data.form.title" autocomplete="off" placeholder="请输入标题"/>
          </el-form-item>
          <el-form-item label="攻略分类" prop="title">
            <el-select
                v-model="data.form.categoryId"
                placeholder="请选择攻略分类"
                style="width: 100%"
            >
              <el-option
                  v-for="item in data.categoryData"
                  :key="item.id"
                  :label="item.title"
                  :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="攻略内容" prop="content">
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
      <el-dialog v-model="data.viewVisible" destroy-on-close title="攻略信息" width="60%">
        <div v-html="data.content"></div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import '@wangeditor/editor/dist/css/style.css'
import {Editor, Toolbar} from '@wangeditor/editor-for-vue'
import {Search} from "@element-plus/icons-vue";
import {onBeforeMount, reactive, ref, shallowRef} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";

const formRef = ref()

const data = reactive({
  user: JSON.parse(localStorage.getItem('CodeUser') || "{}"),
  pageNum: 1,
  pageSize: 5,
  total: 0,
  tableData: [],
  title: null,
  form: {},
  formVisible: false,
  content: null,
  viewVisible: false,
  categoryData: []
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
  request.get('/introduction/selectPage', {
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

const handleFileSuccess = (res) => {
  data.form.img = res.data
}

const add = () => {
  request.post('/introduction/add', data.form).then(res => {
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
  request.put('/introduction/update', data.form).then(res => {
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
    request.delete('/introduction/delete/' + id).then((res) => {
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

const loadCategory = () => {
  request.get('/category/selectAll').then(res => {
    if (res.code === '200') {
      data.categoryData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadCategory()
</script>

<style scoped>

</style>