# SecurityAutoConfiguration

```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(DefaultAuthenticationEventPublisher.class)
@EnableConfigurationProperties(SecurityProperties.class)
@Import({ SpringBootWebSecurityConfiguration.class, // 提供默认WebSecurityConfigurerAdapter
	WebSecurityEnablerConfiguration.class, // 核心配置
	SecurityDataConfiguration.class }) // 提供Spring Data 支持,不常用
public class SecurityAutoConfiguration {
	@Bean
	@ConditionalOnMissingBean(AuthenticationEventPublisher.class) 
	public DefaultAuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher publisher) {
		return new DefaultAuthenticationEventPublisher(publisher); // 定义了一个事件发布器
	}
}
```
# SpringBootWebSecurityConfiguration

当用户没有自定义情况下,提供一个默认的WebSecurityConfigurerAdapter

```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(WebSecurityConfigurerAdapter.class)
@ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
@ConditionalOnWebApplication(type = Type.SERVLET)
public class SpringBootWebSecurityConfiguration {
	@Configuration(proxyBeanMethods = false)
	@Order(SecurityProperties.BASIC_AUTH_ORDER)
	static class DefaultConfigurerAdapter extends WebSecurityConfigurerAdapter {}
}
```

# WebSecurityEnablerConfiguration

引入核心注解 @EnableWebSecurity

```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(WebSecurityConfigurerAdapter.class)
@ConditionalOnMissingBean(name = BeanIds.SPRING_SECURITY_FILTER_CHAIN)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableWebSecurity // 核心注解
public class WebSecurityEnablerConfiguration {
}
```

## @EnableWebSecurity
```java
@Import({ WebSecurityConfiguration.class, // [重点2]
	SpringWebMvcImportSelector.class,
	OAuth2ImportSelector.class })
@EnableGlobalAuthentication // 引入全局配置[重点1]
@Configuration
public @interface EnableWebSecurity {
	boolean debug() default false;
}
```

### EnableGlobalAuthentication

```java
@Import(AuthenticationConfiguration.class) // [重点]
@Configuration
public @interface EnableGlobalAuthentication {
}
```

#### AuthenticationConfiguration

全局配置

- 重点 authenticationManagerBuilder() 方法, 注入了 AuthenticationManagerBuilder 对象到容器
- 还有 getAuthenticationManager() 方法,可以看出,所有属性都是为了这个方法的材料. 在 WebSecurity 会用到,也有可能不调用该方法,用不到的时候,就是自己从容器中获取AuthenticationManagerBuilder,自定义build的过程来替代该方法.

```java
@Configuration(proxyBeanMethods = false)
@Import(ObjectPostProcessorConfiguration.class) // Spring提供的工具,可以将已有的对象,注入到Spring容器中.
public class AuthenticationConfiguration {
    // [1]
	@Bean
	public AuthenticationManagerBuilder authenticationManagerBuilder(ObjectPostProcessor<Object> objectPostProcessor, ApplicationContext context) {
		LazyPasswordEncoder defaultPasswordEncoder = new LazyPasswordEncoder(context);
		AuthenticationEventPublisher authenticationEventPublisher = getBeanOrNull(context, AuthenticationEventPublisher.class);

		DefaultPasswordEncoderAuthenticationManagerBuilder result = new DefaultPasswordEncoderAuthenticationManagerBuilder(objectPostProcessor, defaultPasswordEncoder);
		if (authenticationEventPublisher != null) {
			result.authenticationEventPublisher(authenticationEventPublisher);
		}
		return result;
	}
    // [2]
	public AuthenticationManager getAuthenticationManager() throws Exception {
		if (this.authenticationManagerInitialized) {
			return this.authenticationManager;
		}
		AuthenticationManagerBuilder authBuilder = this.applicationContext.getBean(AuthenticationManagerBuilder.class);
		if (this.buildingAuthenticationManager.getAndSet(true)) {
			return new AuthenticationManagerDelegator(authBuilder);
		}

		for (GlobalAuthenticationConfigurerAdapter config : globalAuthConfigurers) {
			authBuilder.apply(config);
		}

		authenticationManager = authBuilder.build();

		if (authenticationManager == null) {
			authenticationManager = getAuthenticationManagerBean();
		}

		this.authenticationManagerInitialized = true;
		return authenticationManager;
	}
}
```

### WebSecurityConfiguration

1. 先调用 setFilterChainProxySecurityConfigurer(), 构建 WebSecurity, 并将 webSecurityConfigurer 存入其中,然后注入到 Spring 
2. 再调用 springSecurityFilterChain(),  利用上一步的 WebSecurity 构建 springSecurityFilterChain 过滤器链

```java
@Configuration(proxyBeanMethods = false)
public class WebSecurityConfiguration implements ImportAware, BeanClassLoaderAware {
	// [2] 创建Spring Security的过滤器链
	@Bean(name = AbstractSecurityWebApplicationInitializer.DEFAULT_FILTER_NAME)
	public Filter springSecurityFilterChain() throws Exception {
		// 默认至少有一个setFilterChainProxySecurityConfigurer()中有分析
		boolean hasConfigurers = webSecurityConfigurers != null && !webSecurityConfigurers.isEmpty();
		if (!hasConfigurers) {
			WebSecurityConfigurerAdapter adapter = objectObjectPostProcessor.postProcess(new WebSecurityConfigurerAdapter() {});
			webSecurity.apply(adapter);
		}
		return webSecurity.build(); // 核心步骤 构建 FilterChainProxy
	}

	@Bean
	@DependsOn(AbstractSecurityWebApplicationInitializer.DEFAULT_FILTER_NAME)
	public WebInvocationPrivilegeEvaluator privilegeEvaluator() {
		return webSecurity.getPrivilegeEvaluator();
	}

	/**
	 * [1]
	 * 收集 <SecurityConfigurer<FilterChainProxy, WebSecurityBuilder> 实例,并进行排序后, 用以创建并初始化 WebSecurity 
	 * @param objectPostProcessor  自动装配处理器 AutowireBeanFactoryObjectPostProcessor
	 * @param webSecurityConfigurers 通过本类定义的 AutowiredWebSecurityConfigurersIgnoreParents Bean 的 getWebSecurityConfigurers() 返回值作为参数
	 * 实际代码
	 *	List<SecurityConfigurer<Filter, WebSecurity>> webSecurityConfigurers = new ArrayList<>();
	 *	Map<String, WebSecurityConfigurer> beansOfType = beanFactory.getBeansOfType(WebSecurityConfigurer.class);
	 *	for (Entry<String, WebSecurityConfigurer> entry : beansOfType.entrySet()) {
	 *		webSecurityConfigurers.add(entry.getValue());
	 *	}
	 *	return webSecurityConfigurers;
	 * 也就是说,如果我们没有自定义webSecurityConfigurer,那么就会导入上文 SpringBootWebSecurityConfiguration 中的DefaultConfigurerAdapter,因为DefaultConfigurerAdapter也是一个
	 * WebSecurityConfigurer
	 */
	@Autowired(required = false)
	public void setFilterChainProxySecurityConfigurer(
			ObjectPostProcessor<Object> objectPostProcessor, 
			@Value("#{@autowiredWebSecurityConfigurersIgnoreParents.getWebSecurityConfigurers()}") List<SecurityConfigurer<Filter, WebSecurity>> webSecurityConfigurers) 
			throws Exception {
			
		webSecurity = objectPostProcessor.postProcess(new WebSecurity(objectPostProcessor)); // 创建 WebSecurity, 并手动托管给Spring

		webSecurityConfigurers.sort(AnnotationAwareOrderComparator.INSTANCE); // 对配置排序

		Integer previousOrder = null;

		for (SecurityConfigurer<Filter, WebSecurity> config : webSecurityConfigurers) {
			Integer order = AnnotationAwareOrderComparator.lookupOrder(config);
			if (previousOrder != null && previousOrder.equals(order)) {
				throw new IllegalStateException("优先级不能相同");
			}
			previousOrder = order;
		}
		 // 保存 SecurityConfigurer 到 WebSecurity 父类的 AbstractConfiguredSecurityBuilder 中 configurers Map中
		for (SecurityConfigurer<Filter, WebSecurity> webSecurityConfigurer : webSecurityConfigurers) {
			webSecurity.apply(webSecurityConfigurer);
		}
		// 保存 webSecurityConfigurers 在当前类
		this.webSecurityConfigurers = webSecurityConfigurers;
	}
	
	// Spring ImportAware 接口提供, 可以检查到使用Import注入当前类的属性信息,这里使用@EnableWebSecurity提供导入的
	// 所以这里能看到EnableWebSecurity的相关信息,主要是是否开启debug模式
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		Map<String, Object> enableWebSecurityAttrMap = importMetadata
				.getAnnotationAttributes(EnableWebSecurity.class.getName());
		AnnotationAttributes enableWebSecurityAttrs = AnnotationAttributes.fromMap(enableWebSecurityAttrMap);
		debugEnabled = enableWebSecurityAttrs.getBoolean("debug");
		if (webSecurity != null) {
			webSecurity.debug(debugEnabled);
		}
	}
	
	// Spring BeanClassLoaderAware 接口提供的功能, 获取ClassLoader
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}
}
```

# WebSecurity

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



