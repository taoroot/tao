# AuthenticationManagerBuilder

[<< 目录](/security/README.md)


故名思意,AuthenticationManagerBuilder 是用来构建 AuthenticationManager 的, 那 AuthenticationManager 又是什么呢?

AuthenticationManager 是定义 SpringSecurityFilter 如何执行身份验证的API。通过调用 AuthenticationManager, 在 securitycontext 上设置返回的身份验证。
如果你不结合 SpringSecurityFilter 可以直接设置 SecurityContextHolder 而不需要使用 AuthenticationManager

AuthenticationManager 最常见的实现是 ProviderManager, AuthenticationManagerBuilder 就是用来构建 ProviderManager 的

而 ProviderManager 委托给内部的 AuthenticationProvider 完成认证. 如下图所示:

![providermanager](/providermanager.png)

## AuthenticationProvider

```java
public interface AuthenticationProvider {

	// 验证用户身份
	Authentication authenticate(Authentication authentication)
			throws AuthenticationException;

	// 是否支持当前Authentication类型认证
	// 只对自己感兴趣的认证方式进行认证
	boolean supports(Class<?> authentication);
}
```

因为 AuthenticationManager 具有父子层级关系, 在自身无法验证的情况下,会委托给父节点验证, 多个子节点可以拥有同一个父节点. 如下图所示: 

![providermanagers-parent](/providermanagers-parent.png)

::: tip 
如果对 AuthenticationManager 和 ProviderManager 陌生, 可以先看看 [ Authentication 一文 ](/security/authentication.md)
:::

![AuthenticationManagerBuilder](/AuthenticationManagerBuilder.png)

::: tip 源码分析要点
1. 设置的 Parent AuthenticationManager
2. 设置 ProviderManager
3. 设置 默认 UserDetailsService, 即 defaultUserDetailsService
:::

```java
public class AuthenticationManagerBuilder extends
		AbstractConfiguredSecurityBuilder<AuthenticationManager, AuthenticationManagerBuilder>
		implements ProviderManagerBuilder<AuthenticationManagerBuilder> {

	private AuthenticationManager parentAuthenticationManager;
	private List<AuthenticationProvider> authenticationProviders = new ArrayList<>();
	private Boolean eraseCredentials;
	private AuthenticationEventPublisher eventPublisher;

	// [1]
	// 通过构造函数传入父类 AuthenticationManager, 并保存到属性 parentAuthenticationManager
	// parentAuthenticationManager 默认也是 ProviderManager
	public AuthenticationManagerBuilder parentAuthenticationManager(
			AuthenticationManager authenticationManager) {
				
		if (authenticationManager instanceof ProviderManager) {
			eraseCredentials(((ProviderManager) authenticationManager)
					.isEraseCredentialsAfterAuthentication());
		}

		this.parentAuthenticationManager = authenticationManager;

		return this;
	}

	// [2]
	@Override
	protected ProviderManager performBuild() throws Exception {
		
		// 没有authenticationProviders 或者是 parentAuthenticationManager, 将返回 null.
		// !authenticationProviders.isEmpty() || parentAuthenticationManager != null;
		if (!isConfigured()) {
			return null;
		}
		
		// 构建 ProviderManager
		ProviderManager providerManager = new ProviderManager(authenticationProviders,
				parentAuthenticationManager);

		// 设置是否认证过后,清除内存中的密码
		// 一般清除就好,不过有些认证方式,每次都要匹配密码,那么就不能清除了
		if (eraseCredentials != null) {
			providerManager.setEraseCredentialsAfterAuthentication(eraseCredentials);
		}

		// 事件广播器
		if (eventPublisher != null) {
			providerManager.setAuthenticationEventPublisher(eventPublisher);
		}

		// 托管到 Spring 容器中.
		providerManager = postProcess(providerManager);

		return providerManager;
	}
}
```

## 配置

关于 AbstractConfiguredSecurityBuilder 加载 Configurer 流程, 见 [WebSecurity 源码分析](/security/websecurity.md)

默认提供了基于内存, 基于jdbc, 基于ldap 三种方式, 还有一个自定义通过userDetailsService加载配置

```java
// 注意, 这个是私有方法, 所以只有下面四种方式.
private <C extends UserDetailsAwareConfigurer<AuthenticationManagerBuilder, ? extends UserDetailsService>> 
		C apply(C configurer) throws Exception {
	this.defaultUserDetailsService = configurer.getUserDetailsService();
	return super.apply(configurer);
}

// [1] 基于内存
public InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> inMemoryAuthentication()
		throws Exception {
	return apply(new InMemoryUserDetailsManagerConfigurer<>());
}

// [2] 基于jdbc
public JdbcUserDetailsManagerConfigurer<AuthenticationManagerBuilder> jdbcAuthentication()
		throws Exception {
	return apply(new JdbcUserDetailsManagerConfigurer<>());
}

// [3] 基于ldap
public LdapAuthenticationProviderConfigurer<AuthenticationManagerBuilder> ldapAuthentication()
		throws Exception {
	return apply(new LdapAuthenticationProviderConfigurer<>());
}

// [4] 基于 DaoAuthenticationConfigurer 自定义
public <T extends UserDetailsService> DaoAuthenticationConfigurer<AuthenticationManagerBuilder, T> userDetailsService(
		T userDetailsService) throws Exception {
	this.defaultUserDetailsService = userDetailsService;
	return apply(new DaoAuthenticationConfigurer<>(userDetailsService));
}
```

### 继承关系图

![AbstractDaoAuthenticationConfigurer](/AbstractDaoAuthenticationConfigurer.png)

### DaoAuthenticationConfigurer

```java
public class DaoAuthenticationConfigurer<B extends ProviderManagerBuilder<B>, U extends UserDetailsService>
		extends AbstractDaoAuthenticationConfigurer<B, DaoAuthenticationConfigurer<B, U>, U> {

	public DaoAuthenticationConfigurer(U userDetailsService) {
		super(userDetailsService);
	}
}
```

### AbstractDaoAuthenticationConfigurer


```java
abstract class AbstractDaoAuthenticationConfigurer<B extends ProviderManagerBuilder<B>, C extends AbstractDaoAuthenticationConfigurer<B, C, U>, U extends UserDetailsService>
		extends UserDetailsAwareConfigurer<B, U> {
	private DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
	private final U userDetailsService;

	protected AbstractDaoAuthenticationConfigurer(U userDetailsService) {
		this.userDetailsService = userDetailsService;
		provider.setUserDetailsService(userDetailsService);
		if (userDetailsService instanceof UserDetailsPasswordService) {
			this.provider.setUserDetailsPasswordService((UserDetailsPasswordService) userDetailsService);
		}
	}

	@SuppressWarnings("unchecked")
	public C withObjectPostProcessor(ObjectPostProcessor<?> objectPostProcessor) {
		addObjectPostProcessor(objectPostProcessor);
		return (C) this;
	}

	@SuppressWarnings("unchecked")
	public C passwordEncoder(PasswordEncoder passwordEncoder) {
		provider.setPasswordEncoder(passwordEncoder);
		return (C) this;
	}

	public C userDetailsPasswordManager(UserDetailsPasswordService passwordManager) {
		provider.setUserDetailsPasswordService(passwordManager);
		return (C) this;
	}

	@Override
	public void configure(B builder) throws Exception {
		provider = postProcess(provider);
		builder.authenticationProvider(provider);
	}

	public U getUserDetailsService() {
		return userDetailsService;
	}
}
```

### DaoAuthenticationConfigurer

```java
public class DaoAuthenticationConfigurer<B extends ProviderManagerBuilder<B>, U extends UserDetailsService>
		extends AbstractDaoAuthenticationConfigurer<B, DaoAuthenticationConfigurer<B, U>, U> {

	public DaoAuthenticationConfigurer(U userDetailsService) {
		super(userDetailsService);
	}
}
```

### UserDetailsAwareConfigurer

```java
public abstract class UserDetailsAwareConfigurer<B extends ProviderManagerBuilder<B>, U extends UserDetailsService>
		extends SecurityConfigurerAdapter<AuthenticationManager, B> {

	public abstract U getUserDetailsService();
}
```

#### InMemoryUserDetailsManagerConfigurer

```java
public class InMemoryUserDetailsManagerConfigurer<B extends ProviderManagerBuilder<B>>
		extends UserDetailsManagerConfigurer<B, InMemoryUserDetailsManagerConfigurer<B>> {

	public InMemoryUserDetailsManagerConfigurer() {
		super(new InMemoryUserDetailsManager(new ArrayList<>()));
	}
}
```

#### JdbcUserDetailsManagerConfigurer

```java
public class JdbcUserDetailsManagerConfigurer<B extends ProviderManagerBuilder<B>>
		extends UserDetailsManagerConfigurer<B, JdbcUserDetailsManagerConfigurer<B>> {

	private DataSource dataSource;

	private List<Resource> initScripts = new ArrayList<>();

	public JdbcUserDetailsManagerConfigurer(JdbcUserDetailsManager manager) {
		super(manager);
	}

	public JdbcUserDetailsManagerConfigurer() {
		this(new JdbcUserDetailsManager());
	}

	public JdbcUserDetailsManagerConfigurer<B> dataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		getUserDetailsService().setDataSource(dataSource);
		return this;
	}

	public JdbcUserDetailsManagerConfigurer<B> usersByUsernameQuery(String query) {
		getUserDetailsService().setUsersByUsernameQuery(query);
		return this;
	}

	public JdbcUserDetailsManagerConfigurer<B> authoritiesByUsernameQuery(String query) {
		getUserDetailsService().setAuthoritiesByUsernameQuery(query);
		return this;
	}

	public JdbcUserDetailsManagerConfigurer<B> groupAuthoritiesByUsername(String query) {
		JdbcUserDetailsManager userDetailsService = getUserDetailsService();
		userDetailsService.setEnableGroups(true);
		userDetailsService.setGroupAuthoritiesByUsernameQuery(query);
		return this;
	}

	public JdbcUserDetailsManagerConfigurer<B> rolePrefix(String rolePrefix) {
		getUserDetailsService().setRolePrefix(rolePrefix);
		return this;
	}

	public JdbcUserDetailsManagerConfigurer<B> userCache(UserCache userCache) {
		getUserDetailsService().setUserCache(userCache);
		return this;
	}

	@Override
	protected void initUserDetailsService() throws Exception {
		if (!initScripts.isEmpty()) {
			getDataSourceInit().afterPropertiesSet();
		}
		super.initUserDetailsService();
	}

	@Override
	public JdbcUserDetailsManager getUserDetailsService() {
		return (JdbcUserDetailsManager) super.getUserDetailsService();
	}

	public JdbcUserDetailsManagerConfigurer<B> withDefaultSchema() {
		this.initScripts.add(new ClassPathResource(
				"org/springframework/security/core/userdetails/jdbc/users.ddl"));
		return this;
	}

	protected DatabasePopulator getDatabasePopulator() {
		ResourceDatabasePopulator dbp = new ResourceDatabasePopulator();
		dbp.setScripts(initScripts.toArray(new Resource[0]));
		return dbp;
	}

	private DataSourceInitializer getDataSourceInit() {
		DataSourceInitializer dsi = new DataSourceInitializer();
		dsi.setDatabasePopulator(getDatabasePopulator());
		dsi.setDataSource(dataSource);
		return dsi;
	}
}
```