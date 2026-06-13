<template>
  <el-main style="padding: 16px 24px; background: #F6F5F2;">
    <bread-crumbs :title="title" class="bread-crumbs"></bread-crumbs>
    <router-view class="router-view"></router-view>
  </el-main>
</template>

<script>
import menu from "@/utils/menu";

export default {
  data() {
    return {
      menuList: [],
      role: "",
      currentIndex: -2,
      itemMenu: [],
      title: '',
    };
  },
  mounted() {
    let menus = menu.list();
    this.menuList = menus;
    this.role = this.$storage.get("role");
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      this.$nextTick(() => {
        // let h = document.getElementsByClassName('el-aside')[0].clientHeight
        // document.getElementsByClassName('el-main')[0].style.minHeight = "calc(100vh - 80px - " + h+'px)'
      })
    },
    menuHandler(menu) {
      this.$router.push({
        name: menu.tableName
      });
      this.title = menu.menu;
    },
    titleChange(index, menus) {
      this.currentIndex = index
      this.itemMenu = menus;
      console.log(menus);
    },
    homeChange(index) {
      this.itemMenu = [];
      this.title = ""
      this.currentIndex = index
      this.$router.push({
        name: 'home'
      });
    },
    centerChange(index) {
      this.itemMenu = [{
        "buttons": ["新增", "查看", "修改", "删除"],
        "menu": "修改密码",
        "tableName": "updatePassword"
      }, {
        "buttons": ["新增", "查看", "修改", "删除"],
        "menu": "个人信息",
        "tableName": "center"
      }];
      this.title = ""
      this.currentIndex = index
      this.$router.push({
        name: 'home'
      });

    }
  }
};
</script>
<style lang="scss" scoped>
a {
  text-decoration: none;
  color: #555;
}

a:hover {
  background: transparent;
  color: #8C7853;
}

.nav-list {
  width: 100%;
  margin: 0 auto;
  text-align: left;
  margin-top: 20px;

  .nav-title {
    display: inline-block;
    font-size: 15px;
    color: #333;
    padding: 15px 25px;
    border: none;
  }

  .nav-title.active {
    color: #555;
    cursor: default;
    background-color: #fff;
  }
}

.nav-item {
  margin-top: 20px;
  background: #FFFFFF;
  padding: 15px 0;

  .menu {
    padding: 15px 25px;
  }
}

.el-main {
  background-color: #F6F5F2;
  padding: 16px 24px;
  min-height: 100vh;
}

.router-view {
  padding: 18px;
  margin-top: 14px;
  background: #FFFFFF;
  border-radius: 14px;
  box-shadow: 0 6px 24px rgba(60, 50, 30, 0.05);
  box-sizing: border-box;
}

.bread-crumbs {
  width: 100%;
  margin-top: 2px;
  box-sizing: border-box;
}

.detail-form-content {
  background: transparent;
}
</style>
