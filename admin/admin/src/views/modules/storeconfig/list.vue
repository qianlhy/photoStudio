<template>
  <div class="main-content">
    <el-card shadow="never">
      <div slot="header"><b>门店信息</b></div>
      <el-form label-width="120px" style="max-width: 720px">
        <el-form-item label="门店名称">
          <el-input v-model="form.storeName"></el-input>
        </el-form-item>
        <el-form-item label="门店地址">
          <el-input v-model="form.storeAddress"></el-input>
        </el-form-item>
        <el-form-item label="营业时间">
          <el-input v-model="form.storeBusinessHours" placeholder="如 09:00-22:00"></el-input>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.storePhone"></el-input>
        </el-form-item>
        <el-form-item label="经度(lng)">
          <el-input v-model="form.storeLng"></el-input>
        </el-form-item>
        <el-form-item label="纬度(lat)">
          <el-input v-model="form.storeLat"></el-input>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">
      <div slot="header"><b>规则配置</b></div>
      <el-form label-width="120px" style="max-width: 720px">
        <el-form-item label="取消/扣费规则">
          <el-input type="textarea" :rows="3" v-model="form.cancelRule"></el-input>
        </el-form-item>
        <el-form-item label="排队规则">
          <el-input type="textarea" :rows="3" v-model="form.queueRule"></el-input>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">
      <div slot="header"><b>消息模板</b></div>
      <el-form label-width="120px" style="max-width: 720px">
        <el-form-item label="活动通知">
          <el-input type="textarea" :rows="2" v-model="form.msgTplActivity"
                    placeholder="可用占位符 {title} {content}"></el-input>
          <el-button size="mini" style="margin-top:8px" @click="previewTpl('msgTplActivity')">预览效果</el-button>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-input type="textarea" :rows="2" v-model="form.msgTplAudit"
                    placeholder="可用占位符 {result} {reason}"></el-input>
          <el-button size="mini" style="margin-top:8px" @click="previewTpl('msgTplAudit')">预览效果</el-button>
        </el-form-item>
        <el-form-item label="成品上线">
          <el-input type="textarea" :rows="2" v-model="form.msgTplFinish" placeholder="可用占位符 {order}"></el-input>
          <el-button size="mini" style="margin-top:8px" @click="previewTpl('msgTplFinish')">预览效果</el-button>
        </el-form-item>
        <el-form-item label="档期变更">
          <el-input type="textarea" :rows="2" v-model="form.msgTplSchedule"
                    placeholder="可用占位符 {order} {date}"></el-input>
          <el-button size="mini" style="margin-top:8px" @click="previewTpl('msgTplSchedule')">预览效果</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div style="margin-top: 20px; text-align: center">
      <el-button type="primary" :loading="saving" @click="saveAll">保存全部配置</el-button>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      keys: ["storeName", "storeAddress", "storeBusinessHours", "storePhone", "storeLng", "storeLat",
        "cancelRule", "queueRule", "msgTplAudit", "msgTplFinish", "msgTplSchedule", "msgTplActivity"],
      form: {},
      idMap: {},
      saving: false
    };
  },
  created() {
    this.loadAll();
  },
  methods: {
    loadAll() {
      this.keys.forEach(key => {
        this.$http({url: "config/info", method: "get", params: {name: key}}).then(({data}) => {
          if (data && data.code === 0 && data.data) {
            this.$set(this.form, key, data.data.value);
            this.$set(this.idMap, key, data.data.id);
          } else {
            this.$set(this.form, key, "");
          }
        });
      });
    },
    async saveAll() {
      this.saving = true;
      try {
        for (let key of this.keys) {
          let payload = {name: key, value: this.form[key] || ""};
          if (this.idMap[key]) {
            payload.id = this.idMap[key];
            await this.$http({url: "config/update", method: "post", data: payload});
          } else {
            await this.$http({url: "config/save", method: "post", data: payload});
          }
        }
        this.$message.success("配置已保存");
        this.loadAll();
      } catch (e) {
        this.$message.error("保存失败");
      }
      this.saving = false;
    },
    previewTpl(key) {
      let tpl = this.form[key] || '';
      let sample = {
        msgTplAudit: {result: '审核通过', reason: '欢迎光临'},
        msgTplFinish: {order: 'DD20260101001'},
        msgTplSchedule: {order: 'DD20260101001', date: '2026-06-20'},
        msgTplActivity: {title: '夏日特惠', content: '全场套餐8折，限时一周'}
      }[key] || {};
      Object.keys(sample).forEach(k => {
        tpl = tpl.replace(new RegExp('\\{' + k + '\\}', 'g'), sample[k]);
      });
      this.$alert(tpl || '（模板为空）', '模板预览', {confirmButtonText: '知道了'});
    }
  }
};
</script>
