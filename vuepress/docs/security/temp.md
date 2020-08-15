
## WebSecurity

上文已经分析出干活的是build方法,由WebSecurity的父类AbstractSecurityBuilder提供

## AbstractSecurityBuilder#build

```java
protected final O doBuild()  {
    synchronized (configurers) {
        buildState = BuildState.INITIALIZING;

        beforeInit(); 
        init(); // 执行所有 WebSecurityConfigurerAdapter 的中的 init() 方法

        buildState = BuildState.CONFIGURING;

        beforeConfigure();
        configure();  // 执行所有 WebSecurityConfigurerAdapter 的中的 configure() 方法,默认空实现,一般就是我们自定义的地方

        buildState = BuildState.BUILDING;

        O result = performBuild(); // 调用 HttpSecurity 的 performBuild()

        buildState = BuildState.BUILT;

        return result;
    }
}
```

## WebSecurityConfigurerAdapter#init

```java
public void init(final WebSecurity web) throws Exception {
    final HttpSecurity http = getHttp(); // 创建HttpSecurity
    web.addSecurityFilterChainBuilder(http).postBuildAction(() -> {
        FilterSecurityInterceptor securityInterceptor = http.getSharedObject(FilterSecurityInterceptor.class);
        web.securityInterceptor(securityInterceptor);
    });
}

protected final HttpSecurity getHttp() throws Exception {
    if (http != null) {
        return http;
    }

    AuthenticationEventPublisher eventPublisher = getAuthenticationEventPublisher();
    localConfigureAuthenticationBldr.authenticationEventPublisher(eventPublisher);

    AuthenticationManager authenticationManager = authenticationManager(); // 全局 AuthenticationManager
    authenticationBuilder.parentAuthenticationManager(authenticationManager); // 设置父级
    Map<Class<?>, Object> sharedObjects = createSharedObjects(); // 缓存一些对象

    http = new HttpSecurity(objectPostProcessor, authenticationBuilder, sharedObjects);
    if (!disableDefaults) { // 是否加载默认的配置
        http
            .csrf().and()
            .addFilter(new WebAsyncManagerIntegrationFilter())
            .exceptionHandling().and()
            .headers().and()
            .sessionManagement().and()
            .securityContext().and()
            .requestCache().and()
            .anonymous().and()
            .servletApi().and()
            .apply(new DefaultLoginPageConfigurer<>()).and()
            .logout();
        ClassLoader classLoader = this.context.getClassLoader();
        // 通过spring.factories 文件加载
        List<AbstractHttpConfigurer> defaultHttpConfigurers = SpringFactoriesLoader.loadFactories(AbstractHttpConfigurer.class, classLoader);
        for (AbstractHttpConfigurer configurer : defaultHttpConfigurers) {
            http.apply(configurer);
        }
    }
    configure(http); // 自定义路口 一般我们就需要覆盖这个方法, 这就是为什么默认启动表单登录和Basic登录的地方
    return http;
}

protected AuthenticationManager authenticationManager() throws Exception {
    if (!authenticationManagerInitialized) {
        // 默认是disableLocalConfigureAuthenticationBldr = true, 如果我们覆盖了,就是false,就相当于不走默认了
        configure(localConfigureAuthenticationBldr); // 自定义路口
        if (disableLocalConfigureAuthenticationBldr) {
            authenticationManager = authenticationConfiguration.getAuthenticationManager(); // 获取默认的全局AuthenticationManager
        } else {
            authenticationManager = localConfigureAuthenticationBldr.build(); // 构建自定义的全局AuthenticationManager
        }
        authenticationManagerInitialized = true;
    }
    return authenticationManager;
}

private Map<Class<?>, Object> createSharedObjects() {
    Map<Class<?>, Object> sharedObjects = new HashMap<>();
    sharedObjects.putAll(localConfigureAuthenticationBldr.getSharedObjects());
    sharedObjects.put(UserDetailsService.class, userDetailsService());
    sharedObjects.put(ApplicationContext.class, context);
    sharedObjects.put(ContentNegotiationStrategy.class, contentNegotiationStrategy);
    sharedObjects.put(AuthenticationTrustResolver.class, trustResolver);
    return sharedObjects;
}

protected void configure(HttpSecurity http) throws Exception {
    logger.debug("Using default configure(HttpSecurity). If subclassed this will potentially override subclass configure(HttpSecurity).");
    http.authorizeRequests().anyRequest().authenticated().and()
        .formLogin().and()
        .httpBasic();
}
```

## WebSecurity#performBuild

构建 FilterChainProxy

```java
protected Filter performBuild() throws Exception {
    int chainSize = ignoredRequests.size() + securityFilterChainBuilders.size();
    List<SecurityFilterChain> securityFilterChains = new ArrayList<>(chainSize);
    for (RequestMatcher ignoredRequest : ignoredRequests) {
        securityFilterChains.add(new DefaultSecurityFilterChain(ignoredRequest));
    }
    for (SecurityBuilder<? extends SecurityFilterChain> securityFilterChainBuilder : securityFilterChainBuilders) {
        securityFilterChains.add(securityFilterChainBuilder.build()); // 调用 HttpSecurity.build() 构建 Security过滤器链
    }
    FilterChainProxy filterChainProxy = new FilterChainProxy(securityFilterChains); // FilterChainProxy
    if (httpFirewall != null) {
        filterChainProxy.setFirewall(httpFirewall);
    }
    filterChainProxy.afterPropertiesSet();

    Filter result = filterChainProxy;
    if (debugEnabled) {
        result = new DebugFilter(filterChainProxy);
    }
    postBuildAction.run();
    return result; 
}
```

## HttpSecurity

```java
/*
 *  0 = {CsrfConfigurer@5981} 
 *  1 = {ExceptionHandlingConfigurer@5982} 
 *  2 = {HeadersConfigurer@5983} 
 *  3 = {SessionManagementConfigurer@5984} 
 *  4 = {SecurityContextConfigurer@5985} 
 *  5 = {RequestCacheConfigurer@5986} 
 *  6 = {AnonymousConfigurer@5987} 
 *  7 = {ServletApiConfigurer@5988} 
 *  8 = {DefaultLoginPageConfigurer@5989} 
 *  9 = {LogoutConfigurer@5990} 
 *  10 = {ExpressionUrlAuthorizationConfigurer@5991} 
 *  11 = {FormLoginConfigurer@5992} 
 *  12 = {HttpBasicConfigurer@5993} 
 */
protected final O doBuild()  {
    synchronized (configurers) {
        buildState = BuildState.INITIALIZING;

        beforeInit(); 
        init();

        buildState = BuildState.CONFIGURING;

        beforeConfigure();
        configure();  

        buildState = BuildState.BUILDING;

        O result = performBuild(); // 调用 HttpSecurity 的 performBuild()

        buildState = BuildState.BUILT;

        return result;
    }
}

// [1]
private void init() throws Exception {
    Collection<SecurityConfigurer<O, B>> configurers = getConfigurers(); // 13 个

    for (SecurityConfigurer<O, B> configurer : configurers) {
        configurer.init((B) this);
    }

    for (SecurityConfigurer<O, B> configurer : configurersAddedInInitializing) {
        configurer.init((B) this);
    }
}

// [2]
protected void beforeConfigure() throws Exception {
    // 构建通过 AuthenticationManagerBuilder 构建 AuthenticationManager
    setSharedObject(AuthenticationManager.class, getAuthenticationRegistry().build());
}
private AuthenticationManagerBuilder getAuthenticationRegistry() {
    return getSharedObject(AuthenticationManagerBuilder.class);
}
// AuthenticationManagerBuilder#performBuild
protected ProviderManager performBuild() throws Exception {
    if (!isConfigured()) {
        return null;
    }
    // 重点: ProviderManager
    ProviderManager providerManager = new ProviderManager(authenticationProviders, parentAuthenticationManager);
    if (eraseCredentials != null) {
        providerManager.setEraseCredentialsAfterAuthentication(eraseCredentials);
    }
    if (eventPublisher != null) {
        providerManager.setAuthenticationEventPublisher(eventPublisher);
    }
    providerManager = postProcess(providerManager);
    return providerManager;
}
```
到这里主要的 securityFilterChains 创建过程就完成了,主要流程也算完成了.

# SpringWebMvcImportSelector

Servlet 环境下,导入 WebMvcSecurityConfiguration 配置

```java
class SpringWebMvcImportSelector implements ImportSelector {
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		boolean webmvcPresent = ClassUtils.isPresent( "org.springframework.web.servlet.DispatcherServlet", getClass().getClassLoader());
		return webmvcPresent ? new String[] { "org.springframework.security.config.annotation.web.configuration.WebMvcSecurityConfiguration" } : new String[] {};
	}
}
```

## WebMvcSecurityConfiguration

非重点

用于为Spring MVC和Spring Security CSRF集成添加一个RequestDataValueProcessor。
只要SpringWebMvcImportSelector 添加EnableWebMvc，并且DispatcherServlet出现在类路径上，就会添加此配置。
它还添加了AuthenticationPrincipalArgumentResolver作为HandlerMethodArgumentResolver。

```java
class WebMvcSecurityConfiguration implements WebMvcConfigurer, ApplicationContextAware {
	private BeanResolver beanResolver;

	@Override
	@SuppressWarnings("deprecation")
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		AuthenticationPrincipalArgumentResolver authenticationPrincipalResolver = new AuthenticationPrincipalArgumentResolver();
		authenticationPrincipalResolver.setBeanResolver(beanResolver);
		argumentResolvers.add(authenticationPrincipalResolver);
		argumentResolvers.add(new org.springframework.security.web.bind.support.AuthenticationPrincipalArgumentResolver());

		CurrentSecurityContextArgumentResolver currentSecurityContextArgumentResolver = new CurrentSecurityContextArgumentResolver();
		currentSecurityContextArgumentResolver.setBeanResolver(beanResolver);
		argumentResolvers.add(currentSecurityContextArgumentResolver);
		argumentResolvers.add(new CsrfTokenArgumentResolver());
	}

	@Bean
	public RequestDataValueProcessor requestDataValueProcessor() {
		return new CsrfRequestDataValueProcessor();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.beanResolver = new BeanFactoryResolver(applicationContext.getAutowireCapableBeanFactory());
	}
}

```

# OAuth2ImportSelector

非重点

OAuth2ClientConfiguration 生效,当存在spring-security-oauth2-client 模块
SecurityReactorContextConfiguration 生效: 当存在 the spring-security-oauth2-client 或者 spring-security-oauth2-resource-server module , 并且需要存在 spring-webflux 模块

```java
final class OAuth2ImportSelector implements ImportSelector {
	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		Set<String> imports = new LinkedHashSet<>();
		boolean oauth2ClientPresent = ClassUtils.isPresent("org.springframework.security.oauth2.client.registration.ClientRegistration", getClass().getClassLoader());
		if (oauth2ClientPresent) {
			imports.add("org.springframework.security.config.annotation.web.configuration.OAuth2ClientConfiguration");
		}
		boolean webfluxPresent = ClassUtils.isPresent( "org.springframework.web.reactive.function.client.ExchangeFilterFunction", getClass().getClassLoader());
		if (webfluxPresent && oauth2ClientPresent) {
			imports.add("org.springframework.security.config.annotation.web.configuration.SecurityReactorContextConfiguration");
		}
		boolean oauth2ResourceServerPresent = ClassUtils.isPresent( "org.springframework.security.oauth2.server.resource.BearerTokenError", getClass().getClassLoader());
		if (webfluxPresent && oauth2ResourceServerPresent) {
			imports.add("org.springframework.security.config.annotation.web.configuration.SecurityReactorContextConfiguration");
		}
		return imports.toArray(new String[0]);
	}
}
```



