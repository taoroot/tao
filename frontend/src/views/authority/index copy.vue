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
              <el-input v-model="dataForm.parentId" :disabled="!isEditForm" />
            </el-form-item>

            <el-form-item label="name" prop="name">
              <el-input v-model="dataForm.name" :disabled="!isEditForm" />
            </el-form-item>

            <el-form-item label="title" prop="title">
              <el-input v-model="dataForm.title" :disabled="!isEditForm" />
            </el-form-item>

            <el-form-item label="type" prop="type">
              <el-radio-group v-model="dataForm.type">
                <el-radio v-for="(type, index) in authorityTypeList" :key="index" :disabled="!isEditForm" :label="index">{{ type }}</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item v-if="dataForm.type === 0" label="icon">
              <el-popover placement="bottom-start" width="460" trigger="click" :disabled="!isEditForm" @show="$refs['iconSelect'].reset()">
                <IconSelect ref="iconSelect" @selected="dataForm.icon = name" />
                <el-input slot="reference" v-model="dataForm.icon" placeholder="点击选择图标" :disabled="!isEditForm" readonly>
                  <svg-icon v-if="dataForm.icon" slot="prefix" :icon-class="dataForm.icon" class="el-input__icon" style="height: 32px;width: 16px;" />
                  <i v-else slot="prefix" class="el-icon-search el-input__icon" />
                </el-input>
              </el-popover>
            </el-form-item>

            <el-form-item v-if="dataForm.type === 0" label="path" prop="path">
              <el-input v-model="dataForm.path" :disabled="!isEditForm" />
            </el-form-item>

            <el-form-item v-if="dataForm.type === 0" label="component" prop="url">
              <el-input v-model="dataForm.component" :disabled="!isEditForm" placeholder="Layout" />
            </el-form-item>

            <el-form-item v-if="dataForm.type === 0" label="breadcrumb" prop="url">
              <el-input v-model="dataForm.breadcrumb" :disabled="!isEditForm" />
            </el-form-item>

            <el-form-item v-if="dataForm.type === 0" label="redirect" prop="url">
              <el-input v-model="dataForm.redirect" :disabled="!isEditForm" />
            </el-form-item>

            <el-form-item v-if="dataForm.type === 0" label="weight" prop="weight">
              <el-input-number v-model="dataForm.weight" :disabled="!isEditForm" controls-position="right" label="weight" :min="0" />
            </el-form-item>

            <el-form-item label="hidden" prop="hidden">
              <el-switch v-model="dataForm.hidden" :disabled="!isEditForm" />
            </el-form-item>
            <el-form-item label="alwaysShow" prop="alwaysShow">
              <el-switch v-model="dataForm.alwaysShow" :disabled="!isEditForm" />
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
import IconSelect from '@/components/IconSelect'
import { getTree, getAuthorityById, saveAuthority, deleteAuthority, updateAuthority } from '@/api/authority'

var _defaultRow = {
  'id': -1,
  'path': '',
  'component': null,
  'hidden': null,
  'alwaysShow': null,
  'redirect': null,
  'name': '',
  'title': '',
  'icon': '',
  'breadcrumb': null,
  'parentId': 0,
  'weight': 1,
  'type': 0
}
export default {
  components: { IconSelect },
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
        getAuthorityById(data.id).then(res => {
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
        updateAuthority(this.dataForm).then(res => {
          if (res.code === 0) {
            this.findTreeData()
            this.isEditForm = false
          }
        })
      } else {
        saveAuthority(this.dataForm).then(res => {
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
    // 删除操作
    handleDelete() {
      this.$confirm('此操作将把分类删除, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteAuthority(this.dataForm.id).then(response => {
          if (response.code === 0) {
            this.findTreeData()
          }
        })
      })
    }

  }
}
</script>
