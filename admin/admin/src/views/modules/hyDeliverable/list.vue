<template>
  <div class="main-content">
    <!-- 筛选 -->
    <el-form :inline="true" :model="searchForm" class="form-content">
      <el-row :gutter="20" class="slt">
        <el-form-item label="客户名称"><el-input v-model="searchForm.customerName" placeholder="请选择客户" clearable></el-input></el-form-item>
        <el-form-item label="订单编号"><el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable></el-input></el-form-item>
        <el-form-item label="行业分类"><el-input v-model="searchForm.industry" placeholder="请选择" clearable></el-input></el-form-item>
        <el-form-item label="拍摄人员"><el-input v-model="searchForm.shooterName" placeholder="请选择" clearable></el-input></el-form-item>
        <el-form-item label="剪辑人员"><el-input v-model="searchForm.editorName" placeholder="请选择" clearable></el-input></el-form-item>
        <el-form-item label="是否下载">
          <el-select v-model="searchForm.download" placeholder="请选择" clearable style="width:120px">
            <el-option label="已下载" value="已下载"></el-option>
            <el-option label="未下载" value="未下载"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="客户满意度">
          <el-select v-model="searchForm.satisfaction" placeholder="请选择" clearable style="width:120px">
            <el-option v-for="n in [5,4,3,2,1]" :key="n" :label="n+'星及以上'" :value="n"></el-option>
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
          <el-button icon="el-icon-download" @click="exportReport()">导出</el-button>
        </el-form-item>
      </el-row>
    </el-form>

    <!-- 标签 -->
    <div class="dl-tabs">
      <div class="dt" :class="{on:activeTab==='all'}" @click="switchTab('all')">全部成品 <span class="dt-n">{{ counts.all }}</span></div>
      <div class="dt" :class="{on:activeTab==='quality'}" @click="switchTab('quality')">已标记优质 <span class="dt-n">{{ counts.quality }}</span></div>
      <div class="dt" :class="{on:activeTab==='recycle'}" @click="switchTab('recycle')">待回收 <span class="dt-n">{{ counts.recycle }}</span></div>
    </div>

    <div class="dl-body">
      <!-- 左：表格 -->
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
            <el-table-column prop="shooterName" label="拍摄人员" align="center" width="90"></el-table-column>
            <el-table-column prop="editorName" label="剪辑人员" align="center" width="90"></el-table-column>
            <el-table-column label="上传日期" align="center" width="105">
              <template slot-scope="s">{{ (s.row.uploadDate||s.row.addtime||'').substr(0,10) }}</template>
            </el-table-column>
            <el-table-column label="查看状态" align="center" width="90">
              <template slot-scope="s"><el-tag size="mini" :type="s.row.viewStatus==='已查看'?'success':'info'">{{ s.row.viewStatus||'未查看' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="下载状态/日期" align="center" width="120">
              <template slot-scope="s">
                <el-tag size="mini" :type="s.row.downloadStatus==='已下载'?'success':'info'">{{ s.row.downloadStatus||'未下载' }}</el-tag>
                <div class="small" v-if="s.row.downloadTime">{{ (s.row.downloadTime||'').substr(0,10) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="满意度" align="center" width="120">
              <template slot-scope="s"><el-rate :value="Number(s.row.satisfaction)||0" disabled></el-rate></template>
            </el-table-column>
            <el-table-column label="优质标签" align="center" width="90">
              <template slot-scope="s"><el-tag v-if="s.row.qualityFlag===1" size="mini" type="warning">优质作品</el-tag><span v-else>—</span></template>
            </el-table-column>
            <el-table-column label="可复用价值" align="center" width="90">
              <template slot-scope="s"><el-tag v-if="s.row.reuseValue" size="mini" type="success">可复用</el-tag><span v-else>—</span></template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="120" fixed="right">
              <template slot-scope="s">
                <el-button type="text" size="small" @click="openDetail(s.row)">查看作品</el-button>
                <el-button type="text" size="small" @click="openDetail(s.row)">标记优质</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination @size-change="v=>{pageSize=v;getDataList()}" @current-change="v=>{pageIndex=v;getDataList()}"
                         :current-page="pageIndex" :page-sizes="[10,20,50]" :page-size="pageSize"
                         :total="totalPage" layout="total, sizes, prev, pager, next, jumper"></el-pagination>
        </div>
      </div>

      <!-- 右：作品详情面板 -->
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
                <div class="dw-sub">行业：{{ detail.industry }}</div>
              </div>
            </div>

            <div class="sec-title">客户反馈</div>
            <div class="d-row"><span class="d-l">满意度</span><el-rate :value="Number(detail.satisfaction)||0" disabled></el-rate></div>
            <div class="d-block">{{ detail.customerComment || '暂无客户评价' }}</div>

            <div class="sec-title">内部备注</div>
            <div class="d-block">{{ detail.remark || '暂无内部备注' }}</div>
            <div class="d-row"><span class="d-l">备注人</span><span class="d-v">{{ detail.remarkBy || '—' }}</span></div>
            <div class="d-row"><span class="d-l">备注日期</span><span class="d-v">{{ (detail.remarkDate||'').substr(0,10) || '—' }}</span></div>

            <div class="sec-title">创作归属</div>
            <div class="d-row"><span class="d-l">拍摄人员</span><span class="d-v"><span class="mgr-av av2">{{ (detail.shooterName||'—').charAt(0) }}</span>{{ detail.shooterName }}</span></div>
            <div class="d-row"><span class="d-l">剪辑人员</span><span class="d-v"><span class="mgr-av av3">{{ (detail.editorName||'—').charAt(0) }}</span>{{ detail.editorName }}</span></div>

            <div class="sec-title">可复用价值说明</div>
            <el-input type="textarea" :rows="3" v-model="detail.reuseValue" placeholder="如：适合餐饮门店获客，开场3秒抓人"></el-input>
            <div class="sp-label" style="margin:12px 0 6px">适用行业（逗号分隔）</div>
            <el-input v-model="detail.applyIndustry" placeholder="如：餐饮,零售"></el-input>
          </div>
          <div class="sp-foot-line">
            <el-button @click="preview(detail)" style="margin-right:8px">预览作品</el-button>
            <el-button type="primary" @click="saveReuse()">加入优质案例库</el-button>
          </div>
        </div>
        <div class="side-panel" v-else>
          <div class="sp-empty"><i class="el-icon-film"></i><p>点击左侧作品<br/>查看作品详情</p></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      searchForm: {customerName: "", orderNo: "", industry: "", shooterName: "", editorName: "", download: "", satisfaction: "", quality: ""},
      activeTab: "all",
      counts: {all: 0, quality: 0, recycle: 0},
      dataList: [],
      pageIndex: 1, pageSize: 10, totalPage: 0, dataListLoading: false,
      detail: {}
    };
  },
  created() { this.getDataList(); this.loadCounts(); },
  methods: {
    img(v) { return this.$base.url + String(v).split(",")[0]; },
    dur(sec) {
      sec = Number(sec) || 0;
      const m = Math.floor(sec / 60), s = sec % 60;
      return (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
    },
    search() { this.pageIndex = 1; this.getDataList(); },
    switchTab(t) { this.activeTab = t; this.pageIndex = 1; this.detail = {}; this.getDataList(); },
    reset() {
      this.searchForm = {customerName: "", orderNo: "", industry: "", shooterName: "", editorName: "", download: "", satisfaction: "", quality: ""};
      this.search();
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
      if (this.searchForm.shooterName) params.shooterName = "%" + this.searchForm.shooterName + "%";
      if (this.searchForm.editorName) params.editorName = "%" + this.searchForm.editorName + "%";
      if (this.activeTab === "quality") params.qualityFlag = 1;
      this.$http({url: "hyDeliverable/page", method: "get", params}).then(({data}) => {
        let list = (data.code === 0 ? data.data.list : []) || [];
        if (this.activeTab === "recycle") list = list.filter(d => d.downloadStatus !== "已下载");
        if (this.searchForm.download) list = list.filter(d => (d.downloadStatus || "未下载") === this.searchForm.download);
        if (this.searchForm.satisfaction) list = list.filter(d => (Number(d.satisfaction) || 0) >= this.searchForm.satisfaction);
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
    saveReuse() {
      if (!this.detail.id) return;
      this.$http({url: "hyDeliverable/update", method: "post", data: {id: this.detail.id, reuseValue: this.detail.reuseValue, applyIndustry: this.detail.applyIndustry, qualityFlag: 1}}).then(({data}) => {
        if (data.code === 0) { this.$message.success("已加入优质案例库"); this.getDataList(); this.loadCounts(); }
        else this.$message.error(data.msg);
      });
    },
    exportReport() {
      const head = ["作品名称", "客户", "订单号", "拍摄", "剪辑", "满意度", "优质", "可复用价值"];
      const rows = this.dataList.map(d => [d.title, d.customerName, d.orderNo, d.shooterName, d.editorName, d.satisfaction, d.qualityFlag === 1 ? "是" : "否", (d.reuseValue || "").replace(/,/g, " ")]);
      let csv = "\ufeff" + head.join(",") + "\n" + rows.map(r => r.join(",")).join("\n");
      const blob = new Blob([csv], {type: "text/csv"});
      const a = document.createElement("a");
      a.href = URL.createObjectURL(blob);
      a.download = "成品报表.csv";
      a.click();
    },
    preview(row) {
      if (row.video) window.open(this.$base.url + row.video);
      else this.$message.info("暂无视频");
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

.side-panel { height:100%; box-sizing:border-box; display:flex; flex-direction:column; background:#fff; border:none; border-radius:0; box-shadow:none; overflow:hidden; }
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
.d-v { color: #1F2733; font-weight: 500; display: flex; align-items: center; gap: 6px; }
.d-block { background: #F7F9FC; border-radius: 8px; padding: 10px 12px; font-size: 13px; color: #5a6473; line-height: 1.6; }
.mgr-av { width: 22px; height: 22px; border-radius: 50%; color: #fff; font-size: 12px; display: inline-flex; align-items: center; justify-content: center; }
.mgr-av.av2 { background: linear-gradient(135deg,#22B07D,#1c9268); }
.mgr-av.av3 { background: linear-gradient(135deg,#FF8A3D,#e5701f); }
.sp-foot-line { padding: 12px 18px 16px; display: flex; }
.sp-foot-line .el-button { flex: 1; }
.sp-empty { padding: 60px 24px; text-align: center; color: #a8b0bd; }
.sp-empty i { font-size: 40px; margin-bottom: 12px; }
.sp-empty p { font-size: 13px; line-height: 1.7; }
</style>
