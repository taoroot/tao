
# 第三方登录

自2019年7月起不再支持 spring-social, 见[《Spring Social 报废公告》](https://spring.io/blog/2018/07/03/spring-social-end-of-life-announcement)

官方不建议使用 Spring Security OAuth 项目。 Spring Security 已提供了最新的 OAuth 2.0 支持[《OAuth 2.0迁移指南》](https://github.com/spring-projects/spring-security/wiki/OAuth-2.0-Migration-Guide) 默认支持: Github, Google, Facebook . 

总知,官方觉得一个OAuth2功能被分散多个依赖库中,应该得到统一解决,所以在 Security 5 集成进来统一处理.


## 授权码模式

第三方登录,通常采用授权码模式,一下是对授权码模式过程的解释.

```
    +--------+                               +---------------+
    |        |--(A)- Authorization Request ->|   Resource    |
    |        |                               |     Owner     |
    |        |<-(B)-- Authorization Grant ---|               |
    |        |                               +---------------+
    |        |
    |        |                               +---------------+
    |        |--(C)-- Authorization Grant -->| Authorization |
    | Client |                               |     Server    |
    |        |<-(D)----- Access Token -------|               |
    |        |                               +---------------+
    |        |
    |        |                               +---------------+
    |        |--(E)----- Access Token ------>|    Resource   |
    |        |                               |     Server    |
    |        |<-(F)--- Protected Resource ---|               |
    +--------+                               +---------------+
```

- Resource Owner: 资源所有者, 指登录之人,此人已在第三方应用注册过账号
- Client: 客户端, 指当前应用
- Authorization Server: 授权服务器, 指第三方应用提供令牌服务
- Resource Server: 资源服务器, 提供业务接口服务(这里是调取获取用户个人信息接口), 需携带正确令牌

::: tip 登录流程
用户在当前应用点击第三方登录按钮, 跳转到第三方应用(步骤A), 用户点击授权后携带code跳回(步骤B), 当前应用携带code访问授权服务器(步骤C), 返回token(步骤D), 当前应用携带token访问资源服务器(步骤E), 返回内容(步骤F).

在最后一步,我们调取的是用户个人信息接口,就可以获取到用户在第三方平台的唯一id, 需要将这个id与平台自身的唯一id关联起来
:::

## 关于前后端分离

OAuth2Login 默认实现是非前后端分离的,如果前后端分离化,就会涉及到两个域名, 本文通过先从前端域名通过A标签跳转到后端域名,此时后端记录前端当前地址,当后端与第三方完成登录登录,再跳回前端地址,并在前端地址上加上token=xxx参数, 前端在页面加载时,判断有无此参数,有的话,就将其保存,并刷新当前页面,再次刷新后即完成登录. 这里未做白名单,意味着所有网站都跨域跳转过来,可以根据需要,判断是否允许跳转到当前前端地址. 这种方式,并不适合非Web应用

默认在调用 http://xxxx/oauth2/authorization/{registerId} 默认会保存 HttpServletRequest 一些信息, 再 SuccessHandler 就可以重新设置到当前的 HttpServletRequest 中, 但是不包括 Referer 字段或是 redirect_url 参数, 故重写: CustomOAuth2AuthorizationRequestResolver(保存redirect_url ), CustomHttpSessionOAuth2AuthorizationRequestRepository(获取redirect_url). 
其实这个字段默认是存在的,不需要处理也是可以的,但是得看第三方跳转回来的是否有没有帮我们带回来, 保险起见,自己保存一下. (实际测试时,第一次授权不会待会,第二次就带回来)
另外值得一提的是, 在第一次请求的时候,会生成一个state,作为key, 等第三方回调后端时,重新把第一次请求的信息找回, 例如appId, secret, 也可以加入自定义的,比如这里的 Referer, 默认存入session中.


### 前端获取 Code

是否可以前端获取 Code, 然后调用后端接口来获取token 

实际编写时遇到了问题, Spring-Security 5 对 OAuth2 实现格外严格, 会再次判断请求地址是否是我们配置的回调地址, 因为我们配置的是回调前端地址, 不匹配, 所以无法成功. 所以这里采用了后端域名与第三方完成了OAuth2以后,将我们自己的生成的token回调给我们自己的前端.严格的说,不能说是两个域名完成的OAuth2,不过从实际出发,是解决了前后端分离[不同域名]下,完成OAuth2登录,实际意义是有的. 

### 安全问题

从安全角度出发,不建议通过前端请求头中的 Referer 参数, 作为最终回调, 因为这样就不管什么域名,只要 a 标签配置了 后端的 oauth2 请求地址, 最终token都会跳转回去, 可以加一个白名单,  或者说后端忽略Referer,而是和前端规定一个地址,专门接受token. [为了调试灵活性,我这里没有白名单,也没有写死]

## oauth2Login

以下是对Security 对 OAuth2 的配置 

关于源码请看:[OAuth2LoginConfigurer 源码分析](/resouce/)

```java
.oauth2Login(config -> config
        .authorizationEndpoint(this::authorizationEndpoint) // 获取 redirect_url
        .tokenEndpoint(this::tokenEndpoint)                 // 获取 access_token
        .userInfoEndpoint(this::userInfoEndpoint)           // 获取 user_info
        .successHandler(this::successHandler))              // 认证成功处理
```

::: tip
OAuth2Login 还是 OAuth2Client ?

OAuth2Login 扩展点多, 而且支持 successHandler 和 failhandler, 这就提供了与 formLogin 类似的处理能力, 而 OAuth2Client 没有.
OAuth2Login 会多获取userInfo这一步, 一般做登录最终目的是为了确定用户的唯一性, 而不是只是为了获取token, 所以更加实用, 所需做的是在 successHandler 中将第三方的用户id,和本平台的用户id进行绑定.
:::

### 个性化问题

不同的第三方,可能实现起来会有一些出入

#### 码云

码云必须得有加 User-Agent 头, 详情见: CustomOAuth2AuthorizationCodeGrantRequestEntityConverter 和 CustomOAuth2UserRequestEntityConverter

```java
headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36");
```

### 微信&QQ

1. 获取token接口,没有 TokenType 字段, 详情见: CustomMapOAuth2AccessTokenResponseConverter 
2. 调接口,返回的是 TEXT_PLAIN 类型, 而不是 application/json, 默认 restTemplate 转换器都不支持, 详情见: CustomSecurityConfigurer#tokenEndpoint 和 CustomSecurityConfigurer#userInfoEndpoint 
3. 获取token接口, 得加 appid 和 secret 两个字段, 详情见: CustomOAuth2AuthorizationCodeGrantRequestEntityConverter#buildFormParameters
4. 获取用户个人信息,得加 openid 字段, 详情见: CustomOAuth2UserRequestEntityConverter
