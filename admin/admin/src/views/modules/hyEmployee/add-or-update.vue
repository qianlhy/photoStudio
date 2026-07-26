<template>
  <div class="addEdit-block">
    <div class="addEdit-head">
      <i class="el-icon-arrow-left back-arrow" @click="back()"></i>
      <span class="addEdit-title" @click="back()">{{ ruleForm.id ? '编辑员工' : '新增员工' }}</span>
    </div>
    <el-form class="detail-form-content" ref="ruleForm" :model="ruleForm" :rules="rules" label-width="100px">
      <el-row>
        <el-col :span="12"><el-form-item label="姓名" prop="name"><el-input v-model="ruleForm.name"></el-input></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="账号" prop="username"><el-input v-model="ruleForm.username" :disabled="!!ruleForm.id"></el-input></el-form-item></el-col>
      </el-row>
      <el-row>
        <el-col :span="12"><el-form-item label="手机" prop="phone"><el-input v-model="ruleForm.phone"></el-input></el-form-item></el-col>
        <el-col :span="12">
          <el-form-item label="角色" prop="role">
            <el-select v-model="ruleForm.role" placeholder="请选择" style="width:100%">
              <el-option v-for="r in roleOptions" :key="r" :label="r" :value="r"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12"><el-form-item label="部门"><el-input v-model="ruleForm.department"></el-input></el-form-item></el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-select v-model="ruleForm.status" style="width:100%">
              <el-option label="正常" value="正常"></el-option>
              <el-option label="停用" value="停用"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row v-if="!ruleForm.id">
        <el-col :span="12"><el-form-item label="初始密码"><el-input v-model="ruleForm.password" placeholder="留空默认 123456"></el-input></el-form-item></el-col>
      </el-row>
      <el-form-item>
        <el-button type="primary" @click="onSubmit">提交</el-button>
        <el-button @click="back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  props: ["parent"],
  data() {
    return {
      roleOptions: ["管理员", "销售经理", "拍摄", "剪辑", "编导"],
      ruleForm: {name: "", username: "", phone: "", role: "销售经理", department: "", status: "正常", password: ""},
      rules: {
        name: [{required: true, message: "姓名不能为空", trigger: "blur"}],
        username: [{required: true, message: "账号不能为空", trigger: "blur"}],
        role: [{required: true, message: "请选择角色", trigger: "change"}]
      }
    };
  },
  methods: {
    init(id) {
      if (id) {
        this.$http({url: `hyEmployee/info/${id}`, method: "get"}).then(({data}) => {
          if (data.code === 0) { this.ruleForm = data.data; this.ruleForm.password = ""; }
        });
      }
    },
    onSubmit() {
      this.$refs.ruleForm.validate(valid => {
        if (!valid) return;
        this.$http({url: `hyEmployee/${this.ruleForm.id ? "update" : "save"}`, method: "post", data: this.ruleForm}).then(({data}) => {
          if (data.code === 0) this.$message({message: "操作成功", type: "success", duration: 1200, onClose: () => this.back(true)});
          else this.$message.error(data.msg);
        });
      });
    },
    back(refresh) {
      this.parent.showFlag = true;
      this.parent.addOrUpdateFlag = false;
      if (refresh) this.parent.loadEmp();
    }
  }
};
</script>

<style lang="scss" scoped>
.addEdit-block { margin: -10px; }
.addEdit-head { display: flex; align-items: center; padding: 12px 16px; border-bottom: 1px solid #eee; }
.back-arrow { font-size: 20px; cursor: pointer; color: #2F6BFF; }
.addEdit-title { font-size: 16px; font-weight: 600; margin-left: 8px; cursor: pointer; }
.detail-form-content { padding: 20px; }
</style>
