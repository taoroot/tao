<template>
  <el-form>
    <el-form-item label="用户昵称">
      <el-input v-model.trim="user.nickname" />
    </el-form-item>
    <el-form-item label="邮箱">
      <el-input v-model.trim="user.email" />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="submit">更新</el-button>
    </el-form-item>
    <el-form-item>
      <span class="wx-svg-container"><svg-icon icon-class="wx" class="icon" /></span>
      <a style="color: blue" @click="handleClick('wx')">绑定微信</a>
    </el-form-item>
  </el-form>
</template>

<script>
import openWindow from '@/utils/open-window.js'
export default {
  props: {
    user: {
      type: Object,
      default: () => {
        return {
          name: '',
          email: ''
        }
      }
    }
  },
  methods: {
    submit() {
      this.$message({
        message: 'User information has been updated successfully',
        type: 'success',
        duration: 5 * 1000
      })
    },
    handleClick(thirdpart) {
      let appid, url
      var redirect_uri = encodeURIComponent('http://auth.flizi.cn/#/authredirect')
      // redirect_uri = encodeURIComponent(window.location.origin + '/#/authredirect')
      if (thirdpart === 'wx') {
        appid = 'wx6cb5e779a9523765'
        url = 'https://open.weixin.qq.com/connect/qrconnect?appid=' + appid + '&redirect_uri=' + redirect_uri + '&state=WX-BIND&response_type=code&scope=snsapi_login#wechat_redirect'
      }
      openWindow(url, thirdpart, 540, 540)
    }
  }
}
</script>

<style scoped>
.wx-svg-container {
  color: #fff;
  display: inline-block;
  width: 40px;
  height: 40px;
  line-height: 40px;
  text-align: center;
  padding-top: 1px;
  border-radius: 4px;
  margin-bottom: 20px;
  margin-right: 5px;
  /* background-color: #24da70; */
}
</style>

