<template>
  <div class="app-container">
    <el-card>
      <el-row>
        <el-col :span="4">
          <div style="margin-top: 5px;">
            <el-input v-model="tree.filterText" size="small" placeholder="输入关键字进行过滤" />
            <el-tree ref="tree" :expand-on-click-node="false" class="filter-tree" :data="tree.data" :props="{ children: 'children', label: 'name' }" default-expand-all :filter-node-method="treeFilterNode" />
          </div>
        </el-col>
        <el-col :span="19" :offset="1">
          <div class="filter-container">
            <el-button type="text" icon="el-icon-plus" @click="tableHandleCreate">新增</el-button>
            <el-button type="text" icon="el-icon-delete" @click="tableHandleDelete">删除</el-button>
            <el-button type="text" icon="el-icon-edit" @click="tableHandleEdit">编辑</el-button>
          </div>

          <div class="table-container">
            <el-table ref="table" v-loading="table.loading" border :data="table.data" style="width: 100%" @selection-change="(val) => table.selection = val" @row-click="(row) => $refs.table.toggleRowSelection(row)">
              <el-table-column type="selection" />

              <el-table-column label="index" width="60" align="center">
                <template slot-scope="scope">
                  <span>{{ scope.$index + 1 }}</span>
                </template>
              </el-table-column>

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

              <el-form-item label="username" prop="username">
                <el-input v-model="form.data.username" />
              </el-form-item>

              <el-form-item label="phone" prop="phone">
                <el-input v-model="form.data.phone" />
              </el-form-item>
            </el-form>

            <div slot="footer" class="dialog-footer">
              <el-button type="text" @click="form.dialog = false">取消</el-button>
              <el-button :loading="table.loading" type="primary" @click="tableDoSubmit">确认</el-button>
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
    this.tableGetPage()
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
    tableHandleCreate() {
      this.form.dialog = true
      this.form.isAdd = true
      this.form.data = Object.assign({}, _defaultRow)
    },
    tableHandleDelete() {
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
    tableHandleEdit() {
      var row = checkOne(this.table.selection)
      if (!row) return

      this.form.dialog = true
      this.form.isAdd = false
      this.form.data = row
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
