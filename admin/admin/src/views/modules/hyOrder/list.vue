<template>
  <div class="main-content">
    <el-form :inline="true" :model="searchForm" class="form-content">
      <el-row :gutter="20" class="slt">
        <el-form-item label="客户/套餐">
          <el-input v-model="searchForm.keyword" placeholder="客户或套餐" clearable></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="activeTab" @change="search">
            <el-radio-button label="undone">未完成</el-radio-button>
            <el-radio-button label="done">已完成</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button icon="el-icon-search" type="primary" @click="search()">查询</el-button>
        </el-form-item>
      </el-row>
    </el-form>

    <div class="table-content">
      <el-table class="tables" :data="dataList" v-loading="dataListLoading" border style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" align="center" width="140"></el-table-column>
        <el-table-column prop="customerName" label="客户" align="center" min-width="140"></el-table-column>
        <el-table-column prop="packageName" label="套餐" align="center" min-width="130"></el-table-column>
        <el-table-column label="进度" align="center" width="160">
          <template slot-scope="s">
            <el-progress :percentage="pct(s.row)" :color="s.row.status==='已完成'?'#22B07D':'#FF8A3D'"></el-progress>
            <span class="small">{{ s.row.completedCount }}/{{ s.row.videoCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="倒计时" align="center" width="100">
          <template slot-scope="s">
            <span v-if="s.row.status!=='已完成'" :class="daysLeft(s.row)<=3?'red':''">剩 {{ daysLeft(s.row) }} 天</span>
            <span v-else class="green">已交付</span>
          </template>
        </el-table-column>
        <el-table-column prop="managerName" label="业务经理" align="center" width="100"></el-table-column>
        <el-table-column prop="shooterName" label="拍摄" align="center" width="90"></el-table-column>
        <el-table-column prop="editorName" label="剪辑" align="center" width="90"></el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template slot-scope="s"><el-tag size="mini" :type="statusType(s.row.status)">{{ s.row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="异常" align="center" width="110">
          <template slot-scope="s"><el-tag v-if="s.row.abnormal" size="mini" type="danger">{{ s.row.abnormal }}</el-tag><span v-else>—</span></template>
        </el-table-column>
        <el-table-column label="优质作品" align="center" width="100">
          <template slot-scope="s"><el-switch :value="s.row.qualityFlag===1" @change="toggleQuality(s.row)"></el-switch></template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120" fixed="right">
          <template slot-scope="s"><el-button type="text" size="small" @click="openDetail(s.row)">详情</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle"
                     :current-page="pageIndex" :page-sizes="[10,20,50]" :page-size="pageSize"
                     :total="totalPage" layout="total, sizes, prev, pager, next, jumper"></el-pagination>
    </div>

    <el-dialog :title="`订单详情 · ${detail.orderNo||''}`" :visible.sync="detailVisible" width="760px">
      <div v-if="detail.id">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="客户">{{ detail.customerName }}</el-descriptions-item>
          <el-descriptions-item label="套餐">{{ detail.packageName }}</el-descriptions-item>
          <el-descriptions-item label="业务经理">{{ detail.managerName }}</el-descriptions-item>
          <el-descriptions-item label="拍摄/剪辑">{{ detail.shooterName }} / {{ detail.editorName }}</el-descriptions-item>
          <el-descriptions-item label="拍摄日期">{{ (detail.shootDate||'').substr(0,10) }}</el-descriptions-item>
          <el-descriptions-item label="交付日期">{{ (detail.deliverDate||'').substr(0,10) }}</el-descriptions-item>
          <el-descriptions-item label="配方" :span="2">{{ detail.recipe }}</el-descriptions-item>
        </el-descriptions>
        <div class="items-title">本次内容清单（{{ items.length }}）</div>
        <el-table :data="items" border size="small">
          <el-table-column type="index" label="#" width="50"></el-table-column>
          <el-table-column prop="title" label="标题"></el-table-column>
          <el-table-column prop="contentType" label="类型" width="100"></el-table-column>
          <el-table-column label="状态" width="100">
            <template slot-scope="s"><el-tag size="mini" :type="s.row.status==1?'success':'info'">{{ s.row.status==1?'已完成':'待制作' }}</el-tag></template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      searchForm: {keyword: ""},
      activeTab: "undone",
      dataList: [],
      pageIndex: 1, pageSize: 10, totalPage: 0, dataListLoading: false,
      detailVisible: false, detail: {}, items: []
    };
  },
  created() { this.getDataList(); },
  methods: {
    pct(o) { return o.videoCount ? Math.round(o.completedCount / o.videoCount * 100) : 0; },
    daysLeft(o) {
      if (!o.deliverDate) return 0;
      const d = new Date(String(o.deliverDate).replace(/-/g, "/"));
      const diff = Math.ceil((d - new Date()) / 86400000);
      return diff < 0 ? 0 : diff;
    },
    statusType(s) { return ({"已完成": "success", "待交付": "warning", "待拍摄": "info"})[s] || "primary"; },
    search() { this.pageIndex = 1; this.getDataList(); },
    getDataList() {
      this.dataListLoading = true;
      let params = {page: this.pageIndex, limit: this.pageSize, sort: "addtime", order: "desc"};
      if (this.searchForm.keyword) params.customerName = "%" + this.searchForm.keyword + "%";
      if (this.activeTab === "done") params.status = "已完成";
      this.$http({url: "hyOrder/page", method: "get", params}).then(({data}) => {
        let list = (data.code === 0 ? data.data.list : []) || [];
        if (this.activeTab === "undone") list = list.filter(o => o.status !== "已完成");
        this.dataList = list;
        this.totalPage = data.code === 0 ? data.data.total : 0;
        this.dataListLoading = false;
      });
    },
    sizeChangeHandle(v) { this.pageSize = v; this.pageIndex = 1; this.getDataList(); },
    currentChangeHandle(v) { this.pageIndex = v; this.getDataList(); },
    toggleQuality(row) {
      const next = row.qualityFlag === 1 ? 0 : 1;
      this.$http({url: "hyOrder/update", method: "post", data: {id: row.id, qualityFlag: next}}).then(({data}) => {
        if (data.code === 0) { row.qualityFlag = next; this.$message.success(next ? "已标记优质作品" : "已取消标记"); }
      });
    },
    openDetail(row) {
      this.$http({url: `hyOrder/detail/${row.id}`, method: "get"}).then(({data}) => {
        if (data.code === 0) { this.detail = data.data || row; this.items = data.items || []; this.detailVisible = true; }
      });
    }
  }
};
</script>

<style scoped>
.small { font-size: 12px; color: #8A94A6; }
.red { color: #FF5A5F; font-weight: 600; }
.green { color: #22B07D; }
.items-title { font-weight: 600; margin: 16px 0 10px; }
</style>
