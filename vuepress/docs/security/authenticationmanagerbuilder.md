# AuthenticationManagerBuilder

[<< 目录](/security/README.md)

![AuthenticationManagerBuilder](/AuthenticationManagerBuilder.png)

AuthenticationManager 是定义Spring安全过滤器如何执行身份验证的API。然后，调用 AuthenticationManager 的控制器(即Spring Security的过滤器)在securitycontext上设置返回的身份验证。
如果你不结合 Spring Security 的过滤器可以设置 SecurityContextHolder 直接和不需要使用 AuthenticationManager

虽然AuthenticationManager的实现可以是任何东西，但最常见的实现是ProviderManager。

```java
public class AuthenticationManagerBuilder extends
		AbstractConfiguredSecurityBuilder<AuthenticationManager, AuthenticationManagerBuilder>
		implements ProviderManagerBuilder<AuthenticationManagerBuilder> {

	private AuthenticationManager parentAuthenticationManager;
	private List<AuthenticationProvider> authenticationProviders = new ArrayList<>();
	private UserDetailsService defaultUserDetailsService;
	private Boolean eraseCredentials;
	private AuthenticationEventPublisher eventPublisher;

	public AuthenticationManagerBuilder parentAuthenticationManager(
			AuthenticationManager authenticationManager) {
		if (authenticationManager instanceof ProviderManager) {
			eraseCredentials(((ProviderManager) authenticationManager)
					.isEraseCredentialsAfterAuthentication());
		}
		this.parentAuthenticationManager = authenticationManager;
		return this;
	}

	public AuthenticationManagerBuilder authenticationEventPublisher(
			AuthenticationEventPublisher eventPublisher) {
		Assert.notNull(eventPublisher, "AuthenticationEventPublisher cannot be null");
		this.eventPublisher = eventPublisher;
		return this;
	}

	public InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> inMemoryAuthentication()
			throws Exception {
		return apply(new InMemoryUserDetailsManagerConfigurer<>());
	}

	public JdbcUserDetailsManagerConfigurer<AuthenticationManagerBuilder> jdbcAuthentication()
			throws Exception {
		return apply(new JdbcUserDetailsManagerConfigurer<>());
	}

	public <T extends UserDetailsService> DaoAuthenticationConfigurer<AuthenticationManagerBuilder, T> userDetailsService(
			T userDetailsService) throws Exception {
		this.defaultUserDetailsService = userDetailsService;
		return apply(new DaoAuthenticationConfigurer<>(
				userDetailsService));
	}

	public AuthenticationManagerBuilder authenticationProvider(
			AuthenticationProvider authenticationProvider) {
		this.authenticationProviders.add(authenticationProvider);
		return this;
	}

	@Override
	protected ProviderManager performBuild() throws Exception {
		if (!isConfigured()) {
			logger.debug("No authenticationProviders and no parentAuthenticationManager defined. Returning null.");
			return null;
		}
		ProviderManager providerManager = new ProviderManager(authenticationProviders,
				parentAuthenticationManager);
		if (eraseCredentials != null) {
			providerManager.setEraseCredentialsAfterAuthentication(eraseCredentials);
		}
		if (eventPublisher != null) {
			providerManager.setAuthenticationEventPublisher(eventPublisher);
		}
		providerManager = postProcess(providerManager);
		return providerManager;
	}

	private <C extends UserDetailsAwareConfigurer<AuthenticationManagerBuilder, ? extends UserDetailsService>> C apply(
			C configurer) throws Exception {
		this.defaultUserDetailsService = configurer.getUserDetailsService();
		return super.apply(configurer);
	}
}
```

## InMemoryUserDetailsManagerConfigurer

```java
public class InMemoryUserDetailsManagerConfigurer<B extends ProviderManagerBuilder<B>>
		extends UserDetailsManagerConfigurer<B, InMemoryUserDetailsManagerConfigurer<B>> {

	public InMemoryUserDetailsManagerConfigurer() {
		super(new InMemoryUserDetailsManager(new ArrayList<>()));
	}
}
```
```java
public class InMemoryUserDetailsManager implements UserDetailsManager,
		UserDetailsPasswordService {
	protected final Log logger = LogFactory.getLog(getClass());

	private final Map<String, MutableUserDetails> users = new HashMap<>();

	private AuthenticationManager authenticationManager;

	public InMemoryUserDetailsManager() {
	}

	public InMemoryUserDetailsManager(Collection<UserDetails> users) {
		for (UserDetails user : users) {
			createUser(user);
		}
	}

	public InMemoryUserDetailsManager(UserDetails... users) {
		for (UserDetails user : users) {
			createUser(user);
		}
	}

	public InMemoryUserDetailsManager(Properties users) {
		Enumeration<?> names = users.propertyNames();
		UserAttributeEditor editor = new UserAttributeEditor();

		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			editor.setAsText(users.getProperty(name));
			UserAttribute attr = (UserAttribute) editor.getValue();
			UserDetails user = new User(name, attr.getPassword(), attr.isEnabled(), true,
					true, true, attr.getAuthorities());
			createUser(user);
		}
	}

	public void createUser(UserDetails user) {
		Assert.isTrue(!userExists(user.getUsername()), "user should not exist");

		users.put(user.getUsername().toLowerCase(), new MutableUser(user));
	}

	public void deleteUser(String username) {
		users.remove(username.toLowerCase());
	}

	public void updateUser(UserDetails user) {
		Assert.isTrue(userExists(user.getUsername()), "user should exist");

		users.put(user.getUsername().toLowerCase(), new MutableUser(user));
	}

	public boolean userExists(String username) {
		return users.containsKey(username.toLowerCase());
	}

	public void changePassword(String oldPassword, String newPassword) {
		Authentication currentUser = SecurityContextHolder.getContext()
				.getAuthentication();

		if (currentUser == null) {
			// This would indicate bad coding somewhere
			throw new AccessDeniedException(
					"Can't change password as no Authentication object found in context "
							+ "for current user.");
		}

		String username = currentUser.getName();

		logger.debug("Changing password for user '" + username + "'");

		// If an authentication manager has been set, re-authenticate the user with the
		// supplied password.
		if (authenticationManager != null) {
			logger.debug("Reauthenticating user '" + username
					+ "' for password change request.");

			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
					username, oldPassword));
		}
		else {
			logger.debug("No authentication manager set. Password won't be re-checked.");
		}

		MutableUserDetails user = users.get(username);

		if (user == null) {
			throw new IllegalStateException("Current user doesn't exist in database.");
		}

		user.setPassword(newPassword);
	}

	@Override
	public UserDetails updatePassword(UserDetails user, String newPassword) {
		String username = user.getUsername();
		MutableUserDetails mutableUser = this.users.get(username.toLowerCase());
		mutableUser.setPassword(newPassword);
		return mutableUser;
	}

	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		UserDetails user = users.get(username.toLowerCase());

		if (user == null) {
			throw new UsernameNotFoundException(username);
		}

		return new User(user.getUsername(), user.getPassword(), user.isEnabled(),
				user.isAccountNonExpired(), user.isCredentialsNonExpired(),
				user.isAccountNonLocked(), user.getAuthorities());
	}

	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}
}
```

## JdbcUserDetailsManagerConfigurer

## DaoAuthenticationConfigurer