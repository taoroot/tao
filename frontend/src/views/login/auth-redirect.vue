<script>
import { param2Obj } from '@/utils'
export default {
  name: 'AuthRedirect',
  created() {
    var token = param2Obj(window.location.hash)['access_token']
    window.localStorage.setItem('x-admin-oauth-code', token)
    if (token) {
      if (window.localStorage) {
        window.localStorage.setItem('x-admin-oauth-code', token)
        window.localStorage.clear('x-admin-oauth-code')
        window.close()
      }
    }
  },
  render: function(h) {
    return h() // avoid warning message
  }
}
</script>
