<template>
  <div class="app-container">
    <!-- <a href="template.xls">excel模板</a>
    <h1>1.点击选择按钮选择Excel文件; 2.点击上传按钮 3. 稍等片刻,转换成功自动下载zip文件</h1>

    <form enctype="multipart/form-data" method="post" action="http://api.flizi.cn/tj/upload">
      <input type="file" name="file">
      <input type="submit" value="上传">
    </form> -->
    <!-- <el-upload class="upload-demo" drag action="http://api.flizi.cn/tj/upload"> -->
    <el-upload v-loading="loading" style="padding: 0 auto" class="upload-demo" drag action="/upload" :show-file-list="false" :on-success="success" :http-request="uploadSectionFile">
      <i class="el-icon-upload" />
      <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
    </el-upload>
  </div>
</template>

<script>
import axios from 'axios'
export default {
  data() {
    return {
      loading: false,
      dialog: false,
      baseURL: 'https://api.flizi.cn/tj/',
      // baseURL: 'http://localhost:8080/',
      id: ''
    }
  },
  methods: {
    uploadSectionFile(item) {
      const formData = new FormData()
      formData.append('file', item.file)
      this.loading = true
      axios({
        baseURL: this.baseURL,
        timeout: 2 * 60 * 60 * 1000,
        url: '/upload',
        method: 'post',
        data: formData
      }).then(res => {
        this.loading = true
        this.id = res.data
        this.$alert(`<a href="${this.baseURL}file/${this.id}">点击下载</a>`, '转换完成', {
          dangerouslyUseHTMLString: true,
          center: true
        })
      }).catch(res => {
        this.loading = false
      }).finally(res => {
        this.loading = false
      })
    },

    convertRes2Blob(response) {
      // 提取文件名
      var filename = response.headers['content-disposition']
      // 将二进制流转为blob
      const blob = new Blob([response.data], { type: 'application/octet-stream' })
      if (typeof window.navigator.msSaveBlob !== 'undefined') {
        // 兼容IE，window.navigator.msSaveBlob：以本地方式保存文件
        window.navigator.msSaveBlob(blob, decodeURI(filename))
      } else {
        // 创建新的URL并指向File对象或者Blob对象的地址
        const blobURL = window.URL.createObjectURL(blob)
        // 创建a标签，用于跳转至下载链接
        const tempLink = document.createElement('a')
        tempLink.style.display = 'none'
        tempLink.href = blobURL
        tempLink.setAttribute('download', decodeURI(filename))
        // 兼容：某些浏览器不支持HTML5的download属性
        if (typeof tempLink.download === 'undefined') {
          tempLink.setAttribute('target', '_blank')
        }
        // 挂载a标签
        document.body.appendChild(tempLink)
        tempLink.click()
        document.body.removeChild(tempLink)
        // 释放blob URL地址
        window.URL.revokeObjectURL(blobURL)
      }
    }

  }
}
</script>
