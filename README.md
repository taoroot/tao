# tao

前后端分离项目

[预览地址](https://tao.flizi.cn)

前端技术:

- [vue.js](https://github.com/vuejs/vue)
- [vue-element-admin](https://github.com/PanJiaChen/vue-element-admin)

后端技术:

- [Spring Boot 2.3.x](https://github.com/spring-projects/spring-boot)
- [Spring Security 5.x](https://github.com/spring-projects/spring-security) 


> spring-social, security-oauth 不再更新, 这里将不引入相关包

> [Spring Security 过滤器架构分析](https://github.com/taoroot/tao/blob/master/document/SpringSecurity%E8%BF%87%E6%BB%A4%E5%99%A8%E6%9E%B6%E6%9E%84.md)

> [Spring Security 启动流程分析](https://github.com/taoroot/tao/blob/master/document/SpringSecurity%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B.md)

# 登录方式

## 密码登录

表单登录 和 密码登录都可以直接使用默认的, 表单登录可以添加一下图片验证码过滤器

## 社会登录(OAuth2)

### 为什么不用 spring-social, 和 spring-security-oauth ?

因为官方不在维护, 并且 spring-security 5 已整合相关功能

官方默认支持: Github, Google, Facebook 


### 用 OAuth2Login 还是 OAuth2Client ?

OAuth2Login 扩展点多, 而且支持 successHandler 和 failhandler, 这就提供了与 formLogin 类似的处理能力, 而 OAuth2Client 没有.  

OAuth2Login 会多获取userInfo这一步, 一般做登录最终目的是为了确定用户的唯一性, 而不是只是为了获取token, 所以更加实用, 所需做的是在 successHandler 中将第三方的用户id,和本平台的用户id进行绑定.

### 流程

---> 点击后  ---> 跳转到第三方 

----> 用户点击同意授权(第一次需要点击,如果用户以及授权过,就直接跳转了)  ---> 跳转回后端 http://localhost:8080/login/oauth2/code/gitee?code=xxxx

---> 后端 通过 code  与 第三方换取 token ---> 进入 successHandler

---> 跳转回前端地址 xxx?token=xxx, (此处token为自己平台生成的token)

文字说明: 

OAuth2Login 默认实现是非前后端分离的,如果前后端分离化,就会涉及到两个域名, 本文通过先从前端域名通过A标签跳转到后端域名,此时后端记录前端当前地址,当后端与第三方完成登录登录,再跳回前端地址,并在前端地址上加上token=xxx参数, 前端在页面加载时,判断有无此参数,有的话,就将其保存,并刷新当前页面,再次刷新后即完成登录. 这里未做白名单,意味着所有网站都跨域跳转过来,可以根据需要,判断是否允许跳转到当前前端地址. 这种方式,并不适合非Web应用

### 为什么不让前端获取code,然后调用后端接口来获取token ?

实际编写时遇到了问题, Spring-Security 5 对 OAuth2 实现格外严格, 会再次判断请求地址是否是我们配置的回调地址, 因为我们配置的是回调前端地址, 不匹配, 所以无法成功. 所以这里采用了后端域名与第三方完成了OAuth2以后,将我们自己的生成的token回调给我们自己的前端.严格的说,不能说是两个域名完成的OAuth2,不过从实际出发,是解决了前后端分离[不同域名]下,完成OAuth2登录,实际意义是有的. 

### 安全问题 ?

从安全角度出发,不建议通过前端请求头中的 Referer 参数, 作为最终回调, 因为这样就不管什么域名,只要 <a> 标签配置了 后端的 oauth2 请求地址, 最终token都会跳转回去, 可以加一个白名单,  或者说后端忽略Referer,而是和前端规定一个地址,专门接受token. [为了调试灵活性,我这里没有白名单,也没有写死]

### 码云 失败?

码云必须得有加 User-Agent 头, 详情见: CustomOAuth2AuthorizationCodeGrantRequestEntityConverter 和 CustomOAuth2UserRequestEntityConverter

### SuccessHandler 获取不到 Referer 字段 ?

默认在调用 http://xxxx/oauth2/authorization/{registerId} 默认会保存 HttpServletRequest 一些信息, 再 SuccessHandler 就可以重新设置到当前的 HttpServletRequest 中, 但是不包括 Referer 字段, 故重写: CustomOAuth2AuthorizationRequestResolver(保存Referer), CustomHttpSessionOAuth2AuthorizationRequestRepository(获取Referer). 

其实这个字段默认是存在的,不需要处理也是可以的,但是得看第三方跳转回来的是否有没有帮我们带回来, 保险起见,自己保存一下. (实际测试时,第一次授权不会待会,第二次就带回来)

另外值得一提的是, 在第一次请求的时候,会生成一个state,作为key, 等第三方回调后端时,重新把第一次请求的信息找回, 例如appId, secret, 也可以加入自定义的,比如这里的 Referer, 默认存入session中.

### 微信开放平台问题

1. 获取token接口,没有 TokenType 字段, 详情见: CustomMapOAuth2AccessTokenResponseConverter 
2. 调接口,返回的是 TEXT_PLAIN 类型, 而不是 application/json, 默认 restTemplate 转换器都不支持, 详情见: CustomSecurityConfigurer#tokenEndpoint 和 CustomSecurityConfigurer#userInfoEndpoint 
3. 获取token接口, 得加 appid 和 secret 两个字段, 详情见: CustomOAuth2AuthorizationCodeGrantRequestEntityConverter#buildFormParameters
4. 获取用户个人信息,得加 openid 字段, 详情见: CustomOAuth2UserRequestEntityConverter

## 手机号登录

这里的手机号登录过滤器 仅仅是对参数phone判断,用户是否存在, 所以还得在前面添加一个短信验证码过滤器,将手机号登录的白名单地址加入到该过滤器判断中

为什么要这么做? 可以复用该过滤器,比如你有一些高级操作,也需要发送验证码,那么就只需要修改白名单地址就好


## 验证码问题

因为验证码是单独的两个过滤器,因此无法做到一个接口 既是图像验证 又是 短信验证 (有待完善). 短信验证只有特殊情况下使用, 利用 短信登录, 绑定手机号等.其他情况用图像验证码.

# 公众号

![公众号-知一码园](./document/zymy.jpg)
