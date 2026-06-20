<template>
  <el-container class="layout-root">
    <index-header></index-header>
    <el-container class="inner-container">
      <index-aside></index-aside>
      <index-main></index-main>
    </el-container>
  </el-container>
</template>
<script>
import IndexHeader from '@/components/index/IndexHeader'
import IndexAside from '@/components/index/IndexAsideStatic'
import IndexMain from '@/components/index/IndexMain'

export default {
  components: {
    IndexHeader,
    IndexAside,
    IndexMain
  }
}
</script>

<style lang="scss" scoped>
// 铺满全屏
.layout-root {
  position: absolute;
  width: 100%;
  top: 0;
  left: 0;
  bottom: 0;
  display: block;
}

// 左侧深色菜单栏固定（含顶部 Logo，占满整个高度）
::v-deep .el-aside {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 210px !important;
  height: auto !important;
  z-index: 1001;
}

.inner-container {
  display: block;
}

// 顶部导航固定（仅覆盖右侧内容区）
::v-deep .navbar {
  position: fixed;
  top: 0;
  left: 210px;
  right: 0;
  height: 60px;
  z-index: 1000;
}

// 主内容区让出左侧菜单与顶部导航的位置
::v-deep .el-main {
  margin-left: 210px;
  margin-top: 60px;
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
}
</style>
