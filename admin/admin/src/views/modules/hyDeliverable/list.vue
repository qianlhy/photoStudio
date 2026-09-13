<template>
  <div class="main-content">
    <!-- 筛选 -->
    <el-form :inline="true" :model="searchForm" class="form-content">
      <el-row :gutter="20" class="slt">
        <el-form-item label="客户名称"><el-input v-model="searchForm.customerName" placeholder="客户名称" clearable></el-input></el-form-item>
        <el-form-item label="订单编号"><el-input v-model="searchForm.orderNo" placeholder="订单编号" clearable></el-input></el-form-item>
        <el-form-item label="行业分类"><el-input v-model="searchForm.industry" placeholder="行业" clearable></el-input></el-form-item>
        <el-form-item label="是否下载">
          <el-select v-model="searchForm.download" placeholder="请选择" clearable style="width:120px">
            <el-option label="已下载" value="已下载"></el-option>
            <el-option label="未下载" value="未下载"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="查看状态">
          <el-select v-model="searchForm.viewed" placeholder="请选择" clearable style="width:120px">
            <el-option label="已查看" value="已查看"></el-option>
            <el-option label="未查看" value="未查看"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="是否优质作品">
          <el-select v-model="searchForm.quality" placeholder="请选择" clearable style="width:120px">
            <el-option label="是" value="yes"></el-option>
            <el-option label="否" value="no"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button icon="el-icon-search" type="primary" @click="search()">查询</el-button>
          <el-button @click="reset()">重置</el-button>
          <el-button type="success" icon="el-icon-upload2" @click="openUpload()">上传成品</el-button>
          <el-button icon="el-icon-download" @click="exportReport()">导出</el-button>
        </el-form-item>
      </el-row>
    </el-form>

    <div class="dl-tabs">
      <div class="dt" :class="{on:activeTab==='all'}" @click="switchTab('all')">全部成品 <span class="dt-n">{{ counts.all }}</span></div>
      <div class="dt" :class="{on:activeTab==='quality'}" @click="switchTab('quality')">已标记优质 <span class="dt-n">{{ counts.quality }}</span></div>
      <div class="dt" :class="{on:activeTab==='recycle'}" @click="switchTab('recycle')">待回收 <span class="dt-n">{{ counts.recycle }}</span></div>
    </div>

    <div class="dl-body">
      <div class="dl-left">
        <div class="table-content">
          <el-table class="tables" :data="dataList" v-loading="dataListLoading" border
                    highlight-current-row @current-change="openDetail" style="width: 100%">
            <el-table-column label="作品名称" min-width="200">
              <template slot-scope="s">
                <div class="dl-work">
                  <div class="dl-thumb"><img v-if="s.row.cover" :src="img(s.row.cover)"/><span class="dl-dur">{{ dur(s.row.duration) }}</span></div>
                  <div class="dl-wt">{{ s.row.title }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="客户/行业" align="center" width="130">
              <template slot-scope="s">{{ s.row.customerName }}<div class="small">{{ s.row.industry }}</div></template>
            </el-table-column>
            <el-table-column prop="orderNo" label="关联订单" align="center" width="140"></el-table-column>
            <el-table-column prop="contentType" label="类型" align="center" width="90"></el-table-column>
            <el-table-column prop="shooterName" label="拍摄" align="center" width="90"></el-table-column>
            <el-table-column prop="editorName" label="剪辑" align="center" width="90"></el-table-column>
            <el-table-column label="上传日期" align="center" width="105">
              <template slot-scope="s">{{ (s.row.addtime||'').substr(0,10) }}</template>
            </el-table-column>
            <el-table-column label="下载状态" align="center" width="100">
              <template slot-scope="s">
                <el-tag size="mini" :type="s.row.downloadStatus==='已下载'?'success':'info'">{{ s.row.downloadStatus||'未下载' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="查看" align="center" width="90">
              <template slot-scope="s">
                <el-tag size="mini" :type="s.row.viewStatus==='已查看'?'success':'info'">{{ s.row.viewStatus||'未查看' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="满意度" align="center" width="110">
              <template slot-scope="s">
                <span v-if="s.row.satisfaction">{{ s.row.satisfaction }} 星</span>
                <span v-else class="small">未评价</span>
              </template>
            </el-table-column>
            <el-table-column label="客户评价" min-width="140" show-overflow-tooltip>
              <template slot-scope="s">{{ s.row.customerComment || '—' }}</template>
            </el-table-column>
            <el-table-column label="优质" align="center" width="80">
              <template slot-scope="s"><el-tag v-if="s.row.qualityFlag===1" size="mini" type="warning">优质</el-tag><span v-else>—</span></template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="160" fixed="right">
              <template slot-scope="s">
                <el-button type="text" size="small" @click="openDetail(s.row)">查看</el-button>
                <el-button type="text" size="small" @click="preview(s.row)">预览</el-button>
                <el-button type="text" size="small" @click="removeOne(s.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination @size-change="v=>{pageSize=v;getDataList()}" @current-change="v=>{pageIndex=v;getDataList()}"
                         :current-page="pageIndex" :page-sizes="[10,20,50]" :page-size="pageSize"
                         :total="totalPage" layout="total, sizes, prev, pager, next, jumper"></el-pagination>
        </div>
      </div>

      <div class="dl-right">
        <div class="side-panel" v-if="detail.id">
          <div class="sp-head"><span>作品详情</span><i class="el-icon-close" @click="detail={}"></i></div>
          <div class="sp-body">
            <div class="dw-head">
              <div class="dl-thumb big"><img v-if="detail.cover" :src="img(detail.cover)"/><span class="dl-dur">{{ dur(detail.duration) }}</span></div>
              <div class="dw-meta">
                <div class="dw-title">{{ detail.title }}</div>
                <div class="dw-sub">{{ detail.orderNo }}</div>
                <div class="dw-sub">客户：{{ detail.customerName }}</div>
                <div class="dw-sub">类型：{{ detail.contentType || '—' }}</div>
              </div>
            </div>
            <div class="sec-title">创作归属</div>
            <div class="d-row"><span class="d-l">拍摄人员</span><span class="d-v">{{ detail.shooterName || '—' }}</span></div>
            <div class="d-row"><span class="d-l">剪辑人员</span><span class="d-v">{{ detail.editorName || '—' }}</span></div>
            <div class="sec-title">客户反馈</div>
            <div class="d-row"><span class="d-l">查看状态</span><span class="d-v">{{ detail.viewStatus || '未查看' }}</span></div>
            <div class="d-row"><span class="d-l">下载状态</span><span class="d-v">{{ detail.downloadStatus || '未下载' }}</span></div>
            <div class="d-row"><span class="d-l">满意度</span><span class="d-v">{{ detail.satisfaction ? (detail.satisfaction + ' 星') : '未评价' }}</span></div>
            <div class="d-row" style="align-items:flex-start"><span class="d-l">评价</span><span class="d-v" style="max-width:160px;text-align:right;word-break:break-all">{{ detail.customerComment || '—' }}</span></div>
            <div class="sec-title">可复用价值说明</div>
            <el-input type="textarea" :rows="3" v-model="detail.reuseValue" placeholder="如：适合餐饮门店获客"></el-input>
            <div class="sp-label" style="margin:12px 0 6px">适用行业（逗号分隔）</div>
            <el-input v-model="detail.applyIndustry" placeholder="如：餐饮,零售"></el-input>
          </div>
          <div class="sp-foot-line">
            <el-button @click="preview(detail)">预览作品</el-button>
            <el-button type="primary" @click="saveReuse()">加入优质案例库</el-button>
          </div>
        </div>
        <div class="side-panel" v-else>
          <div class="sp-empty"><i class="el-icon-film"></i><p>点击左侧作品查看详情<br/>或点上方「上传成品」</p></div>
        </div>
      </div>
    </div>

    <!-- 上传成品 -->
    <el-dialog title="上传成品" :visible.sync="uploadVisible" width="640px" :close-on-click-modal="false">
      <el-form ref="uploadForm" :model="uploadForm" :rules="uploadRules" label-width="100px">
        <el-form-item label="关联客户" prop="customerId">
          <el-select v-model="uploadForm.customerId" filterable placeholder="请选择客户" style="width:100%" @change="onCustomerChange">
            <el-option v-for="c in customers" :key="c.id" :label="c.name + (c.phone ? ' · ' + c.phone : '')" :value="c.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="关联订单" prop="orderId">
          <el-select v-model="uploadForm.orderId" filterable clearable placeholder="建议选择订单（回写完成进度）" style="width:100%" @change="onOrderChange">
            <el-option v-for="o in orders" :key="o.id"
                       :label="(o.orderNo || o.id) + ' · ' + (o.status || '') + ' · ' + (o.completedCount||0) + '/' + (o.videoCount||0)"
                       :value="o.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="作品标题" prop="title">
          <el-input v-model="uploadForm.title" placeholder="如：刘合印-锅底熬制成片"></el-input>
        </el-form-item>
        <el-form-item label="内容类型">
          <el-select v-model="uploadForm.contentType" placeholder="选填" clearable style="width:100%">
            <el-option v-for="t in typeOptions" :key="t" :label="t" :value="t"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="封面图">
          <file-upload tip="点击上传封面（jpg/png）" action="file/upload" :limit="1" :multiple="false"
                       :fileUrls="uploadForm.cover || ''" @change="v => uploadForm.cover = v"></file-upload>
        </el-form-item>
        <el-form-item label="成品视频" prop="video">
          <file-upload mode="video" tip="点击上传成品视频（必填，支持 mp4 等）" action="file/upload" :limit="1"
                       :fileUrls="uploadForm.video || ''" @change="v => uploadForm.video = v"></file-upload>
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="时长(秒)">
              <el-input-number v-model="uploadForm.duration" :min="0" :max="3600" style="width:100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拍摄人员">
              <el-input v-model="uploadForm.shooterName" placeholder="选填"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="剪辑人员">
          <el-input v-model="uploadForm.editorName" placeholder="选填"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="uploadVisible=false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitUpload()">确认上传</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      searchForm: {customerName: "", orderNo: "", industry: "", download: "", viewed: "", quality: ""},
      activeTab: "all",
      counts: {all: 0, quality: 0, recycle: 0},
      dataList: [],
      pageIndex: 1, pageSize: 10, totalPage: 0, dataListLoading: false,
      detail: {},
      uploadVisible: false,
      saving: false,
      customers: [],
      orders: [],
      typeOptions: ["硬广", "厨过程", "教知识", "说观点", "讲故事"],
      uploadForm: this.emptyUpload(),
      uploadRules: {
        customerId: [{required: true, message: "请选择客户", trigger: "change"}],
        title: [{required: true, message: "请填写作品标题", trigger: "blur"}],
        video: [{required: true, message: "请上传成品视频", trigger: "change"}]
      }
    };
  },
  created() {
    this.getDataList();
    this.loadCounts();
    this.loadCustomers();
  },
  methods: {
    emptyUpload() {
      return {
        customerId: null, customerName: "", orderId: null, orderNo: "",
        title: "", cover: "", video: "", duration: 0, contentType: "",
        shooterName: "", editorName: "", industry: ""
      };
    },
    img(v) {
      if (!v) return "";
      const first = String(v).split(",")[0];
      if (/^https?:\/\//i.test(first)) return first;
      const rel = first.startsWith("upload/") ? first : first.replace(/^\//, "");
      return this.$base.url + rel;
    },
    dur(sec) {
      sec = Number(sec) || 0;
      const m = Math.floor(sec / 60), s = sec % 60;
      return (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
    },
    search() { this.pageIndex = 1; this.getDataList(); },
    switchTab(t) { this.activeTab = t; this.pageIndex = 1; this.detail = {}; this.getDataList(); },
    reset() {
      this.searchForm = {customerName: "", orderNo: "", industry: "", download: "", viewed: "", quality: ""};
      this.search();
    },
    loadCustomers() {
      this.$http({url: "hyCustomer/page", method: "get", params: {page: 1, limit: 500}}).then(({data}) => {
        if (data.code === 0) this.customers = data.data.list || [];
      });
    },
    loadOrdersByCustomer(customerId) {
      if (!customerId) { this.orders = []; return; }
      this.$http({url: "hyOrder/page", method: "get", params: {page: 1, limit: 100, customerId}}).then(({data}) => {
        this.orders = data.code === 0 ? (data.data.list || []) : [];
      });
    },
    loadCounts() {
      this.$http({url: "hyDeliverable/page", method: "get", params: {page: 1, limit: 2000}}).then(({data}) => {
        if (data.code !== 0) return;
        const l = data.data.list || [];
        this.counts.all = data.data.total;
        this.counts.quality = l.filter(d => d.qualityFlag === 1).length;
        this.counts.recycle = l.filter(d => (d.downloadStatus !== "已下载")).length;
      });
    },
    getDataList() {
      this.dataListLoading = true;
      let params = {page: this.pageIndex, limit: this.pageSize};
      if (this.searchForm.customerName) params.customerName = "%" + this.searchForm.customerName + "%";
      if (this.searchForm.orderNo) params.orderNo = "%" + this.searchForm.orderNo + "%";
      if (this.searchForm.industry) params.industry = "%" + this.searchForm.industry + "%";
      if (this.activeTab === "quality") params.qualityFlag = 1;
      this.$http({url: "hyDeliverable/page", method: "get", params}).then(({data}) => {
        let list = (data.code === 0 ? data.data.list : []) || [];
        if (this.activeTab === "recycle") list = list.filter(d => d.downloadStatus !== "已下载");
        if (this.searchForm.download) list = list.filter(d => (d.downloadStatus || "未下载") === this.searchForm.download);
        if (this.searchForm.viewed) list = list.filter(d => (d.viewStatus || "未查看") === this.searchForm.viewed);
        if (this.searchForm.quality === "yes") list = list.filter(d => d.qualityFlag === 1);
        else if (this.searchForm.quality === "no") list = list.filter(d => d.qualityFlag !== 1);
        this.dataList = list;
        this.totalPage = data.code === 0 ? data.data.total : 0;
        this.dataListLoading = false;
      });
    },
    openDetail(row) {
      if (!row || !row.id) return;
      this.detail = Object.assign({}, row);
    },
    openUpload() {
      this.uploadForm = this.emptyUpload();
      this.uploadVisible = true;
      this.$nextTick(() => this.$refs.uploadForm && this.$refs.uploadForm.clearValidate());
    },
    onCustomerChange(id) {
      const c = this.customers.find(x => x.id === id);
      this.uploadForm.customerName = c ? c.name : "";
      this.uploadForm.industry = c ? (c.industry || "") : "";
      this.uploadForm.orderId = null;
      this.uploadForm.orderNo = "";
      this.loadOrdersByCustomer(id);
      if (!this.uploadForm.title && c) {
        this.uploadForm.title = c.name + " · 交付视频";
      }
    },
    onOrderChange(id) {
      const o = this.orders.find(x => x.id === id);
      if (!o) { this.uploadForm.orderNo = ""; return; }
      this.uploadForm.orderNo = o.orderNo || "";
      if (o.shooterName) this.uploadForm.shooterName = o.shooterName;
      if (o.editorName) this.uploadForm.editorName = o.editorName;
      if (!this.uploadForm.customerId && o.customerId) {
        this.uploadForm.customerId = o.customerId;
        this.uploadForm.customerName = o.customerName;
      }
    },
    submitUpload() {
      this.$refs.uploadForm.validate(valid => {
        if (!valid) return;
        if (!this.uploadForm.video) {
          this.$message.warning("请上传成品视频");
          return;
        }
        this.saving = true;
        const payload = Object.assign({}, this.uploadForm, {
          status: "上架",
          viewStatus: "未查看",
          downloadStatus: "未下载",
          qualityFlag: 0
        });
        this.$http({url: "hyDeliverable/save", method: "post", data: payload}).then(({data}) => {
          this.saving = false;
          if (data.code === 0) {
            this.$message.success("上传成功，客户小程序「我的内容」可查看下载");
            this.uploadVisible = false;
            this.getDataList();
            this.loadCounts();
          } else {
            this.$message.error(data.msg || "上传失败");
          }
        }).catch(() => { this.saving = false; });
      });
    },
    saveReuse() {
      if (!this.detail.id) return;
      this.$http({
        url: "hyDeliverable/update", method: "post",
        data: {id: this.detail.id, reuseValue: this.detail.reuseValue, applyIndustry: this.detail.applyIndustry, qualityFlag: 1}
      }).then(({data}) => {
        if (data.code === 0) { this.$message.success("已加入优质案例库"); this.getDataList(); this.loadCounts(); }
        else this.$message.error(data.msg);
      });
    },
    removeOne(row) {
      this.$confirm("确定删除该成品？客户将无法再下载。", "提示", {type: "warning"}).then(() => {
        this.$http({url: "hyDeliverable/delete", method: "post", data: [Number(row.id)]}).then(({data}) => {
          if (data.code === 0) { this.$message.success("已删除"); this.detail = {}; this.getDataList(); this.loadCounts(); }
          else this.$message.error(data.msg);
        });
      }).catch(() => {});
    },
    exportReport() {
      const head = ["作品名称", "客户", "订单号", "类型", "拍摄", "剪辑", "查看状态", "下载状态", "满意度", "客户评价", "优质"];
      const rows = this.dataList.map(d => [
        d.title, d.customerName, d.orderNo, d.contentType, d.shooterName, d.editorName,
        d.viewStatus || "未查看", d.downloadStatus || "未下载",
        d.satisfaction || "", (d.customerComment || "").replace(/,/g, "，"),
        d.qualityFlag === 1 ? "是" : "否"
      ]);
      let csv = "\ufeff" + head.join(",") + "\n" + rows.map(r => r.join(",")).join("\n");
      const blob = new Blob([csv], {type: "text/csv"});
      const a = document.createElement("a");
      a.href = URL.createObjectURL(blob);
      a.download = "成品报表.csv";
      a.click();
    },
    preview(row) {
      if (!row || !row.video) { this.$message.info("暂无视频"); return; }
      window.open(this.img(row.video));
    }
  }
};
</script>

<style scoped>
.main-content { padding-right: 316px; }
.small { font-size: 12px; color: #8A94A6; }
.dl-tabs { display: flex; gap: 28px; padding: 0 4px 12px; border-bottom: 1px solid #EBEEF5; margin-bottom: 14px; }
.dt { position: relative; font-size: 15px; color: #5a6473; cursor: pointer; padding-bottom: 12px; margin-bottom: -13px; }
.dt .dt-n { color: #a8b0bd; font-size: 13px; margin-left: 2px; }
.dt.on { color: #2F6BFF; font-weight: 600; }
.dt.on .dt-n { color: #2F6BFF; }
.dt.on::after { content: ""; position: absolute; left: 0; right: 0; bottom: 0; height: 2px; background: #2F6BFF; border-radius: 2px; }

.dl-body { display: flex; gap: 16px; align-items: flex-start; }
.dl-left { flex: 1; min-width: 0; }
.dl-right { position:fixed; top:60px; right:0; bottom:0; z-index:30; width:300px; background:#fff; border-left:1px solid #E7ECF3; }

.dl-work { display: flex; align-items: center; gap: 10px; }
.dl-thumb { position: relative; width: 54px; height: 40px; border-radius: 6px; overflow: hidden; background: #1F2733; flex-shrink: 0; }
.dl-thumb img { width: 100%; height: 100%; object-fit: cover; }
.dl-thumb.big { width: 90px; height: 66px; }
.dl-dur { position: absolute; right: 3px; bottom: 2px; background: rgba(0,0,0,.65); color: #fff; font-size: 10px; padding: 0 3px; border-radius: 3px; }
.dl-wt { font-weight: 500; color: #1F2733; }

.side-panel { height:100%; box-sizing:border-box; display:flex; flex-direction:column; background:#fff; overflow:hidden; }
.sp-head { display: flex; align-items: center; justify-content: space-between; padding: 14px 18px; font-size: 15px; font-weight: 700; color: #1F2733; border-bottom: 1px solid #EEF1F5; }
.sp-head i { cursor: pointer; color: #a8b0bd; }
.sp-body { padding: 16px 18px; flex:1; overflow:auto; }
.sp-label { font-size: 13px; color: #8A94A6; }
.dw-head { display: flex; gap: 12px; margin-bottom: 8px; }
.dw-title { font-size: 15px; font-weight: 700; color: #1F2733; margin-bottom: 4px; }
.dw-sub { font-size: 12px; color: #8A94A6; line-height: 1.7; }
.sec-title { font-size: 14px; font-weight: 700; color: #1F2733; margin: 16px 0 8px; padding-left: 8px; border-left: 3px solid #2F6BFF; }
.d-row { display: flex; justify-content: space-between; align-items: center; padding: 7px 0; font-size: 13px; }
.d-l { color: #8A94A6; }
.d-v { color: #1F2733; font-weight: 500; }
.sp-foot-line { padding: 12px 18px 16px; display: flex; gap: 8px; }
.sp-foot-line .el-button { flex: 1; }
.sp-empty { padding: 60px 24px; text-align: center; color: #a8b0bd; }
.sp-empty i { font-size: 40px; margin-bottom: 12px; }
.sp-empty p { font-size: 13px; line-height: 1.7; }
</style>
