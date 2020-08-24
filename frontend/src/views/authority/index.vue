<template>
  <div class="app-container">
    <el-card>
      <div class="filter-container">
        <el-form :inline="true" :model="search" size="small">
          <el-form-item label="菜单名称">
            <el-input v-model="search.title" placeholder="请输入菜单名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="search.hidden" placeholder="菜单状态" clearable>
              <el-option label="可见" :value="false" />
              <el-option label="隐藏" :value="true" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="tablePage">查询</el-button>
            <el-button type="primary" icon="el-icon-plus" @click="tableCreate">新增</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="table-container">
        <el-table row-key="id" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" border :data="table.data">

          <el-table-column label="菜单名称" header-align="center">
            <template slot-scope="scope">
              <span>{{ scope.row.title }} </span>
            </template>
          </el-table-column>

          <el-table-column label="图标" align="center">
            <template slot-scope="scope">
              <svg-icon v-if="scope.row.icon" :icon-class="scope.row.icon" style="height: 32px;width: 16px;" />
            </template>
          </el-table-column>

          <el-table-column label="排序" align="center">
            <template slot-scope="scope">
              <span>{{ scope.row.weight }} </span>
            </template>
          </el-table-column>

          <el-table-column label="权限标识" align="center">
            <template slot-scope="scope">
              <span>{{ scope.row.authority }} </span>
            </template>
          </el-table-column>

          <el-table-column label="路径" header-align="center">
            <template slot-scope="scope">
              <span>{{ scope.row.path }} </span>
            </template>
          </el-table-column>

          <el-table-column label="可见" align="center">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.hidden == false">可见</el-tag>
              <el-tag v-else-if="scope.row.hidden == true" type="warning">隐藏</el-tag>
              <el-tag v-else>--</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="type" align="center">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.type == 0">菜单</el-tag>
              <el-tag v-else type="success">按钮</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" align="center" width="285">
            <template slot-scope="scope">
              <el-button type="text" icon="el-icon-plus" @click="tableCreate(scope.row)">新增</el-button>
              <el-button type="text" icon="el-icon-delete" @click="tableDelete(scope.row)">删除</el-button>
              <el-button type="text" icon="el-icon-edit" @click="tableEdit(scope.row)">编辑</el-button>
            </template>
          </el-table-column>

        </el-table>
      </div>

      <el-dialog :append-to-body="true" :visible.sync="form.dialog" :title="form.isAdd ? '新增' : '编辑'" width="600px">
        <el-form ref="form" :model="form.data" :rules="form.rules" label-width="80px">

          <el-form-item label="上级菜单" prop="name">
            <treeselect v-model="form.data.parentId" :options="form.authorityTree" :normalizer="formNormalizer" :show-count="true" placeholder="选择上级菜单" />
          </el-form-item>

          <el-row>
            <el-col :span="12">
              <el-form-item label="菜单标题">
                <el-input v-model="form.data.title" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="显示排序">
                <el-input-number v-model="form.data.weight" controls-position="right" :min="0" :max="20" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="菜单类型" prop="role">
            <el-radio-group v-model="form.data.type">
              <el-radio :label="0">菜单</el-radio>
              <el-radio :label="1">功能</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="菜单图标" prop="role">
            <el-popover placement="bottom-start" width="460" trigger="click" @show="$refs['iconSelect'].reset()">
              <IconSelect ref="iconSelect" @selected="name => {form.data.icon = name}" />
              <el-input slot="reference" v-model="form.data.icon" placeholder="点击选择图标" readonly>
                <svg-icon v-if="form.data.icon" slot="prefix" :icon-class="form.data.icon" class="el-input__icon" style="height: 40px;width: 16px;" />
                <i v-else slot="prefix" class="el-icon-search el-input__icon" />
              </el-input>
            </el-popover>
          </el-form-item>

          <el-row>
            <el-col :span="12">
              <el-form-item label="路由名称">
                <el-input v-model="form.data.name" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="组件路径" prop="component">
                <el-input v-model="form.data.component" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="12">
              <el-form-item label="是否隐藏">
                <el-radio-group v-model="form.data.hidden">
                  <el-radio :label="true">是</el-radio>
                  <el-radio :label="false">否</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="路由地址" prop="path">
                <el-input v-model="form.data.path" />
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
import { getAuthorities, updateItem, delItem } from '@/api/authority'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import IconSelect from '@/components/IconSelect'
const _defaultRow = {
  id: 0,
  role: '',
  description: ''
}

export default {
  name: 'Role',
  components: { Treeselect, IconSelect },
  props: {},
  data() {
    return {
      search: {
        title: undefined,
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
        isAdd: true,
        authorityTree: []
      }
    }
  },
  mounted() {
    this.tablePage()
  },
  methods: {
    tableCreate() {
      this.form.dialog = true
      this.form.isAdd = true
      this.form.data = Object.assign({}, _defaultRow)
    },
    tableDelete(row) {
      this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delItem(row.id).then(response => {
          this.tablePage()
        })
      })
    },
    tableEdit(row) {
      this.form.dialog = true
      this.form.isAdd = false
      this.form.data = Object.assign({}, row)
      getAuthorities().then(response => {
        this.form.authorityTree = []
        const menu = { id: -1, title: '主类目', children: [] }
        menu.children = response.data
        this.form.authorityTree.push(menu)
      })
    },
    tablePage() {
      this.table.loading = true
      getAuthorities(this.search).then(response => {
        this.table.loading = false
        this.table.data = response.data
      })
    },
    formNormalizer(node) {
      if (node.children && !node.children.length) {
        delete node.children
      }
      return {
        id: node.id,
        label: node.title,
        children: node.children
      }
    },
    formSubmit() {
      updateItem(this.form.data).then(res => {
        this.form.dialog = false
        this.tablePage()
      })
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
