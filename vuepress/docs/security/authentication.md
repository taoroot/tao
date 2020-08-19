# Authentication

[<< 目录](/security/README.md)


本文是对[官方文档](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#servlet-authentication)的翻译

本节描述 Spring Security 在 Servlet 身份验证中使用的主要体系结构组件

- [SecurityContextHolder](#securitycontextholder) 
- [SecurityContext](#securitycontext)
- [Authentication](#authentication)
- [GrantedAuthority](#grantedauthority)
- [AuthenticationManager](#authenticationmanager)
- [ProviderManager](#providermanager)
- [AuthenticationProvider](#authenticationprovider)
- [Request Credentials with AuthenticationEntryPoint](#request-credentials-with-authenticationentrypoint)
- [AbstractAuthenticationProcessingFilter](#abstractauthenticationprocessingfilter)

## SecurityContextHolder 

Spring Security 的身份验证模型的核心是 SecurityContextHolder. 内部持有 SecurityContext. 

![SecurityContextHolder](/securitycontextholder.png)

SecurityContextHolder 是 Spring Security 存储身份验证的详细信息的地方。Spring Security 并不关心 SecurityContext 是如何填充的。如果它包含一个值，那么它将被用作当前经过身份验证的用户。

表示用户已通过身份验证的最简单方法是直接设置 SecurityContextHolder。

```java
SecurityContext context = SecurityContextHolder.createEmptyContext(); // [1]
Authentication authentication =
    new TestingAuthenticationToken("username", "password", "ROLE_USER"); // [2]
context.setAuthentication(authentication);

SecurityContextHolder.setContext(context); // [3]
```

1. 我们从创建一个空的 SecurityContext 开始. 创建一个新的 SecurityContext 实例而不是使用 创建一个新的SecurityContext实例而不是使用SecurityContextHolder.getContext().setAuthentication(authentication)来避免多线程之间的竞争条件是很重要的。来避免多线程之间的竞争条件是很重要的。
2. 接下来，我们创建一个新的 Authentication (身份验证对象)。属性上设置了什么类型的身份验证实现并不关心. 这里我们使用TestingAuthenticationToken，因为它非常简单。更常见的生产场景是 UsernamePasswordAuthenticationToken(userDetails, password, authorities).
3. 最后，我们在 SecurityContextHolder 上设置 SecurityContext。Spring Security将使用这些信息进行授权.

如果您希望获得关于认证主体的信息，您可以通过访问 SecurityContextHolder 来实现.

```java
SecurityContext context = SecurityContextHolder.getContext();
Authentication authentication = context.getAuthentication();
String username = authentication.getName();
Object principal = authentication.getPrincipal();
Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
```

默认情况下, SecurityContextHolder 使用 ThreadLocal 来存储这些细节. 这意味着 SecurityContext 始终对同一执行线程中的方法可用, 即使SecurityContext 没有显式地作为参数传递给那些方法。如果在当前主体的请求被处理后清除线程，那么以这种方式使用ThreadLocal是非常安全的.Spring Security 的 FilterChainProxy 确保 SecurityContext 总是被清除。

有些应用程序并不完全适合使用 ThreadLocal，这是因为它们使用线程的特定方式。例如，Swing客户机可能希望Java虚拟机中的所有线程使用相同的安全上下文。SecurityContextHolder 可以在启动时配置一个策略，以指定您希望如何存储上下文。对于独立应用程序，您将使用ThreadLocal，这是因为它们使用线程的特定方式。例如，Swing客户机可能希望Java虚拟机中的所有线程使用相同的安全上下文。对于独立应用程序，您将使用 SecurityContextHolder.MODE_GLOBAL 策略。其他应用程序可能希望由安全线程派生的线程也采用相同的安全标识。这是通过使用 SecurityContextHolder.MODE_INHERITABLETHREADLOCAL 实现的。你可以将默认的SecurityContextHolder.你可以从默认的 SecurityContextHolder.MODE_THREADLOCAL有两种方式 改为上述两种模式通过两种方式: 第一个是设置一个系统属性，第二个是调用 SecurityContextHolder 的静态方法. 大多数应用程序不需要更改默认值

## SecurityContext

SecurityContext 是从 SecurityContextHolder 中获得的. SecurityContext 包含一个身份验证对象(Authentication)。

## Authentication

在 Spring Security 中, Authentication 有两个用处

- 作为 AuthenticationManager 的输入参数，用于提供用户为进行身份验证而提供的凭据。但当前场景 isAuthenticated() 返回 false 时候.
- 表示当前经过身份验证的用户。当前的 Authentication 可以从 SecurityContext 获得。

Authentication 包含一下内容:

- principal: 用户唯一标识, 当使用 用户名/密码 进行身份验证时，这通常是 UserDetails 的实例
- credentials: 通常一个密码, 在许多情况下，这将在用户经过身份验证后被清除，以确保它不会泄漏。
- authorities: 授予的权限是授予用户的高级权限。有时候是角色或作用域。
  
## GrantedAuthority

GrantedAuthority

授予的权限是授予用户的高级权限。有时候是角色或作用域。

GrantedAuthoritys 可以从 authentication.getauthority() 方法获得。此方法提供了一个GrantedAuthority对象的集合。一个 GrantedAuthority 代表的是一个已授权给当前 principal 的权限. 这样的权限通常是 “角色”，比如 ROLE_ADMINISTRATOR 或 ROLE_HR_SUPERVISOR. 这些角色稍后将配置为 web授权、方法授权和域对象授权。当使用基于用户名/密码的身份验证时，授权权限通常由UserDetailsService加载。

通常，GrantedAuthority 对象是应用程序范围的权限。它们不是特定于给定域对象的。因此，您不应该出现授予专门权限来表示对Employee对象编号54的权限，因为如果有数千个这样的权限，您将很快耗尽内存(或者，至少会导致应用程序花费很长时间来验证用户). 当然，Spring Security 是专门为处理这种常见需求而设计的，但是您可以使用项目的域对象安全功能来实现这一目的。

## AuthenticationManager

AuthenticationManager 是定义 Spring Security 过滤器如何执行身份验证的API。在 Spring Security 的过滤器中调用 AuthenticationManager 返回 Authentication 并设置到 SecurityContextHolder 上. 如果你不注入 Spring Security 的过滤器, 你能直接设置到SecurityContextHolder不需要通过调用AuthenticationManager

虽然 AuthenticationManager 的实现可以是任何实现类，但最常见的实现是 ProviderManager。

## ProviderManager

ProviderManager 是最常见的 ProviderManager 实现类.  ProviderManager 委托给 AuthenticationProviders 列表. 每个AuthenticationProvider都有机会指出身份验证应该是成功的、失败的，或者指出它不能做出决定，并允许下游的AuthenticationProvider做出决定。如果配置的authenticationprovider中没有一个能够进行身份验证，那么身份验证将会失败，而ProviderNotFoundException是一个特殊的AuthenticationException，它表示ProviderManager没有配置支持传递给它的身份验证类型。

![providermanager](/providermanager.png)

每个AuthenticationProvider都知道如何执行特定类型的身份验证。例如，一个AuthenticationProvider可能能够验证用户名/密码，而另一个可能能够验证SAML断言。这允许每个AuthenticationProvider执行一种非常特定的身份验证类型，同时支持多种类型的身份验证，并且只公开一个AuthenticationManager bean。

ProviderManager还允许配置一个可选的父级AuthenticationManager，当AuthenticationProvider无法执行身份验证时，可以咨询该父AuthenticationManager。父类可以是任何类型的AuthenticationManager，但它通常是ProviderManager的一个实例。

![providermanager-parent](/providermanager-parent.png)

是多个ProviderManager实例可能共享一个父级AuthenticationManager. 这在有多个SecurityFilterChain实例的场景中比较常见，这些实例有一些共同的身份验证(共享的父AuthenticationManager)，但也有不同的身份验证机制(不同的ProviderManager实例)。

![providermanagers-parent](/providermanagers-parent.png)

默认情况下，ProviderManager将尝试从成功的身份验证请求返回的Authentication对象中清除任何敏感凭据信息。这可以防止密码等信息在HttpSession中保留的时间超过所需的时间。

当您使用用户对象的缓存时，这可能会导致问题，例如，在使用用户对象的缓存来提高无状态应用程序中的性能时，这可能会导致问题。如果身份验证包含对缓存中的对象的引用(例如UserDetails实例)，并且删除了它的凭据，那么将不再可能根据缓存的值进行身份验证。如果使用缓存，则需要考虑这一点。一个明显的解决方案是首先复制对象，要么在缓存实现中，要么在创建返回的身份验证对象的AuthenticationProvider中。或者，您可以禁用ProviderManager上的eraseCredentialsAfterAuthentication属性。

## AuthenticationProvider

可以在 ProviderManager 中注入多个authenticationprovider。每个AuthenticationProvider执行特定类型的身份验证。例如，DaoAuthenticationProvider支持基于用户名/密码的身份验证，而JwtAuthenticationProvider支持对JWT令牌的身份验证。

## Request Credentials with AuthenticationEntryPoint

AuthenticationEntryPoint用于发送请求客户端凭据的HTTP响应。有时，客户端会主动包含诸如用户名/密码之类的凭证来请求资源。在这些情况下，Spring Security不需要提供从客户端请求凭证的HTTP响应，因为凭证已经包含在其中。

在其他情况下，客户机将向未授权访问的资源发出未经身份验证的请求。例如使用AuthenticationEntryPoint的实现从客户机请求凭据。AuthenticationEntryPoint实现可能执行重定向到登录页面，使用WWW-Authenticate头进行响应，等等。

## AbstractAuthenticationProcessingFilter

AbstractAuthenticationProcessingFilter 用作对用户凭据进行身份验证的基本过滤器。在验证凭证之前，Spring Security通常使用AuthenticationEntryPoint请求凭证。接下来，AbstractAuthenticationProcessingFilter 可以对提交给它的任何身份验证请求进行身份验证。

![abstractauthenticationprocessingfilter](/abstractauthenticationprocessingfilter.png)

1. 当用户提交其凭证时, AbstractAuthenticationProcessingFilter从要进行身份验证的HttpServletRequest创建身份验证。创建的身份验证类型取决于AbstractAuthenticationProcessingFilter的子类. 例如，UsernamePasswordAuthenticationFilter从在HttpServletRequest中提交的用户名和密码创建一个UsernamePasswordAuthenticationToken
2. 接下来，将身份验证传递到AuthenticationManager以进行身份验证。
3. 如果身份验证失败
   1. SecurityContextHandler.clearContext()
   2. RememberMeServices.loginFail() 被调用,在配置了记住我操作时
   3. AuthenticationFailureHandler 被调用
4. 如果身份验证成功
   1. SessionAuthenticationStrategy 将被通知有新的用户登录
   2. Authentication 加入到 SecurityContextHolder. 后面的 SecurityContextPersistenceFilter 将保存 SecurityContext 到 HttpSession.
   3. RememberMeServices.loginSuccess 被调用,在配置了记住我操作时
   4. ApplicationEventPublisher 被通知事件 InteractiveAuthenticationSuccessEvent.
   5. AuthenticationSuccessHandler 被调用