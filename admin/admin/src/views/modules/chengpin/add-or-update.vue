<template>
  <div class="addEdit-block">
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="120px" class="add-update-preview"
             :disabled="info">
      <el-form-item label="关联订单" prop="dingdanid">
        <el-select v-model="ruleForm.dingdanid" placeholder="选择已完成拍摄的订单" filterable @change="onOrderChange"
                   style="width: 100%">
          <el-option v-for="o in orderList" :key="o.id"
                     :label="`${o.dingdanbianhao} | ${o.xingming} | ${o.taocanmingcheng}`" :value="o.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="成品标题" prop="biaoti">
        <el-input v-model="ruleForm.biaoti" placeholder="如 张三-古风写真成片"></el-input>
      </el-form-item>
      <el-form-item label="成品图片" prop="tupian">
        <file-upload tip="上传成品照片(可多张)" action="file/upload" :limit="30" :multiple="true" :fileUrls="ruleForm.tupian"
                     @change="tupianUploadChange"></file-upload>
      </el-form-item>
      <el-form-item label="成品视频">
        <file-upload tip="上传成品视频" action="file/upload" :limit="2" :multiple="true" :fileUrls="ruleForm.shipin"
                     @change="shipinUploadChange"></file-upload>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="ruleForm.shangxiajia" placeholder="状态">
          <el-option label="上架" value="上架"></el-option>
          <el-option label="下架" value="下架"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input type="textarea" v-model="ruleForm.beizhu" placeholder="备注"></el-input>
      </el-form-item>
      <el-form-item class="btn">
        <el-button v-if="!info" type="primary" class="btn-success" @click="onSubmit">提交</el-button>
        <el-button class="btn-close" @click="back()">{{ info ? '返回' : '取消' }}</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      info: false,
      orderList: [],
      ruleForm: {
        dingdanid: "",
        dingdanbianhao: "",
        taocanmingcheng: "",
        userid: "",
        xingming: "",
        biaoti: "",
        tupian: "",
        shipin: "",
        shangxiajia: "上架",
        beizhu: ""
      },
      rules: {
        dingdanid: [{required: true, message: "请选择关联订单", trigger: "change"}],
        tupian: [{required: true, message: "请上传成品图片", trigger: "blur"}]
      }
    };
  },
  props: ["parent"],
  created() {
    this.loadOrders();
  },
  methods: {
    loadOrders() {
      this.$http({
        url: "dingdan/page",
        method: "get",
        params: {page: 1, limit: 1000, sort: "addtime", order: "desc"}
      }).then(({data}) => {
        if (data && data.code === 0) {
          this.orderList = data.data.list.filter(o => o.zhuangtai !== "已取消");
        }
      });
    },
    onOrderChange(id) {
      let o = this.orderList.find(item => item.id === id);
      if (o) {
        this.ruleForm.dingdanbianhao = o.dingdanbianhao;
        this.ruleForm.taocanmingcheng = o.taocanmingcheng;
        this.ruleForm.userid = o.userid;
        this.ruleForm.xingming = o.xingming;
        if (!this.ruleForm.biaoti) {
          this.ruleForm.biaoti = `${o.xingming || ''}-${o.taocanmingcheng || ''}成片`;
        }
      }
    },
    init(id, type) {
      this.info = type === "info";
      if (id) {
        this.$http({url: `chengpin/info/${id}`, method: "get"}).then(({data}) => {
          if (data && data.code === 0) {
            this.ruleForm = data.data;
          }
        });
      }
    },
    tupianUploadChange(fileUrls) {
      this.ruleForm.tupian = fileUrls;
    },
    shipinUploadChange(fileUrls) {
      this.ruleForm.shipin = fileUrls;
    },
    onSubmit() {
      this.$refs["ruleForm"].validate(valid => {
        if (valid) {
          this.$http({
            url: `chengpin/${!this.ruleForm.id ? "save" : "update"}`,
            method: "post",
            data: this.ruleForm
          }).then(({data}) => {
            if (data && data.code === 0) {
              this.$message({
                message: "操作成功，已通知客户成品上线",
                type: "success",
                duration: 1500,
                onClose: () => {
                  this.parent.showFlag = true;
                  this.parent.addOrUpdateFlag = false;
                  this.parent.search();
                }
              });
            } else {
              this.$message.error(data.msg);
            }
          });
        }
      });
    },
    back() {
      this.parent.showFlag = true;
      this.parent.addOrUpdateFlag = false;
    }
  }
};
</script>

<style lang="scss" scoped>
.add-update-preview {
  padding: 20px;
}
</style>
