<template>
  <div class="main-content">
    <!-- 顶部批量工具栏 -->
    <div class="mt-toolbar">
      <div class="tb-left">
        <el-button type="primary" icon="el-icon-video-camera" @click="openUploadPanel">
          批量上传视频
          <el-badge v-if="upRemainCount>0" :value="upRemainCount" class="up-badge"/>
        </el-button>
        <el-button icon="el-icon-plus" @click="openUploadPanel">新建素材</el-button>
        <el-button icon="el-icon-folder" :disabled="sel.length<=0" @click="batchCategory()">批量修改分类</el-button>
        <el-button icon="el-icon-sort" @click="batchShelf()">批量上下架</el-button>
        <el-button icon="el-icon-delete" :disabled="sel.length<=0" @click="batchRecycle()">批量删除</el-button>
        <span class="tb-sel" v-if="sel.length>0">已选 {{ sel.length }} 项 <a @click="clearSel()">清空</a></span>
      </div>
      <div class="tb-right">
        <el-button :type="recycle?'warning':'default'" icon="el-icon-delete" @click="toggleRecycle()">{{ recycle ? '返回素材库' : '回收站' }}</el-button>
        <el-button icon="el-icon-download" @click="exportReport()">导出报表</el-button>
      </div>
    </div>

    <!-- 筛选 -->
    <el-form :inline="true" :model="searchForm" class="form-content mt-filter">
      <el-form-item label="行业大类">
        <el-select v-model="searchForm.industryBig" placeholder="全部" clearable @change="onBig" style="width:120px">
          <el-option v-for="i in bigList" :key="i.id" :label="i.name" :value="i.name"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="业态分类">
        <el-select v-model="searchForm.industrySub" placeholder="全部" clearable style="width:120px">
          <el-option v-for="i in subList" :key="i.id" :label="i.name" :value="i.name"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="内容类型">
        <el-select v-model="searchForm.contentType" placeholder="全部" clearable style="width:120px">
          <el-option v-for="t in contentTypes" :key="t" :label="t" :value="t"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="标签">
        <el-input v-model="searchForm.title" placeholder="标题/标签" clearable style="width:140px"></el-input>
      </el-form-item>
      <el-form-item label="上架状态">
        <el-select v-model="searchForm.status" placeholder="全部" clearable style="width:110px">
          <el-option label="上架" value="上架"></el-option>
          <el-option label="下架" value="下架"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="热度排序">
        <el-select v-model="searchForm.order" style="width:110px">
          <el-option label="默认" value="desc"></el-option>
          <el-option label="升序" value="asc"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="reset()">重置</el-button>
        <el-button icon="el-icon-search" type="primary" @click="search()">查询</el-button>
      </el-form-item>
    </el-form>

    <div class="mt-body">
      <!-- 左：分类树 -->
      <div class="mt-tree">
        <div class="tree-head"><span>全部分类</span><a @click="$message.info('分类管理')">管理分类</a></div>
        <el-tree :data="treeData" node-key="key" :expand-on-click-node="false" default-expand-all
                 :highlight-current="true" @node-click="onTreeClick">
          <span class="tree-node" slot-scope="{ data }">
            <span>{{ data.label }}</span>
            <span class="tree-cnt" v-if="data.count!=null">{{ data.count }}</span>
          </span>
        </el-tree>
        <el-button class="tree-add" icon="el-icon-plus" @click="$message.info('新建分类')">新建分类</el-button>
      </div>

      <!-- 中：统计卡 + 表格 -->
      <div class="mt-main">
        <div class="mt-stats">
          <div class="ms"><i class="el-icon-files ms-ic b1"></i><div><div class="ms-l">素材总数</div><div class="ms-n">{{ stat.total }}</div></div></div>
          <div class="ms"><i class="el-icon-upload2 ms-ic b2"></i><div><div class="ms-l">已上架</div><div class="ms-n">{{ stat.online }} <span class="ms-p">{{ statPct(stat.online) }}</span></div></div></div>
          <div class="ms"><i class="el-icon-time ms-ic b3"></i><div><div class="ms-l">待审核</div><div class="ms-n">{{ stat.draft }} <span class="ms-p">{{ statPct(stat.draft) }}</span></div></div></div>
          <div class="ms"><i class="el-icon-star-on ms-ic b4"></i><div><div class="ms-l">推荐素材</div><div class="ms-n">{{ stat.recommend }} <span class="ms-p">{{ statPct(stat.recommend) }}</span></div></div></div>
          <div class="ms"><i class="el-icon-view ms-ic b5"></i><div><div class="ms-l">总浏览</div><div class="ms-n">{{ stat.views }}</div></div></div>
          <div class="ms"><i class="el-icon-warning-outline ms-ic b6"></i><div><div class="ms-l">待清理</div><div class="ms-n">{{ stat.recycle }} <span class="ms-p">{{ statPct(stat.recycle) }}</span></div></div></div>
        </div>

        <div class="table-content">
          <el-table class="tables" :data="dataList" v-loading="dataListLoading" border @selection-change="v=>sel=v" style="width: 100%">
            <el-table-column type="selection" width="45" align="center"></el-table-column>
            <el-table-column label="封面" align="center" width="90">
              <template slot-scope="s"><div class="mt-thumb"><img v-if="s.row.cover" :src="img(s.row.cover)"/><span class="mt-dur">{{ dur(s.row.duration) }}</span></div></template>
            </el-table-column>
            <el-table-column label="标题" min-width="160">
              <template slot-scope="s">
                <div class="mt-title">{{ s.row.title }}</div>
                <el-tag v-for="(t,i) in tags(s.row.tags)" :key="i" size="mini" class="tg">{{ t }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="分类路径" align="center" width="140">
              <template slot-scope="s">{{ s.row.industryBig }}<span v-if="s.row.industrySub"> &gt; {{ s.row.industrySub }}</span></template>
            </el-table-column>
            <el-table-column prop="contentType" label="内容类型" align="center" width="90"></el-table-column>
            <el-table-column label="上传时间" align="center" width="110">
              <template slot-scope="s">{{ (s.row.addtime||'').substr(0,10) }}</template>
            </el-table-column>
            <el-table-column label="喜欢" align="center" width="90">
              <template slot-scope="s">{{ s.row.likeCount||0 }}<div class="small">{{ pctText(s.row.likeCount, s.row) }}</div></template>
            </el-table-column>
            <el-table-column label="不喜欢" align="center" width="80">
              <template slot-scope="s">{{ s.row.dislikeCount||0 }}</template>
            </el-table-column>
            <el-table-column label="收藏" align="center" width="70">
              <template slot-scope="s">{{ s.row.favoriteCount||0 }}</template>
            </el-table-column>
            <el-table-column label="通用次数" align="center" width="80">
              <template slot-scope="s">{{ s.row.usedCount||0 }}</template>
            </el-table-column>
            <el-table-column label="状态" align="center" width="90">
              <template slot-scope="s"><el-tag size="mini" :type="s.row.status==='上架'?'success':(s.row.status==='回收站'?'danger':'info')">{{ s.row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="220" fixed="right">
              <template slot-scope="s">
                <el-button v-if="!recycle" type="text" size="small" @click="openEditMeta(s.row)">分类/标签</el-button>
                <el-button v-if="!recycle" type="text" size="small" @click="toggleShelf(s.row)">{{ s.row.status==='上架'?'下架':'上架' }}</el-button>
                <el-button type="text" size="small" @click="$message.info('数据详情')">数据</el-button>
                <el-button type="text" size="small" @click="deleteHandler(s.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination @size-change="v=>{pageSize=v;getDataList()}" @current-change="v=>{pageIndex=v;getDataList()}"
                         :current-page="pageIndex" :page-sizes="[10,20,50]" :page-size="pageSize"
                         :total="totalPage" layout="total, sizes, prev, pager, next, jumper"></el-pagination>
        </div>
      </div>

      <!-- 单条修改分类/标签 -->
      <el-dialog title="修改分类与标签" :visible.sync="editMeta.visible" width="420px" append-to-body>
        <el-form label-width="72px" size="small">
          <el-form-item label="标题">
            <div class="edit-meta-title">{{ editMeta.title }}</div>
          </el-form-item>
          <el-form-item label="行业">
            <el-select v-model="editMeta.industryBig" placeholder="选择行业" style="width:100%" @change="onEditBig">
              <el-option v-for="i in bigList" :key="i.id" :label="i.name" :value="i.name"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="业态">
            <el-select v-model="editMeta.industrySub" placeholder="选择业态" clearable style="width:100%">
              <el-option v-for="i in editSubList" :key="i.id" :label="i.name" :value="i.name"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="标签">
            <el-input v-model="editMeta.tags" placeholder="多个标签用逗号分隔，如：火锅,后厨"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer">
          <el-button size="small" @click="editMeta.visible=false">取消</el-button>
          <el-button type="primary" size="small" :loading="editMeta.saving" @click="saveEditMeta">保存</el-button>
        </div>
      </el-dialog>

      <!-- 右：批量上传面板（v-show 关闭不丢队列） -->
      <div class="mt-right" v-show="showUpload">
        <div class="side-panel">
          <div class="sp-head"><span>批量上传</span><i class="el-icon-close" @click="closeUploadPanel"></i></div>
          <div class="sp-body">
            <div class="sp-label">上传进度 <span class="up-pct">{{ upPercent }}%</span></div>
            <el-progress :percentage="upPercent" :show-text="false"></el-progress>
            <div class="up-stat">
              <span class="ok">成功 {{ upDoneCount }}</span>
              <span class="fail">失败 {{ upFailCount }}</span>
              <span>上传中 {{ upUploadingCount }}</span>
              <span>待传 {{ upPendingCount }}</span>
            </div>

            <div v-if="refreshHintNames.length" class="up-hint">
              <div class="up-hint-title">服务器对账：成功 {{ upDoneCount }}，未完成 {{ refreshHintNames.length }}。点「继续上传」将尝试从上次文件夹自动找回；找不到再手选：</div>
              <div class="up-hint-list">
                <div v-for="(n,i) in refreshHintNames" :key="'h'+i" class="up-hint-item">{{ n }}</div>
              </div>
            </div>

            <div class="sec-title">应用到全部视频</div>
            <div class="sp-label">分类</div>
            <div class="up-cat">
              <el-select v-model="up.industryBig" placeholder="行业" @change="onUpBig" size="small" style="width:48%">
                <el-option v-for="i in bigList" :key="i.id" :label="i.name" :value="i.name"></el-option>
              </el-select>
              <el-select v-model="up.industrySub" placeholder="业态" size="small" style="width:48%;margin-left:4%">
                <el-option v-for="i in upSubList" :key="i.id" :label="i.name" :value="i.name"></el-option>
              </el-select>
            </div>
            <div class="sp-label" style="margin-top:10px">内容类型</div>
            <el-select v-model="up.contentType" size="small" style="width:100%">
              <el-option v-for="t in contentTypes" :key="t" :label="t" :value="t"></el-option>
            </el-select>
            <div class="sp-label" style="margin-top:10px">标签（逗号分隔）</div>
            <el-input v-model="up.tags" size="small" placeholder="如：火锅,后厨"></el-input>

            <div class="sec-title">添加视频</div>
            <div v-if="filePickerOk || folderPickerOk" class="up-folder-row">
              <el-button v-if="filePickerOk" type="primary" size="small" icon="el-icon-document" style="width:100%;margin-bottom:8px" @click="pickFilesWithResume">
                选择视频文件（可多选，支持一键续传）
              </el-button>
              <el-button v-if="folderPickerOk" type="primary" plain size="small" icon="el-icon-folder-opened" style="width:100%" @click="pickFolderAndEnqueue">
                选择整个文件夹上传
              </el-button>
              <div v-if="up.folderName" class="up-folder-tip">已记住：{{ up.folderName }}</div>
              <div v-else class="up-folder-tip">推荐用上面「选择视频文件」直接勾选 mp4；关掉/刷新后再点「继续上传」可自动找回。下方拖拽方式刷新后需重选。</div>
            </div>
            <el-upload
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              multiple
              drag
              class="up-drag"
              :on-change="onPickFiles"
            >
              <i class="el-icon-upload"></i>
              <div class="el-upload__text">拖拽或<em>点击选择</em>（刷新后不可自动续传）</div>
            </el-upload>

            <div class="sec-title">上传队列（{{ up.queue.length }}）</div>
            <div v-if="up.queue.length===0" class="up-empty">暂无文件，请先选择</div>
            <div class="up-queue">
              <div v-for="item in up.queue" :key="item.id" class="up-q-item">
                <div class="up-q-name" :title="item.name">{{ item.name }}</div>
                <el-tag size="mini" :type="queueTagType(item.status)">{{ queueStatusText(item) }}</el-tag>
              </div>
            </div>
          </div>
          <div class="sp-foot-line">
            <el-button type="primary" :disabled="!canContinueUpload || up.running" :loading="up.running || up.autoLinking" @click="continueUpload" style="width:100%;margin-bottom:8px">
              {{ up.running || up.autoLinking ? '上传中…' : '继续上传剩余文件' }}
            </el-button>
            <el-button @click="clearFinishedQueue" :disabled="upDoneCount===0" style="width:48%">清除已成功</el-button>
            <el-button @click="closeUploadPanel" style="width:48%;margin-left:4%">关闭面板</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
/** 只保留「最后一批」上传名单，重开时用服务器素材对账 */
const UP_BATCH_KEY = "hyMaterial_upload_last_batch";
import {
  supportsFolderPicker,
  supportsFilePicker,
  saveDirHandle,
  loadDirHandle,
  ensureDirPermission,
  findFilesByNames,
  collectMediaFromDir,
  pickMediaFileHandles,
  mergeFileHandles,
  restoreFilesFromHandles,
  saveFileHandles,
  clearFileHandles
} from "@/utils/uploadFolderHandle";

export default {
  data() {
    return {
      searchForm: {title: "", industryBig: "", industrySub: "", contentType: "", status: "", order: "desc"},
      contentTypes: ["晒过程", "教知识", "讲故事", "说观点", "硬广"],
      bigList: [], subList: [], upSubList: [], editSubList: [], allIndustry: [],
      recycle: false,
      dataList: [], sel: [],
      pageIndex: 1, pageSize: 10, totalPage: 0, dataListLoading: false,
      showUpload: false,
      reconciling: false,
      folderPickerOk: supportsFolderPicker(),
      filePickerOk: supportsFilePicker(),
      editMeta: {
        visible: false,
        saving: false,
        id: null,
        title: "",
        industryBig: "",
        industrySub: "",
        tags: ""
      },
      stat: {total: 0, online: 0, draft: 0, recommend: 0, views: 0, recycle: 0},
      treeCounts: {},
      refreshHintNames: [],
      up: {
        industryBig: "",
        industrySub: "",
        contentType: "硬广",
        tags: "",
        running: false,
        autoLinking: false,
        concurrency: 2,
        queue: [],
        batchId: "",
        startedAt: 0,
        folderName: ""
      }
    };
  },
  computed: {
    uploadUrl() { return this.$base.url + "file/upload"; },
    uploadHeaders() { return {Token: this.$storage.get("Token")}; },
    treeData() {
      const all = {key: "__all__", label: "全部分类", count: this.stat.total, children: []};
      const bigs = this.bigList.map(b => ({
        key: "b_" + b.id, label: b.name, big: b.name, count: this.treeCounts[b.name] || null,
        children: this.allIndustry.filter(i => i.parentId === b.id).map(s => ({
          key: "s_" + s.id, label: s.name, big: b.name, sub: s.name, count: this.treeCounts[b.name + ">" + s.name] || null
        }))
      }));
      all.children = bigs;
      return [all];
    },
    upDoneCount() { return this.up.queue.filter(i => i.status === "success").length; },
    upFailCount() { return this.up.queue.filter(i => i.status === "fail").length; },
    upUploadingCount() { return this.up.queue.filter(i => i.status === "uploading").length; },
    upPendingCount() { return this.up.queue.filter(i => i.status === "pending" || i.status === "need_reselect").length; },
    upRemainCount() {
      return this.up.queue.filter(i => i.status === "pending" || i.status === "fail" || i.status === "uploading" || i.status === "need_reselect").length;
    },
    upPercent() {
      const t = this.up.queue.length;
      if (!t) return 0;
      return Math.round(this.upDoneCount / t * 100);
    },
    canContinueUpload() {
      return this.up.queue.some(i => ((i.status === "pending" || i.status === "fail") && i.file) || i.status === "need_reselect");
    }
  },
  created() {
    this.loadIndustry();
    this.getDataList();
    this.loadStat();
    this.restoreFolderName();
  },
  methods: {
    img(v) { return this.$base.url + String(v).split(",")[0]; },
    dur(sec) {
      if (sec == null || sec === "") return "00:00";
      if (typeof sec === "string" && sec.indexOf(":") >= 0) return sec;
      sec = Number(sec) || 0;
      const m = Math.floor(sec / 60), s = sec % 60;
      return (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
    },
    tags(v) { return v ? String(v).split(/[,，]/).filter(Boolean).slice(0, 3) : []; },
    pctText(n, row) {
      const tot = (row.likeCount || 0) + (row.dislikeCount || 0);
      return tot ? Math.round((n || 0) / tot * 100) + "%" : "";
    },
    statPct(n) { return this.stat.total ? Math.round((n || 0) / this.stat.total * 100) + "%" : "0%"; },
    loadIndustry() {
      this.$http({url: "hyIndustry/list", method: "get"}).then(({data}) => {
        if (data.code === 0) {
          this.allIndustry = data.data || [];
          this.bigList = this.allIndustry.filter(i => i.level === 1);
        }
      });
    },
    loadStat() {
      this.$http({url: "hyMaterial/page", method: "get", params: {page: 1, limit: 3000}}).then(({data}) => {
        if (data.code !== 0) return;
        const l = data.data.list || [];
        const live = l.filter(m => m.status !== "回收站");
        const counts = {};
        live.forEach(m => {
          if (m.industryBig) {
            counts[m.industryBig] = (counts[m.industryBig] || 0) + 1;
            if (m.industrySub) counts[m.industryBig + ">" + m.industrySub] = (counts[m.industryBig + ">" + m.industrySub] || 0) + 1;
          }
        });
        this.treeCounts = counts;
        this.stat = {
          total: live.length,
          online: live.filter(m => m.status === "上架").length,
          draft: live.filter(m => m.status === "下架").length,
          recommend: live.filter(m => (m.heat || 0) >= 90).length,
          views: live.reduce((a, m) => a + (m.usedCount || 0), 0),
          recycle: l.filter(m => m.status === "回收站").length
        };
      });
    },
    onTreeClick(node) {
      if (node.key === "__all__") { this.searchForm.industryBig = ""; this.searchForm.industrySub = ""; }
      else { this.searchForm.industryBig = node.big || ""; this.onBig(node.big); this.searchForm.industrySub = node.sub || ""; }
      this.search();
    },
    onBig(v) {
      const big = this.bigList.find(b => b.name === v);
      this.subList = big ? this.allIndustry.filter(i => i.parentId === big.id) : [];
    },
    onUpBig(v) {
      const big = this.bigList.find(b => b.name === v);
      this.upSubList = big ? this.allIndustry.filter(i => i.parentId === big.id) : [];
      this.up.industrySub = "";
    },
    search() { this.pageIndex = 1; this.getDataList(); },
    reset() {
      this.searchForm = {title: "", industryBig: "", industrySub: "", contentType: "", status: "", order: "desc"};
      this.subList = []; this.search();
    },
    toggleRecycle() { this.recycle = !this.recycle; this.search(); },
    getDataList() {
      this.dataListLoading = true;
      let params = {page: this.pageIndex, limit: this.pageSize, sort: "heat", order: this.searchForm.order || "desc"};
      if (this.searchForm.title) params.title = "%" + this.searchForm.title + "%";
      if (this.searchForm.industryBig) params.industryBig = this.searchForm.industryBig;
      if (this.searchForm.industrySub) params.industrySub = this.searchForm.industrySub;
      if (this.searchForm.contentType) params.contentType = this.searchForm.contentType;
      if (this.searchForm.status && !this.recycle) params.status = this.searchForm.status;
      else params.status = this.recycle ? "回收站" : null;
      this.$http({url: "hyMaterial/page", method: "get", params}).then(({data}) => {
        let list = (data.code === 0 ? data.data.list : []) || [];
        if (!this.recycle) list = list.filter(m => m.status !== "回收站");
        this.dataList = list;
        this.totalPage = data.code === 0 ? data.data.total : 0;
        this.dataListLoading = false;
      });
    },
    clearSel() { this.sel = []; this.dataList = this.dataList.slice(); },
    toggleShelf(row) {
      const next = row.status === "上架" ? "下架" : "上架";
      this.$http({url: "hyMaterial/update", method: "post", data: {id: row.id, status: next}}).then(({data}) => {
        if (data.code === 0) { this.$message.success("操作成功"); this.getDataList(); this.loadStat(); }
      });
    },
    batchShelf() {
      if (!this.sel.length) {
        this.$message.warning("请先勾选表格中的素材");
        return;
      }
      const allOnline = this.sel.every(m => m.status === "上架");
      this.bulkStatus(this.sel.map(m => m.id), allOnline ? "下架" : "上架", "已批量" + (allOnline ? "下架" : "上架"));
    },
    openEditMeta(row) {
      this.editMeta = {
        visible: true,
        saving: false,
        id: row.id,
        title: row.title || "",
        industryBig: row.industryBig || "",
        industrySub: row.industrySub || "",
        tags: row.tags || ""
      };
      this.onEditBig(this.editMeta.industryBig, true);
    },
    onEditBig(v, keepSub) {
      const big = this.bigList.find(b => b.name === v);
      this.editSubList = big ? this.allIndustry.filter(i => i.parentId === big.id) : [];
      if (!keepSub) this.editMeta.industrySub = "";
    },
    saveEditMeta() {
      if (!this.editMeta.id) return;
      if (!this.editMeta.industryBig) {
        this.$message.warning("请选择行业");
        return;
      }
      this.editMeta.saving = true;
      this.$http({
        url: "hyMaterial/update",
        method: "post",
        data: {
          id: this.editMeta.id,
          industryBig: this.editMeta.industryBig,
          industrySub: this.editMeta.industrySub || "",
          tags: this.editMeta.tags || ""
        }
      }).then(({data}) => {
        this.editMeta.saving = false;
        if (data.code === 0) {
          this.$message.success("已保存分类与标签");
          this.editMeta.visible = false;
          this.getDataList();
          this.loadStat();
        } else {
          this.$message.error(data.msg || "保存失败");
        }
      }).catch(() => {
        this.editMeta.saving = false;
      });
    },
    batchCategory() {
      this.$prompt("请输入新的行业大类>业态（用 > 分隔）", "批量修改分类", {inputPlaceholder: "如：餐饮>火锅"}).then(({value}) => {
        const [big, sub] = String(value || "").split(">");
        Promise.all(this.sel.map(m => this.$http({url: "hyMaterial/update", method: "post", data: {id: m.id, industryBig: big || m.industryBig, industrySub: sub || ""}}))).then(() => {
          this.$message.success("已修改分类"); this.getDataList(); this.loadStat();
        });
      }).catch(() => {});
    },
    batchRecycle() {
      this.bulkStatus(this.sel.map(m => m.id), "回收站", "已移入回收站");
    },
    bulkStatus(ids, status, msg) {
      Promise.all(ids.map(id => this.$http({url: "hyMaterial/update", method: "post", data: {id, status}}))).then(() => {
        this.$message.success(msg); this.getDataList(); this.loadStat();
      });
    },

    openUploadPanel() {
      this.showUpload = true;
      this.restoreFolderName();
      this.reconcileLastBatch().then(() => {
        if (this.refreshHintNames.length) {
          this.$message.warning("有未完成上传，可直接点「继续上传」尝试自动找回");
        }
      });
    },
    closeUploadPanel() {
      this.saveBatchRecord();
      this.showUpload = false;
      if (this.upRemainCount > 0) {
        this.$message.info(`已关闭面板，本批剩余 ${this.upRemainCount} 个未完成，再次打开将按服务器对账`);
      }
    },
    queueTagType(status) {
      return ({success: "success", fail: "danger", uploading: "warning", pending: "info", need_reselect: "danger"})[status] || "info";
    },
    queueStatusText(item) {
      return ({
        success: "已完成",
        fail: "失败",
        uploading: "上传中",
        pending: "待传",
        need_reselect: "需重选"
      })[item.status] || item.status;
    },
    parseAddtime(t) {
      if (!t) return 0;
      if (typeof t === "number") return t;
      const s = String(t).replace("T", " ").replace(/-/g, "/");
      const ms = new Date(s).getTime();
      return isNaN(ms) ? 0 : ms;
    },
    loadBatchRecord() {
      try {
        const raw = localStorage.getItem(UP_BATCH_KEY);
        if (!raw) return null;
        const batch = JSON.parse(raw);
        if (!batch || !Array.isArray(batch.files) || !batch.files.length) return null;
        return batch;
      } catch (e) {
        return null;
      }
    },
    saveBatchRecord() {
      const files = this.up.queue.map(i => ({
        name: i.name,
        size: i.size || (i.file && i.file.size) || 0
      }));
      if (!files.length) {
        try { localStorage.removeItem(UP_BATCH_KEY); } catch (e) {}
        this.up.batchId = "";
        this.up.startedAt = 0;
        return;
      }
      if (!this.up.startedAt) this.up.startedAt = Date.now();
      if (!this.up.batchId) this.up.batchId = "b_" + this.up.startedAt;
      const batch = {
        batchId: this.up.batchId,
        startedAt: this.up.startedAt,
        files,
        folderName: this.up.folderName || "",
        industryBig: this.up.industryBig,
        industrySub: this.up.industrySub,
        contentType: this.up.contentType,
        tags: this.up.tags
      };
      try { localStorage.setItem(UP_BATCH_KEY, JSON.stringify(batch)); } catch (e) {}
    },
    clearBatchRecord() {
      try { localStorage.removeItem(UP_BATCH_KEY); } catch (e) {}
      this.up.batchId = "";
      this.up.startedAt = 0;
      clearFileHandles().catch(() => {});
    },
    restoreFolderName() {
      const batch = this.loadBatchRecord();
      if (batch && batch.folderName) this.up.folderName = batch.folderName;
      if (!this.folderPickerOk) return;
      loadDirHandle().then(h => {
        if (h && h.name) this.up.folderName = h.name;
      }).catch(() => {});
    },
    /** 多选单个/多个 mp4（文件句柄可续传） */
    async pickFilesWithResume() {
      if (!this.filePickerOk) {
        this.$message.warning("当前浏览器不支持，请用 Chrome/Edge");
        return;
      }
      try {
        const {handles, files} = await pickMediaFileHandles();
        if (!files.length) return;
        await mergeFileHandles(handles);
        this.up.folderName = `已选 ${files.length} 个文件`;

        const startNew = this.up.queue.length === 0 || this.up.queue.every(i => i.status === "success");
        if (startNew) {
          this.up.queue = [];
          this.up.startedAt = Date.now();
          this.up.batchId = "b_" + this.up.startedAt;
          this.refreshHintNames = [];
        }

        let added = 0;
        files.forEach(raw => {
          const name = raw.name;
          const size = raw.size || 0;
          const need = this.up.queue.find(i => i.status === "need_reselect" && i.name === name && !i.file);
          if (need) {
            need.file = raw;
            need.size = size;
            need.status = "pending";
            need.msg = "";
            added++;
            return;
          }
          const dup = this.up.queue.find(i => i.name === name && i.size === size &&
            (i.status === "pending" || i.status === "uploading" || i.status === "success"));
          if (dup) return;
          this.up.queue.push({
            id: "u_" + Date.now() + "_" + Math.random().toString(36).slice(2, 8),
            name,
            size,
            file: raw,
            status: "pending",
            msg: ""
          });
          added++;
        });
        this.refreshHintNames = this.up.queue.filter(i => i.status === "need_reselect").map(i => i.name);
        this.saveBatchRecord();
        this.$message.success(`已加入 ${added} 个文件，开始上传`);
        this.continueUpload();
      } catch (e) {
        if (e && e.name === "AbortError") return;
        this.$message.error((e && e.message) || "选择文件失败");
      }
    },
    async pickFolderAndEnqueue() {
      if (!this.folderPickerOk) {
        this.$message.warning("当前浏览器不支持选文件夹，请用 Chrome/Edge，或上方选择视频文件");
        return;
      }
      try {
        const dir = await window.showDirectoryPicker({mode: "read"});
        await saveDirHandle(dir);
        this.up.folderName = dir.name || "已选文件夹";
        const files = await collectMediaFromDir(dir);
        if (!files.length) {
          this.$message.warning("该文件夹下没有视频/图片");
          return;
        }
        // 文件夹模式也尽量记下文件句柄（若枚举得到的是同源 File，续传主要靠目录）
        this.up.queue = [];
        this.up.startedAt = Date.now();
        this.up.batchId = "b_" + this.up.startedAt;
        this.refreshHintNames = [];
        files.forEach(raw => {
          this.up.queue.push({
            id: "u_" + Date.now() + "_" + Math.random().toString(36).slice(2, 8),
            name: raw.name,
            size: raw.size || 0,
            file: raw,
            status: "pending",
            msg: ""
          });
        });
        this.saveBatchRecord();
        this.$message.success(`已从文件夹加入 ${files.length} 个文件，开始上传`);
        this.continueUpload();
      } catch (e) {
        if (e && e.name === "AbortError") return;
        this.$message.error((e && e.message) || "选择文件夹失败");
      }
    },
    /** 优先用记住的文件句柄找回；否则用文件夹；再不行提示手选 */
    async autoRelinkFromFolder() {
      const needs = this.up.queue.filter(i => i.status === "need_reselect" && !i.file);
      if (!needs.length) return {linked: 0, miss: []};
      const needNames = needs.map(i => i.name);

      // 1) 单个文件句柄续传
      if (this.filePickerOk) {
        try {
          const fromFiles = await restoreFilesFromHandles(needNames);
          let linked = 0;
          needs.forEach(item => {
            const raw = fromFiles[item.name];
            if (!raw || item.file) return;
            item.file = raw;
            item.size = raw.size || item.size || 0;
            item.status = "pending";
            item.msg = "";
            linked++;
          });
          this.refreshHintNames = this.up.queue.filter(i => i.status === "need_reselect").map(i => i.name);
          this.saveBatchRecord();
          if (!this.refreshHintNames.length) return {linked, miss: []};
          if (linked > 0 && this.refreshHintNames.length) {
            // 还有剩余，继续尝试文件夹
          } else if (linked > 0) {
            return {linked, miss: []};
          }
        } catch (e) { /* fall through */ }
      }

      const stillNeed = this.up.queue.filter(i => i.status === "need_reselect" && !i.file);
      if (!stillNeed.length) {
        return {linked: needs.length, miss: []};
      }
      if (!this.folderPickerOk) {
        return {linked: needs.length - stillNeed.length, miss: stillNeed.map(i => i.name)};
      }

      let dir = await loadDirHandle();
      if (!dir) {
        try {
          dir = await window.showDirectoryPicker({mode: "read"});
          await saveDirHandle(dir);
          this.up.folderName = dir.name || "已选文件夹";
        } catch (e) {
          if (e && e.name === "AbortError") {
            return {linked: needs.length - stillNeed.length, miss: stillNeed.map(i => i.name), aborted: true};
          }
          // 没有文件夹时，引导再选一次文件
          if (this.filePickerOk) {
            try {
              const {handles, files} = await pickMediaFileHandles();
              await mergeFileHandles(handles);
              let linked2 = 0;
              files.forEach(raw => {
                const item = stillNeed.find(i => i.name === raw.name && !i.file);
                if (!item) return;
                item.file = raw;
                item.size = raw.size || item.size || 0;
                item.status = "pending";
                item.msg = "";
                linked2++;
              });
              this.refreshHintNames = this.up.queue.filter(i => i.status === "need_reselect").map(i => i.name);
              this.saveBatchRecord();
              return {
                linked: needs.length - this.refreshHintNames.length,
                miss: this.refreshHintNames.slice()
              };
            } catch (e2) {
              if (e2 && e2.name === "AbortError") {
                return {linked: needs.length - stillNeed.length, miss: stillNeed.map(i => i.name), aborted: true};
              }
            }
          }
          return {linked: needs.length - stillNeed.length, miss: stillNeed.map(i => i.name), noDir: true};
        }
      }

      const ok = await ensureDirPermission(dir);
      if (!ok) return {linked: needs.length - stillNeed.length, miss: stillNeed.map(i => i.name), denied: true};

      this.up.folderName = dir.name || this.up.folderName;
      const found = await findFilesByNames(dir, stillNeed.map(i => i.name));
      let linked = needs.length - stillNeed.length;
      const miss = [];
      stillNeed.forEach(item => {
        const raw = found[item.name];
        if (raw) {
          item.file = raw;
          item.size = raw.size || item.size || 0;
          item.status = "pending";
          item.msg = "";
          linked++;
        } else {
          miss.push(item.name);
        }
      });
      this.refreshHintNames = this.up.queue.filter(i => i.status === "need_reselect").map(i => i.name);
      this.saveBatchRecord();
      return {linked, miss};
    },
    /** 用服务器素材表对账最后一批：有记录=成功，没有=需重选 */
    reconcileLastBatch() {
      if (this.reconciling) return Promise.resolve();
      // 内存里还在传，只刷新成功态，不整表重建
      const alive = this.up.queue.some(i => i.file && (i.status === "uploading" || i.status === "pending"));
      if (alive) return Promise.resolve();

      const batch = this.loadBatchRecord();
      if (!batch) {
        this.refreshHintNames = [];
        return Promise.resolve();
      }

      this.reconciling = true;
      const startedAt = Number(batch.startedAt) || 0;
      // 允许 2 分钟时钟偏差
      const since = Math.max(0, startedAt - 2 * 60 * 1000);
      const nameSet = {};
      (batch.files || []).forEach(f => { if (f && f.name) nameSet[f.name] = f; });

      return this.$http({
        url: "hyMaterial/list",
        method: "get",
        params: {}
      }).then(({data}) => {
        const list = (data && data.code === 0 && data.data) || [];
        const hitNames = {};
        list.forEach(m => {
          const title = m.title || "";
          if (!nameSet[title]) return;
          const t = this.parseAddtime(m.addtime);
          if (t >= since) hitNames[title] = true;
        });

        // 恢复分类等表单
        if (batch.industryBig) {
          this.up.industryBig = batch.industryBig;
          this.onUpBig(batch.industryBig);
          this.up.industrySub = batch.industrySub || "";
        }
        if (batch.contentType) this.up.contentType = batch.contentType;
        if (batch.tags != null) this.up.tags = batch.tags;
        if (batch.folderName) this.up.folderName = batch.folderName;
        this.up.batchId = batch.batchId || ("b_" + startedAt);
        this.up.startedAt = startedAt;

        // 保留内存里已有 File 的项
        const keepFile = {};
        this.up.queue.forEach(i => {
          if (i.file && i.name) keepFile[i.name] = i.file;
        });

        this.up.queue = (batch.files || []).map((f, idx) => {
          const name = f.name;
          const size = f.size || 0;
          const id = "r_" + startedAt + "_" + idx;
          if (hitNames[name]) {
            return {id, name, size, file: null, status: "success", msg: ""};
          }
          if (keepFile[name]) {
            return {id, name, size, file: keepFile[name], status: "pending", msg: ""};
          }
          return {id, name, size, file: null, status: "need_reselect", msg: "服务器未找到，请重新选择该文件"};
        });

        this.refreshHintNames = this.up.queue.filter(i => i.status === "need_reselect").map(i => i.name);
        const done = this.upDoneCount;
        const miss = this.refreshHintNames.length;
        if (miss) {
          this.$nextTick(() => {
            this.$alert(
              `上一批共 ${this.up.queue.length} 个文件。\n服务器已确认成功 ${done} 个，未完成 ${miss} 个。\n直接点「继续上传剩余文件」可自动找回（需用「选择视频文件」选过的）；找不到再手选：\n\n` +
              this.refreshHintNames.join("\n"),
              "上传对账结果",
              {confirmButtonText: "知道了", type: "warning"}
            );
          });
        } else if (done > 0) {
          this.$message.success(`上一批 ${done} 个文件均已在服务器确认成功`);
        }
      }).catch(() => {
        this.$message.error("对账失败，请稍后重试打开面板");
      }).finally(() => {
        this.reconciling = false;
      });
    },
    onPickFiles(file) {
      if (!file || file.status !== "ready") return;
      const raw = file.raw;
      if (!raw) return;
      const name = raw.name || file.name || "未命名";
      const size = raw.size || 0;

      // 补回「需重选」
      const need = this.up.queue.find(i => i.status === "need_reselect" && i.name === name && !i.file);
      if (need) {
        if (need.size && size && need.size !== size) {
          this.$message.warning(`「${name}」大小与上次不一致，请确认是否同一文件`);
        }
        need.file = raw;
        need.size = size;
        need.status = "pending";
        need.msg = "";
        this.refreshHintNames = this.up.queue.filter(i => i.status === "need_reselect").map(i => i.name);
        this.saveBatchRecord();
        if (!this.up.running) this.continueUpload();
        return;
      }

      const dup = this.up.queue.find(i => i.name === name && i.size === size &&
        (i.status === "pending" || i.status === "uploading" || i.status === "success"));
      if (dup) return;

      // 新开一批：当前队列空，或上一批已全部成功
      const startNew = this.up.queue.length === 0 || this.up.queue.every(i => i.status === "success");
      if (startNew) {
        if (this.up.queue.every(i => i.status === "success") && this.up.queue.length) {
          this.up.queue = [];
        }
        this.up.startedAt = Date.now();
        this.up.batchId = "b_" + this.up.startedAt;
      }

      this.up.queue.push({
        id: "u_" + Date.now() + "_" + Math.random().toString(36).slice(2, 8),
        name,
        size,
        file: raw,
        status: "pending",
        msg: ""
      });
      this.saveBatchRecord();
      if (!this.up.running) this.continueUpload();
    },
    clearFinishedQueue() {
      this.up.queue = this.up.queue.filter(i => i.status !== "success");
      this.refreshHintNames = this.up.queue.filter(i => i.status === "need_reselect").map(i => i.name);
      if (!this.up.queue.length) this.clearBatchRecord();
      else this.saveBatchRecord();
      this.$message.success("清除已成功");
    },
    async continueUpload() {
      // 先尝试从上次文件夹自动补回「需重选」
      const needCount = this.up.queue.filter(i => i.status === "need_reselect" && !i.file).length;
      if (needCount > 0) {
        this.up.autoLinking = true;
        try {
          const r = await this.autoRelinkFromFolder();
          if (r.noDir) {
            this.$message.warning("未能打开文件夹，请用「选择文件夹上传」或手动拖入未完成文件");
          } else if (r.aborted) {
            return;
          } else if (r.denied) {
            this.$message.warning("需要允许访问文件夹后才能自动续传");
          } else if (r.linked > 0) {
            this.$message.success(`已自动找回 ${r.linked} 个文件`);
          }
          if (r.miss && r.miss.length && r.linked === 0 && !r.noDir && !r.denied) {
            this.$message.warning("未能自动找回，请再用「选择视频文件」勾选未完成的 mp4");
          } else if (r.miss && r.miss.length && r.linked > 0) {
            this.$message.warning(`另有 ${r.miss.length} 个未找到，请手选或检查是否改名/挪走`);
          }
        } catch (e) {
          this.$message.error((e && e.message) || "自动找回失败");
        } finally {
          this.up.autoLinking = false;
        }
      }

      const ready = this.up.queue.filter(i => (i.status === "pending" || i.status === "fail") && i.file);
      if (!ready.length) {
        if (this.up.queue.some(i => i.status === "need_reselect")) {
          this.$message.warning("仍有文件需重选：点「选择视频文件」勾选同名 mp4");
        } else {
          this.$message.info("没有可继续上传的文件");
        }
        return;
      }
      ready.forEach(i => { if (i.status === "fail") { i.status = "pending"; i.msg = ""; } });
      if (!this.up.startedAt) this.up.startedAt = Date.now();
      this.saveBatchRecord();
      this.pumpUpload();
    },
    pumpUpload() {
      if (this.up.running) {
        this.scheduleNext();
        return;
      }
      this.up.running = true;
      this.scheduleNext();
    },
    scheduleNext() {
      const uploading = this.up.queue.filter(i => i.status === "uploading").length;
      const slots = this.up.concurrency - uploading;
      if (slots <= 0) return;
      const nexts = this.up.queue.filter(i => i.status === "pending" && i.file).slice(0, slots);
      if (!nexts.length) {
        if (uploading === 0) {
          this.up.running = false;
          this.saveBatchRecord();
        }
        return;
      }
      nexts.forEach(item => this.uploadOne(item));
    },
    uploadOne(item) {
      item.status = "uploading";
      item.msg = "";
      const fd = new FormData();
      fd.append("file", item.file);
      this.$http({
        url: "file/upload",
        method: "post",
        data: fd,
        headers: {"Content-Type": "multipart/form-data"},
        timeout: 1000 * 86400
      }).then(({data}) => {
        if (!(data && data.code === 0 && data.file)) {
          item.status = "fail";
          item.msg = (data && data.msg) || "上传失败";
          return;
        }
        return this.$http({
          url: "hyMaterial/save",
          method: "post",
          data: {
            title: item.name,
            cover: data.file,
            video: data.file,
            industryBig: this.up.industryBig,
            industrySub: this.up.industrySub,
            contentType: this.up.contentType,
            tags: this.up.tags,
            status: "下架",
            heat: 0
          }
        }).then(({data: saveRes}) => {
          if (saveRes && saveRes.code === 0) {
            item.status = "success";
            item.msg = "";
            item.file = null;
            this.getDataList();
            this.loadStat();
          } else {
            item.status = "fail";
            item.msg = (saveRes && saveRes.msg) || "保存素材失败";
          }
        });
      }).catch(err => {
        item.status = "fail";
        item.msg = (err && err.message) || "网络错误";
      }).finally(() => {
        this.saveBatchRecord();
        this.scheduleNext();
        const still = this.up.queue.some(i => i.status === "pending" || i.status === "uploading");
        if (!still) this.up.running = false;
      });
    },
    exportReport() {
      const head = ["标题", "分类", "内容类型", "喜欢", "不喜欢", "收藏", "通用次数", "状态"];
      const rows = this.dataList.map(m => [m.title, m.industryBig + (m.industrySub ? ">" + m.industrySub : ""), m.contentType, m.likeCount || 0, m.dislikeCount || 0, m.favoriteCount || 0, m.usedCount || 0, m.status]);
      let csv = "\ufeff" + head.join(",") + "\n" + rows.map(r => r.join(",")).join("\n");
      const blob = new Blob([csv], {type: "text/csv"});
      const a = document.createElement("a");
      a.href = URL.createObjectURL(blob);
      a.download = "素材报表.csv";
      a.click();
    },
    deleteHandler(id) {
      this.$confirm("确定彻底删除该素材?", "提示", {type: "warning"}).then(() => {
        this.$http({url: "hyMaterial/delete", method: "post", data: [Number(id)]}).then(({data}) => {
          if (data.code === 0) { this.$message.success("操作成功"); this.getDataList(); this.loadStat(); }
        });
      });
    }
  }
};
</script>

<style scoped>
.small { font-size: 12px; color: #8A94A6; }
.tg { margin: 2px 2px 0 0; background: #EAF1FF; color: #2F6BFF; border-color: #dbe7ff; }

.mt-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; flex-wrap: wrap; gap: 8px; }
.tb-left { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.tb-sel { font-size: 13px; color: #5a6473; margin-left: 4px; }
.tb-sel a { color: #2F6BFF; cursor: pointer; margin-left: 4px; }
.tb-right { display: flex; gap: 8px; }
.mt-filter { padding: 12px 14px; }

.mt-body { display: flex; gap: 14px; align-items: flex-start; }
.mt-tree { width: 160px; flex-shrink: 0; background: #fff; border: 1px solid #E7ECF3; border-radius: 10px; padding: 12px 10px; box-shadow: 0 1px 4px rgba(0,21,41,.04); }
.tree-head { display: flex; justify-content: space-between; align-items: center; font-weight: 700; color: #1F2733; padding: 4px 4px 10px; border-bottom: 1px solid #F0F2F5; margin-bottom: 6px; }
.tree-head a { color: #2F6BFF; cursor: pointer; font-size: 12px; font-weight: 400; }
.tree-node { display: flex; justify-content: space-between; align-items: center; width: 100%; font-size: 13px; padding-right: 6px; }
.tree-cnt { color: #a8b0bd; font-size: 12px; }
.tree-add { width: 100%; margin-top: 10px; }

.mt-main { flex: 1; min-width: 0; }
.mt-stats { display: grid; grid-template-columns: repeat(6, 1fr); gap: 10px; margin-bottom: 14px; }
.ms { display: flex; align-items: center; gap: 8px; background: #fff; border: 1px solid #EEF1F5; border-radius: 10px; padding: 10px 12px; box-shadow: 0 1px 4px rgba(0,21,41,.05); }
.ms-ic { width: 34px; height: 34px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 17px; color: #fff; flex-shrink: 0; }
.ms-ic.b1 { background: #2F6BFF; } .ms-ic.b2 { background: #22B07D; } .ms-ic.b3 { background: #FF8A3D; }
.ms-ic.b4 { background: #7C5CFF; } .ms-ic.b5 { background: #16B8A6; } .ms-ic.b6 { background: #FF5A5F; }
.ms-l { font-size: 12px; color: #8A94A6; }
.ms-n { font-size: 20px; font-weight: 800; color: #1F2733; line-height: 1.1; }
.ms-p { font-size: 11px; font-weight: 500; color: #22B07D; }

.mt-thumb { position: relative; width: 62px; height: 44px; border-radius: 6px; overflow: hidden; background: #1F2733; margin: 0 auto; }
.mt-thumb img { width: 100%; height: 100%; object-fit: cover; }
.mt-dur { position: absolute; right: 3px; bottom: 2px; background: rgba(0,0,0,.65); color: #fff; font-size: 10px; padding: 0 3px; border-radius: 3px; }
.mt-title { font-weight: 500; color: #1F2733; margin-bottom: 4px; }

.mt-right { width: 300px; flex-shrink: 0; }
.side-panel { background: #fff; border: 1px solid #EEF1F5; border-radius: 12px; box-shadow: 0 1px 4px rgba(0,21,41,.05); overflow: hidden; }
.sp-head { display: flex; align-items: center; justify-content: space-between; padding: 14px 18px; font-size: 15px; font-weight: 700; color: #1F2733; border-bottom: 1px solid #EEF1F5; }
.sp-head i { cursor: pointer; color: #a8b0bd; }
.sp-body { padding: 14px 18px; }
.sp-label { font-size: 13px; color: #8A94A6; margin-bottom: 6px; }
.up-pct { color: #2F6BFF; font-weight: 700; float: right; }
.up-stat { display: flex; gap: 12px; font-size: 12px; margin: 8px 0; color: #8A94A6; }
.up-stat .ok { color: #22B07D; } .up-stat .fail { color: #FF5A5F; }
.sec-title { font-size: 14px; font-weight: 700; color: #1F2733; margin: 16px 0 10px; padding-left: 8px; border-left: 3px solid #2F6BFF; }
.up-cat { display: flex; }
.up-drag { width: 100%; }
.up-folder-row { margin-bottom: 10px; }
.up-folder-tip { font-size: 12px; color: #8A94A6; margin-top: 6px; line-height: 1.4; }
.up-badge { margin-left: 6px; }
.up-hint {
  background: #FFF6E9; border: 1px solid #FFE0B2; border-radius: 8px;
  padding: 10px 12px; margin: 10px 0; font-size: 12px; color: #B8791F;
}
.up-hint-title { font-weight: 600; margin-bottom: 6px; line-height: 1.5; }
.up-hint-list { max-height: 120px; overflow-y: auto; }
.up-hint-item { padding: 2px 0; word-break: break-all; }
.up-empty { font-size: 12px; color: #a8b0bd; padding: 8px 0; }
.up-queue { max-height: 220px; overflow-y: auto; border: 1px solid #EEF1F5; border-radius: 8px; }
.up-q-item {
  display: flex; align-items: center; justify-content: space-between; gap: 8px;
  padding: 8px 10px; border-bottom: 1px dashed #F0F2F5; font-size: 12px;
}
.up-q-item:last-child { border-bottom: none; }
.up-q-name {
  flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #1F2733;
}
.sp-foot-line { padding: 12px 18px 16px; }
.edit-meta-title {
  font-size: 13px; color: #1F2733; line-height: 1.4;
  word-break: break-all; max-height: 40px; overflow: hidden;
}
</style>
