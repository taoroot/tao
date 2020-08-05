# tao

预览地址: https://tao.flizi.cn

- Spring Boot 2.3.x
- Spring Security 5.x

> spring-social, security-oauth 不再更新, 这里将不引入相关包

# Spring Security Filter (从上到下执行)

- 'WebAsyncManagerIntegrationFilter'
- 'SecurityContextPersistenceFilter'
- 'HeaderWriterFilter'
- 'CorsFilter'
- 'LogoutFilter'
- 'OAuth2AuthorizationRequestRedirectFilter'
- 'OAuth2LoginAuthenticationFilter'
- 'CustomUsernamePasswordAuthenticationFilter'
- 'SmsCodeAuthenticationFilter'
- 'BearerTokenAuthenticationFilter'
- 'RequestCacheAwareFilter'
- 'SecurityContextHolderAwareRequestFilter'
- 'AnonymousAuthenticationFilter'
- 'SessionManagementFilter'
- 'ExceptionTranslationFilter'
- 'FilterSecurityInterceptor'

# 登录方式

## 密码登录 [已完成]

默认是 form 表单登录, 而我习惯性得想使用 application/json 中 body 来传递

```shell
curl --location --request POST 'localhost:8080/login' \
--header 'Content-Type: application/json' \
--data-raw '{
    "username": "username",
    "password": "password"
}'
```

## 社会登录(OAuth2) [已完成]

为什么不用 spring-social, 和 spring-security-oauth 

因为官方弃坑了, 不在维护, 而且 spring-security 5 已整合进来相关功能

官方默认支持: Github, Google, Facebook

这里采用 OAuth2Login 默认实现, 不过默认实现是非前后端分离的,如果前后端分离化,就会涉及到两个域名, 本文通过先从前端域名通过A标签跳转到后端域名,此时后端记录前端当前地址,当后端与第三方完成登录登录,再跳回前端地址,并在前端地址上加上token=xxx参数, 前端在页面加载时,判断有无此参数,有的话,就将其保存,并刷新当前页面,再次刷新后即完成登录. 这里未做白名单,意味着所有网站都跨域跳转过来,可以根据需要,判断是否允许跳转到当前前端地址.

## 手机号登录 [开发中]