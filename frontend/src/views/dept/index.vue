<template>
  <div class="app-container">
    <el-card>
      <div class="filter-container">
        <el-form :inline="true" :model="search" size="small">
          <el-form-item label="部门名称">
            <el-input v-model="search.name" placeholder="请输入部门名称" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="tablePage">查询</el-button>
            <el-button type="primary" icon="el-icon-plus" @click="tableCreate">新增</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="table-container">
        <el-table row-key="id" size="medium" default-expand-all :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" border :data="table.data">

          <el-table-column label="部门名称" align="left" header-align="center" prop="name" :show-overflow-tooltip="true" />

          <el-table-column label="显示排序" align="center" prop="weight" :show-overflow-tooltip="true" />

          <el-table-column label="部门状态" align="center">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.enabled">启用</el-tag>
              <el-tag v-else type="warning">停用</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" align="center" width="285">
            <template slot-scope="scope">
              <el-button type="text" size="mini" icon="el-icon-plus" @click="tableCreate(scope.row)">新增</el-button>
              <el-button type="text" size="mini" icon="el-icon-delete" @click="tableDelete(scope.row)">删除</el-button>
              <el-button type="text" size="mini" icon="el-icon-edit" @click="tableEdit(scope.row)">编辑</el-button>
            </template>
          </el-table-column>

        </el-table>
      </div>

      <el-dialog :append-to-body="true" :visible.sync="form.dialog" :title="form.data.id === undefined ? '新增' : '编辑'" width="600px">
        <el-form ref="form" :model="form.data" :rules="form.rules" label-width="80px">
          <el-row>
            <el-col :span="24">
              <el-form-item label="上级部门" prop="parentId">
                <treeselect v-model="form.data.parentId" :options="dict.deptOptions" :normalizer="node => {if (node.children && !node.children.length) delete node.children; return { id: node.id, label: node.name, children: node.children }}" placeholder="请选择上级部门" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="部门名称" prop="name">
                <el-input v-model="form.data.name" placeholder="请输入部门名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="显示排序" prop="weight">
                <el-input-number v-model="form.data.weight" controls-position="right" :min="0" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="负责人" prop="leader">
                <el-input v-model="form.data.leader" placeholder="请输入负责人" maxlength="20" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="form.data.phone" placeholder="请输入联系电话" maxlength="11" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="部门状态">
                <el-radio-group v-model="form.data.enabled">
                  <el-radio :label="true">启用</el-radio>
                  <el-radio :label="false">停用</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.data.email" placeholder="请输入邮箱" maxlength="50" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>

        <div slot="footer" class="dialog-footer">
          <el-button type="text" @click="form.dialog = false">取消</el-button>
          <el-button :loading="table.loading" type="primary" @click="formSubmit">确认</el-button>
        </div>

      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { getDepts, createDept, updateDept, delDepts } from '@/api/dept'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'

const _defaultRow = {
  id: undefined,
  name: '',
  weight: 1,
  leader: '',
  phone: '',
  email: '',
  enabled: true
}

export default {
  name: 'Role',
  components: { Treeselect },
  props: {},
  data() {
    return {
      search: {
        name: undefined
      },
      table: {
        data: [],
        currentPage: 1,
        size: 10,
        total: 0,
        loading: false
      },
      form: {
        data: Object.assign({}, _defaultRow),
        rules: {
          parentId: [{ required: true, message: '上级部门不能为空', trigger: 'blur' }],
          name: [{ required: true, message: '部门名称不能为空', trigger: 'blur' }],
          weight: [{ required: true, message: '排列顺序不能为空', trigger: 'blur' }],
          email: [{ type: 'email', message: "'请输入正确的邮箱地址", trigger: ['blur', 'change'] }],
          phone: [{ pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: '请输入正确的手机号码', trigger: 'blur' }]
        },
        dialog: false
      },
      dict: {
        deptOptions: []
      }
    }
  },
  mounted() {
    this.tablePage()
  },
  methods: {
    tableCreate(row) {
      this.form.dialog = true
      this.form.data = Object.assign({}, _defaultRow)
      this.form.data.parentId = row.id === undefined ? -1 : row.id
      getDepts().then(response => {
        this.dict.deptOptions = [{ id: -1, name: '主类目', isDisabled: false, children: [...response.data] }]
      })
    },
    tableDelete(row) {
      this.$confirm('此操作将删除选中数据, 是否继续?', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }).then(() => {
        delDepts([row.id]).then(response => {
          this.tablePage()
        })
      })
    },
    tableEdit(row) {
      this.form.dialog = true
      this.form.data = Object.assign({}, row)
      getDepts().then(response => {
        this.dict.deptOptions = [{ id: -1, name: '主类目', isDisabled: true, children: [...response.data] }]
      })
    },
    tablePage() {
      this.table.loading = true
      getDepts(this.search).then(response => {
        this.table.loading = false
        this.table.data = response.data
      })
    },
    formSubmit() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          if (this.form.data.id === undefined) {
            createDept(this.form.data).then((res) => {
              this.form.dialog = false
              this.tablePage()
              this.$refs['form'].resetFields()
            })
          } else {
            updateDept(this.form.data).then((res) => {
              this.form.dialog = false
              this.tablePage()
            })
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
