# HttpSecurity

[<< 目录](/security/README.md)

![HttpSecurity](/HttpSecurity.png)


从继承关系可以看出,最后执行的是 performBuild() 方法

```java
public final class HttpSecurity extends
		AbstractConfiguredSecurityBuilder<DefaultSecurityFilterChain, HttpSecurity>
		implements SecurityBuilder<DefaultSecurityFilterChain>,
		HttpSecurityBuilder<HttpSecurity> {

	@Override
	protected DefaultSecurityFilterChain performBuild() {
		filters.sort(comparator); // 过滤器排序
		return new DefaultSecurityFilterChain(requestMatcher, filters); 
	}
}
```

关于AbstractConfiguredSecurityBuilder 和 SecurityBuilder 已在[ WebSecurity一章 ](/security/websecurity.md)叙述过.
这里多出一个 HttpSecurityBuilder

## HttpSecurityBuilder

- 带排序的添加过滤器
- 添加或移除配置
- 添加 UserDetailsService
- 添加 AuthenticationProvider

```java
public interface HttpSecurityBuilder<H extends HttpSecurityBuilder<H>> extends
		SecurityBuilder<DefaultSecurityFilterChain> {
	
	// 获取某个配置
	<C extends SecurityConfigurer<DefaultSecurityFilterChain, H>> C getConfigurer(Class<C> clazz);
	// 移除某个配置 disable() 就是依据这个原理实现
	<C extends SecurityConfigurer<DefaultSecurityFilterChain, H>> C removeConfigurer(Class<C> clazz);
	// 缓存某个对象
	<C> void setSharedObject(Class<C> sharedType, C object);
	// 获取缓存对象
	<C> C getSharedObject(Class<C> sharedType);
	// 添加 AuthenticationProvider
	H authenticationProvider(AuthenticationProvider authenticationProvider);
	// 添加 UserDetailsService
	H userDetailsService(UserDetailsService userDetailsService) throws Exception;
	// 在某个过滤器后添加过滤器
	H addFilterAfter(Filter filter, Class<? extends Filter> afterFilter);
	// 在某个过滤器前添加过滤器
	H addFilterBefore(Filter filter, Class<? extends Filter> beforeFilter);
	// 默认添加 (map.put)
	H addFilter(Filter filter);
}
```

## 过滤器排序

先记录到 filters 列表, 然后注册到比较器中, 最后利用比较器进行对列表重新排序.

重点在于这个比较器

```java
private List<Filter> filters = new ArrayList<>(); 
private RequestMatcher requestMatcher = AnyRequestMatcher.INSTANCE;
private FilterComparator comparator = new FilterComparator(); // 比较器

public HttpSecurity addFilterAfter(Filter filter, Class<? extends Filter> afterFilter) {
	comparator.registerAfter(filter.getClass(), afterFilter);
	return addFilter(filter);
}

public HttpSecurity addFilterBefore(Filter filter,
		Class<? extends Filter> beforeFilter) {
	comparator.registerBefore(filter.getClass(), beforeFilter);
	return addFilter(filter);
}

public HttpSecurity addFilter(Filter filter) {
	Class<? extends Filter> filterClass = filter.getClass();
	if (!comparator.isRegistered(filterClass)) {
		throw new IllegalArgumentException(
				"The Filter class "
						+ filterClass.getName()
						+ "没有已注册的Order，并且在没有指定Order的情况下无法添加。"
						+ "考虑使用addFilterBefore或addFilterAfter代替");
	}
	this.filters.add(filter);
	return this;
}

public HttpSecurity addFilterAt(Filter filter, Class<? extends Filter> atFilter) {
	this.comparator.registerAt(filter.getClass(), atFilter);
	return addFilter(filter);
}


```

#### FilterComparator

比较器在构造函数中提供了一大堆内置过滤器的排序,而且不能修改.

```java
// <类名, Order> Order 越小越在前
private final Map<String, Integer> filterToOrder = new HashMap<>();

FilterComparator() {
	Step order = new Step(INITIAL_ORDER, ORDER_STEP);
	put(ChannelProcessingFilter.class, order.next());
	put(ConcurrentSessionFilter.class, order.next());
	put(WebAsyncManagerIntegrationFilter.class, order.next());
	put(SecurityContextPersistenceFilter.class, order.next());
	put(HeaderWriterFilter.class, order.next());
	put(CorsFilter.class, order.next());
	put(CsrfFilter.class, order.next());
	put(LogoutFilter.class, order.next());
	filterToOrder.put(
		"org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter",
			order.next());
	filterToOrder.put(
			"org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationRequestFilter",
			order.next());
	put(X509AuthenticationFilter.class, order.next());
	put(AbstractPreAuthenticatedProcessingFilter.class, order.next());
	filterToOrder.put("org.springframework.security.cas.web.CasAuthenticationFilter",
			order.next());
	filterToOrder.put(
		"org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter",
			order.next());
	filterToOrder.put(
			"org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationFilter",
			order.next());
	put(UsernamePasswordAuthenticationFilter.class, order.next());
	put(ConcurrentSessionFilter.class, order.next());
	filterToOrder.put(
			"org.springframework.security.openid.OpenIDAuthenticationFilter", order.next());
	put(DefaultLoginPageGeneratingFilter.class, order.next());
	put(DefaultLogoutPageGeneratingFilter.class, order.next());
	put(ConcurrentSessionFilter.class, order.next());
	put(DigestAuthenticationFilter.class, order.next());
	filterToOrder.put(
			"org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter", order.next());
	put(BasicAuthenticationFilter.class, order.next());
	put(RequestCacheAwareFilter.class, order.next());
	put(SecurityContextHolderAwareRequestFilter.class, order.next());
	put(JaasApiIntegrationFilter.class, order.next());
	put(RememberMeAuthenticationFilter.class, order.next());
	put(AnonymousAuthenticationFilter.class, order.next());
	filterToOrder.put(
		"org.springframework.security.oauth2.client.web.OAuth2AuthorizationCodeGrantFilter",
			order.next());
	put(SessionManagementFilter.class, order.next());
	put(ExceptionTranslationFilter.class, order.next());
	put(FilterSecurityInterceptor.class, order.next());
	put(SwitchUserFilter.class, order.next());
}

public void registerBefore(Class<? extends Filter> filter,
		Class<? extends Filter> beforeFilter) {
	Integer position = getOrder(beforeFilter); // 获取当前Order
	if (position == null) {
		throw new IllegalArgumentException(
				"Cannot register after unregistered Filter " + beforeFilter);
	}

	put(filter, position - 1); // Order 小1
}

private void put(Class<? extends Filter> filter, int position) {
	String className = filter.getName();
	filterToOrder.put(className, position);
}
```

## userDetailsService()
## authenticationProvider()


```java
// 从缓存中获取 AuthenticationManagerBuilder
private AuthenticationManagerBuilder getAuthenticationRegistry() {
	return getSharedObject(AuthenticationManagerBuilder.class);
}

public HttpSecurity userDetailsService(UserDetailsService userDetailsService) {
	getAuthenticationRegistry().userDetailsService(userDetailsService);
	return this;
}

public HttpSecurity authenticationProvider( AuthenticationProvider authenticationProvider) {
	getAuthenticationRegistry().authenticationProvider(authenticationProvider);
	return this;
}
```
::: tip 关于 AuthenticationManagerBuilder
将在 [AuthenticationManagerBuilder 一章](/security/authenticationmanagerbuilder.md) 具体分析
::: 

## 常见配置 

```java
public HttpSecurity httpBasic(Customizer<HttpBasicConfigurer<HttpSecurity>> httpBasicCustomizer) throws Exception {
	httpBasicCustomizer.customize(getOrApply(new HttpBasicConfigurer<>()));
	return HttpSecurity.this;
}

public HttpSecurity oauth2ResourceServer(Customizer<OAuth2ResourceServerConfigurer<HttpSecurity>> oauth2ResourceServerCustomizer)
		throws Exception {
	OAuth2ResourceServerConfigurer<HttpSecurity> configurer = getOrApply(new OAuth2ResourceServerConfigurer<>(getContext()));
	this.postProcess(configurer);
	oauth2ResourceServerCustomizer.customize(configurer);
	return HttpSecurity.this;
}

public HttpSecurity oauth2Login(Customizer<OAuth2LoginConfigurer<HttpSecurity>> oauth2LoginCustomizer) throws Exception {
	oauth2LoginCustomizer.customize(getOrApply(new OAuth2LoginConfigurer<>()));
	return HttpSecurity.this;
}

public HttpSecurity formLogin(Customizer<FormLoginConfigurer<HttpSecurity>> formLoginCustomizer) throws Exception {
	formLoginCustomizer.customize(getOrApply(new FormLoginConfigurer<>()));
	return HttpSecurity.this;
}

public HttpSecurity exceptionHandling(Customizer<ExceptionHandlingConfigurer<HttpSecurity>> exceptionHandlingCustomizer) throws Exception {
	exceptionHandlingCustomizer.customize(getOrApply(new ExceptionHandlingConfigurer<>()));
	return HttpSecurity.this;
}

public HttpSecurity cors(Customizer<CorsConfigurer<HttpSecurity>> corsCustomizer) throws Exception {
	corsCustomizer.customize(getOrApply(new CorsConfigurer<>()));
	return HttpSecurity.this;
}
```
::: tip 内容过多,分章叙述
- httpBasic 见 [HttpBasicConfigurer 源码分析](formloginconfigurer.md)
- oauth2ResourceServer 见 [OAuth2ResourceServerConfigurer 源码分析](oauth2resourceserverconfigurer.md)
- oauth2Login 见 [OAuth2LoginConfigure 源码分析r](oauth2loginconfigurer.md)
- formLogin 见 [FormLoginConfigurer 源码分析](formloginconfigurer.md)
- exceptionHandling 见 [ExceptionHandlingConfigurer 源码分析](exceptionhandlingconfigurer.md)
- cors 见 [CorsConfigurer 源码分析](corsconfigurer.md)
::: 