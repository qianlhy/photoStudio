<template>
  <!-- <el-header>
    <el-menu background-color="#00c292" text-color="#FFFFFF" active-text-color="#FFFFFF" mode="horizontal">
      <div class="fl title">{{this.$project.projectName}}</div>
      <div class="fr logout" style="display:flex;">
        <el-menu-item index="3">
          <div>{{this.$storage.get('role')}} {{this.$storage.get('adminName')}}</div>
        </el-menu-item>
        <el-menu-item @click="onLogout" index="2">
          <div>退出登录</div>
        </el-menu-item>
      </div>
    </el-menu>
  </el-header> -->
  <div class="navbar"
       :style="{background:heads.headBgColor,height:heads.headHeight,boxShadow:heads.headBoxShadow,lineHeight:heads.headHeight}">
    <div class="title-menu" :style="{justifyContent:heads.headTitleStyle=='1'?'flex-start':'center'}">
      <div class="brand-badge">摄</div>
      <div class="title-name" :style="{color:heads.headFontColor,fontSize:heads.headFontSize}">
        {{ this.$project.projectName }}
      </div>
    </div>
    <div class="right-menu">
      <div class="user-info" :style="{color:heads.headUserInfoFontColor,fontSize:heads.headUserInfoFontSize}">
        {{ this.$storage.get('role') }} {{ this.$storage.get('adminName') }}
      </div>
      <div class="logout" :style="{color:heads.headLogoutFontColor,fontSize:heads.headLogoutFontSize}"
           @click="onLogout">退出登录
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      dialogVisible: false,
      ruleForm: {},
      user: {},
      heads: {
        "headLogoutFontHoverColor": "#fff",
        "headFontSize": "18px",
        "headUserInfoFontColor": "#666",
        "headBoxShadow": "0 1px 0 #ECECEC",
        "headTitleImgHeight": "40px",
        "headLogoutFontHoverBgColor": "#B49A6B",
        "headFontColor": "#222",
        "headTitleImg": false,
        "headHeight": "64px",
        "headTitleImgBorderRadius": "10px",
        "headTitleImgUrl": "",
        "headBgColor": "#ffffff",
        "headTitleImgBoxShadow": "none",
        "headLogoutFontColor": "#8C7853",
        "headUserInfoFontSize": "15px",
        "headTitleImgWidth": "40px",
        "headTitleStyle": "1",
        "headLogoutFontSize": "14px"
      },
    };
  },
  created() {
    this.setHeaderStyle()
  },
  mounted() {
    let sessionTable = this.$storage.get("sessionTable")
    this.$http({
      url: sessionTable + '/session',
      method: "get"
    }).then(({
               data
             }) => {
      if (data && data.code === 0) {
        this.user = data.data;
        this.$storage.set('userid', data.data.id);
      } else {
        let message = this.$message
        message.error(data.msg);
      }
    });
  },
  methods: {
    onLogout() {
      let storage = this.$storage
      let router = this.$router
      storage.clear()
      router.replace({
        name: "login"
      });
    },
    onIndexTap() {
      window.location.href = `${this.$base.indexUrl}`
    },
    setHeaderStyle() {
      this.$nextTick(() => {
        document.querySelectorAll('.navbar .right-menu .logout').forEach(el => {
          el.addEventListener("mouseenter", e => {
            e.stopPropagation()
            el.style.backgroundColor = this.heads.headLogoutFontHoverBgColor
            el.style.color = this.heads.headLogoutFontHoverColor
          })
          el.addEventListener("mouseleave", e => {
            e.stopPropagation()
            el.style.backgroundColor = "transparent"
            el.style.color = this.heads.headLogoutFontColor
          })
        })
      })
    },
  }
};
</script>


<style lang="scss" scoped>
.navbar {
  height: 64px;
  line-height: 64px;
  width: 100%;
  padding: 0 28px;
  box-sizing: border-box;
  background-color: #fff;
  border-bottom: 1px solid #ECECEC;
  position: relative;
  z-index: 111;

  .right-menu {
    position: absolute;
    right: 28px;
    top: 0;
    height: 100%;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    z-index: 111;

    .user-info {
      font-size: 15px;
      color: #666;
      padding: 0 14px;
    }

    .logout {
      font-size: 14px;
      color: #8C7853;
      padding: 7px 18px;
      line-height: 1;
      border: 1px solid #E2D9C5;
      border-radius: 999px;
      cursor: pointer;
      transition: all .2s;
    }

  }

  .title-menu {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    width: 100%;
    height: 100%;

    .brand-badge {
      width: 38px;
      height: 38px;
      border-radius: 10px;
      margin-right: 14px;
      background: linear-gradient(135deg, #B49A6B, #8C7853);
      color: #fff;
      font-size: 20px;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 12px rgba(140, 120, 83, 0.28);
    }

    .title-name {
      font-size: 18px;
      color: #222;
      font-weight: 700;
      letter-spacing: 1px;
    }
  }
}

// .el-header .fr {
// 	float: right;
// }

// .el-header .fl {
// 	float: left;
// }

// .el-header {
// 	width: 100%;
// 	color: #333;
// 	text-align: center;
// 	line-height: 60px;
// 	padding: 0;
// 	z-index: 99;
// }

// .logo {
// 	width: 60px;
// 	height: 60px;
// 	margin-left: 70px;
// }

// .avator {
// 	width: 40px;
// 	height: 40px;
// 	background: #ffffff;
// 	border-radius: 50%;
// }

// .title {
// 	color: #ffffff;
// 	font-size: 20px;
// 	font-weight: bold;
// 	margin-left: 20px;
// }
</style>
