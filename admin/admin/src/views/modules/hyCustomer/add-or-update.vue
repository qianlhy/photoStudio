<template>
  <div class="addEdit-block">
    <div class="addEdit-head">
      <i class="el-icon-arrow-left back-arrow" @click="back()"></i>
      <span class="addEdit-title" @click="back()">{{ type === 'info' ? '查看客户' : (ruleForm.id ? '编辑客户' : '新增客户') }}</span>
    </div>
    <el-form class="detail-form-content" ref="ruleForm" :model="ruleForm" :rules="rules" label-width="110px" :disabled="type==='info'">
      <el-row>
        <el-col :span="12"><el-form-item label="客户名称" prop="name"><el-input v-model="ruleForm.name"></el-input></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="联系人" prop="contact"><el-input v-model="ruleForm.contact"></el-input></el-form-item></el-col>
      </el-row>
      <el-row>
        <el-col :span="12"><el-form-item label="手机号" prop="phone"><el-input v-model="ruleForm.phone"></el-input></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="所属行业"><el-input v-model="ruleForm.industry"></el-input></el-form-item></el-col>
      </el-row>
      <el-row>
        <el-col :span="12"><el-form-item label="业务/业态"><el-input v-model="ruleForm.biztype"></el-input></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="规模"><el-input v-model="ruleForm.scale"></el-input></el-form-item></el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="业务经理">
            <el-select v-model="ruleForm.managerId" placeholder="请选择" @change="onManager" clearable style="width:100%">
              <el-option v-for="m in managers" :key="m.id" :label="m.name" :value="m.id"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="跟进状态">
            <el-select v-model="ruleForm.followStatus" placeholder="请选择" clearable style="width:100%">
              <el-option v-for="s in ['跟进中','已成交','待付款','已流失']" :key="s" :label="s" :value="s"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="意向">
            <el-select v-model="ruleForm.intention" placeholder="请选择" clearable style="width:100%">
              <el-option v-for="s in ['高','中','低']" :key="s" :label="s" :value="s"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12"><el-form-item label="满意度"><el-rate v-model="ruleForm.satisfaction"></el-rate></el-form-item></el-col>
      </el-row>
      <el-row>
        <el-col :span="12"><el-form-item label="客户标签"><el-input v-model="ruleForm.tags" placeholder="逗号分隔，如：重点客户,长期合作"></el-input></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="内容偏好"><el-input v-model="ruleForm.preference" placeholder="逗号分隔，如：硬广,真实烟火气"></el-input></el-form-item></el-col>
      </el-row>
      <el-form-item>
        <el-button v-if="type!=='info'" type="primary" @click="onSubmit">提交</el-button>
        <el-button @click="back()">{{ type==='info' ? '返回' : '取消' }}</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  props: ["parent"],
  data() {
    return {
      id: "",
      type: "",
      managers: [],
      ruleForm: {name: "", contact: "", phone: "", industry: "", biztype: "", scale: "", managerId: null, managerName: "", followStatus: "跟进中", intention: "中", satisfaction: 5, tags: "", preference: ""},
      rules: {
        name: [{required: true, message: "客户名称不能为空", trigger: "blur"}],
        phone: [{required: true, message: "手机号不能为空", trigger: "blur"}]
      }
    };
  },
  created() {
    this.$http({url: "hyEmployee/page", method: "get", params: {page: 1, limit: 100, role: "销售经理"}}).then(({data}) => {
      if (data.code === 0) this.managers = data.data.list || [];
    });
  },
  methods: {
    init(id, type) {
      if (id) { this.id = id; this.type = type; this.info(id); }
    },
    info(id) {
      this.$http({url: `hyCustomer/info/${id}`, method: "get"}).then(({data}) => {
        if (data.code === 0) this.ruleForm = data.data;
      });
    },
    onManager(val) {
      const m = this.managers.find(m => m.id === val);
      this.ruleForm.managerName = m ? m.name : "";
    },
    onSubmit() {
      this.$refs.ruleForm.validate(valid => {
        if (!valid) return;
        this.$http({url: `hyCustomer/${this.ruleForm.id ? "update" : "save"}`, method: "post", data: this.ruleForm}).then(({data}) => {
          if (data.code === 0) {
            this.$message({message: "操作成功", type: "success", duration: 1200, onClose: () => this.back(true)});
          } else this.$message.error(data.msg);
        });
      });
    },
    back(refresh) {
      this.parent.showFlag = true;
      this.parent.addOrUpdateFlag = false;
      if (refresh) { this.parent.search(); this.parent.loadStat(); }
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
