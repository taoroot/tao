# FilterChainProxy

[<< 目录](/security/README.md)

关于 FilterChainProxy 的作用已经在 [<Spring Security 过滤器架构分析>](/security/filter.md) 已说明,这里就不在重复,直接分析源码

## 属性

FilterChainProxy 负责管理 Security 的过滤器链, 映射到属性里面就是 filterChains.

```java
private final static String FILTER_APPLIED = FilterChainProxy.class.getName().concat(
        ".APPLIED");

// 过滤器链列表
private List<SecurityFilterChain> filterChains;

// 对 FilterChainProxy 后置校验, 默认空实现
private FilterChainValidator filterChainValidator = new NullFilterChainValidator();

// Security 提供的防火墙 (对请求内容格式进行一次校验)
private HttpFirewall firewall = new StrictHttpFirewall();
```

## doFilter 

通过 FILTER_APPLIED 常量, 判断是否执行完 Security 所有流程, 从而做一些收尾操作
这里的收尾操作时 SecurityContextHolder.clearContext(). 主要是考虑到递归的情况,
如果内部又执行了一次当前类(一般不会出现),那么就不应该执行清除操作

```java
public void doFilter(ServletRequest request, ServletResponse response,
        FilterChain chain) throws IOException, ServletException {
    
    boolean clearContext = request.getAttribute(FILTER_APPLIED) == null;

    if (clearContext) {
        try {
            request.setAttribute(FILTER_APPLIED, Boolean.TRUE); 
            doFilterInternal(request, response, chain); // 执行 Security 过滤器链
        }
        finally {
            SecurityContextHolder.clearContext(); // 清除
            request.removeAttribute(FILTER_APPLIED);
        }
    }
    else {
        doFilterInternal(request, response, chain);
    }
}
```

## doFilterInternal

该方法挑选出需要执行的 Security 过滤器链, 并按顺序执行里面的过滤器,执行完后,继续执行 FilterChainProxy 下一个过滤器

```java
private void doFilterInternal(ServletRequest request, ServletResponse response,
        FilterChain chain) throws IOException, ServletException {

    FirewalledRequest fwRequest = firewall
            .getFirewalledRequest((HttpServletRequest) request);
    HttpServletResponse fwResponse = firewall
            .getFirewalledResponse((HttpServletResponse) response);

    // 获取第一个匹配到的过滤器链, 并放回该列中的所有过滤器
    List<Filter> filters = getFilters(fwRequest); 

    if (filters == null || filters.size() == 0) {

        fwRequest.reset();

        chain.doFilter(fwRequest, fwResponse);

        return;
    }

    // 委托给 VirtualFilterChain 执行, 
    // 先按顺序执行完 filters, 然后继续执行 chain
    VirtualFilterChain vfc = new VirtualFilterChain(fwRequest, chain, filters);
    vfc.doFilter(fwRequest, fwResponse);
}

private List<Filter> getFilters(HttpServletRequest request) {
    for (SecurityFilterChain chain : filterChains) {
        if (chain.matches(request)) { // 匹配上
            return chain.getFilters(); // 放回内部持有的过滤器
        }
    }

    return null;
}
```

## VirtualFilterChain

代码很简单, 就是通过计数方式,判断是否执行完了 Security 的过滤器, 如果执行完, 就继续执行 FilterChain 的下一个处理器.

```java
private static class VirtualFilterChain implements FilterChain {
    private final FilterChain originalChain;
    private final List<Filter> additionalFilters;
    private final FirewalledRequest firewalledRequest;
    private final int size;
    private int currentPosition = 0;

    private VirtualFilterChain(FirewalledRequest firewalledRequest,
            FilterChain chain, List<Filter> additionalFilters) {
        this.originalChain = chain;
        this.additionalFilters = additionalFilters;
        this.size = additionalFilters.size();
        this.firewalledRequest = firewalledRequest;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        if (currentPosition == size) {
            this.firewalledRequest.reset();

            originalChain.doFilter(request, response);
        }
        else {
            currentPosition++;

            Filter nextFilter = additionalFilters.get(currentPosition - 1);

            nextFilter.doFilter(request, response, this);
        }
    }
}
```