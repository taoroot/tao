<template>
  <div class="app-container">
    <el-card style="min-height: 600px">
      <el-button type="text" icon="el-icon-plus" @click="handleAdd" />
      <el-button type="text" icon="el-icon-refresh" @click="findTreeData" />
      <el-table row-key="id" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" border :data="tableTreeData">

        <el-table-column label="菜单名称" align="left">
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

        <el-table-column label="权限标识" align="left" width="333">
          <template slot-scope="scope">
            <span>{{ scope.row.authority }} </span>
          </template>
        </el-table-column>

        <el-table-column label="路径" align="left">
          <template slot-scope="scope">
            <span>{{ scope.row.path }} </span>
          </template>
        </el-table-column>

        <el-table-column label="可见" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.hidden == true">可见</el-tag>
            <el-tag v-else-if="scope.row.hidden == undefined">--</el-tag>
            <el-tag v-else type="warning">隐藏</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="type" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.type == 0">菜单</el-tag>
            <el-tag v-else type="success">按钮</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="185">
          <template slot-scope="scope">
            <el-button type="text" size="mini" icon="el-icon-edit" @click="_delObj(scope.row)">编辑</el-button>
            <el-button type="text" size="mini" icon="el-icon-plus" @click="_delObj(scope.row)">新增</el-button>
            <el-button type="text" size="mini" icon="el-icon-delete" @click="_delObj(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
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
  created() {
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
