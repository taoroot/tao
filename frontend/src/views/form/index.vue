<template>
  <div class="app-container">
    <!--<a href="template.xls">excel模板</a>-->
    <h1>1.点击选择按钮选择Excel文件; 2.点击上传按钮 3. 稍等片刻,转换成功自动下载zip文件</h1>

    <form enctype="multipart/form-data" method="post" action="http://api.flizi.cn/tj/upload">
      <input type="file" name="file" accept=".csv, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel">
      <input type="submit" value="上传">
    </form>
  </div>
</template>
