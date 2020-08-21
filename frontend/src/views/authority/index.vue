<template>
  <div class="app-container">
    <el-card style="min-height: 600px">
      <el-button type="primary" icon="el-icon-plus" @click="handleAdd" />
      <el-button type="primary" icon="el-icon-edit" @click="isEditForm = true" />
      <el-button type="primary" icon="el-icon-delete" @click="handleDelete" />
      <el-table row-key="id" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" border :data="tableTreeData">
        <el-table-column label="id" align="left" width="185">
          <template slot-scope="scope">
            <span>{{ scope.row.id }} </span>
          </template>
        </el-table-column>

        <el-table-column label="path" align="left" width="333">
          <template slot-scope="scope">
            <span>{{ scope.row.path }} </span>
          </template>
        </el-table-column>

        <el-table-column label="component" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.component }} </span>
          </template>
        </el-table-column>

        <el-table-column label="hidden" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.hidden }} </span>
          </template>
        </el-table-column>

        <el-table-column label="alwaysShow" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.alwaysShow }} </span>
          </template>
        </el-table-column>

        <el-table-column label="redirect" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.redirect }} </span>
          </template>
        </el-table-column>

        <el-table-column label="name" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.name }} </span>
          </template>
        </el-table-column>

        <el-table-column label="title" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.title }} </span>
          </template>
        </el-table-column>
        <el-table-column label="icon" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.icon }} </span>
          </template>
        </el-table-column>

        <el-table-column label="breadcrumb" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.breadcrumb }} </span>
          </template>
        </el-table-column>

        <el-table-column label="weight" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.weight }} </span>
          </template>
        </el-table-column>

        <el-table-column label="type" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.type }} </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="185">
          <template slot-scope="scope">
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
