<template>
  <div class="app-container">
    <el-card style="min-height: 600px">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-button-group style="margin: 10px 0">
            <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAdd" />
            <el-button type="primary" icon="el-icon-edit" size="mini" @click="isEditForm = true" />
            <el-button type="primary" icon="el-icon-delete" size="mini" @click="handleDelete" />
          </el-button-group>
          <el-tree :data="tableTreeData" :props="defaultProps" @node-click="handleNodeClick" />
        </el-col>
        <el-col :span="16">
          <el-form
            ref="dataForm"
            :model="dataForm"
            :rules="dataRule"
            label-width="80px"
            :size="size"
            style="text-align:left;"
          >
            <el-form-item label="主键">
              <el-input v-model="dataForm.authorityId" :disabled="!isEditForm" />
            </el-form-item>

            <el-form-item label="父节点">
              <el-input v-model="dataForm.parentId" :disabled="true" />
            </el-form-item>

            <el-form-item label="名称" prop="name">
              <el-input v-model="dataForm.name" :disabled="!isEditForm" />
            </el-form-item>

            <el-form-item label="类型" prop="type">
              <el-radio-group v-model="dataForm.type">
                <el-radio v-for="(type, index) in authorityTypeList" :key="index" :disabled="!isEditForm" :label="index">{{ type }}</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item v-if="dataForm.type === 0" label="图标">
              <el-popover
                placement="bottom-start"
                width="460"
                trigger="click"
                @show="$refs['iconSelect'].reset()"
              >
                <IconSelect ref="iconSelect" @selected="selected" />
                <el-input
                  slot="reference"
                  v-model="dataForm.icon"
                  placeholder="点击选择图标"
                  :disabled="!isEditForm"
                  readonly
                >
                  <svg-icon
                    v-if="dataForm.icon"
                    slot="prefix"
                    :icon-class="dataForm.icon"
                    class="el-input__icon"
                    style="height: 32px;width: 16px;"
                  />
                  <i v-else slot="prefix" class="el-icon-search el-input__icon" />
                </el-input>
              </el-popover>
            </el-form-item>

            <el-form-item v-if="dataForm.type === 0" label="URL" prop="url">
              <el-input v-model="dataForm.path" :disabled="!isEditForm" placeholder="URL" />
            </el-form-item>

            <el-form-item v-if="dataForm.type === 0" label="VUE" prop="url">
              <el-input v-model="dataForm.components" :disabled="!isEditForm" placeholder="Layout" />
            </el-form-item>

            <el-form-item v-if="dataForm.type === 0" label="排序编号" prop="sort">
              <el-input-number
                v-model="dataForm.sort"
                :disabled="!isEditForm"
                controls-position="right"
                :min="0"
                label="排序编号"
              />
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
import { getAdminAuthorityTree, getAuthorityById, saveAuthority, deleteAuthority, updateAuthority } from '@/api/authority'
export default {
  components: { IconSelect },
  data() {
    return {
      tableTreeData: [],
      defaultProps: {
        children: 'children',
        label: (data, node) => {
          // var name = this.$t(`route.${data.name}`)
          return data.name
        }
      },
      size: 'small',
      isEditForm: false,
      authorityTypeList: ['菜单', '按钮'],
      dataForm: {
        authorityId: -1,
        type: null,
        name: '',
        parentId: -1,
        component: '',
        path: '',
        sort: 0,
        icon: ''
      },
      // 表单校验
      dataRule: {
        name: [{ required: true, message: '菜单名称不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.findTreeData()
  },
  methods: {
    // 选中的icon
    selected(name) {
      this.dataForm.icon = name
    },
    // 选中的节点
    handleNodeClick(data) {
      if (data.id) {
        getAuthorityById(data.id).then(res => {
          this.dataForm = res.data
        })
      }
    },
    // 获取菜单【TREE】
    findTreeData() {
      getAdminAuthorityTree().then(res => {
        this.tableTreeData = res.data
      })
    },
    handleAdd() {
      this.isEditForm = true
      this.dataForm = {
        type: 1,
        name: '',
        parentId: this.dataForm.authorityId,
        component: '',
        path: '',
        sort: 0,
        icon: ''
      }
    },
    create() {
      if (this.dataForm.authorityId) {
        updateAuthority(this.dataForm).then(res => {
          if (!res.result) {
            this.findTreeData()
            this.isEditForm = false
          }
        })
      } else {
        saveAuthority(this.dataForm).then(res => {
          if (!res.result) {
            this.findTreeData()
            this.isEditForm = false
          }
        })
      }
    },
    cancel() {
      this.$refs.dataForm.resetFields()
      this.isEditForm = false
      this.dataForm.parentId = -1
    },
    // 删除操作
    handleDelete() {
      this.$confirm('此操作将把分类删除, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          deleteAuthority(this.dataForm.authorityId).then(response => {
            if (!response.result) {
              this.findTreeData()
            }
          })
        })
    }

  }
}
</script>
