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
          <el-form-item label="审核状态">
            <el-select v-model="searchForm.auditStatus" placeholder="全部" clearable>
              <el-option v-for="s in auditOptions" :key="s" :label="s" :value="s"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="业务经理">
            <el-select v-model="searchForm.managerName" placeholder="全部" clearable>
              <el-option v-for="m in managers" :key="m.id" :label="m.name" :value="m.name"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="最近跟进时间">
            <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="-"
                            start-placeholder="开始日期" end-placeholder="结束日期" value-format="yyyy-MM-dd"
                            style="width:240px"></el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button icon="el-icon-search" type="primary" @click="search()">查询</el-button>
            <el-button @click="reset()">重置</el-button>
            <el-button icon="el-icon-download" @click="exportReport()">导出报表</el-button>
          </el-form-item>
        </el-row>
      </el-form>

      <div class="cm-body">
        <!-- 左：指标 + 表格 -->
        <div class="cm-left">
          <el-row :gutter="16" class="stat-row">
            <el-col :span="6"><div class="stat s-blue"><i class="el-icon-user-solid s-ic"></i><div><div class="s-l">全部客户</div><div class="s-n">{{ stat.total }}</div></div></div></el-col>
            <el-col :span="6"><div class="stat s-green"><i class="el-icon-data-line s-ic"></i><div><div class="s-l">跟进中</div><div class="s-n">{{ stat.following }}</div></div></div></el-col>
            <el-col :span="6"><div class="stat s-teal"><i class="el-icon-circle-check s-ic"></i><div><div class="s-l">已成交</div><div class="s-n">{{ stat.deal }}</div></div></div></el-col>
            <el-col :span="6"><div class="stat s-amber"><i class="el-icon-wallet s-ic"></i><div><div class="s-l">待付款</div><div class="s-n">{{ stat.unpaid }}</div></div></div></el-col>
          </el-row>

          <div class="table-content">
            <el-table class="tables" :data="dataList" v-loading="dataListLoading" border
                      highlight-current-row @current-change="rowClick"
                      @selection-change="selectionChangeHandler" style="width: 100%">
              <el-table-column type="selection" header-align="center" align="center" width="45"></el-table-column>
              <el-table-column label="头像" align="center" width="70">
                <template slot-scope="s">
                  <img v-if="s.row.avatar" :src="img(s.row.avatar)" class="tbl-avatar"/>
                  <span v-else class="tbl-avatar-ph">{{ (s.row.name||'?').charAt(0) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="name" label="客户名称" min-width="140"></el-table-column>
              <el-table-column label="行业/业务" align="center" width="110">
                <template slot-scope="s">{{ s.row.industry }}<span v-if="s.row.biztype">·{{ s.row.biztype }}</span></template>
              </el-table-column>
              <el-table-column prop="contact" label="联系人" align="center" width="90"></el-table-column>
              <el-table-column label="业务经理" align="center" width="110">
                <template slot-scope="s">
                  <div class="mgr-cell">
                    <span class="mgr-av">{{ (s.row.managerName||'—').charAt(0) }}</span>
                    <span>{{ s.row.managerName }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="跟进状态" align="center" width="90">
                <template slot-scope="s"><el-tag size="mini" :type="statusType(s.row.followStatus)">{{ s.row.followStatus }}</el-tag></template>
              </el-table-column>
              <el-table-column label="审核状态" align="center" width="100">
                <template slot-scope="s">
                  <el-tag size="mini" :type="auditType(s.row.auditStatus)">{{ auditLabel(s.row.auditStatus) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="最近跟进" align="center" width="140">
                <template slot-scope="s">{{ (s.row.lastFollowTime||'').replace('T',' ').substr(0,16) }}</template>
              </el-table-column>
              <el-table-column prop="dealCount" label="成交次数" align="center" width="80"></el-table-column>
              <el-table-column label="未成订单" align="center" width="80">
                <template slot-scope="s">{{ s.row.unpaidCount || 0 }}</template>
              </el-table-column>
              <el-table-column label="满意度" align="center" width="120">
                <template slot-scope="s"><el-rate :value="Number(s.row.satisfaction)||0" disabled></el-rate></template>
              </el-table-column>
              <el-table-column label="客户标签" align="center" width="130">
                <template slot-scope="s">
                  <el-tag v-for="(t,i) in tags(s.row.tags)" :key="i" size="mini" class="ctag">{{ t }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="180" fixed="right">
                <template slot-scope="s">
                  <el-button type="text" size="small" @click="addOrUpdateHandler(s.row.id,'info')">查看</el-button>
                  <el-button v-if="needAudit(s.row)" type="text" size="small" @click="openAudit(s.row)">审核</el-button>
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

        <!-- 右：操作 + 一键划拨面板 -->
        <div class="cm-right">
          <div class="cm-actions">
            <el-button v-if="isAuth('hyCustomer','新增')" type="primary" icon="el-icon-plus" @click="addOrUpdateHandler()">新增客户</el-button>
            <el-button icon="el-icon-sort" :disabled="dataListSelections.length<=0" @click="openBatch()">批量分流</el-button>
          </div>
          <div class="side-panel">
            <div class="sp-head">
              <span>客户一键划拨</span>
            </div>
            <div class="sp-body" v-if="transferForm.customerId">
              <div class="sp-field">
                <div class="sp-label">当前客户</div>
                <div class="sp-cust">{{ transferForm.customerName }}</div>
              </div>
              <div class="sp-field">
                <div class="sp-label">原业务经理</div>
                <div class="sp-mgr"><span class="mgr-av">{{ (transferForm.fromName||'—').charAt(0) }}</span>{{ transferForm.fromName || '—' }}</div>
              </div>
              <div class="sp-field">
                <div class="sp-label">新业务经理</div>
                <el-select v-model="transferForm.toId" placeholder="请选择新业务经理" style="width:100%">
                  <el-option v-for="m in managers" :key="m.id" :label="m.name" :value="m.id"></el-option>
                </el-select>
              </div>
              <div class="sp-field">
                <div class="sp-label">交接备注</div>
                <el-input type="textarea" v-model="transferForm.remark" :rows="4" maxlength="200" show-word-limit placeholder="请输入交接备注（选填）"></el-input>
              </div>
              <div class="sp-foot">
                <el-button @click="transferForm.customerId=null">取消</el-button>
                <el-button type="primary" @click="doTransfer()">确认划拨</el-button>
              </div>
            </div>
            <div class="sp-empty" v-else>
              <i class="el-icon-s-promotion"></i>
              <p>点击左侧客户「划拨」<br/>可将其转交给其他业务经理</p>
            </div>
          </div>
        </div>
      </div>
    </div>

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

    <!-- 审核客户 -->
    <el-dialog title="审核客户" :visible.sync="auditVisible" width="480px">
      <el-form label-width="100px">
        <el-form-item label="客户名称"><span>{{ auditForm.name }}</span></el-form-item>
        <el-form-item label="手机号"><span>{{ auditForm.phone || '—' }}</span></el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio label="已通过">通过</el-radio>
            <el-radio label="已驳回">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="auditForm.auditStatus === '已通过'" label="服务经理" required>
          <el-select v-model="auditForm.managerId" placeholder="审核通过后 Pad 可见，请指定经理" clearable style="width:100%">
            <el-option v-for="m in managers" :key="m.id" :label="m.name" :value="m.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input type="textarea" v-model="auditForm.auditReply" :rows="3" maxlength="200" show-word-limit placeholder="驳回时请填写原因"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="auditVisible=false">取消</el-button>
        <el-button type="primary" @click="doAudit()">确认</el-button>
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
      searchForm: {keyword: "", industry: "", followStatus: "", auditStatus: "", managerName: "", dateRange: []},
      statusOptions: ["跟进中", "已成交", "待付款", "已流失"],
      auditOptions: ["待审核", "已通过", "已驳回"],
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
      batchForm: {toId: null, remark: ""},
      auditVisible: false,
      auditForm: {id: null, name: "", phone: "", auditStatus: "已通过", auditReply: "", managerId: null}
    };
  },
  created() {
    this.loadManagers();
    this.getDataList();
    this.loadStat();
  },
  methods: {
    img(v) {
      if (!v) return '';
      if (/^https?:\/\//i.test(v)) return v.split('?')[0];
      const rel = v.startsWith('upload/') ? v : v.replace(/^\//, '');
      return '/' + this.$base.name + '/' + rel;
    },
    tags(v) { return v ? String(v).split(",").filter(Boolean) : []; },
    statusType(s) { return ({"已成交": "success", "待付款": "warning", "已流失": "info"})[s] || "primary"; },
    auditLabel(s) {
      if (!s || s === "是") return "已通过";
      if (s === "否") return "待审核";
      if (s === "驳回") return "已驳回";
      return s;
    },
    auditType(s) {
      const v = this.auditLabel(s);
      return ({"已通过": "success", "待审核": "warning", "已驳回": "danger"})[v] || "info";
    },
    needAudit(row) {
      const v = this.auditLabel(row.auditStatus);
      return v === "待审核" || v === "已驳回";
    },
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
    reset() { this.searchForm = {keyword: "", industry: "", followStatus: "", auditStatus: "", managerName: "", dateRange: []}; this.search(); },
    rowClick(row) { if (row) this.openTransfer(row); },
    getDataList() {
      this.dataListLoading = true;
      let params = {page: this.pageIndex, limit: this.pageSize, sort: "last_follow_time", order: "desc"};
      if (this.searchForm.keyword) params.name = "%" + this.searchForm.keyword + "%";
      if (this.searchForm.industry) params.industry = this.searchForm.industry;
      if (this.searchForm.followStatus) params.followStatus = this.searchForm.followStatus;
      if (this.searchForm.auditStatus) params.auditStatus = this.searchForm.auditStatus;
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
    openAudit(row) {
      this.auditForm = {
        id: row.id,
        name: row.name,
        phone: row.phone,
        auditStatus: "已通过",
        auditReply: row.auditReply || "",
        managerId: row.managerId || null
      };
      this.auditVisible = true;
    },
    doAudit() {
      if (this.auditForm.auditStatus === "已驳回" && !this.auditForm.auditReply.trim()) {
        this.$message.warning("驳回时请填写原因");
        return;
      }
      if (this.auditForm.auditStatus === "已通过" && !this.auditForm.managerId) {
        this.$message.warning("审核通过请指定服务经理，以便 Pad 可见");
        return;
      }
      const mgr = this.managers.find(m => m.id === this.auditForm.managerId);
      this.$http({
        url: "hyCustomer/audit",
        method: "post",
        data: {
          id: this.auditForm.id,
          auditStatus: this.auditForm.auditStatus,
          auditReply: this.auditForm.auditReply,
          managerId: this.auditForm.auditStatus === "已通过" ? this.auditForm.managerId : null,
          managerName: this.auditForm.auditStatus === "已通过" && mgr ? mgr.name : null
        }
      }).then(({data}) => {
        if (data.code === 0) {
          this.$message.success("审核完成");
          this.auditVisible = false;
          this.getDataList();
          this.loadStat();
        } else {
          this.$message.error(data.msg);
        }
      });
    },
    openTransfer(row) {
      this.transferForm = {customerId: row.id, customerName: row.name, fromId: row.managerId, fromName: row.managerName, toId: null, remark: ""};
    },
    doTransfer() {
      if (!this.transferForm.toId) { this.$message.warning("请选择新业务经理"); return; }
      const to = this.managers.find(m => m.id === this.transferForm.toId);
      this.$http({url: "hyAssignment/transfer", method: "post", data: {
        customerId: this.transferForm.customerId,
        toManagerId: to.id, toManagerName: to.name, remark: this.transferForm.remark,
        operator: this.$storage.get("adminName") || "管理员"
      }}).then(({data}) => {
        if (data.code === 0) { this.$message.success("划拨成功"); this.transferForm.customerId = null; this.getDataList(); this.loadStat(); }
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
.cm-body { display: flex; gap: 16px; align-items: flex-start; }
.cm-left { flex: 1; min-width: 0; }
.cm-right { width: 270px; flex-shrink: 0; }

.stat-row { margin: 0 0 16px; }
.stat { display: flex; align-items: center; gap: 12px; background: #fff; border: 1px solid #EEF1F5; border-radius: 12px; padding: 14px 16px; box-shadow: 0 1px 4px rgba(0,21,41,.05); }
.s-ic { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 20px; color: #fff; }
.stat.s-blue .s-ic { background: #2F6BFF; }
.stat.s-green .s-ic { background: #22B07D; }
.stat.s-teal .s-ic { background: #16B8A6; }
.stat.s-amber .s-ic { background: #FF8A3D; }
.s-l { font-size: 13px; color: #8A94A6; }
.s-n { font-size: 26px; font-weight: 800; color: #1F2733; line-height: 1.1; }
.ctag { margin: 2px; }
.tbl-avatar { width: 40px; height: 40px; border-radius: 8px; object-fit: cover; }
.tbl-avatar-ph { width: 40px; height: 40px; border-radius: 8px; background: #EEF1F5; color: #8A94A6; display: inline-flex; align-items: center; justify-content: center; font-size: 16px; font-weight: 700; }

.mgr-cell { display: flex; align-items: center; justify-content: center; gap: 6px; }
.mgr-av { width: 22px; height: 22px; border-radius: 50%; background: linear-gradient(135deg,#4f8bff,#2F6BFF); color: #fff; font-size: 12px; display: inline-flex; align-items: center; justify-content: center; flex-shrink: 0; }

.cm-actions { display: flex; gap: 10px; margin-bottom: 14px; }
.cm-actions .el-button { flex: 1; }

.side-panel { min-height: calc(100vh - 228px); display:flex; flex-direction:column; background: #fff; border: 1px solid #E7ECF3; border-radius: 10px; box-shadow: 0 1px 4px rgba(0,21,41,.04); overflow: hidden; }
.sp-head { padding: 14px 18px; font-size: 15px; font-weight: 700; color: #1F2733; border-bottom: 1px solid #EEF1F5; }
.sp-body { padding: 16px 18px; flex:1; display:flex; flex-direction:column; }
.sp-field { margin-bottom: 16px; }
.sp-label { font-size: 13px; color: #8A94A6; margin-bottom: 8px; }
.sp-cust { font-size: 15px; font-weight: 700; color: #1F2733; }
.sp-mgr { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #1F2733; }
.sp-foot { display: flex; gap: 10px; margin-top: auto; padding-top:20px; }
.sp-foot .el-button { flex: 1; }
.sp-empty { padding: 50px 24px; text-align: center; color: #a8b0bd; }
.sp-empty i { font-size: 40px; margin-bottom: 12px; }
.sp-empty p { font-size: 13px; line-height: 1.7; }
</style>
