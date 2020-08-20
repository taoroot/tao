<template>
  <div class="dashboard-container">
    <div class="dashboard-text">账号: {{ name }}</div>
    <div class="dashboard-text">已绑定: {{ socials }}</div>
    <h2> 绑定测试, 想绑定那个点哪个 </h2>
    <el-row>
      <el-col :span="1"><a v-if="getAuthUrl('gitee')" referrerpolicy="origin" :href="getAuthUrl('gitee')"> 码云 </a></el-col>
      <el-col :span="1"><a v-if="getAuthUrl('github')" referrerpolicy="origin" :href="getAuthUrl('github')"> GitHub </a></el-col>
      <el-col :span="1"><a v-if="getAuthUrl('gitea')" referrerpolicy="origin" :href="getAuthUrl('gitea')"> GITEA </a></el-col>
      <el-col :span="1"><a v-if="getAuthUrl('wx')" referrerpolicy="origin" :href="getAuthUrl('wx')"> 微信 </a></el-col>
      <el-col :span="1"><a v-if="getAuthUrl('qq')" referrerpolicy="origin" :href="getAuthUrl('qq')"> QQ </a></el-col>
    </el-row>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getToken } from '@/utils/auth'
import { getSocial } from '@/api/login'

export default {
  name: 'Dashboard',
  data() {
    return {
      socials: [],
      msg: ''
    }
  },
  computed: {
    ...mapGetters([
      'name'
    ])
  },
  mounted() {
    var msg = (window.location.search.match(new RegExp('[?&]msg=([^&]+)')) || [null, null])[1]
    if (msg) {
      this.msg = msg
      this.$alert(decodeURI(msg), '提示', {
        confirmButtonText: '确定',
        callback: action => {
          var { pathname, origin, hash } = window.location
          window.location.href = origin + pathname + hash
        }
      })
    }
    getSocial().then(res => {
      this.socials = res.data
    })
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
