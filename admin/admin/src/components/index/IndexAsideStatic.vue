<template>
  <el-aside class="index-aside" width="210px">
    <!-- 品牌 Logo 区 -->
    <div class="aside-logo">
      <div class="logo-badge">摄</div>
      <span class="logo-text">{{ this.$project.projectName }}</span>
    </div>
    <div class="index-aside-inner menulist">
      <div v-for="item in menuList" :key="item.roleName" v-if="role==item.roleName" class="menulist-item">
        <el-menu
            router
            :default-active="activeMenu"
            :unique-opened="true"
            class="el-menu-demo"
            background-color="#2B3648"
            text-color="#c4ccda"
            active-text-color="#E6C88E">
          <el-menu-item index="/">
            <i class="el-icon-s-home"/>
            <span slot="title">首页</span>
          </el-menu-item>
          <el-submenu index="center">
            <template slot="title">
              <i class="el-icon-user-solid"/>
              <span>个人中心</span>
            </template>
            <el-menu-item index="/updatePassword">修改密码</el-menu-item>
            <el-menu-item index="/center">个人信息</el-menu-item>
          </el-submenu>
          <el-submenu v-for="(menu,index) in item.backMenu" :key="menu.menu" :index="index+2+''">
            <template slot="title">
              <i class="el-icon-menu" :class="icons[index]"/>
              <span>{{ menu.menu }}</span>
            </template>
            <el-menu-item v-for="(child,sort) in menu.child" :key="sort" :index="'/'+child.tableName">
              {{ child.menu }}
            </el-menu-item>
          </el-submenu>
        </el-menu>
      </div>
    </div>
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
        'el-icon-s-cooperation',
        'el-icon-s-order',
        'el-icon-s-platform',
        'el-icon-s-management',
        'el-icon-s-custom',
        'el-icon-s-goods',
        'el-icon-s-claim',
        'el-icon-s-marketing',
        'el-icon-s-flag',
        'el-icon-s-data',
        'el-icon-s-promotion',
        'el-icon-s-comment',
        'el-icon-s-ticket',
        'el-icon-s-finance',
        'el-icon-s-opportunity',
        'el-icon-s-grid',
        'el-icon-menu',
        'el-icon-message',
        'el-icon-picture-outline',
        'el-icon-postcard',
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
$aside-bg: #2B3648;
$aside-bg-deep: #232C3B;
$aside-gold: #E6C88E;

.index-aside {
  height: 100%;
  background: linear-gradient(180deg, #313D52 0%, #2B3648 55%, #262F3F 100%) !important;
  box-sizing: border-box;
  overflow-y: auto;
  overflow-x: hidden;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.12);

  // Logo 区
  .aside-logo {
    height: 60px;
    display: flex;
    align-items: center;
    padding: 0 18px;
    background: $aside-bg-deep;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);

    .logo-badge {
      width: 34px;
      height: 34px;
      border-radius: 9px;
      margin-right: 12px;
      background: linear-gradient(135deg, #C9B187, #A6885A);
      color: #fff;
      font-size: 18px;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
    }

    .logo-text {
      color: #f4ede0;
      font-size: 16px;
      font-weight: 600;
      letter-spacing: 1px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .index-aside-inner {
    width: 100%;
  }

  .el-menu-demo {
    width: 100%;
    border-right: none !important;
    padding: 8px 0;
    box-sizing: border-box;
    background: transparent !important;
  }

  // 让一级菜单/分组标题透出侧栏渐变底色
  ::v-deep .el-menu-item,
  ::v-deep .el-submenu__title {
    background-color: transparent !important;
  }

  // 一级菜单项（首页）
  ::v-deep .el-menu-item {
    height: 48px;
    line-height: 48px;
    font-size: 14px;

    i {
      color: #98a4b8;
      margin-right: 8px;
      font-size: 17px;
    }

    &:hover {
      background-color: #313d52 !important;
      color: #fff !important;
    }

    &.is-active {
      background-color: $aside-bg-deep !important;
      color: $aside-gold !important;
      border-left: 3px solid $aside-gold;

      i {
        color: $aside-gold;
      }
    }
  }

  // 一级标题（分组）
  ::v-deep .el-submenu__title {
    height: 48px;
    line-height: 48px;
    font-size: 14px;

    i {
      color: #98a4b8;
      margin-right: 8px;
      font-size: 17px;
    }

    &:hover {
      background-color: #313d52 !important;
      color: #fff !important;
    }
  }

  // 二级菜单项（更深底色）
  ::v-deep .el-menu--inline {
    background-color: $aside-bg-deep !important;

    .el-menu-item {
      height: 44px;
      line-height: 44px;
      min-width: auto;
      padding-left: 50px !important;
      background-color: $aside-bg-deep !important;
      color: #aab4c5 !important;

      &:hover {
        background-color: #313d52 !important;
        color: #fff !important;
      }

      &.is-active {
        color: $aside-gold !important;
        background-color: #313d52 !important;
        border-left: 3px solid $aside-gold;
        font-weight: 600;
      }
    }
  }
}
</style>
