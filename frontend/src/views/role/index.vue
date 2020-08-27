<template>
  <div class="app-container">
    <el-card>
      <div class="filter-container">
        <el-button type="text" icon="el-icon-plus" @click="tableCreate">新增</el-button>
      </div>

      <div class="table-container">
        <el-table ref="table" v-loading="table.loading" border :data="table.data" style="width: 100%">
          <el-table-column label="名称" align="center" header-align="center" prop="name" :show-overflow-tooltip="true" />
          <el-table-column label="角色" align="center" header-align="center" prop="role" :show-overflow-tooltip="true" />
          <el-table-column label="描述" align="center" header-align="center" prop="description" :show-overflow-tooltip="true" />
          <el-table-column label="操作" align="center" width="285">
            <template slot-scope="scope">
              <el-button type="text" icon="el-icon-edit" @click="tableEdit(scope.row)">编辑</el-button>
              <el-button type="text" icon="el-icon-delete" @click="tableDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-container">
          <el-pagination
            :current-page.sync="table.current"
            :page-size="table.size"
            :total="table.total"
            layout="total, prev, pager, next"
            @current-change="table.current = val; tableGetPage()"
          />
        </div>
      </div>

      <el-dialog :append-to-body="true" :visible.sync="form.dialog" :title="form.data.id === undefined ? '新增' : '编辑'" width="500px">
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

          <el-form-item label="数据权限" prop="scopeType">
            <el-select v-model="form.data.scopeType" placeholder="请选择">
              <el-option
                v-for="item in dict.scopeType"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item v-if="form.data.scopeType === 'CUSTOMIZE'" label="自定义部门" prop="scope">
            <el-button v-if="dept.checkAll" type="text" size="mini" @click="treeButtonCheckAll(dept, $refs.deptTree, ...arguments)"> 取消全选</el-button>
            <el-button v-else type="text" size="mini" @click="treeButtonCheckAll(dept, $refs.deptTree, ...arguments)">选择全部</el-button>
            <el-tree
              ref="deptTree"
              :data="dept.data"
              :default-checked-keys="form.data.scope"
              show-checkbox
              node-key="id"
              :props="{ children: 'children', label: 'name' }"
              element-loading-text="拼命加载中"
              :check-strictly="true"
            />
          </el-form-item>

          <el-form-item label="权限" prop="desc">
            <el-button v-if="authority.checkAll" type="text" size="mini" @click="treeButtonCheckAll(authority, $refs.authorityTree, ...arguments)"> 取消全选</el-button>
            <el-button v-else type="text" size="mini" @click="treeButtonCheckAll(authority, $refs.authorityTree, ...arguments)">选择全部</el-button>
            <el-tree
              ref="authorityTree"
              :data="authority.data"
              :default-checked-keys="form.data.authorities"
              show-checkbox
              node-key="id"
              :props="{ children: 'children', label: 'title' }"
              element-loading-text="拼命加载中"
              :check-strictly="true"
              @check-change="authroityCheckChange($refs.authorityTree, ...arguments)"
            />
          </el-form-item>

        </el-form>

        <div slot="footer" class="dialog-footer">
          <el-button type="text" @click="form.dialog = false">取消</el-button>
          <el-button :loading="table.loading" type="primary" @click="tableSubmit">确认</el-button>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { getRoles, delItem, saveItem, updateItem } from '@/api/role'
import { getAuthorities } from '@/api/authority'
import { getDepts } from '@/api/dept'
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
        loading: false
      },
      form: {
        data: Object.assign({}, _defaultRow),
        rules: {},
        dialog: false
      },
      authority: {
        checkAll: false,
        data: []
      },
      dept: {
        checkAll: false,
        data: []
      },
      dict: {
        scopeType: [
          { 'value': 'ALL', 'label': '全部数据权限' },
          { 'value': 'THIS_LEVEL', 'label': '本部门数据权限' },
          { 'value': 'THIS_LEVEL_CHILDREN', 'label': '本部门及以下数据权限' },
          { 'value': 'CUSTOMIZE', 'label': '自定数据权限' },
          { 'value': 'OWN', 'label': '仅本人数据权限' }
        ]
      }
    }
  },
  mounted() {
    this.tableGetPage()
  },
  methods: {
    tableCreate() {
      this.form.dialog = true
      this.form.data = Object.assign({}, _defaultRow)
    },
    tableDelete(row) {
      this.$confirm('此操作将删除选中数据, 是否继续?', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }).then(() => {
        delItem({ id: row.id }).then(response => {
          this.tableGetPage()
        })
      })
    },
    tableEdit(row) {
      this.form.dialog = true
      this.form.data = Object.assign({}, row)
      this.authority.checkAll = false
      this.dept.checkAll = false
    },
    tableGetPage() {
      this.table.loading = true
      const params = {
        current: this.table.current,
        size: this.table.size
      }
      getRoles(params).then(response => {
        this.table.loading = false
        this.table.data = response.data.records
        this.table.total = response.data.total
      })
      getDepts().then(response => {
        this.dept.data = response.data
      })
      getAuthorities().then(response => {
        this.authority.data = response.data
      })
    },
    tableSubmit() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          if (this.form.data.scopeType === 'CUSTOMIZE') {
            this.form.data.scope = [...this.$refs.deptTree.getCheckedKeys(), ...this.$refs.deptTree.getHalfCheckedKeys()]
          } else {
            this.form.data.scope = []
          }
          this.form.data.authorities = [...this.$refs.authorityTree.getCheckedKeys(), ...this.$refs.authorityTree.getHalfCheckedKeys()]
          if (this.form.data.id === undefined) {
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
    treeButtonCheckAll(treeData, refsTree) {
      treeData.checkAll = !treeData.checkAll
      if (treeData.checkAll) {
        const allAuthoritys = []
        this.treeDataCheckAll(treeData.data, allAuthoritys)
        refsTree.setCheckedNodes(allAuthoritys)
      } else {
        refsTree.setCheckedNodes([])
      }
    },
    treeDataCheckAll(authorityData, allAuthoritys) {
      authorityData.forEach(authority => {
        allAuthoritys.push(authority)
        if (authority.children) {
          this.treeDataCheckAll(authority.children, allAuthoritys)
        }
      })
    },
    authroityCheckChange(refsTree, data, check) {
      if (check) { // 节点选中时同步选中父节点
        refsTree.setChecked(data.parentId, true, false)
      } else {
        if (data.children != null) { // 节点取消选中时同步取消选中子节点
          data.children.forEach(element => {
            refsTree.setChecked(element.id, false, false)
          })
        }
      }
    }
  }
}
</script>

<style scoped>
.filter-container {
  margin-top: 6px;
}
.tools-container {
  margin-top: 5px;
  margin-bottom: 15px;
}
.pagination-container {
  position: relative;
  float: right;
  margin-top: 10px;
}
</style>
