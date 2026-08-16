<template>
  <div class="main-content">
    <!-- 顶部批量工具栏 -->
    <div class="mt-toolbar">
      <div class="tb-left">
        <el-button type="primary" icon="el-icon-video-camera" @click="showUpload=true">批量上传视频</el-button>
        <el-button icon="el-icon-plus" @click="showUpload=true">新建素材</el-button>
        <el-button icon="el-icon-folder" :disabled="sel.length<=0" @click="batchCategory()">批量修改分类</el-button>
        <el-button icon="el-icon-sort" :disabled="sel.length<=0" @click="batchShelf()">批量上下架</el-button>
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
            <el-table-column label="操作" align="center" width="150" fixed="right">
              <template slot-scope="s">
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

      <!-- 右：批量上传面板 -->
      <div class="mt-right" v-if="showUpload">
        <div class="side-panel">
          <div class="sp-head"><span>批量上传</span><i class="el-icon-close" @click="showUpload=false"></i></div>
          <div class="sp-body">
            <div class="sp-label">上传进度 <span class="up-pct">{{ up.total? Math.round(up.done/up.total*100):0 }}%</span></div>
            <el-progress :percentage="up.total? Math.round(up.done/up.total*100):0" :show-text="false"></el-progress>
            <div class="up-stat"><span class="ok">成功 {{ up.done }}</span><span class="fail">失败 {{ up.fail.length }}</span><span>上传中 {{ Math.max(up.total-up.done-up.fail.length,0) }}</span></div>

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

            <div class="sec-title">去重检测</div>
            <el-upload :action="uploadUrl" :headers="uploadHeaders" multiple :on-success="onUploaded" :before-upload="beforeUp" drag class="up-drag">
              <i class="el-icon-upload"></i>
              <div class="el-upload__text">拖拽或<em>点击上传</em>视频/图片</div>
            </el-upload>

            <div v-if="up.fail.length" class="sec-title">失败视频（{{ up.fail.length }}）</div>
            <div v-for="(f,i) in up.fail" :key="i" class="fail-item">{{ f }}</div>
          </div>
          <div class="sp-foot-line"><el-button style="width:100%" @click="showUpload=false">关闭</el-button></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      searchForm: {title: "", industryBig: "", industrySub: "", contentType: "", status: "", order: "desc"},
      contentTypes: ["晒过程", "教知识", "讲故事", "说观点", "硬广"],
      bigList: [], subList: [], upSubList: [], allIndustry: [],
      recycle: false,
      dataList: [], sel: [],
      pageIndex: 1, pageSize: 10, totalPage: 0, dataListLoading: false,
      showUpload: false,
      stat: {total: 0, online: 0, draft: 0, recommend: 0, views: 0, recycle: 0},
      treeCounts: {},
      up: {industryBig: "", industrySub: "", contentType: "硬广", tags: "", done: 0, total: 0, fail: []}
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
    }
  },
  created() {
    this.loadIndustry();
    this.getDataList();
    this.loadStat();
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
      const allOnline = this.sel.every(m => m.status === "上架");
      this.bulkStatus(this.sel.map(m => m.id), allOnline ? "下架" : "上架", "已批量" + (allOnline ? "下架" : "上架"));
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
    beforeUp() { this.up.total++; return true; },
    onUploaded(res, file) {
      if (res.code === 0) {
        this.up.done++;
        this.$http({url: "hyMaterial/save", method: "post", data: {
          title: (file && file.name) || ("新素材-" + this.up.done),
          cover: res.file, video: res.file,
          industryBig: this.up.industryBig, industrySub: this.up.industrySub,
          contentType: this.up.contentType, tags: this.up.tags, status: "下架", heat: 0
        }}).then(() => { this.getDataList(); this.loadStat(); });
      } else {
        this.up.fail.push((file && file.name) || "未知文件");
      }
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
.mt-tree { width: 190px; flex-shrink: 0; background: #fff; border: 1px solid #EEF1F5; border-radius: 12px; padding: 12px; box-shadow: 0 1px 4px rgba(0,21,41,.05); }
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
.fail-item { font-size: 12px; color: #FF5A5F; padding: 4px 0; border-bottom: 1px dashed #F0F2F5; }
.sp-foot-line { padding: 12px 18px 16px; }
</style>
