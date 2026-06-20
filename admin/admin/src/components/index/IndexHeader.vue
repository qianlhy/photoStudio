<template>
  <div class="navbar">
    <div class="navbar-left">
      <span class="page-title">{{ pageTitle }}</span>
    </div>
    <div class="navbar-right">
      <el-dropdown trigger="click" @command="handleCommand">
        <span class="user-info">
          <span class="user-avatar">{{ avatarText }}</span>
          <span class="user-name">{{ this.$storage.get('adminName') || this.$storage.get('role') || '管理员' }}</span>
          <i class="el-icon-arrow-down"/>
        </span>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="center">个人信息</el-dropdown-item>
          <el-dropdown-item command="password">修改密码</el-dropdown-item>
          <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      user: {},
    };
  },
  computed: {
    pageTitle() {
      return (this.$route && this.$route.name) || '首页'
    },
    avatarText() {
      let name = this.$storage.get('adminName') || this.$storage.get('role') || '管'
      return name.toString().charAt(0)
    }
  },
  mounted() {
    let sessionTable = this.$storage.get("sessionTable")
    this.$http({
      url: sessionTable + '/session',
      method: "get"
    }).then(({data}) => {
      if (data && data.code === 0) {
        this.user = data.data;
        this.$storage.set('userid', data.data.id);
      }
    });
  },
  methods: {
    handleCommand(command) {
      if (command === 'logout') {
        this.onLogout()
      } else if (command === 'center') {
        if (this.$route.path !== '/center') this.$router.push('/center')
      } else if (command === 'password') {
        if (this.$route.path !== '/updatePassword') this.$router.push('/updatePassword')
      }
    },
    onLogout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$storage.clear()
        this.$router.replace({name: "login"});
      }).catch(() => {
      })
    },
  }
};
</script>

<style lang="scss" scoped>
.navbar {
  height: 60px;
  width: 100%;
  padding: 0 24px;
  box-sizing: border-box;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .navbar-left {
    display: flex;
    align-items: center;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      position: relative;
      padding-left: 12px;

      &::before {
        content: "";
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 4px;
        height: 18px;
        border-radius: 2px;
        background: #B49A6B;
      }
    }
  }

  .navbar-right {
    display: flex;
    align-items: center;

    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 6px 10px;
      border-radius: 999px;
      transition: background .2s;

      &:hover {
        background: #F3EEE4;
      }

      .user-avatar {
        width: 34px;
        height: 34px;
        border-radius: 50%;
        background: linear-gradient(135deg, #C9B187, #A6885A);
        color: #fff;
        font-size: 15px;
        font-weight: 600;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 10px;
      }

      .user-name {
        font-size: 14px;
        color: #303133;
        margin-right: 6px;
      }

      .el-icon-arrow-down {
        color: #909399;
        font-size: 12px;
      }
    }
  }
}
</style>
