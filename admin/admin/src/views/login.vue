<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="brand-side">
      <div class="brand-inner">
        <div class="brand-logo"><i class="el-icon-video-play"></i></div>
        <div class="brand-name">合意传媒</div>
        <div class="brand-en">HEYI&nbsp;MEDIA</div>
        <div class="brand-sub">轻视频 · 内容 · 增长 —— 合意传媒运营管理平台</div>
        <ul class="brand-points">
          <li><i class="el-icon-user"></i> 客户 / 订单 / 素材 全流程管理</li>
          <li><i class="el-icon-video-camera"></i> 成品在线交付与优质案例沉淀</li>
          <li><i class="el-icon-data-line"></i> 经营数据一目了然</li>
        </ul>
      </div>
      <div class="brand-mask"></div>
    </div>

    <!-- 右侧登录区 -->
    <div class="form-side">
      <div class="login-card">
        <div class="login-head">
          <h3>欢迎登录</h3>
          <p>请使用管理账号进入后台</p>
        </div>
        <el-form class="lg-form" @submit.native.prevent>
          <el-input class="lg-input" prefix-icon="el-icon-user" placeholder="请输入用户名"
                    v-model="rulesForm.username"/>
          <el-input class="lg-input" prefix-icon="el-icon-lock" placeholder="请输入密码" show-password
                    v-model="rulesForm.password" @keyup.enter.native="login()"/>
          <div class="role-row">
            <span class="role-label">角色</span>
            <el-radio
                v-for="item in menus"
                v-if="item.hasBackLogin=='是'"
                v-bind:key="item.roleName"
                v-model="rulesForm.role"
                :label="item.roleName"
            >{{ item.roleName }}
            </el-radio>
          </div>
          <el-button type="primary" @click="login()" class="login-btn">登 录</el-button>
        </el-form>
        <div class="login-foot">© 合意传媒 · 运营管理后台</div>
      </div>
    </div>
  </div>
</template>
<script>

import menu from "@/utils/menu";

export default {
  data() {
    return {
      rulesForm: {
        username: "",
        password: "",
        role: "",
        code: '',
      },
      menus: [],
      tableName: "",
      codes: [{
        num: 1,
        color: '#000',
        rotate: '10deg',
        size: '16px'
      }, {
        num: 2,
        color: '#000',
        rotate: '10deg',
        size: '16px'
      }, {
        num: 3,
        color: '#000',
        rotate: '10deg',
        size: '16px'
      }, {
        num: 4,
        color: '#000',
        rotate: '10deg',
        size: '16px'
      }],
    };
  },
  mounted() {
    let menus = menu.list();
    this.menus = menus;
  },
  created() {
    this.getRandCode()

  },
  methods: {
    register(tableName) {
      this.$storage.set("loginTable", tableName);
      this.$router.push({path: '/register'})
    },
    // 登陆
    login() {
      if (!this.rulesForm.username) {
        this.$message.error("请输入用户名");
        return;
      }
      if (!this.rulesForm.password) {
        this.$message.error("请输入密码");
        return;
      }
      if (!this.rulesForm.role) {
        this.$message.error("请选择角色");
        return;
      }
      let menus = this.menus;
      for (let i = 0; i < menus.length; i++) {
        if (menus[i].roleName == this.rulesForm.role) {
          this.tableName = menus[i].tableName;
        }
      }
      this.$http({
        url: `${this.tableName}/login?username=${this.rulesForm.username}&password=${this.rulesForm.password}`,
        method: "post"
      }).then(({data}) => {
        if (data && data.code === 0) {
          this.$storage.set("Token", data.token);
          this.$storage.set("role", this.rulesForm.role);
          this.$storage.set("sessionTable", this.tableName);
          this.$storage.set("adminName", this.rulesForm.username);
          this.$router.replace({path: "/index/"});
        } else {
          this.$message.error(data.msg);
        }
      });
    },
    getRandCode(len = 4) {
      this.randomString(len)
    },
    randomString(len = 4) {
      let chars = [
        "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k",
        "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
        "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G",
        "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
        "S", "T", "U", "V", "W", "X", "Y", "Z", "0", "1", "2",
        "3", "4", "5", "6", "7", "8", "9"
      ]
      let colors = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"]
      let sizes = ['14', '15', '16', '17', '18']

      let output = [];
      for (let i = 0; i < len; i++) {
        // 随机验证码
        let key = Math.floor(Math.random() * chars.length)
        this.codes[i].num = chars[key]
        // 随机验证码颜色
        let code = '#'
        for (let j = 0; j < 6; j++) {
          let key = Math.floor(Math.random() * colors.length)
          code += colors[key]
        }
        this.codes[i].color = code
        // 随机验证码方向
        let rotate = Math.floor(Math.random() * 60)
        let plus = Math.floor(Math.random() * 2)
        if (plus == 1) rotate = '-' + rotate
        this.codes[i].rotate = 'rotate(' + rotate + 'deg)'
        // 随机验证码字体大小
        let size = Math.floor(Math.random() * sizes.length)
        this.codes[i].size = sizes[size] + 'px'
      }
    },
  }
};
</script>
<style lang="scss" scoped>
$primary: #2F6BFF;
$primary-deep: #1E52D6;
$primary-soft: #EAF1FF;
$ink: #1F2733;
$ink-2: #5a6473;
$ink-3: #9aa3b2;

.login-page {
  position: fixed;
  inset: 0;
  display: flex;
  background: #fff;
}

/* 左侧品牌区 */
.brand-side {
  position: relative;
  flex: 1.1;
  overflow: hidden;
  background: linear-gradient(150deg, #0B1E4D 0%, #1E52D6 48%, #2F6BFF 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;

  .brand-mask {
    position: absolute;
    inset: 0;
    background:
      radial-gradient(circle at 80% 18%, rgba(255, 255, 255, 0.16), transparent 42%),
      radial-gradient(circle at 12% 88%, rgba(255, 255, 255, 0.10), transparent 40%);
    pointer-events: none;
  }

  .brand-inner {
    position: relative;
    z-index: 2;
    width: 72%;
    max-width: 460px;
  }

  .brand-logo {
    width: 76px;
    height: 76px;
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.16);
    border: 1px solid rgba(255, 255, 255, 0.35);
    backdrop-filter: blur(4px);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 38px;
    font-weight: 700;
    letter-spacing: 2px;
  }

  .brand-name {
    margin-top: 28px;
    font-size: 34px;
    font-weight: 700;
    letter-spacing: 2px;
  }

  .brand-en {
    margin-top: 10px;
    font-size: 13px;
    letter-spacing: 4px;
    opacity: 0.7;
  }

  .brand-sub {
    margin-top: 18px;
    font-size: 15px;
    line-height: 1.7;
    opacity: 0.92;
  }

  .brand-points {
    list-style: none;
    padding: 0;
    margin: 40px 0 0;

    li {
      display: flex;
      align-items: center;
      font-size: 15px;
      opacity: 0.95;
      margin-bottom: 18px;

      i {
        margin-right: 12px;
        font-size: 18px;
        width: 34px;
        height: 34px;
        line-height: 34px;
        text-align: center;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.15);
      }
    }
  }
}

/* 右侧登录区 */
.form-side {
  flex: 0.9;
  min-width: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.login-card {
  width: 360px;

  .login-head {
    margin-bottom: 36px;

    h3 {
      margin: 0;
      font-size: 26px;
      color: $ink;
      font-weight: 700;
    }

    p {
      margin: 10px 0 0;
      font-size: 14px;
      color: $ink-3;
    }
  }

  .lg-input {
    margin-bottom: 20px;

    & ::v-deep .el-input__inner {
      height: 50px;
      line-height: 50px;
      border-radius: 12px;
      background: #F5F8FF;
      border: 1px solid #E3ECFF;
      padding-left: 42px;
      font-size: 15px;

      &:focus {
        border-color: $primary;
        background: #fff;
      }
    }

    & ::v-deep .el-input__prefix {
      left: 12px;
      color: $primary;
      font-size: 16px;
      display: flex;
      align-items: center;
    }

    & ::v-deep .el-input__icon {
      line-height: 50px;
    }
  }

  .role-row {
    display: flex;
    align-items: center;
    margin: 6px 0 28px;

    .role-label {
      font-size: 14px;
      color: $ink-2;
      margin-right: 16px;
    }

    & ::v-deep .el-radio {
      margin-right: 18px;
    }

    & ::v-deep .el-radio__input.is-checked .el-radio__inner {
      border-color: $primary;
      background: $primary;
    }

    & ::v-deep .el-radio__input.is-checked + .el-radio__label {
      color: $primary-deep;
    }
  }

  .login-btn {
    width: 100%;
    height: 50px;
    border-radius: 12px;
    font-size: 16px;
    letter-spacing: 4px;
    background: linear-gradient(135deg, $primary, $primary-deep);
    border: none;

    &:hover {
      opacity: 0.94;
    }
  }

  .login-foot {
    margin-top: 28px;
    text-align: center;
    font-size: 12px;
    color: #9aa3b2;
  }
}

@media (max-width: 900px) {
  .brand-side {
    display: none;
  }

  .form-side {
    flex: 1;
    min-width: 0;
  }
}
</style>
