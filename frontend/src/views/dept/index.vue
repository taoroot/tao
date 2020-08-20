<template>
  <div class="app-container">
    <el-card style="min-height: 600px">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-button-group style="margin: 10px 0">
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd" />
            <el-button type="primary" icon="el-icon-edit" @click="isEditForm = true" />
            <el-button type="primary" icon="el-icon-delete" @click="handleDelete" />
          </el-button-group>
          <el-tree :data="tableTreeData" :props="{ children: 'children', label: 'name' }" @node-click="handleNodeClick" />
        </el-col>
        <el-col :span="16">
          <el-form ref="dataForm" :model="dataForm" :rules="dataRule" label-width="150px" size="small" style="text-align:left;">
            <el-form-item label="id">
              <el-input v-model="dataForm.id" :disabled="!isEditForm" />
            </el-form-item>

            <el-form-item label="parentId">
              <el-cascader v-model="dataForm.path" :props="{value: 'id', label: 'name', checkStrictly: true}" :options="tableTreeData" :disabled="!isEditForm" />
            </el-form-item>

            <el-form-item label="name" prop="name">
              <el-input v-model="dataForm.name" :disabled="!isEditForm" />
            </el-form-item>
          </el-form>
          <el-button v-if="isEditForm" @click="create">保存</el-button>
          <el-button v-if="isEditForm" @click="cancel">取消</el-button>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { getTree, getItemById, saveItem, deleteItem, updateItem } from '@/api/dept'

var _defaultRow = {
  'id': -1,
  'name': '',
  'parentId': 0,
  'weight': 1
}
export default {
  data() {
    return {
      tableTreeData: [],
      isEditForm: false,
      authorityTypeList: ['菜单', '按钮'],
      dataForm: Object.assign({}, _defaultRow),
      dataRule: {
        name: [{ required: true, trigger: 'blur' }],
        title: [{ required: true, trigger: 'blur' }],
        path: [{ required: true, trigger: 'blur' }],
        icon: [{ required: true, trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.findTreeData()
  },
  methods: {
    handleNodeClick(data) {
      if (data.id) {
        getItemById(data.id).then(res => {
          this.dataForm = res.data
        })
      }
    },
    findTreeData() {
      getTree().then(res => {
        this.tableTreeData = res.data
      })
    },
    handleAdd() {
      this.isEditForm = true
      this.dataForm = Object.assign({}, _defaultRow)
    },
    create() {
      if (this.dataForm.id) {
        updateItem(this.dataForm).then(res => {
          if (res.code === 0) {
            this.findTreeData()
            this.isEditForm = false
          }
        })
      } else {
        saveItem(this.dataForm).then(res => {
          if (res.code === 0) {
            this.findTreeData()
            this.isEditForm = false
          }
        })
      }
    },
    cancel() {
      this.$refs.dataForm.resetFields()
      this.isEditForm = false
      this.dataForm.parentId = 0
    },
    handleDelete() {
      this.$confirm('此操作将把分类删除, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteItem(this.dataForm.id).then(response => {
          if (response.code === 0) {
            this.findTreeData()
          }
        })
      })
    }

  }
}
</script>
