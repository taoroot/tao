<template>
  <div class="app-container">
    <el-card>
      <el-row :gutter="20">
        <!-- left tree -->
        <el-col :span="4">
          <div style="margin-top: 5px;">
            <el-input v-model="tree.filterText" size="small" placeholder="输入关键字过滤部门" />
            <el-tree ref="tree" style="margin-top: 20px" :expand-on-click-node="false" class="filter-tree" :data="tree.data" :props="{ children: 'children', label: 'name' }" default-expand-all :filter-node-method="treeFilterNode" />
          </div>
        </el-col>
        <!-- right table -->
        <el-col :span="20">
          <div class="filter-container">
            <el-form :inline="true" :model="search" size="small">
              <el-form-item label="用户名称">
                <el-input v-model="search.title" placeholder="请输入菜单名称" clearable />
              </el-form-item>
              <el-form-item label="手机号码">
                <el-input v-model="search.phone" placeholder="请输入手机号码" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="search.hidden" placeholder="用户状态" clearable>
                  <el-option label="启用" :value="true" />
                  <el-option label="停用" :value="false" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="el-icon-search" @click="tablePage">查询</el-button>
                <el-button icon="el-icon-refresh">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <div class="tools-container">
            <el-button type="primary" size="mini" icon="el-icon-plus" @click="tableCreate">新增</el-button>
            <el-button type="info" size="mini" icon="el-icon-edit" :disabled="table.selection.length !== 1" @click="tableEdit">编辑</el-button>
            <el-button type="danger" size="mini" icon="el-icon-delete" :disabled="table.selection.length !== 1" @click="tableDelete">删除</el-button>
          </div>

          <div class="table-container">
            <el-table ref="table" v-loading="table.loading" size="medium" border :data="table.data" style="width: 100%" @selection-change="(val) => table.selection = val">
              <el-table-column type="selection" align="center" />

              <el-table-column label="username" align="center">
                <template slot-scope="scope">
                  <span>{{ scope.row.username }} </span>
                </template>
              </el-table-column>

              <el-table-column label="avatar" align="center">
                <template slot-scope="scope">
                  <el-image style="width: 20px; height: 20px" :src="scope.row.avatar" />
                </template>
              </el-table-column>

              <el-table-column label="enabled" align="center">
                <template slot-scope="scope">
                  <el-switch v-model="scope.row.enabled" />
                </template>
              </el-table-column>

              <el-table-column label="phone" align="center">
                <template slot-scope="scope">
                  <span>{{ scope.row.phone }} </span>
                </template>
              </el-table-column>

              <el-table-column label="dept" align="center">
                <template slot-scope="scope">
                  <span>{{ scope.row.deptName }} </span>
                </template>
              </el-table-column>

              <el-table-column label="操作" align="center" width="285">
                <template slot-scope="scope">
                  <el-button type="text" size="mini" icon="el-icon-edit">编辑</el-button>
                  <el-button type="text" size="mini" icon="el-icon-delete">删除</el-button>
                  <el-button type="text" size="mini" icon="el-icon-delete">重置</el-button>
                </template>
              </el-table-column>

            </el-table>
            <div class="block">
              <el-pagination
                :current-page.sync="table.current"
                :page-size="table.size"
                :total="table.total"
                layout="total, prev, pager, next"
                @current-change="table.current = val; tablePage()"
              />
            </div>
          </div>

          <el-dialog :append-to-body="true" :visible.sync="form.dialog" :title="form.isAdd ? '新增' : '编辑'" width="500px">
            <el-form ref="form" :model="form.data" :rules="form.rules" size="small" label-width="100px">

              <el-form-item label="username" prop="username">
                <el-input v-model="form.data.username" />
              </el-form-item>

              <el-form-item label="phone" prop="phone">
                <el-input v-model="form.data.phone" />
              </el-form-item>
            </el-form>

            <div slot="footer" class="dialog-footer">
              <el-button type="text" @click="form.dialog = false">取消</el-button>
              <el-button :loading="table.loading" type="primary" @click="tableSubmit">确认</el-button>
            </div>

          </el-dialog>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { getPage, delItem, saveItem, updateItem } from '@/api/user'
import { getTree } from '@/api/dept'
import { checkOne } from '@/utils/index'

const _defaultRow = {
  id: 0,
  role: '',
  description: ''
}

export default {
  data() {
    return {
      search: {
        title: undefined,
        phone: undefined,
        hidden: undefined
      },
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
      tree: {
        filterText: '',
        data: []
      }
    }
  },
  mounted() {
    this.treeGetData()
    this.tablePage()
  },
  methods: {
    treeGetData() {
      getTree().then(res => {
        this.tree.data = res.data
      })
    },
    treeFilterNode(value, data) {
      if (!value) return true
      return this.tree.data.label.indexOf(value) !== -1
    },
    tableCreate() {
      this.form.dialog = true
      this.form.isAdd = true
      this.form.data = Object.assign({}, _defaultRow)
    },
    tableDelete() {
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
    tableEdit() {
      var row = checkOne(this.table.selection)
      if (!row) return

      this.form.dialog = true
      this.form.isAdd = false
      this.form.data = row
    },
    tablePage() {
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
    tableSubmit() {
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
</style>
