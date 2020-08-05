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

- 密码登录 
- 社会登录 [Github]
- 手机号登录

## 自定义用户密码登录过滤器

默认是 form 表单登录, 而我习惯性得想使用 application/json 中 body 来传递

```shell
curl --location --request POST 'localhost:8080/login' \
--header 'Content-Type: application/json' \
--data-raw '{
    "username": "username",
    "password": "password"
}'
```

## 为什么不用 spring-social, 和 spring-security-oauth 

因为官方弃坑了, 不在维护, 而且 Security 5 已经整合进来相关功能

官方支持: Github, Google, Facebook

## 手机号登录过滤器 

手机号登录过滤器 