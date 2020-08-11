<template>
  <div class="dashboard-container">
    <div class="dashboard-text">name: {{ name }}</div>

    <h2> 绑定测试 </h2>
    <el-row>
      <el-col :span="5"><a referrerpolicy="origin" :href="getAuthUrl('gitee')"> 码云 </a></el-col>
      <el-col :span="5"><a referrerpolicy="origin" :href="getAuthUrl('github')"> GitHub </a></el-col>
      <el-col :span="4"><a referrerpolicy="origin" :href="getAuthUrl('gitea')"> GITEA </a></el-col>
      <el-col :span="5"><a referrerpolicy="origin" :href="getAuthUrl('wx')"> 微信 </a></el-col>
      <el-col :span="5"><a referrerpolicy="origin" :href="getAuthUrl('qq')"> QQ </a></el-col>
    </el-row>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getToken } from '@/utils/auth'

export default {
  name: 'Dashboard',
  computed: {
    ...mapGetters([
      'name'
    ])
  },
  mounted() {
    var msg = (window.location.search.match(new RegExp('[?&]msg=([^&]+)')) || [null, null])[1]
    alert(msg)
  },
  methods: {
    getAuthUrl(type) {
      return process.env.VUE_APP_BASE_API + 'oauth2/authorization/' + type + '?access_token=' + getToken()
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard {
  &-container {
    margin: 30px;
  }
  &-text {
    font-size: 30px;
    line-height: 46px;
  }
}
</style>
