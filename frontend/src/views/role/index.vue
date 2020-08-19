<template>
  <div class="app-container">
    <el-card>
      <div class="filter-container">
        <el-button type="text" icon="el-icon-plus" @click="handleCreate">新增</el-button>
        <el-button type="text" icon="el-icon-delete" @click="handleDelete">删除</el-button>
        <el-button type="text" icon="el-icon-edit" @click="handleEdit">编辑</el-button>
        <el-button type="text" icon="el-icon-edit" @click="handleAuthority">授权</el-button>
      </div>

      <div class="table-container">
        <el-table ref="table" v-loading="table.loading" border :data="table.data" style="width: 100%" @selection-change="(val) => table.selection = val" @row-click="(row) => $refs.table.toggleRowSelection(row)">
          <el-table-column type="selection" />

          <el-table-column label="index" width="60" align="center">
            <template slot-scope="scope">
              <span>{{ scope.$index + 1 }}</span>
            </template>
          </el-table-column>

          <el-table-column label="名称" align="center">
            <template slot-scope="scope">
              <span>{{ scope.row.name }} </span>
            </template>
          </el-table-column>

          <el-table-column label="角色" align="center">
            <template slot-scope="scope">
              <span>{{ scope.row.role }} </span>
            </template>
          </el-table-column>

          <el-table-column label="描述" align="center">
            <template slot-scope="scope">
              <span>{{ scope.row.description }} </span>
            </template>
          </el-table-column>

        </el-table>
        <div class="block">
          <el-pagination
            :current-page.sync="table.current"
            :page-size="table.size"
            :total="table.total"
            layout="total, prev, pager, next"
            @current-change="table.current = val; _getPage()"
          />
        </div>
      </div>
      <el-dialog :append-to-body="true" :visible.sync="form.dialog" :title="form.isAdd ? '新增' : '编辑'" width="500px">
        <el-form ref="form" :model="form.data" :rules="form.rules" size="small" label-width="100px">

          <el-form-item label="名称" prop="name">
            <el-input v-model="form.data.name" />
          </el-form-item>

          <el-form-item label="标识符" prop="role">
            <el-input v-model="form.data.role" />
          </el-form-item>

          <el-form-item label="描述" prop="desc">
            <el-input v-model="form.data.description" />
          </el-form-item>

        </el-form>

        <div slot="footer" class="dialog-footer">
          <el-button type="text" @click="form.dialog = false">取消</el-button>
          <el-button :loading="table.loading" type="primary" @click="doSubmit">确认</el-button>
        </div>

      </el-dialog>

      <el-dialog :append-to-body="true" :visible.sync="permission.authorityDialog" title="授权" width="500px">
        <el-tree
          ref="authorityTree"
          :data="permission.data"
          :default-checked-keys="permission.checkedKeys"
          show-checkbox
          node-key="id"
          :props="{ children: 'children', label: 'name' }"
          element-loading-text="拼命加载中"
          :check-strictly="true"
          @check-change="handleAuthorityCheckChange"
        />

        <div style="padding-left:24px;padding-top:12px;">
          <el-checkbox v-model="permission.checkAll" :disabled="false" @change="handleCheckAll"><b>全选</b></el-checkbox>
        </div>

        <div slot="footer" class="dialog-footer">
          <el-button type="text" @click="permission.authorityDialog = false">取消</el-button>
          <el-button type="primary" @click="doAuthoritySubmit">确认</el-button>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { getPage, delItem, saveItem, updateItem, updatePermission, getPermission } from '@/api/role'
import { checkOne } from '@/utils/index'
const _defaultRow = {
  id: 0,
  role: '',
  description: ''
}

export default {
  name: 'Role',
  props: {},
  data() {
    return {
      table: {
        data: [],
        currentPage: 1,
        size: 10,
        total: 0,
        loading: false,
        selection: []
      },
      form: {
        data: Object.assign({}, _defaultRow),
        rules: {},
        dialog: false,
        isAdd: true
      },
      permission: {
        checkAll: false,
        authorityDialog: false,
        checkedKeys: [],
        data: []
      }
    }
  },
  mounted() {
    this._getPage()
  },
  methods: {
    handleCreate() {
      this.form.dialog = true
      this.form.isAdd = true
      this.form.data = Object.assign({}, _defaultRow)
    },
    handleDelete() {
      var ids = []
      this.table.selection.forEach(item => ids.push(item.id))

      this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delItem(ids).then(response => {
          this._getPage()
        })
      })
    },
    handleEdit() {
      var row = checkOne(this.table.selection)
      if (!row) return

      this.form.dialog = true
      this.form.isAdd = false
      this.form.data = row
    },
    _getPage() {
      this.table.loading = true
      const params = new URLSearchParams()
      params.append('current', this.table.current)
      params.append('size', this.table.size)
      getPage(params).then(response => {
        this.table.loading = false
        this.table.data = response.data.records
        this.table.total = response.data.total
      })
    },
    // 获取勾选
    handleSelectionChange(val) {
      this.table.selection = val
    },
    // 提交创建 或 更新
    doSubmit() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          if (this.form.isAdd) {
            saveItem(this.form.data).then((res) => {
              this.form.dialog = false
              this._getPage()
              this.$refs['form'].resetFields()
            })
          } else {
            updateItem(this.form.data).then((res) => {
              this.form.dialog = false
              this._getPage()
            })
          }
        }
      })
    },
    // 授权操作
    doAuthoritySubmit() {
      this.authorityIds = this.$refs.authorityTree.getCheckedKeys().join(',').concat(',').concat(this.$refs.authorityTree.getHalfCheckedKeys().join(','))
      console.log('doAuthoritySubmit', this.authorityIds, this.form.data)
      updatePermission(this.form.data.id, this.authorityIds).then((res) => {
        if (!res.result) {
          this.permission.authorityDialog = false
        }
      })
    },
    handleAuthority() {
      var row = checkOne(this.table.selection)
      if (!row) return
      getPermission(row.id).then(response => {
        this.permission.checkedKeys = response.data.checkedKeys
        this.permission.data = response.data.authorityData
        this.permission.authorityDialog = true
        this.form.data = row
      })
    },
    // 全选操作
    handleCheckAll() {
      if (this.permission.checkAll) {
        const allAuthoritys = []
        this.checkAllAuthority(this.permission.data, allAuthoritys)
        this.$refs.authorityTree.setCheckedNodes(allAuthoritys)
      } else {
        this.$refs.authorityTree.setCheckedNodes([])
      }
    },
    // 递归全选
    checkAllAuthority(authorityData, allAuthoritys) {
      authorityData.forEach(authority => {
        allAuthoritys.push(authority)
        if (authority.children) {
          this.checkAllAuthority(authority.children, allAuthoritys)
        }
      })
    },
    // 树节点选择监听
    handleAuthorityCheckChange(data, check) {
      if (check) { // 节点选中时同步选中父节点
        const parentId = data.parentId
        this.$refs.authorityTree.setChecked(parentId, true, false)
      } else {
        if (data.children != null) { // 节点取消选中时同步取消选中子节点
          data.children.forEach(element => {
            this.$refs.authorityTree.setChecked(element.id, false, false)
          })
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
