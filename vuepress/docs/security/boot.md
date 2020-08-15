# SecurityAutoConfiguration

[<< 目录](/security/README.md)

在[过滤器一章](/security/filter)已说明, 自动装配的最终目的是创建 FilterChainProxy 和 SecurityFilterChain.

FilterChainProxy 的创建委托给 WebSecurity. 在 Spring 容器中叫 springSecurityFilterChain

SecurityFilterChain 的创建委托给 HttpSecurity. 因为 Security 支持多个 SecurityFilterChain, 所以可以支持多个 HttpSecurity.

Security 会收集 Spring 容器中 WebSecurityConfigurer 集合,并传入内部创建好的 WebSecurity 用以自定义配置, 例如传入创建 HttpSecurity 并传入.

为了简化初始化,提供了 WebSecurityConfigurerAdapter , 内部会创建一个 HttpSecurity 并注入 WebSecurity, 当然也提供了方法对这个 HttpSecurity 进行后置配置. 
另外还有对 AuthenticationManager 的配置. AuthenticationManager 支持父子关系, 因此还单独提供了一个方法配置一个父级的 AuthenticationManager, 内部创建的父级会在当外部提供后失效.

下面开始分析代码

::: tip 自动装配
- SecurityAutoConfiguration
   - SpringBootWebSecurityConfiguration # WebSecurityConfigurerAdapter 导入
   - WebSecurityEnablerConfiguration
      - @EnableWebSecurity
         - WebSecurityConfiguration # WebSecurity 配置
         - SpringWebMvcImportSelector # mvc 配置
         - OAuth2ImportSelector # OAuth2 配置
         - @EnableGlobalAuthentication 
           - AuthenticationConfiguration # 默认全局 AuthenticationManager 配置
	- SecurityDataConfiguration # Spring Data 配置
:::

```java
@Import({ SpringBootWebSecurityConfiguration.class, // 提供默认 WebSecurityConfigurerAdapter
	WebSecurityEnablerConfiguration.class)} // 核心配置
public class SecurityAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(AuthenticationEventPublisher.class) 
	public DefaultAuthenticationEventPublisher authenticationEventPublisher(
			ApplicationEventPublisher publisher) {
		return new DefaultAuthenticationEventPublisher(publisher); // 定义事件发布器
	}

}
```

#### SpringBootWebSecurityConfiguration

当用户没有自定义情况下, 提供一个默认的 WebSecurityConfigurerAdapter

```java
public class SpringBootWebSecurityConfiguration {

	@Configuration(proxyBeanMethods = false)
	static class DefaultConfigurerAdapter extends WebSecurityConfigurerAdapter {}

}
```

#### WebSecurityEnablerConfiguration

引入核心注解 @EnableWebSecurity

```java
@EnableWebSecurity
public class WebSecurityEnablerConfiguration {
}
```

#### EnableWebSecurity
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

#### EnableGlobalAuthentication

```java
@Import(AuthenticationConfiguration.class) // [重点]
@Configuration
public @interface EnableGlobalAuthentication {
}
```


## AuthenticationConfiguration

::: tip 默认全局 AuthenticationManager 配置
- [1] authenticationManagerBuilder(): 注入了 AuthenticationManagerBuilder 对象到容器
- [2] getAuthenticationManager(): 所有属性都是这个方法的材料. 在 WebSecurity 会用到, 用不到的时候就是从容器中获取自定义AuthenticationManagerBuilder.build的过程来替代该方法.
:::

```java
@Configuration(proxyBeanMethods = false)
@Import(ObjectPostProcessorConfiguration.class)
public class AuthenticationConfiguration {
    
	@Bean // [1]
	public AuthenticationManagerBuilder authenticationManagerBuilder(
				ObjectPostProcessor<Object> objectPostProcessor, ApplicationContext context) {

		LazyPasswordEncoder defaultPasswordEncoder = new LazyPasswordEncoder(context);

		AuthenticationEventPublisher authenticationEventPublisher = getBeanOrNull(context, 
				AuthenticationEventPublisher.class);

		DefaultPasswordEncoderAuthenticationManagerBuilder result = 
				new DefaultPasswordEncoderAuthenticationManagerBuilder(
						objectPostProcessor, defaultPasswordEncoder);

		if (authenticationEventPublisher != null) {
			result.authenticationEventPublisher(authenticationEventPublisher);
		}

		return result;
	}

    // [2]
	public AuthenticationManager getAuthenticationManager() throws Exception {

		if (this.authenticationManagerInitialized) { // 是否已初始化
			return this.authenticationManager;
		}

		AuthenticationManagerBuilder authBuilder = this.applicationContext.getBean(
			AuthenticationManagerBuilder.class); // 从容器中获取(默认就是上面那个Bean)

		if (this.buildingAuthenticationManager.getAndSet(true)) {
			return new AuthenticationManagerDelegator(authBuilder); 
		}

		for (GlobalAuthenticationConfigurerAdapter config : globalAuthConfigurers) {
			authBuilder.apply(config); // 默认就是下面声明的三个Bean
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
	public static InitializeUserDetailsBeanManagerConfigurer initializeUserDetailsBeanManagerConfigurer(
			ApplicationContext context) {
		return new InitializeUserDetailsBeanManagerConfigurer(context);
	}

	@Bean
	public static InitializeAuthenticationProviderBeanManagerConfigurer initializeAuthenticationProviderBeanManagerConfigurer(
			ApplicationContext context) {
		return new InitializeAuthenticationProviderBeanManagerConfigurer(context);
	}
}
```

## WebSecurityConfiguration

::: tip WebSecurity 配置
1. 先调用 setFilterChainProxySecurityConfigurer(), 创建初始化 WebSecurity, 并传入 WebSecurityConfigurer
2. 再调用 springSecurityFilterChain(), 利用 WebSecurity中的WebSecurityConfigurer 完成对 FilterChainProxy 和 SecurityFilterChain 创建与初始化
:::

```java

// [1] 收集 <SecurityConfigurer<FilterChainProxy, WebSecurityBuilder> 实例,
// 并进行排序后, 用以创建并初始化 WebSecurity, 主要就有 WebSecurityConfigurerAdapter 
@Autowired(required = false)
public void setFilterChainProxySecurityConfigurer(
		ObjectPostProcessor<Object> objectPostProcessor, 
		@Value("#{@autowiredWebSecurityConfigurersIgnoreParents.getWebSecurityConfigurers()}") 
			List<SecurityConfigurer<Filter, WebSecurity>> webSecurityConfigurers) 
		throws Exception {
		
	// 创建 WebSecurity, 并手动托管给Spring
	webSecurity = objectPostProcessor.postProcess(new WebSecurity(objectPostProcessor));

	webSecurityConfigurers.sort(AnnotationAwareOrderComparator.INSTANCE); // 对配置排序

	Integer previousOrder = null;

	for (SecurityConfigurer<Filter, WebSecurity> config : webSecurityConfigurers) {
		Integer order = AnnotationAwareOrderComparator.lookupOrder(config);
		if (previousOrder != null && previousOrder.equals(order)) {
			throw new IllegalStateException("优先级不能相同");
		}
		previousOrder = order;
	}
	
	// 保存 SecurityConfigurer 到 WebSecurity 
	for (SecurityConfigurer<Filter, WebSecurity> webSecurityConfigurer : webSecurityConfigurers) {
		webSecurity.apply(webSecurityConfigurer);
	}
	
	this.webSecurityConfigurers = webSecurityConfigurers;
}	
	

// [2] 创建并初始化 FilterChainProxy
@Bean(name = AbstractSecurityWebApplicationInitializer.DEFAULT_FILTER_NAME)
public Filter springSecurityFilterChain() throws Exception {
	
	// 默认至少有一个setFilterChainProxySecurityConfigurer()
	boolean hasConfigurers = webSecurityConfigurers != null 
		&& !webSecurityConfigurers.isEmpty();
	
	if (!hasConfigurers) {
		WebSecurityConfigurerAdapter adapter = objectObjectPostProcessor
			.postProcess(new WebSecurityConfigurerAdapter() {});
		webSecurity.apply(adapter);
	}
	
	return webSecurity.build(); // 核心步骤 构建 FilterChainProxy
}

```
