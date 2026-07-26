<template>
  <div class="main-content">
    <div v-if="showFlag">
      <!-- 筛选 -->
      <el-form :inline="true" :model="searchForm" class="form-content">
        <el-row :gutter="20" class="slt">
          <el-form-item label="客户名称/手机号">
            <el-input v-model="searchForm.keyword" placeholder="客户名称或手机号" clearable></el-input>
          </el-form-item>
          <el-form-item label="所属行业">
            <el-select v-model="searchForm.industry" placeholder="全部" clearable>
              <el-option v-for="i in industries" :key="i" :label="i" :value="i"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="跟进状态">
            <el-select v-model="searchForm.followStatus" placeholder="全部" clearable>
              <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="业务经理">
            <el-select v-model="searchForm.managerName" placeholder="全部" clearable>
              <el-option v-for="m in managers" :key="m.id" :label="m.name" :value="m.name"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button icon="el-icon-search" type="primary" @click="search()">查询</el-button>
            <el-button @click="reset()">重置</el-button>
            <el-button icon="el-icon-download" @click="exportReport()">导出报表</el-button>
          </el-form-item>
        </el-row>
      </el-form>

      <!-- 指标卡 -->
      <el-row :gutter="16" class="stat-row">
        <el-col :span="6"><div class="stat s-blue"><div class="s-l">全部客户</div><div class="s-n">{{ stat.total }}</div></div></el-col>
        <el-col :span="6"><div class="stat s-green"><div class="s-l">跟进中</div><div class="s-n">{{ stat.following }}</div></div></el-col>
        <el-col :span="6"><div class="stat s-teal"><div class="s-l">已成交</div><div class="s-n">{{ stat.deal }}</div></div></el-col>
        <el-col :span="6"><div class="stat s-amber"><div class="s-l">待付款</div><div class="s-n">{{ stat.unpaid }}</div></div></el-col>
      </el-row>

      <el-row class="ad">
        <el-button v-if="isAuth('hyCustomer','新增')" type="primary" icon="el-icon-plus" @click="addOrUpdateHandler()">新增客户</el-button>
        <el-button icon="el-icon-sort" :disabled="dataListSelections.length<=0" @click="openBatch()">批量分流</el-button>
      </el-row>

      <!-- 表格 -->
      <div class="table-content">
        <el-table class="tables" :data="dataList" v-loading="dataListLoading" border
                  @selection-change="selectionChangeHandler" style="width: 100%">
          <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
          <el-table-column prop="name" label="客户名称" min-width="150"></el-table-column>
          <el-table-column label="行业/业务" align="center" width="120">
            <template slot-scope="s">{{ s.row.industry }}<span v-if="s.row.biztype">·{{ s.row.biztype }}</span></template>
          </el-table-column>
          <el-table-column prop="contact" label="联系人" align="center" width="100"></el-table-column>
          <el-table-column prop="managerName" label="业务经理" align="center" width="100"></el-table-column>
          <el-table-column label="跟进状态" align="center" width="100">
            <template slot-scope="s"><el-tag size="mini" :type="statusType(s.row.followStatus)">{{ s.row.followStatus }}</el-tag></template>
          </el-table-column>
          <el-table-column label="最近跟进" align="center" width="150">
            <template slot-scope="s">{{ (s.row.lastFollowTime||'').replace('T',' ').substr(0,16) }}</template>
          </el-table-column>
          <el-table-column prop="dealCount" label="成交次数" align="center" width="90"></el-table-column>
          <el-table-column label="未成订单" align="center" width="90">
            <template slot-scope="s">{{ s.row.unpaidCount || 0 }}</template>
          </el-table-column>
          <el-table-column label="满意度" align="center" width="130">
            <template slot-scope="s"><el-rate :value="Number(s.row.satisfaction)||0" disabled></el-rate></template>
          </el-table-column>
          <el-table-column label="客户标签" align="center" width="140">
            <template slot-scope="s">
              <el-tag v-for="(t,i) in tags(s.row.tags)" :key="i" size="mini" class="ctag">{{ t }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="160" fixed="right">
            <template slot-scope="s">
              <el-button type="text" size="small" @click="addOrUpdateHandler(s.row.id,'info')">查看</el-button>
              <el-button type="text" size="small" @click="openTransfer(s.row)">划拨</el-button>
              <el-button v-if="isAuth('hyCustomer','删除')" type="text" size="small" @click="deleteHandler(s.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle"
                       :current-page="pageIndex" :page-sizes="[10,20,50]" :page-size="pageSize"
                       :total="totalPage" layout="total, sizes, prev, pager, next, jumper"></el-pagination>
      </div>
    </div>

    <!-- 一键划拨 -->
    <el-dialog title="客户一键划拨" :visible.sync="transferVisible" width="460px">
      <el-form label-width="100px">
        <el-form-item label="客户"><span>{{ transferForm.customerName }}</span></el-form-item>
        <el-form-item label="原业务经理"><span>{{ transferForm.fromName }}</span></el-form-item>
        <el-form-item label="新业务经理">
          <el-select v-model="transferForm.toId" placeholder="请选择新业务经理" style="width:100%">
            <el-option v-for="m in managers" :key="m.id" :label="m.name" :value="m.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="交接备注">
          <el-input type="textarea" v-model="transferForm.remark" :rows="3" maxlength="200" placeholder="请输入交接备注（选填）"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="transferVisible=false">取消</el-button>
        <el-button type="primary" @click="doTransfer()">确认划拨</el-button>
      </span>
    </el-dialog>

    <!-- 批量分流 -->
    <el-dialog title="批量分流交接" :visible.sync="batchVisible" width="460px">
      <el-form label-width="100px">
        <el-form-item label="已选客户"><span>{{ dataListSelections.length }} 位</span></el-form-item>
        <el-form-item label="新业务经理">
          <el-select v-model="batchForm.toId" placeholder="请选择新业务经理" style="width:100%">
            <el-option v-for="m in managers" :key="m.id" :label="m.name" :value="m.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="交接备注">
          <el-input type="textarea" v-model="batchForm.remark" :rows="3" maxlength="200"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="batchVisible=false">取消</el-button>
        <el-button type="primary" @click="doBatch()">确认分流</el-button>
      </span>
    </el-dialog>

    <add-or-update v-if="addOrUpdateFlag" :parent="this" ref="addOrUpdate"></add-or-update>
  </div>
</template>

<script>
import AddOrUpdate from "./add-or-update";

export default {
  components: {AddOrUpdate},
  data() {
    return {
      searchForm: {keyword: "", industry: "", followStatus: "", managerName: ""},
      statusOptions: ["跟进中", "已成交", "待付款", "已流失"],
      industries: ["餐饮", "建筑", "企业", "教育培训", "家电"],
      managers: [],
      stat: {total: 0, following: 0, deal: 0, unpaid: 0},
      showFlag: true,
      addOrUpdateFlag: false,
      dataList: [],
      pageIndex: 1,
      pageSize: 10,
      totalPage: 0,
      dataListLoading: false,
      dataListSelections: [],
      transferVisible: false,
      transferForm: {customerId: null, customerName: "", fromId: null, fromName: "", toId: null, remark: ""},
      batchVisible: false,
      batchForm: {toId: null, remark: ""}
    };
  },
  created() {
    this.loadManagers();
    this.getDataList();
    this.loadStat();
  },
  methods: {
    tags(v) { return v ? String(v).split(",").filter(Boolean) : []; },
    statusType(s) { return ({"已成交": "success", "待付款": "warning", "已流失": "info"})[s] || "primary"; },
    loadManagers() {
      this.$http({url: "hyEmployee/page", method: "get", params: {page: 1, limit: 100, role: "销售经理"}}).then(({data}) => {
        if (data.code === 0) this.managers = data.data.list || [];
      });
    },
    loadStat() {
      this.$http({url: "hyCustomer/page", method: "get", params: {page: 1, limit: 1000}}).then(({data}) => {
        if (data.code !== 0) return;
        const l = data.data.list || [];
        this.stat = {
          total: data.data.total,
          following: l.filter(c => c.followStatus === "跟进中").length,
          deal: l.filter(c => (c.dealCount || 0) > 0).length,
          unpaid: l.filter(c => c.followStatus === "待付款").length
        };
      });
    },
    search() { this.pageIndex = 1; this.getDataList(); },
    reset() { this.searchForm = {keyword: "", industry: "", followStatus: "", managerName: ""}; this.search(); },
    getDataList() {
      this.dataListLoading = true;
      let params = {page: this.pageIndex, limit: this.pageSize, sort: "last_follow_time", order: "desc"};
      if (this.searchForm.keyword) params.name = "%" + this.searchForm.keyword + "%";
      if (this.searchForm.industry) params.industry = this.searchForm.industry;
      if (this.searchForm.followStatus) params.followStatus = this.searchForm.followStatus;
      if (this.searchForm.managerName) params.managerName = this.searchForm.managerName;
      this.$http({url: "hyCustomer/page", method: "get", params}).then(({data}) => {
        if (data.code === 0) { this.dataList = data.data.list; this.totalPage = data.data.total; }
        else { this.dataList = []; this.totalPage = 0; }
        this.dataListLoading = false;
      });
    },
    sizeChangeHandle(v) { this.pageSize = v; this.pageIndex = 1; this.getDataList(); },
    currentChangeHandle(v) { this.pageIndex = v; this.getDataList(); },
    selectionChangeHandler(v) { this.dataListSelections = v; },
    addOrUpdateHandler(id, type) {
      this.showFlag = false; this.addOrUpdateFlag = true;
      if (type !== "info") type = "else";
      this.$nextTick(() => this.$refs.addOrUpdate.init(id, type));
    },
    openTransfer(row) {
      this.transferForm = {customerId: row.id, customerName: row.name, fromId: row.managerId, fromName: row.managerName, toId: null, remark: ""};
      this.transferVisible = true;
    },
    doTransfer() {
      if (!this.transferForm.toId) { this.$message.warning("请选择新业务经理"); return; }
      const to = this.managers.find(m => m.id === this.transferForm.toId);
      this.$http({url: "hyAssignment/transfer", method: "post", data: {
        customerId: this.transferForm.customerId,
        toManagerId: to.id, toManagerName: to.name, remark: this.transferForm.remark,
        operator: this.$storage.get("adminName") || "管理员"
      }}).then(({data}) => {
        if (data.code === 0) { this.$message.success("划拨成功"); this.transferVisible = false; this.getDataList(); this.loadStat(); }
        else this.$message.error(data.msg);
      });
    },
    openBatch() { this.batchForm = {toId: null, remark: ""}; this.batchVisible = true; },
    doBatch() {
      if (!this.batchForm.toId) { this.$message.warning("请选择新业务经理"); return; }
      const to = this.managers.find(m => m.id === this.batchForm.toId);
      const ids = this.dataListSelections.map(c => c.id);
      let qs = ids.map(i => "customerIds=" + i).join("&");
      qs += "&toManagerId=" + to.id + "&toManagerName=" + encodeURIComponent(to.name);
      qs += "&operator=" + encodeURIComponent(this.$storage.get("adminName") || "管理员");
      qs += "&remark=" + encodeURIComponent(this.batchForm.remark || "");
      this.$http({url: "hyAssignment/batchTransfer?" + qs, method: "post"}).then(({data}) => {
        if (data.code === 0) { this.$message.success("批量分流成功"); this.batchVisible = false; this.getDataList(); this.loadStat(); }
        else this.$message.error(data.msg);
      });
    },
    exportReport() {
      const head = ["客户名称", "行业", "业务", "联系人", "业务经理", "跟进状态", "成交次数", "满意度", "标签"];
      const rows = this.dataList.map(c => [c.name, c.industry, c.biztype, c.contact, c.managerName, c.followStatus, c.dealCount, c.satisfaction, (c.tags || "").replace(/,/g, " ")]);
      let csv = "\ufeff" + head.join(",") + "\n" + rows.map(r => r.join(",")).join("\n");
      const blob = new Blob([csv], {type: "text/csv"});
      const a = document.createElement("a");
      a.href = URL.createObjectURL(blob);
      a.download = "客户报表.csv";
      a.click();
    },
    deleteHandler(id) {
      this.$confirm("确定删除该客户?", "提示", {type: "warning"}).then(() => {
        this.$http({url: "hyCustomer/delete", method: "post", data: [Number(id)]}).then(({data}) => {
          if (data.code === 0) { this.$message.success("操作成功"); this.search(); }
          else this.$message.error(data.msg);
        });
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.stat-row { margin: 10px 0 16px; }
.stat { border: 1px solid #EEF1F5; border-radius: 12px; padding: 16px 20px; border-left: 4px solid #2F6BFF; }
.stat.s-blue { border-left-color: #2F6BFF; }
.stat.s-green { border-left-color: #22B07D; }
.stat.s-teal { border-left-color: #16B8A6; }
.stat.s-amber { border-left-color: #FF8A3D; }
.s-l { font-size: 13px; color: #8A94A6; }
.s-n { font-size: 28px; font-weight: 800; color: #1F2733; }
.ad { margin-bottom: 12px; }
.ctag { margin: 2px; }
</style>
