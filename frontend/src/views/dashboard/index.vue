<template>
  <div class="app-container">
    <div class="dashboard-text">账号: {{ name }}</div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Dashboard',
  data() {
    return {
      socials: [],
      msg: ''
    }
  },
  computed: {
    ...mapGetters([
      'name'
    ])
  },
  mounted() {

  }
}
</script>

<style lang="scss" scoped>
.dashboard {
  &-container {
    margin: 30px;
  }
  &-text {
    font-size: 30px;
    line-height: 46px;
  }
}
</style>
