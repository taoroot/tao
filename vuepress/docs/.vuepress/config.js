module.exports = {
  title: 'T A O 文档说明',
  description: '基于 Spring Boot 的 RBAC 权限管理系统',
  plugins: [
    [
      '@vuepress/google-analytics',
      { 
        'ga': 'UA-175890446-1'
      }
    ],
    ['@vuepress/back-to-top', {}]
  ],
  head: [
    ['link', { rel: 'icon', href: '/zymy.jpg' }]
  ],
  themeConfig: {
    sidebar: {
      '/guide/': [
        {
          title: '登录方式',
          collapsable: false,
          children: [
            '/guide/mmdl',
            '/guide/oauth2',
            '/guide/sjhdl',
            '/guide/yzm',
          ]
        }
      ],
      '/security/': [
        {
          title: 'Spring Security',
          collapsable: false,
          children: [
            '/security/filter',
            '/security/authentication',
            '/security/securityautoconfiguration',
            '/security/websecurity',
            '/security/websecurityconfigureradapter',
            '/security/httpsecurity',
            '/security/authenticationmanagerbuilder',
            '/security/filterchainproxy',
            '/security/formloginconfigurer',
            '/security/oauth2loginconfigurer',
            '/security/corsconfigurer',
            '/security/exceptionhandlingconfigurer',
            '/security/oauth2resourceserverconfigurer',
          ]
        }
      ]
    },
    sidebarDepth: 2, // h2 和 h3 标题，显示在侧边栏上
    lastUpdated: '更新时间', // 文档更新时间
    nav: [
      { text: '主页', link: '/' },
      { text: '引导', link: '/guide/' },
      {
        text: '源码分析',
        items: [
          { text: 'Spring Framework', link: '/spring/' },
          { text: 'Spring Boot', link: '/springboot/' },
          { text: 'Spring Security', link: '/security/' },
          { text: 'MyBatis', link: '/mybatis/' },
          { text: 'Netty', link: '/netty/' },
        ]
      },
      { text: '预览', link: 'https://tao.flizi.cn' },
      { text: 'Github', link: 'https://github.com/taoroot/tao' }
    ]
  },
}

