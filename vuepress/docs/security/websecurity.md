# WebSecurity

[<< 目录](/security/README.md)

![WebSecurity](/WebSecurity.png)

## SecurityBuilder

泛型O: 需要构建的类型

```java
public interface SecurityBuilder<O> {
	O build() throws Exception;
}
```

## AbstractSecurityBuilder

添加限制,只能构建一次

```java
public abstract class AbstractSecurityBuilder<O> implements SecurityBuilder<O> {
	private AtomicBoolean building = new AtomicBoolean();

	private O object;

	public final O build() throws Exception {
		if (this.building.compareAndSet(false, true)) {
			this.object = doBuild();
			return this.object;
		}
		throw new AlreadyBuiltException("This object has already been built");
	}

	public final O getObject() {
		if (!this.building.get()) {
			throw new IllegalStateException("This object has not been built");
		}
		return this.object;
	}

	protected abstract O doBuild() throws Exception;
}
```

## AbstractConfiguredSecurityBuilder

泛型O: 需要构建的类型

泛型B: 当前类实际类型

所以这是一个可配置的 AbstractSecurityBuilder 子类, 而实际配置的是委托给 SecurityConfigurer

```java
public abstract class AbstractConfiguredSecurityBuilder<O, B extends SecurityBuilder<O>>
		extends AbstractSecurityBuilder<O> {

	private final LinkedHashMap<Class<? extends SecurityConfigurer<O, B>>, 
		List<SecurityConfigurer<O, B>>> configurers = new LinkedHashMap<>();

	private final List<SecurityConfigurer<O, B>> configurersAddedInInitializing 
		= new ArrayList<>();

	@Override
	protected final O doBuild() throws Exception {
		synchronized (configurers) {
			buildState = BuildState.INITIALIZING;

			beforeInit();
			init(); // 调用SecurityConfigurer.init()方法

			buildState = BuildState.CONFIGURING;

			beforeConfigure();
			configure(); //  调用SecurityConfigurer.configure()方法

			buildState = BuildState.BUILDING;

			O result = performBuild(); // 具体子类实现

			buildState = BuildState.BUILT;

			return result;
		}
	}

	private void init() throws Exception {
		Collection<SecurityConfigurer<O, B>> configurers = getConfigurers();

		for (SecurityConfigurer<O, B> configurer : configurers) {
			configurer.init((B) this);
		}

		for (SecurityConfigurer<O, B> configurer : configurersAddedInInitializing) {
			configurer.init((B) this);
		}
	}

	private void configure() throws Exception {
		Collection<SecurityConfigurer<O, B>> configurers = getConfigurers();

		for (SecurityConfigurer<O, B> configurer : configurers) {
			configurer.configure((B) this);
		}
	}
	
	// 真正构建的方法
	protected abstract O performBuild() throws Exception;
}
```

## WebSecurity

执行我上述的 SecurityConfigurer, 生成 SecurityBuilder\<? extends SecurityFilterChain\>,
加入到 securityFilterChainBuilders 集合

WebSecurityConfigurer\<WebSecurity\> 具体类 [WebSecurityConfigurerAdapter](/security/websecurityconfigureradapter)

SecurityBuilder\<? extends SecurityFilterChain\> 具体类 [HttpSecurity](/security/websecurity) 


```java
public final class WebSecurity extends
		AbstractConfiguredSecurityBuilder<Filter, WebSecurity> implements
		SecurityBuilder<Filter>, ApplicationContextAware {

	@Override
	protected Filter performBuild() throws Exception {

		Assert.state(!securityFilterChainBuilders.isEmpty(), () -> "至少要有一个");

		int chainSize = ignoredRequests.size() + securityFilterChainBuilders.size();
		List<SecurityFilterChain> securityFilterChains = new ArrayList<>(
				chainSize);

		// 提供一个默认的过滤器链, 不做任何事情. 
		// 因为只会走一个过滤器链, 所以当URL被这个匹配到, 相当于没发挥作用
		for (RequestMatcher ignoredRequest : ignoredRequests) {
			securityFilterChains.add(new DefaultSecurityFilterChain(ignoredRequest));
		}

		// 构建其他过滤器链
		for (SecurityBuilder<? extends SecurityFilterChain> builder : securityFilterChainBuilders) {
			securityFilterChains.add(builder.build());
		}

		FilterChainProxy filterChainProxy = new FilterChainProxy(securityFilterChains);
		if (httpFirewall != null) {
			filterChainProxy.setFirewall(httpFirewall);
		}

		filterChainProxy.afterPropertiesSet();

		Filter result = filterChainProxy;

		postBuildAction.run();
		return result;
	}
}
```