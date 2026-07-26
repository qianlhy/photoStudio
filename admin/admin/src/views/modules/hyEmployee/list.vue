<template>
  <div class="main-content">
    <div v-if="showFlag">
      <el-tabs v-model="tab" @tab-click="onTab">
        <!-- 员工账号 -->
        <el-tab-pane label="员工账号" name="emp">
          <el-form :inline="true" class="form-content">
            <el-form-item label="姓名/账号"><el-input v-model="empSearch" placeholder="姓名或账号" clearable></el-input></el-form-item>
            <el-form-item><el-button icon="el-icon-search" type="primary" @click="loadEmp()">查询</el-button></el-form-item>
            <el-form-item><el-button type="primary" icon="el-icon-plus" @click="addOrUpdateHandler()">新增员工</el-button></el-form-item>
          </el-form>
          <el-table :data="emps" border v-loading="loading">
            <el-table-column label="头像" align="center" width="70">
              <template slot-scope="s"><img v-if="s.row.avatar" :src="img(s.row.avatar)" width="44" height="44" style="border-radius:50%"/></template>
            </el-table-column>
            <el-table-column prop="name" label="姓名" align="center"></el-table-column>
            <el-table-column prop="username" label="账号" align="center"></el-table-column>
            <el-table-column prop="phone" label="手机" align="center" width="130"></el-table-column>
            <el-table-column prop="role" label="角色" align="center">
              <template slot-scope="s"><el-tag size="mini">{{ s.row.role }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="department" label="部门" align="center"></el-table-column>
            <el-table-column prop="customerCount" label="负责客户" align="center" width="90"></el-table-column>
            <el-table-column label="状态" align="center" width="90">
              <template slot-scope="s"><el-tag size="mini" :type="s.row.status==='正常'?'success':'info'">{{ s.row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="220" fixed="right">
              <template slot-scope="s">
                <el-button type="text" size="small" @click="addOrUpdateHandler(s.row.id)">编辑</el-button>
                <el-button type="text" size="small" @click="resetPass(s.row)">重置密码</el-button>
                <el-button type="text" size="small" @click="toggleStatus(s.row)">{{ s.row.status==='正常'?'停用':'启用' }}</el-button>
                <el-button type="text" size="small" @click="deleteEmp(s.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
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
        <el-tab-pane label="客户交接记录" name="assign">
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
      emps: [], roles: [], assigns: [], logs: [], configs: []
    };
  },
  created() { this.loadEmp(); },
  methods: {
    img(v) { return this.$base.url + String(v).split(",")[0]; },
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
</style>
