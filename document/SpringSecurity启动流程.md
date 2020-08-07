# SecurityAutoConfiguration

```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(DefaultAuthenticationEventPublisher.class)
@EnableConfigurationProperties(SecurityProperties.class)
@Import({ SpringBootWebSecurityConfiguration.class, // 提供默认WebSecurityConfigurerAdapter
	WebSecurityEnablerConfiguration.class, // 核心配置
	SecurityDataConfiguration.class }) // 提供Spring Data 支持
public class SecurityAutoConfiguration {

	// 定义了一个事件发布器
	@Bean
	@ConditionalOnMissingBean(AuthenticationEventPublisher.class)
	public DefaultAuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher publisher) {
		return new DefaultAuthenticationEventPublisher(publisher);
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
	static class DefaultConfigurerAdapter extends WebSecurityConfigurerAdapter {

	}

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

# @EnableWebSecurity
```java
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = { java.lang.annotation.ElementType.TYPE })
@Documented
@Import({ WebSecurityConfiguration.class, // [重点]
	SpringWebMvcImportSelector.class,
	OAuth2ImportSelector.class })
@EnableGlobalAuthentication // 引入全局配置[重点]
@Configuration
public @interface EnableWebSecurity {

	/**
	 * Controls debugging support for Spring Security. Default is false.
	 * @return if true, enables debug support with Spring Security
	 */
	boolean debug() default false;
}
```

# WebSecurityConfiguration

1. 先调用 setFilterChainProxySecurityConfigurer(), 构建 WebSecurity, 并将 webSecurityConfigurer 存入其中,然后注入到 Spring 
2. 再调用 springSecurityFilterChain(),  利用上一步的 WebSecurity 构建 springSecurityFilterChain 过滤器链

```java
@Configuration(proxyBeanMethods = false)
public class WebSecurityConfiguration implements ImportAware, BeanClassLoaderAware {
	private WebSecurity webSecurity;

	private Boolean debugEnabled;

	private List<SecurityConfigurer<Filter, WebSecurity>> webSecurityConfigurers;

	private ClassLoader beanClassLoader;

	@Autowired(required = false)
	private ObjectPostProcessor<Object> objectObjectPostProcessor;

	@Bean
	public static DelegatingApplicationListener delegatingApplicationListener() {
		return new DelegatingApplicationListener();
	}

	@Bean
	@DependsOn(AbstractSecurityWebApplicationInitializer.DEFAULT_FILTER_NAME)
	public SecurityExpressionHandler<FilterInvocation> webSecurityExpressionHandler() {
		return webSecurity.getExpressionHandler();
	}

	// [2] 创建Spring Security的过滤器链
	@Bean(name = AbstractSecurityWebApplicationInitializer.DEFAULT_FILTER_NAME)
	public Filter springSecurityFilterChain() throws Exception {
		// 默认至少有一个setFilterChainProxySecurityConfigurer()中有分析
		boolean hasConfigurers = webSecurityConfigurers != null && !webSecurityConfigurers.isEmpty();
		if (!hasConfigurers) {
			WebSecurityConfigurerAdapter adapter = objectObjectPostProcessor.postProcess(new WebSecurityConfigurerAdapter() {});
			webSecurity.apply(adapter);
		}
		return webSecurity.build(); // 核心步骤
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
		Object previousConfig = null;
		
		// 优先级不能相同
		for (SecurityConfigurer<Filter, WebSecurity> config : webSecurityConfigurers) {
			Integer order = AnnotationAwareOrderComparator.lookupOrder(config);
			if (previousOrder != null && previousOrder.equals(order)) {
				throw new IllegalStateException(
						"@Order on WebSecurityConfigurers must be unique. Order of "
								+ order + " was already used on " + previousConfig + ", so it cannot be used on "
								+ config + " too.");
			}
			previousOrder = order;
			previousConfig = config;
		}
		 // 保存 SecurityConfigurer 到 WebSecurity 父类的 AbstractConfiguredSecurityBuilder 中 configurers Map中
		for (SecurityConfigurer<Filter, WebSecurity> webSecurityConfigurer : webSecurityConfigurers) {
			webSecurity.apply(webSecurityConfigurer);
		}
		// 保存 webSecurityConfigurers 在当前类
		this.webSecurityConfigurers = webSecurityConfigurers;
	}

	@Bean
	public static BeanFactoryPostProcessor conversionServicePostProcessor() {
		return new RsaKeyConversionServicePostProcessor();
	}

	@Bean
	public static AutowiredWebSecurityConfigurersIgnoreParents autowiredWebSecurityConfigurersIgnoreParents(
			ConfigurableListableBeanFactory beanFactory) {
		return new AutowiredWebSecurityConfigurersIgnoreParents(beanFactory);
	}
	
	// Spring ImportAware 接口提供, 可以检查到使用Import注入当前类的属性信息,这里使用@EnableWebSecurity提供导入的
	// 所以这里能看到EnableWebSecurity的相关信息,主要是是否开启debug模式
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		Map<String, Object> enableWebSecurityAttrMap = importMetadata
				.getAnnotationAttributes(EnableWebSecurity.class.getName());
		AnnotationAttributes enableWebSecurityAttrs = AnnotationAttributes
				.fromMap(enableWebSecurityAttrMap);
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

从这里看出 WebSecurity 内至少会有一个webSecurityConfigurer, 一般就是WebSecurityConfigurerAdapter,或者是我们开发者继承这个类的子类.
具体WebSecurity的对象内容将单独讲解.

# EnableGlobalAuthentication

```java
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = { java.lang.annotation.ElementType.TYPE })
@Documented
@Import(AuthenticationConfiguration.class) // [重点]
@Configuration
public @interface EnableGlobalAuthentication {
}
```

## AuthenticationConfiguration

- 重点 authenticationManagerBuilder() 方法, 注入了 AuthenticationManagerBuilder 对象到容器
- 还有 getAuthenticationManager() 方法,可以看出,所有属性都是为了这个方法的材料. 在 WebSecurity 会用到,也有可能不调用该方法,用不到的时候,就是自己从容器中获取AuthenticationManagerBuilder,自定义build的过程来替代该方法.

```java
@Configuration(proxyBeanMethods = false)
@Import(ObjectPostProcessorConfiguration.class) // Spring提供的工具,可以将已有的对象,注入到Spring容器中.
public class AuthenticationConfiguration {

	private AtomicBoolean buildingAuthenticationManager = new AtomicBoolean();

	private ApplicationContext applicationContext;

	private AuthenticationManager authenticationManager;

	private boolean authenticationManagerInitialized;

	private List<GlobalAuthenticationConfigurerAdapter> globalAuthConfigurers = Collections.emptyList();

	private ObjectPostProcessor<Object> objectPostProcessor;

    // [1]
	@Bean
	public AuthenticationManagerBuilder authenticationManagerBuilder(
			ObjectPostProcessor<Object> objectPostProcessor, ApplicationContext context) {
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

	@Bean
	public static GlobalAuthenticationConfigurerAdapter enableGlobalAuthenticationAutowiredConfigurer(
			ApplicationContext context) {
		return new EnableGlobalAuthenticationAutowiredConfigurer(context);
	}

	@Bean
	public static InitializeUserDetailsBeanManagerConfigurer initializeUserDetailsBeanManagerConfigurer(ApplicationContext context) {
		return new InitializeUserDetailsBeanManagerConfigurer(context);
	}

	@Bean
	public static InitializeAuthenticationProviderBeanManagerConfigurer initializeAuthenticationProviderBeanManagerConfigurer(ApplicationContext context) {
		return new InitializeAuthenticationProviderBeanManagerConfigurer(context);
	}

	@Autowired(required = false)
	public void setGlobalAuthenticationConfigurers(
			List<GlobalAuthenticationConfigurerAdapter> configurers) {
		configurers.sort(AnnotationAwareOrderComparator.INSTANCE);
		this.globalAuthConfigurers = configurers;
	}

	@Autowired
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Autowired
	public void setObjectPostProcessor(ObjectPostProcessor<Object> objectPostProcessor) {
		this.objectPostProcessor = objectPostProcessor;
	}

	@SuppressWarnings("unchecked")
	private <T> T lazyBean(Class<T> interfaceName) {
		LazyInitTargetSource lazyTargetSource = new LazyInitTargetSource();
		String[] beanNamesForType = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
				applicationContext, interfaceName);
		if (beanNamesForType.length == 0) {
			return null;
		}
		String beanName;
		if (beanNamesForType.length > 1) {
			List<String> primaryBeanNames = getPrimaryBeanNames(beanNamesForType);

			Assert.isTrue(primaryBeanNames.size() != 0, () -> "Found " + beanNamesForType.length
					+ " beans for type " + interfaceName + ", but none marked as primary");
			Assert.isTrue(primaryBeanNames.size() == 1, () -> "Found " + primaryBeanNames.size()
					+ " beans for type " + interfaceName + " marked as primary");
			beanName = primaryBeanNames.get(0);
		} else {
			beanName = beanNamesForType[0];
		}

		lazyTargetSource.setTargetBeanName(beanName);
		lazyTargetSource.setBeanFactory(applicationContext);
		ProxyFactoryBean proxyFactory = new ProxyFactoryBean();
		proxyFactory = objectPostProcessor.postProcess(proxyFactory);
		proxyFactory.setTargetSource(lazyTargetSource);
		return (T) proxyFactory.getObject();
	}

	private List<String> getPrimaryBeanNames(String[] beanNamesForType) {
		List<String> list = new ArrayList<>();
		if (!(applicationContext instanceof ConfigurableApplicationContext)) {
			return Collections.emptyList();
		}
		for (String beanName : beanNamesForType) {
			if (((ConfigurableApplicationContext) applicationContext).getBeanFactory()
					.getBeanDefinition(beanName).isPrimary()) {
				list.add(beanName);
			}
		}
		return list;
	}

	private AuthenticationManager getAuthenticationManagerBean() {
		return lazyBean(AuthenticationManager.class);
	}

	private static class EnableGlobalAuthenticationAutowiredConfigurer extends
			GlobalAuthenticationConfigurerAdapter {
		private final ApplicationContext context;
		private static final Log logger = LogFactory
				.getLog(EnableGlobalAuthenticationAutowiredConfigurer.class);

		EnableGlobalAuthenticationAutowiredConfigurer(ApplicationContext context) {
			this.context = context;
		}

		@Override
		public void init(AuthenticationManagerBuilder auth) {
			Map<String, Object> beansWithAnnotation = context
					.getBeansWithAnnotation(EnableGlobalAuthentication.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Eagerly initializing " + beansWithAnnotation);
			}
		}
	}

	static final class AuthenticationManagerDelegator implements AuthenticationManager {
		private AuthenticationManagerBuilder delegateBuilder;
		private AuthenticationManager delegate;
		private final Object delegateMonitor = new Object();

		AuthenticationManagerDelegator(AuthenticationManagerBuilder delegateBuilder) {
			Assert.notNull(delegateBuilder, "delegateBuilder cannot be null");
			this.delegateBuilder = delegateBuilder;
		}

		@Override
		public Authentication authenticate(Authentication authentication)
				throws AuthenticationException {
			if (this.delegate != null) {
				return this.delegate.authenticate(authentication);
			}

			synchronized (this.delegateMonitor) {
				if (this.delegate == null) {
					this.delegate = this.delegateBuilder.getObject();
					this.delegateBuilder = null;
				}
			}

			return this.delegate.authenticate(authentication);
		}

		@Override
		public String toString() {
			return "AuthenticationManagerDelegator [delegate=" + this.delegate + "]";
		}
	}

	static class DefaultPasswordEncoderAuthenticationManagerBuilder extends AuthenticationManagerBuilder {
		private PasswordEncoder defaultPasswordEncoder;

		DefaultPasswordEncoderAuthenticationManagerBuilder(
			ObjectPostProcessor<Object> objectPostProcessor, PasswordEncoder defaultPasswordEncoder) {
			super(objectPostProcessor);
			this.defaultPasswordEncoder = defaultPasswordEncoder;
		}

		@Override
		public InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> inMemoryAuthentication()
			throws Exception {
			return super.inMemoryAuthentication()
				.passwordEncoder(this.defaultPasswordEncoder);
		}

		@Override
		public JdbcUserDetailsManagerConfigurer<AuthenticationManagerBuilder> jdbcAuthentication()
			throws Exception {
			return super.jdbcAuthentication()
				.passwordEncoder(this.defaultPasswordEncoder);
		}

		@Override
		public <T extends UserDetailsService> DaoAuthenticationConfigurer<AuthenticationManagerBuilder, T> userDetailsService(
			T userDetailsService) throws Exception {
			return super.userDetailsService(userDetailsService)
				.passwordEncoder(this.defaultPasswordEncoder);
		}
	}

	static class LazyPasswordEncoder implements PasswordEncoder {
		private ApplicationContext applicationContext;
		private PasswordEncoder passwordEncoder;

		LazyPasswordEncoder(ApplicationContext applicationContext) {
			this.applicationContext = applicationContext;
		}

		@Override
		public String encode(CharSequence rawPassword) {
			return getPasswordEncoder().encode(rawPassword);
		}

		@Override
		public boolean matches(CharSequence rawPassword,
			String encodedPassword) {
			return getPasswordEncoder().matches(rawPassword, encodedPassword);
		}

		@Override
		public boolean upgradeEncoding(String encodedPassword) {
			return getPasswordEncoder().upgradeEncoding(encodedPassword);
		}

		private PasswordEncoder getPasswordEncoder() {
			if (this.passwordEncoder != null) {
				return this.passwordEncoder;
			}
			PasswordEncoder passwordEncoder = getBeanOrNull(this.applicationContext, PasswordEncoder.class);
			if (passwordEncoder == null) {
				passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
			}
			this.passwordEncoder = passwordEncoder;
			return passwordEncoder;
		}

		@Override
		public String toString() {
			return getPasswordEncoder().toString();
		}
	}
}
```

# SpringWebMvcImportSelector

Servlet 环境下,导入 WebMvcSecurityConfiguration 配置

```java
class SpringWebMvcImportSelector implements ImportSelector {
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		boolean webmvcPresent = ClassUtils.isPresent(
				"org.springframework.web.servlet.DispatcherServlet",
				getClass().getClassLoader());
		return webmvcPresent
				? new String[] { "org.springframework.security.config.annotation.web.configuration.WebMvcSecurityConfiguration" }
				: new String[] {};
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
		argumentResolvers
				.add(new org.springframework.security.web.bind.support.AuthenticationPrincipalArgumentResolver());

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

		boolean oauth2ClientPresent = ClassUtils.isPresent(
				"org.springframework.security.oauth2.client.registration.ClientRegistration", getClass().getClassLoader());
		if (oauth2ClientPresent) {
			imports.add("org.springframework.security.config.annotation.web.configuration.OAuth2ClientConfiguration");
		}

		boolean webfluxPresent = ClassUtils.isPresent(
				"org.springframework.web.reactive.function.client.ExchangeFilterFunction", getClass().getClassLoader());
		if (webfluxPresent && oauth2ClientPresent) {
			imports.add("org.springframework.security.config.annotation.web.configuration.SecurityReactorContextConfiguration");
		}

		boolean oauth2ResourceServerPresent = ClassUtils.isPresent(
				"org.springframework.security.oauth2.server.resource.BearerTokenError", getClass().getClassLoader());
		if (webfluxPresent && oauth2ResourceServerPresent) {
			imports.add("org.springframework.security.config.annotation.web.configuration.SecurityReactorContextConfiguration");
		}

		return imports.toArray(new String[0]);
	}
}

```

