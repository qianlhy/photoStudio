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

      <div class="nav-user-block">
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-info">
            <span class="user-avatar">{{ avatarText }}</span>
            <span class="user-meta">
              <span class="user-name">{{ displayName }}</span>
              <span class="user-role">{{ displayRole }}</span>
            </span>
            <i class="el-icon-arrow-down"/>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="center">个人信息</el-dropdown-item>
            <el-dropdown-item command="password">修改密码</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        <el-button size="small" class="btn-logout" @click="onLogout">退出</el-button>
      </div>
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
      if (this.$route && (this.$route.path === '/index/' || this.$route.path === '/index')) return '经营总览'
      return (this.$route && this.$route.name) || '经营总览'
    },
    displayName() {
      return (this.user && this.user.name) || this.$storage.get('adminName') || '管理员'
    },
    displayRole() {
      return (this.user && this.user.role) || this.$storage.get('role') || '管理员'
    },
    avatarText() {
      let name = this.displayName
      return name.toString().charAt(0)
    }
  },
  mounted() {
    let sessionTable = this.$storage.get("sessionTable")
    if (!sessionTable) return
    this.$http({
      url: sessionTable + '/session',
      method: "get"
    }).then(({data}) => {
      if (data && data.code === 0) {
        this.user = data.data || {};
        if (data.data && data.data.id) this.$storage.set('userid', data.data.id);
        if (data.data && data.data.name) this.$storage.set('adminName', data.data.name);
        if (data.data && data.data.role) this.$storage.set('role', data.data.role);
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
  padding: 0 22px 0 24px;
  box-sizing: border-box;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #EBEEF5;
  box-shadow: none;

  .navbar-left {
    display: flex;
    align-items: center;

    .page-title {
      font-size: 20px;
      font-weight: 700;
      color: #182033;
    }
  }

  .navbar-right {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-shrink: 0;
    min-width: 0;

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
      flex: 1;
      min-width: 120px;
      max-width: 260px;
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

    .nav-user-block {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-shrink: 0;
    }

    .btn-logout {
      border-color: #dcdfe6;
      color: #606266;
      padding: 8px 12px;
    }

    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 4px 8px 4px 4px;
      border-radius: 999px;
      transition: background .2s;
      border: 1px solid #ebeef5;

      &:hover {
        background: #f0f5ff;
        border-color: #c6d8ff;
      }

      .user-avatar {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        background: linear-gradient(135deg, #4f8bff, #2F6BFF);
        color: #fff;
        font-size: 14px;
        font-weight: 600;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
      }

      .user-meta {
        display: flex;
        flex-direction: column;
        margin: 0 6px 0 8px;
        line-height: 1.2;
        max-width: 120px;
      }

      .user-name {
        font-size: 13px;
        color: #303133;
        font-weight: 600;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .user-role {
        font-size: 11px;
        color: #909399;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .el-icon-arrow-down {
        color: #909399;
        font-size: 12px;
        flex-shrink: 0;
      }
    }
  }
}
</style>
