<template>
  <div class="app-container">
    <el-card>
      <div class="filter-container">
        <el-button type="text" icon="el-icon-plus" @click="handleCreate">新增</el-button>
      </div>

      <div class="table-container">
        <el-table ref="table" v-loading="table.loading" border :data="table.data" style="width: 100%">
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

          <el-table-column label="操作" align="center" width="285">
            <template slot-scope="scope">
              <el-button type="text" icon="el-icon-edit" @click="tableEdit(scope.row)">编辑</el-button>
              <el-button type="text" icon="el-icon-delete" @click="tableDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>

        </el-table>
        <div class="block">
          <el-pagination
            :current-page.sync="table.current"
            :page-size="table.size"
            :total="table.total"
            layout="total, prev, pager, next"
            @current-change="table.current = val; tableGetPage()"
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

          <el-form-item label="权限" prop="desc">
            <el-button v-if="permission.checkAll" type="text" size="mini" @click="permissionHandleCheckAll"> 取消全选</el-button>
            <el-button v-else type="text" size="mini" @click="permissionHandleCheckAll">选择全部</el-button>
            <el-tree
              ref="authorityTree"
              :data="permission.data"
              :default-checked-keys="form.data.authorities"
              show-checkbox
              node-key="id"
              :props="{ children: 'children', label: 'title' }"
              element-loading-text="拼命加载中"
              :check-strictly="true"
              @check-change="permisssionCheckChange"
            />
          </el-form-item>

        </el-form>

        <div slot="footer" class="dialog-footer">
          <el-button type="text" @click="form.dialog = false">取消</el-button>
          <el-button :loading="table.loading" type="primary" @click="tableDoSubmit">确认</el-button>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { getPage, delItem, saveItem, updateItem } from '@/api/role'
import { getAuthorities } from '@/api/authority'
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
        data: []
      }
    }
  },
  mounted() {
    this.tableGetPage()
  },
  methods: {
    handleCreate() {
      this.form.dialog = true
      this.form.isAdd = true
      this.form.data = Object.assign({}, _defaultRow)
    },
    handleDelete(row) {
      this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delItem({ id: row.id }).then(response => {
          this.tableGetPage()
        })
      })
    },
    tableEdit(row) {
      this.form.dialog = true
      this.form.isAdd = false
      this.form.data = row
      this.permission.checkAll = false
      getAuthorities().then(response => {
        this.permission.data = response.data
        this.form.data = row
      })
    },
    tableGetPage() {
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
    tableDoSubmit() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          this.form.data.authorities = this.authorityIds =
            [...this.$refs.authorityTree.getCheckedKeys(), ...this.$refs.authorityTree.getHalfCheckedKeys()]
          if (this.form.isAdd) {
            saveItem(this.form.data).then((res) => {
              this.form.dialog = false
              this.tableGetPage()
              this.$refs['form'].resetFields()
            })
          } else {
            updateItem(this.form.data).then((res) => {
              this.form.dialog = false
              this.tableGetPage()
            })
          }
        }
      })
    },
    permissionHandleCheckAll() {
      this.permission.checkAll = !this.permission.checkAll
      if (this.permission.checkAll) {
        const allAuthoritys = []
        this.permissionCheckAll(this.permission.data, allAuthoritys)
        this.$refs.authorityTree.setCheckedNodes(allAuthoritys)
      } else {
        this.$refs.authorityTree.setCheckedNodes([])
      }
    },
    permissionCheckAll(authorityData, allAuthoritys) {
      authorityData.forEach(authority => {
        allAuthoritys.push(authority)
        if (authority.children) {
          this.permissionCheckAll(authority.children, allAuthoritys)
        }
      })
    },
    permisssionCheckChange(data, check) {
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
