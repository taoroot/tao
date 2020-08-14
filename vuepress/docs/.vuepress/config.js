module.exports = {
  title: 'T A O 文档说明',
  description: '基于 Spring Boot 的 RBAC 权限管理系统',
  head: [
    ['link', { rel: 'icon', href: '/zymy.jpg' }]
  ],
  themeConfig: {
    nav: [
      { text: '主页', link: '/' },
      { text: '引导', link: '/guide/' },
      {
        text: '源码分析',
        items: [
          {
            text: 'Security', items: [
              { text: 'Security 过滤器', link: '/source/security/filter' },
              { text: 'Security 启动流程', link: '/source/security/boot' },
              { text: 'WebSecurity 源码分析', link: '/source/security/websecurity' }
            ]
          }
        ]
      },
      { text: '预览', link: 'https://tao.flizi.cn' },
      { text: 'Github', link: 'https://github.com/taoroot/tao' }
    ]
  }
}

