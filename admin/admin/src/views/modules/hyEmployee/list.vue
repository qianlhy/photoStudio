<template>
  <div class="main-content">
    <div v-if="showFlag">
      <el-tabs v-model="tab" @tab-click="onTab">
        <!-- 员工账号 -->
        <el-tab-pane label="员工账号" name="emp">
          <el-form :inline="true" class="form-content">
            <el-form-item label="姓名/手机号"><el-input v-model="empSearch" placeholder="请输入员工姓名/手机号" clearable></el-input></el-form-item>
            <el-form-item label="岗位">
              <el-select v-model="roleFilter" placeholder="全部" clearable style="width:130px">
                <el-option v-for="r in roleOptions" :key="r" :label="r" :value="r"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="账号状态">
              <el-select v-model="statusFilter" placeholder="全部" clearable style="width:110px">
                <el-option label="正常" value="正常"></el-option>
                <el-option label="停用" value="停用"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="所属部门">
              <el-select v-model="deptFilter" placeholder="全部" clearable style="width:130px">
                <el-option v-for="d in deptOptions" :key="d" :label="d" :value="d"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button @click="resetEmp()">重置</el-button>
              <el-button icon="el-icon-search" type="primary" @click="loadEmp()">查询</el-button>
            </el-form-item>
          </el-form>

          <div class="emp-actions">
            <el-button type="primary" icon="el-icon-plus" @click="addOrUpdateHandler()">新增员工</el-button>
            <el-button icon="el-icon-sort" @click="tab='emp'">批量交接客户</el-button>
          </div>

          <div class="emp-body">
            <div class="emp-left">
              <el-table :data="shownEmps" border v-loading="loading">
                <el-table-column label="员工姓名" align="left" width="130">
                  <template slot-scope="s">
                    <div class="emp-name">
                      <img v-if="s.row.avatar" :src="img(s.row.avatar)" class="emp-av"/>
                      <span v-else class="emp-av emp-av-txt">{{ (s.row.name||'').charAt(0) }}</span>
                      <span>{{ s.row.name }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="role" label="岗位" align="center" width="90">
                  <template slot-scope="s"><el-tag size="mini" :type="roleTag(s.row.role)">{{ s.row.role }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="phone" label="手机号" align="center" width="120"></el-table-column>
                <el-table-column prop="department" label="所属部门" align="center" width="100"></el-table-column>
                <el-table-column label="习惯操作权限" min-width="180">
                  <template slot-scope="s">
                    <el-tag v-for="(p,i) in perms(s.row.permissions)" :key="i" size="mini" class="perm-tag">{{ p }}</el-tag>
                    <span v-if="perms(s.row.permissions).length===0">—</span>
                  </template>
                </el-table-column>
                <el-table-column prop="customerCount" label="名下客户" align="center" width="80"></el-table-column>
                <el-table-column prop="taskCount" label="当前任务" align="center" width="80"></el-table-column>
                <el-table-column label="账号状态" align="center" width="90">
                  <template slot-scope="s"><el-tag size="mini" :type="s.row.status==='正常'?'success':'info'">{{ s.row.status }}</el-tag></template>
                </el-table-column>
                <el-table-column label="操作" align="center" width="150" fixed="right">
                  <template slot-scope="s">
                    <el-button type="text" size="small" @click="addOrUpdateHandler(s.row.id)">编辑</el-button>
                    <el-button type="text" size="small" @click="pickHandover(s.row)">客户交接</el-button>
                    <el-dropdown size="small" @command="c=>moreCmd(c,s.row)" style="margin-left:6px">
                      <span class="more-link">更多<i class="el-icon-arrow-down"></i></span>
                      <el-dropdown-menu slot="dropdown">
                        <el-dropdown-item command="reset">重置密码</el-dropdown-item>
                        <el-dropdown-item command="toggle">{{ s.row.status==='正常'?'停用':'启用' }}</el-dropdown-item>
                        <el-dropdown-item command="delete">删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </el-dropdown>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 右：客户交接面板 -->
            <div class="emp-right">
              <div class="side-panel">
                <div class="sp-head"><span>客户交接</span><i class="el-icon-close" @click="ho.fromId=null"></i></div>
                <div class="sp-body">
                  <div class="sp-field">
                    <div class="sp-label">离职/调岗员工</div>
                    <el-select v-model="ho.fromId" placeholder="请选择员工" style="width:100%" @change="onHoFrom">
                      <el-option v-for="e in emps" :key="e.id" :label="`${e.name}（${e.role}）${e.phone||''}`" :value="e.id"></el-option>
                    </el-select>
                  </div>
                  <div class="sp-field">
                    <div class="sp-label">接收经理</div>
                    <el-select v-model="ho.toId" placeholder="请选择接收经理" style="width:100%">
                      <el-option v-for="m in managers" :key="m.id" :label="`${m.name}（${m.role}）${m.phone||''}`" :value="m.id"></el-option>
                    </el-select>
                  </div>
                  <div class="sp-field">
                    <div class="sp-label">客户数量</div>
                    <div class="ho-count">{{ ho.count }} <span>个</span></div>
                  </div>
                  <div class="sp-field">
                    <div class="sp-label">分流方式</div>
                    <el-radio-group v-model="ho.mode">
                      <el-radio label="avg">平均分流</el-radio>
                      <el-radio label="assign">指定经理</el-radio>
                    </el-radio-group>
                  </div>
                  <div class="sp-field">
                    <div class="sp-label">交接说明（选填）</div>
                    <el-input type="textarea" v-model="ho.note" :rows="3" maxlength="200" show-word-limit placeholder="请填写交接说明，便于后续追溯"></el-input>
                  </div>
                  <el-checkbox v-model="ho.keep">同步未完成任务（包括未完成拍摄与剪辑等语音）</el-checkbox>
                  <div class="ho-warn"><i class="el-icon-warning-outline"></i> 交接后，客户与该员工名下任务将在下个工作日生效，任务串联将记录到接收经理。</div>
                  <div class="sp-foot">
                    <el-button @click="ho.fromId=null">取消</el-button>
                    <el-button type="primary" @click="doHandover()">确认交接</el-button>
                  </div>
                </div>
              </div>

              <div class="side-panel log-panel">
                <div class="sp-head sm"><span>操作日志（最近 5 条）</span><span class="more-link" @click="tab='log';loadLogs()">更多</span></div>
                <div class="log-list">
                  <div v-for="l in recentLogs" :key="l.id" class="log-item">
                    <div class="log-top"><span class="log-op">{{ l.operatorName }}</span><span class="log-time">{{ (l.addtime||'').replace('T',' ').substr(5,11) }}</span></div>
                    <div class="log-detail">{{ l.detail || (l.module+' '+l.action) }}</div>
                  </div>
                  <div v-if="recentLogs.length===0" class="log-empty">暂无操作日志</div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 角色权限 -->
        <el-tab-pane label="角色权限" name="role">
          <el-table :data="roles" border v-loading="loading">
            <el-table-column prop="rolename" label="角色名称" align="center" width="160"></el-table-column>
            <el-table-column prop="permissions" label="权限范围" min-width="300"></el-table-column>
            <el-table-column prop="remark" label="说明" min-width="200"></el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 客户交接记录 -->
        <el-tab-pane label="客户交接" name="assign">
          <el-table :data="assigns" border v-loading="loading">
            <el-table-column prop="customerName" label="客户" align="center"></el-table-column>
            <el-table-column prop="fromManagerName" label="原业务经理" align="center"></el-table-column>
            <el-table-column prop="toManagerName" label="新业务经理" align="center"></el-table-column>
            <el-table-column prop="operator" label="操作人" align="center"></el-table-column>
            <el-table-column prop="remark" label="交接备注" min-width="200"></el-table-column>
            <el-table-column label="时间" align="center" width="170">
              <template slot-scope="s">{{ (s.row.addtime||'').replace('T',' ').substr(0,16) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 操作日志 -->
        <el-tab-pane label="操作日志" name="log">
          <el-table :data="logs" border v-loading="loading">
            <el-table-column prop="operatorName" label="操作人" align="center" width="120"></el-table-column>
            <el-table-column prop="module" label="模块" align="center" width="120"></el-table-column>
            <el-table-column prop="action" label="动作" align="center" width="120"></el-table-column>
            <el-table-column prop="detail" label="详情" min-width="240"></el-table-column>
            <el-table-column label="时间" align="center" width="170">
              <template slot-scope="s">{{ (s.row.addtime||'').replace('T',' ').substr(0,16) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 基础配置 -->
        <el-tab-pane label="基础配置" name="config">
          <el-form label-width="140px" style="max-width:560px">
            <el-form-item v-for="c in configs" :key="c.id" :label="c.name">
              <el-input v-model="c.value"></el-input>
            </el-form-item>
            <el-form-item><el-button type="primary" @click="saveConfigs()">保存配置</el-button></el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
    <add-or-update v-if="addOrUpdateFlag" :parent="this" ref="addOrUpdate"></add-or-update>
  </div>
</template>

<script>
import AddOrUpdate from "./add-or-update";

export default {
  components: {AddOrUpdate},
  data() {
    return {
      tab: "emp",
      showFlag: true, addOrUpdateFlag: false,
      loading: false,
      empSearch: "",
      roleFilter: "", statusFilter: "", deptFilter: "",
      roleOptions: ["管理员", "销售经理", "销售", "剪辑人员", "拍摄人员", "编导"],
      deptOptions: ["管理层", "销售部", "内容制作部", "拍摄部"],
      emps: [], managers: [], roles: [], assigns: [], logs: [], recentLogs: [], configs: [],
      ho: {fromId: null, toId: null, count: 0, mode: "avg", note: "", keep: true}
    };
  },
  computed: {
    shownEmps() {
      return this.emps.filter(e => {
        if (this.roleFilter && e.role !== this.roleFilter) return false;
        if (this.statusFilter && e.status !== this.statusFilter) return false;
        if (this.deptFilter && e.department !== this.deptFilter) return false;
        return true;
      });
    }
  },
  created() { this.loadEmp(); this.loadManagers(); this.loadRecentLogs(); },
  methods: {
    img(v) { return this.$base.url + String(v).split(",")[0]; },
    perms(v) { return v ? String(v).split(/[,，]/).filter(Boolean).slice(0, 4) : []; },
    roleTag(r) { return ({"管理员": "danger", "销售经理": "primary", "销售": "primary", "编导": "warning"})[r] || "info"; },
    loadManagers() {
      this.$http({url: "hyEmployee/page", method: "get", params: {page: 1, limit: 100, role: "销售经理"}}).then(({data}) => {
        if (data.code === 0) this.managers = data.data.list || [];
      });
    },
    loadRecentLogs() {
      this.$http({url: "hyOperationLog/page", method: "get", params: {page: 1, limit: 5, sort: "addtime", order: "desc"}}).then(({data}) => {
        this.recentLogs = (data.code === 0 ? data.data.list : []) || [];
      });
    },
    resetEmp() { this.empSearch = ""; this.roleFilter = ""; this.statusFilter = ""; this.deptFilter = ""; this.loadEmp(); },
    pickHandover(row) { this.ho = {fromId: row.id, toId: null, count: row.customerCount || 0, mode: "avg", note: "", keep: true}; },
    onHoFrom(id) {
      const e = this.emps.find(x => x.id === id);
      this.ho.count = e ? (e.customerCount || 0) : 0;
    },
    doHandover() {
      if (!this.ho.fromId) { this.$message.warning("请选择离职/调岗员工"); return; }
      if (!this.ho.toId) { this.$message.warning("请选择接收经理"); return; }
      const from = this.emps.find(e => e.id === this.ho.fromId);
      const to = this.managers.find(m => m.id === this.ho.toId) || this.emps.find(e => e.id === this.ho.toId);
      this.$http({url: "hyCustomer/page", method: "get", params: {page: 1, limit: 1000, managerId: this.ho.fromId}}).then(({data}) => {
        const ids = (data.code === 0 ? (data.data.list || []) : []).map(c => c.id);
        if (ids.length === 0) { this.$message.warning("该员工名下暂无客户"); return; }
        let qs = ids.map(i => "customerIds=" + i).join("&");
        qs += "&toManagerId=" + to.id + "&toManagerName=" + encodeURIComponent(to.name);
        qs += "&operator=" + encodeURIComponent(this.$storage.get("adminName") || "管理员");
        qs += "&remark=" + encodeURIComponent(this.ho.note || ("由" + (from ? from.name : "") + "交接给" + to.name));
        this.$http({url: "hyAssignment/batchTransfer?" + qs, method: "post"}).then(({data}) => {
          if (data.code === 0) {
            this.$message.success("已交接 " + ids.length + " 位客户");
            this.ho.fromId = null; this.loadEmp(); this.loadManagers(); this.loadRecentLogs();
          } else this.$message.error(data.msg);
        });
      });
    },
    moreCmd(cmd, row) {
      if (cmd === "reset") this.resetPass(row);
      else if (cmd === "toggle") this.toggleStatus(row);
      else if (cmd === "delete") this.deleteEmp(row.id);
    },
    onTab() {
      if (this.tab === "emp") this.loadEmp();
      else if (this.tab === "role") this.loadRoles();
      else if (this.tab === "assign") this.loadAssigns();
      else if (this.tab === "log") this.loadLogs();
      else if (this.tab === "config") this.loadConfigs();
    },
    loadEmp() {
      this.loading = true;
      let params = {page: 1, limit: 200};
      if (this.empSearch) params.name = "%" + this.empSearch + "%";
      this.$http({url: "hyEmployee/page", method: "get", params}).then(({data}) => {
        this.emps = (data.code === 0 ? data.data.list : []) || [];
        this.loading = false;
      });
    },
    loadRoles() {
      this.loading = true;
      this.$http({url: "hyRole/list", method: "get"}).then(({data}) => {
        this.roles = (data.code === 0 ? data.data : []) || []; this.loading = false;
      });
    },
    loadAssigns() {
      this.loading = true;
      this.$http({url: "hyAssignment/page", method: "get", params: {page: 1, limit: 200}}).then(({data}) => {
        this.assigns = (data.code === 0 ? data.data.list : []) || []; this.loading = false;
      });
    },
    loadLogs() {
      this.loading = true;
      this.$http({url: "hyOperationLog/page", method: "get", params: {page: 1, limit: 200}}).then(({data}) => {
        this.logs = (data.code === 0 ? data.data.list : []) || []; this.loading = false;
      });
    },
    loadConfigs() {
      this.loading = true;
      this.$http({url: "hyConfig/all", method: "get"}).then(({data}) => {
        this.configs = (data.code === 0 ? (data.list || []) : []) || []; this.loading = false;
      });
    },
    saveConfigs() {
      Promise.all(this.configs.map(c => this.$http({
        url: `hyConfig/set?name=${encodeURIComponent(c.name)}&value=${encodeURIComponent(c.value || "")}`,
        method: "post"
      }))).then(() => {
        this.$message.success("配置已保存");
      });
    },
    addOrUpdateHandler(id) {
      this.showFlag = false; this.addOrUpdateFlag = true;
      this.$nextTick(() => this.$refs.addOrUpdate.init(id));
    },
    resetPass(row) {
      this.$confirm(`确定重置 ${row.name} 的密码为 123456?`, "提示", {type: "warning"}).then(() => {
        this.$http({url: `hyEmployee/resetPass/${row.id}`, method: "get"}).then(({data}) => {
          if (data.code === 0) this.$message.success(data.msg || "已重置");
        });
      });
    },
    toggleStatus(row) {
      const next = row.status === "正常" ? "停用" : "正常";
      this.$http({url: "hyEmployee/update", method: "post", data: {id: row.id, status: next}}).then(({data}) => {
        if (data.code === 0) { this.$message.success("操作成功"); this.loadEmp(); }
      });
    },
    deleteEmp(id) {
      this.$confirm("确定删除该员工?", "提示", {type: "warning"}).then(() => {
        this.$http({url: "hyEmployee/delete", method: "post", data: [Number(id)]}).then(({data}) => {
          if (data.code === 0) { this.$message.success("操作成功"); this.loadEmp(); }
        });
      });
    },
    search() { this.loadEmp(); }
  }
};
</script>

<style scoped>
.emp-actions { display: flex; gap: 10px; margin: 12px 0 14px; }
.emp-body { display: flex; gap: 16px; align-items: flex-start; }
.emp-left { flex: 1; min-width: 0; }
.emp-right { width: 300px; flex-shrink: 0; }

.emp-name { display: flex; align-items: center; gap: 8px; }
.emp-av { width: 30px; height: 30px; border-radius: 50%; object-fit: cover; flex-shrink: 0; }
.emp-av-txt { background: linear-gradient(135deg,#4f8bff,#2F6BFF); color: #fff; font-size: 13px; display: inline-flex; align-items: center; justify-content: center; }
.perm-tag { margin: 2px; background: #EAF1FF; color: #2F6BFF; }
.more-link { color: #2F6BFF; cursor: pointer; font-size: 13px; }

.side-panel { background: #fff; border: 1px solid #EEF1F5; border-radius: 12px; box-shadow: 0 1px 4px rgba(0,21,41,.05); overflow: hidden; margin-bottom: 16px; }
.sp-head { display: flex; align-items: center; justify-content: space-between; padding: 14px 18px; font-size: 15px; font-weight: 700; color: #1F2733; border-bottom: 1px solid #EEF1F5; }
.sp-head.sm { font-size: 14px; padding: 12px 16px; }
.sp-head i { cursor: pointer; color: #a8b0bd; }
.sp-body { padding: 16px 18px; }
.sp-field { margin-bottom: 14px; }
.sp-label { font-size: 13px; color: #8A94A6; margin-bottom: 8px; }
.ho-count { font-size: 24px; font-weight: 800; color: #2F6BFF; }
.ho-count span { font-size: 13px; color: #8A94A6; font-weight: 400; }
.ho-warn { background: #FFF6E9; color: #B8791F; font-size: 12px; padding: 10px 12px; border-radius: 8px; line-height: 1.6; margin: 12px 0; }
.sp-foot { display: flex; gap: 10px; margin-top: 8px; }
.sp-foot .el-button { flex: 1; }

.log-list { padding: 6px 16px 12px; }
.log-item { padding: 10px 0; border-bottom: 1px dashed #F0F2F5; }
.log-top { display: flex; justify-content: space-between; font-size: 13px; }
.log-op { font-weight: 600; color: #1F2733; }
.log-time { color: #a8b0bd; }
.log-detail { font-size: 12px; color: #5a6473; margin-top: 4px; line-height: 1.5; }
.log-empty { color: #a8b0bd; text-align: center; padding: 20px 0; font-size: 13px; }
</style>
