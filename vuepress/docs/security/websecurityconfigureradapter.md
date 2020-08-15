# WebSecurityConfigurerAdapter

[<< 目录](/security/README.md)

![WebSecurityConfigurerAdapter](/WebSecurityConfigurerAdapter.png)

## SecurityBuilder

泛型O: 需要构建的类型

```java
public interface SecurityBuilder<O> {
	O build() throws Exception;
}
```

## SecurityConfigurer

泛型O: 由 SecurityBuilder 构建的对象

泛型B: 正在配置的 SecurityBuilder。
 
```java
public interface SecurityConfigurer<O, B extends SecurityBuilder<O>> {

	void init(B builder) throws Exception;

	void configure(B builder) throws Exception;
}
```

## WebSecurityConfigurer

具体化泛型类型

```java
public interface WebSecurityConfigurer<T extends SecurityBuilder<Filter>> extends
		SecurityConfigurer<Filter, T> {

}
```

## WebSecurityConfigurerAdapter

适配器,提供了默认实现,对WebSecurity进行了默认配置,并创建了一个 HttpSecurity,并进行了默认配置.

这也就是为什么 Spring Security 不需要进行配置就可以使用的原因

### init

从这里看出 WebSecurity 是可以支持多个 HttpSecurity, 也就是多个 WebSecurityConfigurerAdapter,
很少会遇到这种情况, 一般就一个.

```java
public void init(final WebSecurity web) throws Exception {
    final HttpSecurity http = getHttp(); // 创建 HttpSecurity

    web.addSecurityFilterChainBuilder(http) // 添加到 WebSecurity
        .postBuildAction(() -> {
            FilterSecurityInterceptor securityInterceptor = http 
                .getSharedObject(FilterSecurityInterceptor.class);
            web.securityInterceptor(securityInterceptor);
    });
}
```

### getHttp

```java
protected final HttpSecurity getHttp() throws Exception {
    if (http != null) {
        return http;
    }

    AuthenticationEventPublisher eventPublisher = getAuthenticationEventPublisher();
    localConfigureAuthenticationBldr.authenticationEventPublisher(eventPublisher);

    // 全局 AuthenticationManager
    AuthenticationManager authenticationManager = authenticationManager(); 
    // 设置父级
    authenticationBuilder.parentAuthenticationManager(authenticationManager);
    // 缓存一些对象
    Map<Class<?>, Object> sharedObjects = createSharedObjects();
    
    http = new HttpSecurity(objectPostProcessor, authenticationBuilder,
            sharedObjects);

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
        List<AbstractHttpConfigurer> defaultHttpConfigurers =
                SpringFactoriesLoader.loadFactories(AbstractHttpConfigurer.class, classLoader);

        for (AbstractHttpConfigurer configurer : defaultHttpConfigurers) {
            http.apply(configurer);
        }
    }

    configure(http); // 自定义路口

    return http;
}

//  一般我们就需要覆盖这个方法, 默认启动表单登录和Basic登录
protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests().anyRequest().authenticated().and()
        .formLogin().and()
        .httpBasic();
}
```

### authenticationManager

在创建 HttpSecurity 时, 传入 AuthenticationManager, 因为 AuthenticationManager 支持父子关系,所以这里也提供了父级的配置

```java
// 这是一个很精巧的设计, 子类覆盖该方法,就意味着使用了自定义的全局AuthenticationManager,
// disableLocalConfigureAuthenticationBldr 就是默认值 false
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    this.disableLocalConfigureAuthenticationBldr = true;
}

protected AuthenticationManager authenticationManager() throws Exception {
    if (!authenticationManagerInitialized) {
        configure(localConfigureAuthenticationBldr);
        if (disableLocalConfigureAuthenticationBldr) { // 是否自定义了全局 AuthenticationManager
            authenticationManager = authenticationConfiguration
                    .getAuthenticationManager();  // 获取默认的全局 AuthenticationManager
        }
        else {
            // 构建自定义的全局AuthenticationManager
            authenticationManager = localConfigureAuthenticationBldr.build();
        }
        authenticationManagerInitialized = true;
    }
    return authenticationManager;
}
```