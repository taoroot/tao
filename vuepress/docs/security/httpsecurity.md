# HttpSecurity

[<< 目录](/security/README.md)

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
