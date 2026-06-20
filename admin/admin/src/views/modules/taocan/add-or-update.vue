<template>
  <div class="addEdit-block">
    <div class="addEdit-head">
      <i class="el-icon-arrow-left back-arrow" @click="back()"></i>
      <span class="addEdit-title" @click="back()">{{ info ? '查看详情' : (ruleForm.id ? '编辑' : '新增') }}</span>
    </div>
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="120px" class="add-update-preview"
             :disabled="info">
      <el-form-item label="套餐名称" prop="taocanmingcheng">
        <el-input v-model="ruleForm.taocanmingcheng" placeholder="套餐名称"></el-input>
      </el-form-item>
      <el-form-item label="品类" prop="pinlei">
        <el-select v-model="ruleForm.pinlei" placeholder="请选择品类">
          <el-option label="写真" value="写真"></el-option>
          <el-option label="宣传片" value="宣传片"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="风格" prop="fengge">
        <el-select v-model="ruleForm.fengge" placeholder="请选择风格" filterable allow-create>
          <el-option v-for="(item, idx) in fenggeList" :key="idx" :label="item.leixing" :value="item.leixing"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="封面图" prop="fengmian">
        <file-upload tip="点击上传封面(可多张)" action="file/upload" :limit="6" :multiple="true" :fileUrls="ruleForm.fengmian"
                     @change="fengmianUploadChange"></file-upload>
      </el-form-item>
      <el-form-item label="样片视频">
        <file-upload tip="点击上传视频" action="file/upload" :limit="1" :multiple="false" :fileUrls="ruleForm.shipin"
                     @change="shipinUploadChange"></file-upload>
      </el-form-item>
      <el-form-item label="实拍图">
        <file-upload tip="点击上传实拍样片(可多张)" action="file/upload" :limit="9" :multiple="true" :fileUrls="ruleForm.shctp"
                     @change="shctpUploadChange"></file-upload>
      </el-form-item>
      <el-form-item label="线下标价" prop="xianxiabiaojia">
        <el-input v-model="ruleForm.xianxiabiaojia" placeholder="线下标价"></el-input>
      </el-form-item>
      <el-form-item label="服装数量">
        <el-input v-model="ruleForm.fuzhuangshuliang" placeholder="服装数量"></el-input>
      </el-form-item>
      <el-form-item label="精修张数">
        <el-input v-model="ruleForm.jingxiuzhangshu" placeholder="精修张数"></el-input>
      </el-form-item>
      <el-form-item label="拍摄时长">
        <el-input v-model="ruleForm.paishishichang" placeholder="如 3小时"></el-input>
      </el-form-item>
      <el-form-item label="可升级项目">
        <el-input v-model="ruleForm.shengjixiangmu" placeholder="可升级项目"></el-input>
      </el-form-item>
      <el-form-item label="排序">
        <el-input v-model="ruleForm.paixu" placeholder="数字越小越靠前"></el-input>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="ruleForm.shangxiajia" placeholder="上架状态">
          <el-option label="上架" value="上架"></el-option>
          <el-option label="下架" value="下架"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="套餐介绍">
        <editor v-model="ruleForm.jianjie" class="editor" action="file/upload"></editor>
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
      id: 0,
      info: false,
      fenggeList: [],
      ruleForm: {
        taocanmingcheng: "",
        pinlei: "",
        fengge: "",
        fengmian: "",
        shipin: "",
        shctp: "",
        jianjie: "",
        xianxiabiaojia: "",
        fuzhuangshuliang: "",
        jingxiuzhangshu: "",
        paishishichang: "",
        shengjixiangmu: "",
        paixu: 100,
        shangxiajia: "上架",
        clicknum: 0
      },
      rules: {
        taocanmingcheng: [{required: true, message: "请填写套餐名称", trigger: "blur"}],
        pinlei: [{required: true, message: "请选择品类", trigger: "change"}],
        fengmian: [{required: true, message: "请上传封面图", trigger: "blur"}],
        xianxiabiaojia: [{required: true, message: "请填写线下标价", trigger: "blur"}]
      }
    };
  },
  props: ["parent"],
  created() {
    this.loadFengge();
  },
  methods: {
    loadFengge() {
      this.$http({
        url: "leixing/page",
        method: "get",
        params: {page: 1, limit: 100}
      }).then(({data}) => {
        if (data && data.code === 0) {
          this.fenggeList = data.data.list;
        }
      });
    },
    init(id, type) {
      this.id = id || 0;
      this.info = type === "info";
      if (id) {
        this.$http({
          url: `taocan/info/${id}`,
          method: "get"
        }).then(({data}) => {
          if (data && data.code === 0) {
            this.ruleForm = data.data;
          }
        });
      }
    },
    fengmianUploadChange(fileUrls) {
      this.ruleForm.fengmian = fileUrls;
    },
    shipinUploadChange(fileUrls) {
      this.ruleForm.shipin = fileUrls;
    },
    shctpUploadChange(fileUrls) {
      this.ruleForm.shctp = fileUrls;
    },
    onSubmit() {
      this.$refs["ruleForm"].validate(valid => {
        if (valid) {
          this.$http({
            url: `taocan/${!this.ruleForm.id ? "save" : "update"}`,
            method: "post",
            data: this.ruleForm
          }).then(({data}) => {
            if (data && data.code === 0) {
              this.$message({
                message: "操作成功",
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

.editor {
  height: 360px;
}
</style>
