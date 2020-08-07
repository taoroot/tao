# tao

前后端分离项目

前端:

https://tao.flizi.cn

- [vue.js](https://github.com/vuejs/vue)
- [vue-element-admin](https://github.com/PanJiaChen/vue-element-admin)

后端:

https://api.flizi.cn/tao

- [Spring Boot 2.3.x](https://github.com/spring-projects/spring-boot)
- [Spring Security 5.x](https://github.com/spring-projects/spring-security) 


> spring-social, security-oauth 不再更新, 这里将不引入相关包

> [Spring Security 启动流程分析](https://github.com/taoroot/tao/blob/master/document/SpringSecurity%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B.md)

> [Spring Security 过滤器架构分析](https://github.com/taoroot/tao/blob/master/document/SpringSecurity%E8%BF%87%E6%BB%A4%E5%99%A8%E6%9E%B6%E6%9E%84.md)


# 登录方式

## 密码登录 [开发中]

表单登录 和 密码登录都可以直接使用默认的, 表单登录可以添加一下图片验证码过滤器

## 社会登录(OAuth2) [开发中]

为什么不用 spring-social, 和 spring-security-oauth 

因为官方弃坑了, 不在维护, 而且 spring-security 5 已整合进来相关功能

官方默认支持: Github, Google, Facebook

这里采用 OAuth2Login 默认实现, 不过默认实现是非前后端分离的,如果前后端分离化,就会涉及到两个域名, 本文通过先从前端域名通过A标签跳转到后端域名,此时后端记录前端当前地址,当后端与第三方完成登录登录,再跳回前端地址,并在前端地址上加上token=xxx参数, 前端在页面加载时,判断有无此参数,有的话,就将其保存,并刷新当前页面,再次刷新后即完成登录. 这里未做白名单,意味着所有网站都跨域跳转过来,可以根据需要,判断是否允许跳转到当前前端地址.

## 手机号登录 [开发中]

这里的手机号登录过滤器 仅仅是对参数phone判断,用户是否存在, 所以还得在前面添加一个短信验证码过滤器,将手机号登录的白名单地址加入到该过滤器判断中

为什么要这么做? 可以复用该过滤器,比如你有一些高级操作,也需要发送验证码,那么就只需要修改白名单地址就好
