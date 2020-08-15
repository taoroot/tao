# Filter

[<< 目录](/security/README.md)

Spring Security的Servlet支持是基于Servlet过滤器的，因此首先了解过滤器的作用是有帮助的。下图显示了单个HTTP请求处理程序的典型分层。

![](/filterchain.png)

客户端向应用程序发送一个请求，容器创建一个包含过滤器的过滤器链和根据请求URI的路径处理HttpServletRequest的Servlet。
在Spring MVC应用程序中，Servlet是DispatcherServlet的一个实例。
最多一个Servlet可以处理一个HttpServletRequest和HttpServletResponse, 但是，可以使用多个过滤器,过滤器调顺序录下
```java
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
    // do something before the rest of the application
    chain.doFilter(request, response); // invoke the rest of the application
    // do something after the rest of the application
}
```
从中可以看出过滤器的调用顺序是很重要的.

## DelegatingFilterProxy

Spring提供了一个名为DelegatingFilterProxy的过滤器实现，它允许在Servlet容器的生命周期和Spring的ApplicationContext之间架桥。
Servlet容器允许使用它自己的标准注册过滤器，但是它不知道Spring定义的bean。
可以通过标准Servlet容器机制注册DelegatingFilterProxy，但将所有工作委托给实现过滤器的Spring Bean。
下面是如何将DelegatingFilterProxy融入过滤器和过滤器链的图片。

![](/delegatingfilterproxy.png)

DelegatingFilterProxy从ApplicationContext中查找Bean Filter0，然后调用Bean Filter0。下面可以看到DelegatingFilterProxy的伪代码。

```java
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
    // Lazily get Filter that was registered as a Spring Bean
    // For the example in DelegatingFilterProxy delegate is an instance of Bean Filter0
    Filter delegate = getFilterBean(someBeanName);
    // delegate work to the Spring Bean
    delegate.doFilter(request, response);
}
```

委托filterproxy的另一个好处是，它允许延迟查找过滤器bean实例。这很重要，因为容器需要在启动之前注册过滤器实例。
但是，Spring通常使用ContextLoaderListener来加载Spring bean，直到需要注册过滤器实例之后才会执行。

## FilterChainProxy

Spring Security的Servlet支持包含在FilterChainProxy中。
FilterChainProxy是Spring Security提供的一个特殊过滤器，它允许通过SecurityFilterChain委托给许多过滤器实例。
由于FilterChainProxy是一个Bean，它通常被包装在一个DelegatingFilterProxy中。

![](/filterchainproxy.png)

## SecurityFilterChain

FilterChainProxy使用SecurityFilterChain来确定应该为此请求调用哪个Spring安全过滤器。
SecurityFilterChain中的安全过滤器通常是bean，但是它们是通过FilterChainProxy注册的，而不是通过DelegatingFilterProxy。
FilterChainProxy为直接注册Servlet容器或委托filterproxy提供了许多好处。

首先，它为所有Spring Security的Servlet支持提供了一个起点。因此，如果您试图对Spring Security的Servlet支持进行故障排除，那么在FilterChainProxy中添加一个调试点是一个很好的起点

其次，由于FilterChainProxy是使用Spring安全性的核心，所以它可以执行不被视为可选的任务.例如，它清除SecurityContext以避免内存泄漏。它还应用Spring Security的HttpFirewall来保护应用程序免受某些类型的攻击。

此外，它在决定何时应该调用SecurityFilterChain方面提供了更大的灵活性。在Servlet容器中，仅根据URL调用筛选器。然而，FilterChainProxy可以通过利用RequestMatcher接口基于HttpServletRequest中的任何内容来确定调用。

实际上，可以使用FilterChainProxy来确定应该使用哪个SecurityFilterChain。这允许为不同的应用程序片提供完全独立的配置。

![](/multi-securityfilterchain.png)

只有第一个匹配的SecurityFilterChain才会被调用。如果请求一个/api/messages/的URL，它将首先匹配SecurityFilterChain0的/api/**模式，因此即使它也匹配SecurityFilterChainn，也只会调用SecurityFilterChain0。如果请求的URL为/messages/，它将与SecurityFilterChain0的/api/**模式不匹配，因此FilterChainProxy将继续尝试每个SecurityFilterChain。假设没有其他实例，则将调用与SecurityFilterChainn匹配的SecurityFilterChain实例。

请注意，SecurityFilterChain0只配置了三个安全过滤器实例。但是，SecurityFilterChainn配置了四个安全过滤器。需要注意的是，每个SecurityFilterChain可以是惟一的，并且是隔离配置的。事实上，如果应用程序希望Spring security忽略某些请求，SecurityFilterChain可能没有安全过滤器。

# 处理安全异常

ExceptionTranslationFilter允许将AccessDeniedException和AuthenticationException翻译成HTTP响应。

ExceptionTranslationFilter作为一个安全过滤器插入到FilterChainProxy中。

![exceptiontranslationfilter](/exceptiontranslationfilter.png)

1. 首先，ExceptionTranslationFilter调用FilterChain.doFilter(request, response)来调用应用程序的其余部分。

2. 如果用户没有经过身份验证，或者它是AuthenticationException异常，则启动身份验证。
   
- SecurityContextHolder 被清除
- HttpServletRequest保存在RequestCache中。当用户成功进行身份验证时，将使用RequestCache重试原始请求。
- AuthenticationEntryPoint用于从客户端请求凭据。例如，它可能重定向到登录页面或发送WWW-Authenticate头。前后端分离的情况下, 就直接提示未登录

3. 否则，如果它是AccessDeniedException，那么访问被拒绝。调用AccessDeniedHandler处理被拒绝的访问。

```java
try {
    filterChain.doFilter(request, response); 
} catch (AccessDeniedException | AuthenticationException e) {
    if (!authenticated || e instanceof AuthenticationException) {
        startAuthentication(); 
    } else {
        accessDenied(); 
    }
}
```


# 内置过滤器

使用SecurityFilterChain API将安全过滤器插入到FilterChainProxy中。
过滤器的顺序很重要。通常没有必要知道Spring Security过滤器的顺序。然而，有时知道顺序是有益的

```shell
ChannelProcessingFilter
ConcurrentSessionFilter
WebAsyncManagerIntegrationFilter
SecurityContextPersistenceFilter
HeaderWriterFilter
CorsFilter
CsrfFilter
LogoutFilter
OAuth2AuthorizationRequestRedirectFilter
Saml2WebSsoAuthenticationRequestFilter
X509AuthenticationFilter
AbstractPreAuthenticatedProcessingFilter
CasAuthenticationFilter
OAuth2LoginAuthenticationFilter
Saml2WebSsoAuthenticationFilter
UsernamePasswordAuthenticationFilter
ConcurrentSessionFilter
OpenIDAuthenticationFilter
DefaultLoginPageGeneratingFilter
DefaultLogoutPageGeneratingFilter
DigestAuthenticationFilter
BearerTokenAuthenticationFilter
BasicAuthenticationFilter
RequestCacheAwareFilter
SecurityContextHolderAwareRequestFilter
JaasApiIntegrationFilter
RememberMeAuthenticationFilter
AnonymousAuthenticationFilter
OAuth2AuthorizationCodeGrantFilter
SessionManagementFilter
ExceptionTranslationFilter
FilterSecurityInterceptor
SwitchUserFilter
```
