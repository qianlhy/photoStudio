<template>
  <div class="main-content">
    <el-form :inline="true" :model="searchForm" class="form-content">
      <el-row :gutter="20" class="slt">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.title" placeholder="标题/客户" clearable></el-input>
        </el-form-item>
        <el-form-item label="视图">
          <el-radio-group v-model="activeTab" @change="search">
            <el-radio-button label="all">全部成品</el-radio-button>
            <el-radio-button label="quality">优质作品库</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button icon="el-icon-search" type="primary" @click="search()">查询</el-button>
        </el-form-item>
      </el-row>
    </el-form>

    <div class="table-content">
      <el-table class="tables" :data="dataList" v-loading="dataListLoading" border style="width: 100%">
        <el-table-column label="封面" align="center" width="80">
          <template slot-scope="s"><img v-if="s.row.cover" :src="img(s.row.cover)" width="56" height="56" style="object-fit:cover;border-radius:6px"/></template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="150"></el-table-column>
        <el-table-column prop="customerName" label="客户" align="center" width="130"></el-table-column>
        <el-table-column prop="orderNo" label="订单号" align="center" width="130"></el-table-column>
        <el-table-column label="归属(拍/剪)" align="center" width="120">
          <template slot-scope="s">{{ s.row.shooterName }}/{{ s.row.editorName }}</template>
        </el-table-column>
        <el-table-column label="查看" align="center" width="90">
          <template slot-scope="s"><el-tag size="mini" :type="s.row.viewStatus==='已查看'?'success':'info'">{{ s.row.viewStatus||'未查看' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="下载" align="center" width="90">
          <template slot-scope="s"><el-tag size="mini" :type="s.row.downloadStatus==='已下载'?'success':'info'">{{ s.row.downloadStatus||'未下载' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="满意度" align="center" width="130">
          <template slot-scope="s"><el-rate :value="Number(s.row.satisfaction)||0" disabled></el-rate></template>
        </el-table-column>
        <el-table-column label="优质" align="center" width="80">
          <template slot-scope="s"><el-switch :value="s.row.qualityFlag===1" @change="toggleQuality(s.row)"></el-switch></template>
        </el-table-column>
        <el-table-column prop="reuseValue" label="可复用价值" align="center" min-width="140"></el-table-column>
        <el-table-column label="操作" align="center" width="150" fixed="right">
          <template slot-scope="s">
            <el-button type="text" size="small" @click="openReuse(s.row)">复用价值</el-button>
            <el-button type="text" size="small" @click="preview(s.row)">预览</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination @size-change="v=>{pageSize=v;getDataList()}" @current-change="v=>{pageIndex=v;getDataList()}"
                     :current-page="pageIndex" :page-sizes="[10,20,50]" :page-size="pageSize"
                     :total="totalPage" layout="total, sizes, prev, pager, next, jumper"></el-pagination>
    </div>

    <el-dialog title="标记优质案例 · 可复用价值" :visible.sync="reuseVisible" width="480px">
      <el-form label-width="100px">
        <el-form-item label="作品">{{ reuseForm.title }}</el-form-item>
        <el-form-item label="可复用价值"><el-input type="textarea" :rows="3" v-model="reuseForm.reuseValue" placeholder="如：适合餐饮门店获客，开场3秒抓人"></el-input></el-form-item>
        <el-form-item label="适用行业"><el-input v-model="reuseForm.applyIndustry" placeholder="逗号分隔，如：餐饮,零售"></el-input></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="reuseVisible=false">取消</el-button><el-button type="primary" @click="saveReuse()">保存并标记优质</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      searchForm: {title: ""},
      activeTab: "all",
      dataList: [],
      pageIndex: 1, pageSize: 10, totalPage: 0, dataListLoading: false,
      reuseVisible: false, reuseForm: {id: null, title: "", reuseValue: "", applyIndustry: ""}
    };
  },
  created() { this.getDataList(); },
  methods: {
    img(v) { return this.$base.url + String(v).split(",")[0]; },
    search() { this.pageIndex = 1; this.getDataList(); },
    getDataList() {
      this.dataListLoading = true;
      let params = {page: this.pageIndex, limit: this.pageSize, sort: "addtime", order: "desc"};
      if (this.searchForm.title) params.title = "%" + this.searchForm.title + "%";
      if (this.activeTab === "quality") params.qualityFlag = 1;
      this.$http({url: "hyDeliverable/page", method: "get", params}).then(({data}) => {
        this.dataList = (data.code === 0 ? data.data.list : []) || [];
        this.totalPage = data.code === 0 ? data.data.total : 0;
        this.dataListLoading = false;
      });
    },
    toggleQuality(row) {
      const next = row.qualityFlag === 1 ? 0 : 1;
      this.$http({url: "hyDeliverable/update", method: "post", data: {id: row.id, qualityFlag: next}}).then(({data}) => {
        if (data.code === 0) { row.qualityFlag = next; this.$message.success(next ? "已加入优质作品库" : "已移出"); this.getDataList(); }
      });
    },
    openReuse(row) {
      this.reuseForm = {id: row.id, title: row.title, reuseValue: row.reuseValue || "", applyIndustry: row.applyIndustry || ""};
      this.reuseVisible = true;
    },
    saveReuse() {
      this.$http({url: "hyDeliverable/update", method: "post", data: {id: this.reuseForm.id, reuseValue: this.reuseForm.reuseValue, applyIndustry: this.reuseForm.applyIndustry, qualityFlag: 1}}).then(({data}) => {
        if (data.code === 0) { this.$message.success("已保存"); this.reuseVisible = false; this.getDataList(); }
      });
    },
    preview(row) {
      if (row.video) window.open(this.$base.url + row.video);
      else this.$message.info("暂无视频");
    }
  }
};
</script>

<style scoped>
</style>
