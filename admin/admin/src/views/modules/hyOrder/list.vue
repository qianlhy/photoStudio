<template>
  <div class="main-content">
    <!-- 状态标签 -->
    <div class="ord-tabs">
      <div class="ot" :class="{on:activeTab==='undone'}" @click="switchTab('undone')">未完成 <span class="ot-n">{{ counts.undone }}</span></div>
      <div class="ot" :class="{on:activeTab==='done'}" @click="switchTab('done')">已完成 <span class="ot-n">{{ counts.done }}</span></div>
    </div>

    <!-- 筛选 -->
    <el-form :inline="true" :model="searchForm" class="form-content">
      <el-row :gutter="20" class="slt">
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.customerName" placeholder="请选择客户" clearable></el-input>
        </el-form-item>
        <el-form-item label="套餐名称">
          <el-input v-model="searchForm.packageName" placeholder="请选择套餐" clearable></el-input>
        </el-form-item>
        <el-form-item label="业务经理">
          <el-input v-model="searchForm.managerName" placeholder="请选择" clearable></el-input>
        </el-form-item>
        <el-form-item label="拍摄人员">
          <el-input v-model="searchForm.shooterName" placeholder="请选择" clearable></el-input>
        </el-form-item>
        <el-form-item label="剪辑人员">
          <el-input v-model="searchForm.editorName" placeholder="请选择" clearable></el-input>
        </el-form-item>
        <el-form-item label="是否异常">
          <el-select v-model="searchForm.abnormal" placeholder="请选择" clearable style="width:120px">
            <el-option label="是" value="yes"></el-option>
            <el-option label="否" value="no"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="优质作品">
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

    <div class="ord-body">
      <!-- 左：表格 -->
      <div class="ord-left">
        <div class="table-content">
          <el-table class="tables" :data="dataList" v-loading="dataListLoading" border
                    highlight-current-row @current-change="openDetail" style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" align="center" width="140"></el-table-column>
            <el-table-column prop="customerName" label="客户名称" align="center" min-width="130"></el-table-column>
            <el-table-column label="套餐/本次数量" align="center" min-width="130">
              <template slot-scope="s">{{ s.row.packageName }}<div class="small">{{ s.row.completedCount }}/{{ s.row.videoCount }}</div></template>
            </el-table-column>
            <el-table-column label="完成进度" align="center" width="150">
              <template slot-scope="s">
                <el-progress :percentage="pct(s.row)" :color="s.row.status==='已完成'?'#22B07D':'#2F6BFF'"></el-progress>
              </template>
            </el-table-column>
            <el-table-column label="交付倒计时" align="center" width="100">
              <template slot-scope="s">
                <span v-if="s.row.status!=='已完成'" :class="daysLeft(s.row)<=3?'red':''">剩{{ daysLeft(s.row) }}天</span>
                <span v-else class="green">已交付</span>
              </template>
            </el-table-column>
            <el-table-column prop="managerName" label="业务经理" align="center" width="90"></el-table-column>
            <el-table-column prop="shooterName" label="拍摄人员" align="center" width="90"></el-table-column>
            <el-table-column prop="editorName" label="剪辑人员" align="center" width="90"></el-table-column>
            <el-table-column label="异常状态" align="center" width="100">
              <template slot-scope="s"><el-tag v-if="s.row.abnormal" size="mini" type="danger">{{ s.row.abnormal }}</el-tag><span v-else>—</span></template>
            </el-table-column>
            <el-table-column label="优质作品" align="center" width="90">
              <template slot-scope="s"><el-tag v-if="s.row.qualityFlag===1" size="mini" type="warning">优质作品</el-tag><span v-else>—</span></template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="90" fixed="right">
              <template slot-scope="s"><el-button type="text" size="small" @click="openDetail(s.row)">查看详情</el-button></template>
            </el-table-column>
          </el-table>
          <el-pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle"
                         :current-page="pageIndex" :page-sizes="[10,20,50]" :page-size="pageSize"
                         :total="totalPage" layout="total, sizes, prev, pager, next, jumper"></el-pagination>
        </div>
      </div>

      <!-- 右：订单详情面板 -->
      <div class="ord-right">
        <div class="side-panel" v-if="detail.id">
          <div class="sp-head">
            <span>订单详情</span>
            <i class="el-icon-close" @click="detail={}"></i>
          </div>
          <div class="sp-body">
            <div class="d-row"><span class="d-l">订单编号</span><span class="d-v">{{ detail.orderNo }}</span></div>
            <div class="d-row"><span class="d-l">客户名称</span><span class="d-v">{{ detail.customerName }}</span></div>
            <div class="d-row"><span class="d-l">所属行业</span><span class="d-v">{{ detail.industry }}<span v-if="detail.biztype">·{{ detail.biztype }}</span></span></div>
            <div class="d-row"><span class="d-l">套餐名称</span><span class="d-v">{{ detail.packageName }}</span></div>
            <div class="d-row"><span class="d-l">本次数量</span><span class="d-v">{{ detail.videoCount }}</span></div>
            <div class="d-row"><span class="d-l">完成进度</span><span class="d-v">{{ pct(detail) }}%</span></div>
            <el-progress :percentage="pct(detail)" :color="detail.status==='已完成'?'#22B07D':'#2F6BFF'" :show-text="false" class="d-bar"></el-progress>
            <div class="d-row"><span class="d-l">业务经理</span><span class="d-v"><span class="mgr-av">{{ (detail.managerName||'—').charAt(0) }}</span>{{ detail.managerName }}</span></div>
            <div class="d-row"><span class="d-l">拍摄人员</span><span class="d-v"><span class="mgr-av av2">{{ (detail.shooterName||'—').charAt(0) }}</span>{{ detail.shooterName }}</span></div>
            <div class="d-row"><span class="d-l">剪辑人员</span><span class="d-v"><span class="mgr-av av3">{{ (detail.editorName||'—').charAt(0) }}</span>{{ detail.editorName }}</span></div>
            <div class="d-row"><span class="d-l">异常状态</span><span class="d-v">{{ detail.abnormal || '—' }}</span></div>
            <div class="d-row"><span class="d-l">优质作品</span><span class="d-v"><el-tag v-if="detail.qualityFlag===1" size="mini" type="warning">优质作品</el-tag><span v-else>—</span></span></div>
            <div class="d-row"><span class="d-l">内部目标日期</span><span class="d-v">{{ (detail.targetDate||'').substr(0,10) }}</span></div>
            <div class="d-row"><span class="d-l">承诺交付日期</span><span class="d-v">{{ (detail.deliverDate||'').substr(0,10) }}</span></div>
            <div class="d-row"><span class="d-l">交付倒计时</span><span class="d-v red">剩 {{ daysLeft(detail) }} 天</span></div>
            <div class="d-row"><span class="d-l">视频总数</span><span class="d-v">{{ detail.videoCount }} 条</span></div>
            <div class="d-row"><span class="d-l">已完成视频</span><span class="d-v">{{ detail.completedCount }} 条</span></div>
            <div class="d-row"><span class="d-l">优质作品标记</span><span class="d-v">{{ detail.qualityFlag===1?1:0 }} 条</span></div>
          </div>
          <div class="sp-foot-line">
            <el-button style="width:100%" @click="detail={}">关闭</el-button>
          </div>
        </div>
        <div class="side-panel" v-else>
          <div class="sp-empty"><i class="el-icon-document"></i><p>点击左侧订单<br/>查看订单详情</p></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      searchForm: {customerName: "", packageName: "", managerName: "", shooterName: "", editorName: "", abnormal: "", quality: ""},
      activeTab: "undone",
      counts: {undone: 0, done: 0},
      dataList: [],
      pageIndex: 1, pageSize: 10, totalPage: 0, dataListLoading: false,
      detail: {}, items: []
    };
  },
  created() { this.getDataList(); this.loadCounts(); },
  methods: {
    pct(o) { return o.videoCount ? Math.round(o.completedCount / o.videoCount * 100) : 0; },
    daysLeft(o) {
      if (!o.deliverDate) return 0;
      const d = new Date(String(o.deliverDate).replace(/-/g, "/"));
      const diff = Math.ceil((d - new Date()) / 86400000);
      return diff < 0 ? 0 : diff;
    },
    statusType(s) { return ({"已完成": "success", "待交付": "warning", "待拍摄": "info"})[s] || "primary"; },
    switchTab(t) { this.activeTab = t; this.pageIndex = 1; this.detail = {}; this.getDataList(); },
    search() { this.pageIndex = 1; this.getDataList(); },
    reset() {
      this.searchForm = {customerName: "", packageName: "", managerName: "", shooterName: "", editorName: "", abnormal: "", quality: ""};
      this.search();
    },
    loadCounts() {
      this.$http({url: "hyOrder/page", method: "get", params: {page: 1, limit: 1000}}).then(({data}) => {
        if (data.code !== 0) return;
        const l = data.data.list || [];
        this.counts.done = l.filter(o => o.status === "已完成").length;
        this.counts.undone = l.length - this.counts.done;
      });
    },
    getDataList() {
      this.dataListLoading = true;
      let params = {page: this.pageIndex, limit: this.pageSize, sort: "addtime", order: "desc"};
      if (this.searchForm.customerName) params.customerName = "%" + this.searchForm.customerName + "%";
      if (this.searchForm.packageName) params.packageName = "%" + this.searchForm.packageName + "%";
      if (this.searchForm.managerName) params.managerName = "%" + this.searchForm.managerName + "%";
      if (this.searchForm.shooterName) params.shooterName = "%" + this.searchForm.shooterName + "%";
      if (this.searchForm.editorName) params.editorName = "%" + this.searchForm.editorName + "%";
      if (this.activeTab === "done") params.status = "已完成";
      this.$http({url: "hyOrder/page", method: "get", params}).then(({data}) => {
        let list = (data.code === 0 ? data.data.list : []) || [];
        if (this.activeTab === "undone") list = list.filter(o => o.status !== "已完成");
        if (this.searchForm.abnormal === "yes") list = list.filter(o => !!o.abnormal);
        else if (this.searchForm.abnormal === "no") list = list.filter(o => !o.abnormal);
        if (this.searchForm.quality === "yes") list = list.filter(o => o.qualityFlag === 1);
        else if (this.searchForm.quality === "no") list = list.filter(o => o.qualityFlag !== 1);
        this.dataList = list;
        this.totalPage = data.code === 0 ? data.data.total : 0;
        this.dataListLoading = false;
      });
    },
    sizeChangeHandle(v) { this.pageSize = v; this.pageIndex = 1; this.getDataList(); },
    currentChangeHandle(v) { this.pageIndex = v; this.getDataList(); },
    exportReport() {
      const head = ["订单号", "客户", "套餐", "完成", "总数", "业务经理", "拍摄", "剪辑", "状态", "异常"];
      const rows = this.dataList.map(o => [o.orderNo, o.customerName, o.packageName, o.completedCount, o.videoCount, o.managerName, o.shooterName, o.editorName, o.status, o.abnormal || ""]);
      let csv = "\ufeff" + head.join(",") + "\n" + rows.map(r => r.join(",")).join("\n");
      const blob = new Blob([csv], {type: "text/csv"});
      const a = document.createElement("a");
      a.href = URL.createObjectURL(blob);
      a.download = "订单报表.csv";
      a.click();
    },
    openDetail(row) {
      if (!row || !row.id) return;
      this.$http({url: `hyOrder/detail/${row.id}`, method: "get"}).then(({data}) => {
        if (data.code === 0) { this.detail = data.data || row; this.items = data.items || []; }
        else { this.detail = row; this.items = []; }
      });
    }
  }
};
</script>

<style scoped>
.small { font-size: 12px; color: #8A94A6; }
.red { color: #FF5A5F; font-weight: 600; }
.green { color: #22B07D; }

.ord-tabs { display: flex; gap: 28px; padding: 0 4px 12px; border-bottom: 1px solid #EBEEF5; margin-bottom: 14px; }
.ot { position: relative; font-size: 15px; color: #5a6473; cursor: pointer; padding-bottom: 12px; margin-bottom: -13px; }
.ot .ot-n { color: #a8b0bd; font-size: 13px; margin-left: 2px; }
.ot.on { color: #2F6BFF; font-weight: 600; }
.ot.on .ot-n { color: #2F6BFF; }
.ot.on::after { content: ""; position: absolute; left: 0; right: 0; bottom: 0; height: 2px; background: #2F6BFF; border-radius: 2px; }

.ord-body { display: flex; gap: 16px; align-items: flex-start; }
.ord-left { flex: 1; min-width: 0; }
.ord-right { width: 300px; flex-shrink: 0; }

.side-panel { background: #fff; border: 1px solid #EEF1F5; border-radius: 12px; box-shadow: 0 1px 4px rgba(0,21,41,.05); overflow: hidden; }
.sp-head { display: flex; align-items: center; justify-content: space-between; padding: 14px 18px; font-size: 15px; font-weight: 700; color: #1F2733; border-bottom: 1px solid #EEF1F5; }
.sp-head i { cursor: pointer; color: #a8b0bd; }
.sp-body { padding: 8px 18px 16px; }
.d-row { display: flex; justify-content: space-between; align-items: center; padding: 9px 0; border-bottom: 1px dashed #F0F2F5; font-size: 13px; }
.d-l { color: #8A94A6; }
.d-v { color: #1F2733; font-weight: 500; display: flex; align-items: center; gap: 6px; }
.d-v.red { color: #FF5A5F; font-weight: 700; }
.d-bar { margin: 6px 0 4px; }
.mgr-av { width: 22px; height: 22px; border-radius: 50%; background: linear-gradient(135deg,#4f8bff,#2F6BFF); color: #fff; font-size: 12px; display: inline-flex; align-items: center; justify-content: center; }
.mgr-av.av2 { background: linear-gradient(135deg,#22B07D,#1c9268); }
.mgr-av.av3 { background: linear-gradient(135deg,#FF8A3D,#e5701f); }
.sp-foot-line { padding: 12px 18px 16px; }
.sp-empty { padding: 60px 24px; text-align: center; color: #a8b0bd; }
.sp-empty i { font-size: 40px; margin-bottom: 12px; }
.sp-empty p { font-size: 13px; line-height: 1.7; }
</style>
