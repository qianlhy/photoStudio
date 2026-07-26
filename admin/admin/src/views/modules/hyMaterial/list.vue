<template>
  <div class="main-content">
    <el-form :inline="true" :model="searchForm" class="form-content">
      <el-row :gutter="20" class="slt">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.title" placeholder="标题/标签" clearable></el-input>
        </el-form-item>
        <el-form-item label="行业">
          <el-select v-model="searchForm.industryBig" placeholder="全部" clearable @change="onBig">
            <el-option v-for="i in bigList" :key="i.id" :label="i.name" :value="i.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="业态">
          <el-select v-model="searchForm.industrySub" placeholder="全部" clearable>
            <el-option v-for="i in subList" :key="i.id" :label="i.name" :value="i.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="内容类型">
          <el-select v-model="searchForm.contentType" placeholder="全部" clearable>
            <el-option v-for="t in contentTypes" :key="t" :label="t" :value="t"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button icon="el-icon-search" type="primary" @click="search()">查询</el-button>
        </el-form-item>
      </el-row>
      <el-row class="ad">
        <el-button type="primary" icon="el-icon-upload" @click="uploadVisible=true">批量上传</el-button>
        <el-button icon="el-icon-copy-document" @click="dedup()">视频去重</el-button>
        <el-button :type="recycle?'warning':'default'" icon="el-icon-delete" @click="toggleRecycle()">{{ recycle ? '返回素材库' : '回收站' }}</el-button>
        <el-button type="danger" :disabled="sel.length<=0" icon="el-icon-delete" @click="batchRecycle()" v-if="!recycle">移入回收站</el-button>
        <el-button type="success" :disabled="sel.length<=0" @click="restore()" v-if="recycle">恢复</el-button>
      </el-row>
    </el-form>

    <div class="table-content">
      <el-table class="tables" :data="dataList" v-loading="dataListLoading" border @selection-change="v=>sel=v" style="width: 100%">
        <el-table-column type="selection" width="50" align="center"></el-table-column>
        <el-table-column label="封面" align="center" width="80">
          <template slot-scope="s"><img v-if="s.row.cover" :src="img(s.row.cover)" width="56" height="56" style="object-fit:cover;border-radius:6px"/></template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160"></el-table-column>
        <el-table-column label="行业/业态" align="center" width="130">
          <template slot-scope="s">{{ s.row.industryBig }}<span v-if="s.row.industrySub">·{{ s.row.industrySub }}</span></template>
        </el-table-column>
        <el-table-column prop="contentType" label="内容类型" align="center" width="100"></el-table-column>
        <el-table-column label="标签" align="center" width="140">
          <template slot-scope="s"><el-tag v-for="(t,i) in tags(s.row.tags)" :key="i" size="mini" class="tg">{{ t }}</el-tag></template>
        </el-table-column>
        <el-table-column label="互动" align="center" width="180">
          <template slot-scope="s">
            <span class="mi">👍{{ s.row.likeCount||0 }}</span>
            <span class="mi">👎{{ s.row.dislikeCount||0 }}</span>
            <span class="mi">⭐{{ s.row.favoriteCount||0 }}</span>
            <span class="mi">▶{{ s.row.usedCount||0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="heat" label="热度" align="center" width="80" sortable></el-table-column>
        <el-table-column label="状态" align="center" width="90">
          <template slot-scope="s"><el-tag size="mini" :type="s.row.status==='上架'?'success':(s.row.status==='回收站'?'danger':'info')">{{ s.row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="150" fixed="right">
          <template slot-scope="s">
            <el-button v-if="!recycle" type="text" size="small" @click="toggleShelf(s.row)">{{ s.row.status==='上架'?'下架':'上架' }}</el-button>
            <el-button type="text" size="small" @click="deleteHandler(s.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination @size-change="v=>{pageSize=v;getDataList()}" @current-change="v=>{pageIndex=v;getDataList()}"
                     :current-page="pageIndex" :page-sizes="[10,20,50]" :page-size="pageSize"
                     :total="totalPage" layout="total, sizes, prev, pager, next, jumper"></el-pagination>
    </div>

    <!-- 批量上传 -->
    <el-dialog title="批量上传素材" :visible.sync="uploadVisible" width="560px">
      <el-form label-width="90px">
        <el-form-item label="行业">
          <el-select v-model="up.industryBig" placeholder="选择行业" @change="onUpBig" style="width:100%">
            <el-option v-for="i in bigList" :key="i.id" :label="i.name" :value="i.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="业态">
          <el-select v-model="up.industrySub" placeholder="选择业态" style="width:100%">
            <el-option v-for="i in upSubList" :key="i.id" :label="i.name" :value="i.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="内容类型">
          <el-select v-model="up.contentType" placeholder="选择类型" style="width:100%">
            <el-option v-for="t in contentTypes" :key="t" :label="t" :value="t"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="文件">
          <el-upload :action="uploadUrl" :headers="uploadHeaders" multiple :on-success="onUploaded" list-type="picture-card">
            <i class="el-icon-plus"></i>
          </el-upload>
          <div class="tip">支持图片/视频，上传成功后自动入库为草稿（下架），可补充信息后上架。已上传 {{ up.done }} 个</div>
        </el-form-item>
      </el-form>
      <span slot="footer"><el-button type="primary" @click="uploadVisible=false">完成</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      searchForm: {title: "", industryBig: "", industrySub: "", contentType: ""},
      contentTypes: ["厨过程", "教知识", "讲故事", "说观点", "硬广"],
      bigList: [], subList: [], upSubList: [], allIndustry: [],
      recycle: false,
      dataList: [], sel: [],
      pageIndex: 1, pageSize: 10, totalPage: 0, dataListLoading: false,
      uploadVisible: false,
      up: {industryBig: "", industrySub: "", contentType: "硬广", done: 0}
    };
  },
  computed: {
    uploadUrl() { return this.$base.url + "file/upload"; },
    uploadHeaders() { return {Token: this.$storage.get("Token")}; }
  },
  created() {
    this.loadIndustry();
    this.getDataList();
  },
  methods: {
    img(v) { return this.$base.url + String(v).split(",")[0]; },
    tags(v) { return v ? String(v).split(",").filter(Boolean) : []; },
    loadIndustry() {
      this.$http({url: "hyIndustry/list", method: "get"}).then(({data}) => {
        if (data.code === 0) {
          this.allIndustry = data.data || [];
          this.bigList = this.allIndustry.filter(i => i.level === 1);
        }
      });
    },
    onBig(v) {
      const big = this.bigList.find(b => b.name === v);
      this.subList = big ? this.allIndustry.filter(i => i.parentId === big.id) : [];
      this.searchForm.industrySub = "";
    },
    onUpBig(v) {
      const big = this.bigList.find(b => b.name === v);
      this.upSubList = big ? this.allIndustry.filter(i => i.parentId === big.id) : [];
      this.up.industrySub = "";
    },
    search() { this.pageIndex = 1; this.getDataList(); },
    toggleRecycle() { this.recycle = !this.recycle; this.search(); },
    getDataList() {
      this.dataListLoading = true;
      let params = {page: this.pageIndex, limit: this.pageSize, sort: "heat", order: "desc"};
      if (this.searchForm.title) params.title = "%" + this.searchForm.title + "%";
      if (this.searchForm.industryBig) params.industryBig = this.searchForm.industryBig;
      if (this.searchForm.industrySub) params.industrySub = this.searchForm.industrySub;
      if (this.searchForm.contentType) params.contentType = this.searchForm.contentType;
      params.status = this.recycle ? "回收站" : null;
      this.$http({url: "hyMaterial/page", method: "get", params}).then(({data}) => {
        let list = (data.code === 0 ? data.data.list : []) || [];
        if (!this.recycle) list = list.filter(m => m.status !== "回收站");
        this.dataList = list;
        this.totalPage = data.code === 0 ? data.data.total : 0;
        this.dataListLoading = false;
      });
    },
    toggleShelf(row) {
      const next = row.status === "上架" ? "下架" : "上架";
      this.$http({url: "hyMaterial/update", method: "post", data: {id: row.id, status: next}}).then(({data}) => {
        if (data.code === 0) { this.$message.success("操作成功"); this.getDataList(); }
      });
    },
    batchRecycle() {
      this.bulkStatus(this.sel.map(m => m.id), "回收站", "已移入回收站");
    },
    restore() {
      this.bulkStatus(this.sel.map(m => m.id), "下架", "已恢复");
    },
    bulkStatus(ids, status, msg) {
      Promise.all(ids.map(id => this.$http({url: "hyMaterial/update", method: "post", data: {id, status}}))).then(() => {
        this.$message.success(msg); this.getDataList();
      });
    },
    dedup() {
      const seen = {}, dups = [];
      this.dataList.forEach(m => {
        const key = (m.video || m.title || "").trim();
        if (!key) return;
        if (seen[key]) dups.push(m); else seen[key] = true;
      });
      if (dups.length === 0) { this.$message.success("未发现重复视频"); return; }
      this.$confirm(`发现 ${dups.length} 个重复视频，是否移入回收站?`, "视频去重", {type: "warning"}).then(() => {
        this.bulkStatus(dups.map(m => m.id), "回收站", `已处理 ${dups.length} 个重复视频`);
      });
    },
    onUploaded(res) {
      if (res.code === 0) {
        this.up.done++;
        this.$http({url: "hyMaterial/save", method: "post", data: {
          title: "新素材-" + this.up.done,
          cover: res.file, video: res.file,
          industryBig: this.up.industryBig, industrySub: this.up.industrySub,
          contentType: this.up.contentType, status: "下架", heat: 0
        }}).then(() => this.getDataList());
      }
    },
    deleteHandler(id) {
      this.$confirm("确定彻底删除该素材?", "提示", {type: "warning"}).then(() => {
        this.$http({url: "hyMaterial/delete", method: "post", data: [Number(id)]}).then(({data}) => {
          if (data.code === 0) { this.$message.success("操作成功"); this.getDataList(); }
        });
      });
    }
  }
};
</script>

<style scoped>
.ad { margin-top: 10px; }
.tg { margin: 2px; }
.mi { margin-right: 8px; font-size: 12px; }
.tip { font-size: 12px; color: #8A94A6; margin-top: 6px; }
</style>
