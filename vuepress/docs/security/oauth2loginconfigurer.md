# OAuth2LoginConfigurer

[<< 目录](/security/README.md)

OAuth 2.0 Login 登录特性提供了一个应用程序，用户可以使用他们在OAuth 2.0提供商(如GitHub)或OpenID Connect 1.0提供商(如谷歌)的现有帐户登录到该应用程序。

::: tip 
OAuth 2.0 Login 是通过使用授权码实现的，这在OAuth 2.0授权框架和OpenID Connect Core 1.0中都有指定。
::: 

一般分为四个步骤:

- 账号创建
- 设置