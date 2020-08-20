# OAuth2LoginConfigurer

[<< 目录](/security/README.md)


本章介绍 Spring Security 内置的 OAuth2 登录, 也就是常说的第三方应用授权登录

## OAuth2 授权码模式

Spring Security  采用的是 OAuth2 的 授权码模式, 以下是登录流程图

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

- Resource Owner: 资源所有者, 指登录之人已在第三方应用注册
- Client: 客户端, 指当前应用
- Authorization Server: 授权服务器, 指第三方应用提供访问令牌服务器
- Resource Server: 资源服务器, 提供业务接口服务(个人信息接口), 需携带正确令牌

::: tip 登录流程
用户在当前应用点击第三方登录按钮, 跳转到第三方应用(步骤A), 用户点击授权后携带code跳回(步骤B), 当前应用携带code访问授权服务器(步骤C), 返回token(步骤D), 当前应用携带token访问资源服务器(步骤E), 返回内容(步骤F).
在最后一步,我们调取的是用户个人信息接口,就可以获取到用户在第三方平台的唯一id, 需要将这个id与平台自身的唯一id关联起来
:::

## 实现流程

1. 前端请求 `/login/oauth2/code/{registerId}`
2. 根据 registerId 生成第三方授权地址: `https://{authorization-uri}?client_id={clientId}&client_secret={secret}&redirect_url={redirect_url}&state={state}`
3. 保存当前请求内容,key为state,内容有clientId, screct, 请求时添加的参数,也可以自定义添加
4. 指使前端发生302跳转到生成第三方授权地址
5. 用户点击授权后,第三方会跳转会 `redirect_url?code={code}&state={state}`
6. 根据state, 恢复第一次请求时的内容,
7. 通过 code 请求 `https://{token-uri}?code={code}?client_id={clientId}&client_secret={secret}&redirect_url={redirect_url}`
8. 获取到 access_token
9. 通过 access_token 请求 `https://{user-info-uri}?access_token={access_token}`
10. 获取到用户个人信息, 并提取其中的唯一凭证


- registerId 代表一个第三方应用, 例如 github, facebook, 配置信息 ClientRegistration 实例中
- state 是一个随机字符串

## 源码分析

```java
public final class OAuth2LoginConfigurer<B extends HttpSecurityBuilder<B>> extends
	AbstractAuthenticationFilterConfigurer<B, OAuth2LoginConfigurer<B>, OAuth2LoginAuthenticationFilter> {
    
    // 步骤 [1, 2, 3] 相关配置
	private final AuthorizationEndpointConfig authorizationEndpointConfig = new AuthorizationEndpointConfig(); 
    // 过程[2]
	private final TokenEndpointConfig tokenEndpointConfig = new TokenEndpointConfig(); 
	private final RedirectionEndpointConfig redirectionEndpointConfig = new RedirectionEndpointConfig();
	private final UserInfoEndpointConfig userInfoEndpointConfig = new UserInfoEndpointConfig();
	private String loginPage; 
	private String loginProcessingUrl = OAuth2LoginAuthenticationFilter.DEFAULT_FILTER_PROCESSES_URI;
}
```