<template>
  <el-aside class="index-aside" width="210px">
    <!-- 品牌 Logo 区 -->
    <div class="aside-logo">
      <div class="logo-badge"><i class="el-icon-video-play"/></div>
      <div class="logo-texts">
        <span class="logo-title">{{ this.$project.projectName }}</span>
        <span class="logo-sub">轻视频 · 内容 · 增长</span>
      </div>
    </div>
    <div class="index-aside-inner menulist">
      <div v-for="item in menuList" :key="item.roleName" v-if="role==item.roleName" class="menulist-item">
        <el-menu
            router
            :default-active="activeMenu"
            class="el-menu-demo"
            background-color="transparent"
            text-color="#9aa7bd"
            active-text-color="#ffffff">
          <el-menu-item index="/">
            <i class="el-icon-s-home"/>
            <span slot="title">总览</span>
          </el-menu-item>
          <el-menu-item v-for="(menu,index) in item.backMenu" :key="menu.menu" :index="'/'+menu.child[0].tableName">
            <i :class="icons[index]"/>
            <span slot="title">{{ menu.menu }}</span>
            <span v-if="menu.child[0].tableName==='hyMaterial'" class="menu-badge">12</span>
          </el-menu-item>
        </el-menu>
      </div>
    </div>
    <div class="aside-collapse"><i class="el-icon-s-fold"/></div>
  </el-aside>
</template>

<script>
import menu from '@/utils/menu'

export default {
  data() {
    return {
      menuList: [],
      dynamicMenuRoutes: [],
      role: '',
      icons: [
        'el-icon-user',
        'el-icon-s-order',
        'el-icon-video-camera',
        'el-icon-film',
        'el-icon-setting',
        'el-icon-s-data',
        'el-icon-s-promotion',
        'el-icon-s-comment',
      ],
    }
  },
  computed: {
    activeMenu() {
      return this.$route.path
    }
  },
  mounted() {
    const menus = menu.list()
    if (menus) {
      this.menuList = menus
    } else {
      let params = {
        page: 1,
        limit: 1,
        sort: 'id',
      }
      this.$http({
        url: "menu/list",
        method: "get",
        params: params
      }).then(({
                 data
               }) => {
        if (data && data.code === 0) {
          this.menuList = JSON.parse(data.data.list[0].menujson);
          this.$storage.set("menus", this.menuList);
        }
      })
    }
    this.role = this.$storage.get('role')
  },
}
</script>

<style lang="scss" scoped>
$aside-bg: #0f1c30;
$aside-bg-deep: #0b1626;
$aside-blue: #2F6BFF;

.index-aside {
  height: 100%;
  position: relative;
  background: linear-gradient(180deg, #12213a 0%, #0f1c30 60%, #0b1424 100%) !important;
  box-sizing: border-box;
  overflow-y: auto;
  overflow-x: hidden;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.18);

  // Logo 区
  .aside-logo {
    height: 60px;
    display: flex;
    align-items: center;
    padding: 0 18px;
    background: $aside-bg-deep;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);

    .logo-badge {
      width: 34px;
      height: 34px;
      border-radius: 9px;
      margin-right: 12px;
      background: linear-gradient(135deg, #4f8bff, #2F6BFF);
      color: #fff;
      font-size: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      box-shadow: 0 4px 12px rgba(47, 107, 255, 0.4);
    }

    .logo-texts {
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    .logo-title {
      color: #ffffff;
      font-size: 17px;
      font-weight: 700;
      letter-spacing: 1px;
      line-height: 1.2;
      white-space: nowrap;
    }

    .logo-sub {
      color: #6b7a92;
      font-size: 11px;
      margin-top: 2px;
      white-space: nowrap;
    }
  }

  .index-aside-inner {
    width: 100%;
  }

  .el-menu-demo {
    width: 100%;
    border-right: none !important;
    padding: 14px 0;
    box-sizing: border-box;
    background: transparent !important;
  }

  ::v-deep .el-menu-item {
    height: 46px;
    line-height: 46px;
    font-size: 14px;
    margin: 6px 14px;
    padding: 0 16px !important;
    border-radius: 10px;
    color: #9aa7bd !important;
    background-color: transparent !important;

    i {
      color: #8494ac;
      margin-right: 10px;
      font-size: 17px;
    }

    &:hover {
      background-color: rgba(255, 255, 255, 0.06) !important;
      color: #ffffff !important;

      i { color: #ffffff; }
    }

    &.is-active {
      background: linear-gradient(135deg, #4f8bff, #2F6BFF) !important;
      color: #ffffff !important;
      font-weight: 600;
      box-shadow: 0 6px 16px rgba(47, 107, 255, 0.35);

      i { color: #ffffff; }
    }
  }

  .menu-badge {
    display: inline-block;
    min-width: 20px;
    height: 18px;
    line-height: 18px;
    text-align: center;
    padding: 0 6px;
    margin-left: 8px;
    border-radius: 9px;
    background: #22B07D;
    color: #fff;
    font-size: 11px;
    font-weight: 600;
    vertical-align: middle;
  }

  .aside-collapse {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    height: 46px;
    display: flex;
    align-items: center;
    padding: 0 26px;
    color: #6b7a92;
    font-size: 18px;
    border-top: 1px solid rgba(255, 255, 255, 0.05);
    background: $aside-bg-deep;
  }
}
</style>
