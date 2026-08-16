<template>
  <div class="navbar">
    <div class="navbar-left">
      <span class="page-title">{{ pageTitle }}</span>
    </div>
    <div class="navbar-right">
      <el-dropdown trigger="click" class="month-picker" @command="onMonth">
        <span class="month-btn">
          <i class="el-icon-date"/>
          <span>{{ monthLabel }}</span>
          <i class="el-icon-arrow-down"/>
        </span>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="本月">本月</el-dropdown-item>
          <el-dropdown-item command="上月">上月</el-dropdown-item>
          <el-dropdown-item command="本季度">本季度</el-dropdown-item>
          <el-dropdown-item command="本年">本年</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>

      <div class="nav-search">
        <i class="el-icon-search"/>
        <input v-model="keyword" placeholder="搜索订单、客户、素材、员工" @keyup.enter="onSearch"/>
      </div>

      <div class="nav-bell">
        <i class="el-icon-bell"/>
        <span class="bell-dot"></span>
      </div>

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
      keyword: '',
      monthLabel: '本月',
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
    onMonth(cmd) {
      this.monthLabel = cmd
    },
    onSearch() {
      if (!this.keyword) return
      this.$message({ message: '搜索：' + this.keyword, type: 'info' })
    },
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
        background: #2F6BFF;
      }
    }
  }

  .navbar-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .month-btn {
      display: flex;
      align-items: center;
      gap: 6px;
      cursor: pointer;
      height: 36px;
      padding: 0 14px;
      border-radius: 8px;
      border: 1px solid #e4e7ed;
      background: #fff;
      font-size: 13px;
      color: #4a5566;

      .el-icon-arrow-down { font-size: 12px; color: #909399; }
      &:hover { border-color: #2F6BFF; color: #2F6BFF; }
    }

    .nav-search {
      display: flex;
      align-items: center;
      width: 300px;
      height: 36px;
      padding: 0 14px;
      border-radius: 8px;
      background: #f4f6fa;
      color: #909399;

      i { font-size: 15px; margin-right: 8px; }

      input {
        flex: 1;
        border: none;
        outline: none;
        background: transparent;
        font-size: 13px;
        color: #303133;
      }
    }

    .nav-bell {
      position: relative;
      width: 36px;
      height: 36px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: #4a5566;
      font-size: 18px;

      &:hover { background: #f4f6fa; }

      .bell-dot {
        position: absolute;
        top: 7px;
        right: 8px;
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #E8423F;
        border: 1.5px solid #fff;
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 6px 10px;
      border-radius: 999px;
      transition: background .2s;

      &:hover {
        background: #f0f5ff;
      }

      .user-avatar {
        width: 34px;
        height: 34px;
        border-radius: 50%;
        background: linear-gradient(135deg, #4f8bff, #2F6BFF);
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
