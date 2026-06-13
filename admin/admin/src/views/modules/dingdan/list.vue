<template>
  <div class="main-content">
    <div v-if="showFlag">
      <el-form :inline="true" :model="searchForm" class="form-content">
        <el-row :gutter="20" class="slt">
          <el-form-item label="订单编号">
            <el-input v-model="searchForm.dingdanbianhao" placeholder="订单编号" clearable></el-input>
          </el-form-item>
          <el-form-item label="联系人">
            <el-input v-model="searchForm.xingming" placeholder="联系人" clearable></el-input>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.zhuangtai" placeholder="全部" clearable>
              <el-option v-for="s in statusList" :key="s" :label="s" :value="s"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button icon="el-icon-search" type="success" @click="search()">查询</el-button>
          </el-form-item>
        </el-row>
        <el-row class="ad">
          <el-form-item>
            <el-button v-if="isAuth('dingdan','删除')" :disabled="dataListSelections.length <= 0" type="danger"
                       icon="el-icon-delete" @click="deleteHandler()">删除
            </el-button>
          </el-form-item>
        </el-row>
      </el-form>
      <div class="table-content">
        <el-table class="tables" :data="dataList" v-loading="dataListLoading" border
                  @selection-change="selectionChangeHandler" style="width: 100%">
          <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
          <el-table-column prop="dingdanbianhao" label="订单编号" align="center" width="180"></el-table-column>
          <el-table-column prop="taocanmingcheng" label="套餐" align="center"></el-table-column>
          <el-table-column prop="pinlei" label="品类" align="center" width="80"></el-table-column>
          <el-table-column prop="xingming" label="联系人" align="center" width="90"></el-table-column>
          <el-table-column prop="shoujihaoma" label="手机号" align="center" width="120"></el-table-column>
          <el-table-column prop="yixiangdangqi" label="意向档期" align="center" width="110">
            <template slot-scope="scope">{{ fmtDate(scope.row.yixiangdangqi) }}</template>
          </el-table-column>
          <el-table-column prop="yugudangqi" label="预估档期" align="center" width="110">
            <template slot-scope="scope">{{ fmtDate(scope.row.yugudangqi) }}</template>
          </el-table-column>
          <el-table-column prop="paiduixuhao" label="排队序号" align="center" width="90"></el-table-column>
          <el-table-column prop="jiaofeisuoding" label="缴费锁档" align="center" width="90"></el-table-column>
          <el-table-column prop="zhuangtai" label="状态" align="center" width="100">
            <template slot-scope="scope">
              <el-tag :type="statusTag(scope.row.zhuangtai)">{{ scope.row.zhuangtai }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column width="160" label="操作" align="center" fixed="right">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="viewDetail(scope.row.id)">详情</el-button>
              <el-button v-if="isAuth('dingdan','修改')" type="text" size="small" @click="openProcess(scope.row)">处理
              </el-button>
              <el-button v-if="isAuth('dingdan','删除')" type="text" size="small"
                         @click="deleteHandler(scope.row.id)">删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle"
                       :current-page="pageIndex" :page-sizes="[10, 20, 50]" :page-size="pageSize"
                       :total="totalPage" layout="total, sizes, prev, pager, next, jumper"></el-pagination>
      </div>
    </div>

    <!-- 处理对话框 -->
    <el-dialog title="订单处理 / 排期" :visible.sync="processVisible" width="520px">
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="订单状态">
          <el-select v-model="processForm.zhuangtai" placeholder="选择状态">
            <el-option v-for="s in statusList" :key="s" :label="s" :value="s"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="预估档期">
          <el-date-picker v-model="processForm.yugudangqi" type="date" value-format="yyyy-MM-dd"
                          placeholder="选择拍摄日期"></el-date-picker>
        </el-form-item>
        <el-form-item label="排队序号">
          <el-input-number v-model="processForm.paiduixuhao" :min="1"></el-input-number>
        </el-form-item>
        <el-form-item label="缴费锁档">
          <el-switch v-model="processForm.lock" active-text="已缴费锁定档期"></el-switch>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="processVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcess">保存</el-button>
      </span>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="订单详情" :visible.sync="detailVisible" width="560px">
      <el-descriptions :column="1" border v-if="detailObj.id">
        <el-descriptions-item label="订单编号">{{ detailObj.dingdanbianhao }}</el-descriptions-item>
        <el-descriptions-item label="套餐">{{ detailObj.taocanmingcheng }}</el-descriptions-item>
        <el-descriptions-item label="品类/风格">{{ detailObj.pinlei }} / {{ detailObj.fengge }}</el-descriptions-item>
        <el-descriptions-item label="线下标价">￥{{ detailObj.xianxiabiaojia }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detailObj.xingming }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailObj.shoujihaoma }}</el-descriptions-item>
        <el-descriptions-item label="拍摄人数">{{ detailObj.paisherenshu }}</el-descriptions-item>
        <el-descriptions-item label="意向档期">{{ fmtDate(detailObj.yixiangdangqi) }}</el-descriptions-item>
        <el-descriptions-item label="预估档期">{{ fmtDate(detailObj.yugudangqi) }}</el-descriptions-item>
        <el-descriptions-item label="个性化需求">{{ detailObj.beizhu }}</el-descriptions-item>
        <el-descriptions-item label="取消原因">{{ detailObj.quxiaoyuanyin }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      searchForm: {dingdanbianhao: "", xingming: "", zhuangtai: ""},
      statusList: ["待排队", "已排期", "待拍摄", "拍摄完成", "制作中", "成品已上线", "已取消"],
      showFlag: true,
      dataList: [],
      pageIndex: 1,
      pageSize: 10,
      totalPage: 0,
      dataListLoading: false,
      dataListSelections: [],
      processVisible: false,
      processForm: {id: 0, zhuangtai: "", yugudangqi: "", paiduixuhao: 1, lock: false},
      detailVisible: false,
      detailObj: {}
    };
  },
  created() {
    this.getDataList();
  },
  methods: {
    fmtDate(d) {
      if (!d) return "-";
      return d.toString().split(" ")[0];
    },
    statusTag(s) {
      if (s === "已取消") return "info";
      if (s === "成品已上线") return "success";
      return "warning";
    },
    search() {
      this.pageIndex = 1;
      this.getDataList();
    },
    getDataList() {
      this.dataListLoading = true;
      let params = {page: this.pageIndex, limit: this.pageSize, sort: "addtime", order: "desc"};
      if (this.searchForm.dingdanbianhao) params["dingdanbianhao"] = "%" + this.searchForm.dingdanbianhao + "%";
      if (this.searchForm.xingming) params["xingming"] = "%" + this.searchForm.xingming + "%";
      if (this.searchForm.zhuangtai) params["zhuangtai"] = this.searchForm.zhuangtai;
      this.$http({url: "dingdan/page", method: "get", params}).then(({data}) => {
        if (data && data.code === 0) {
          let list = data.data.list || [];
          list.sort((a, b) => {
            let la = (a.jiaofeisuoding === '是' || a.jiaofeisuoding === '已缴费锁定档期') ? 0 : 1;
            let lb = (b.jiaofeisuoding === '是' || b.jiaofeisuoding === '已缴费锁定档期') ? 0 : 1;
            if (la !== lb) return la - lb;
            return (a.paiduixuhao || 999) - (b.paiduixuhao || 999);
          });
          this.dataList = list;
          this.totalPage = data.data.total;
        } else {
          this.dataList = [];
          this.totalPage = 0;
        }
        this.dataListLoading = false;
      });
    },
    sizeChangeHandle(val) {
      this.pageSize = val;
      this.pageIndex = 1;
      this.getDataList();
    },
    currentChangeHandle(val) {
      this.pageIndex = val;
      this.getDataList();
    },
    selectionChangeHandler(val) {
      this.dataListSelections = val;
    },
    viewDetail(id) {
      this.$http({url: `dingdan/info/${id}`, method: "get"}).then(({data}) => {
        if (data && data.code === 0) {
          this.detailObj = data.data;
          this.detailVisible = true;
        }
      });
    },
    openProcess(row) {
      this.processForm = {
        id: row.id,
        zhuangtai: row.zhuangtai,
        yugudangqi: row.yugudangqi ? row.yugudangqi.toString().split(" ")[0] : "",
        paiduixuhao: row.paiduixuhao || 1,
        lock: row.jiaofeisuoding === "是" || row.jiaofeisuoding === "已缴费锁定档期"
      };
      this.processVisible = true;
    },
    submitProcess() {
      let data = {
        id: this.processForm.id,
        zhuangtai: this.processForm.zhuangtai,
        yugudangqi: this.processForm.yugudangqi,
        paiduixuhao: this.processForm.paiduixuhao,
        jiaofeisuoding: this.processForm.lock ? "已缴费锁定档期" : "否"
      };
      this.$http({url: "dingdan/update", method: "post", data}).then(({data}) => {
        if (data && data.code === 0) {
          this.$message.success("处理成功");
          this.processVisible = false;
          this.getDataList();
        } else {
          this.$message.error(data.msg);
        }
      });
    },
    deleteHandler(id) {
      var ids = id ? [Number(id)] : this.dataListSelections.map(item => Number(item.id));
      this.$confirm(`确定进行[${id ? "删除" : "批量删除"}]操作?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        this.$http({url: "dingdan/delete", method: "post", data: ids}).then(({data}) => {
          if (data && data.code === 0) {
            this.$message({message: "操作成功", type: "success", duration: 1500, onClose: () => this.search()});
          } else {
            this.$message.error(data.msg);
          }
        });
      });
    }
  }
};
</script>
