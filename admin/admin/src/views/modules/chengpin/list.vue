<template>
  <div class="main-content">
    <div v-if="showFlag">
      <el-form :inline="true" :model="searchForm" class="form-content">
        <el-row :gutter="20" class="slt">
          <el-form-item label="订单编号">
            <el-input v-model="searchForm.dingdanbianhao" placeholder="订单编号" clearable></el-input>
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="searchForm.biaoti" placeholder="成品标题" clearable></el-input>
          </el-form-item>
          <el-form-item>
            <el-button icon="el-icon-search" type="success" @click="search()">查询</el-button>
          </el-form-item>
        </el-row>
        <el-row class="ad">
          <el-form-item>
            <el-button v-if="isAuth('chengpin','新增')" type="success" icon="el-icon-plus" @click="addOrUpdateHandler()">
              上传成品
            </el-button>
            <el-button v-if="isAuth('chengpin','删除')" :disabled="dataListSelections.length <= 0" type="danger"
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
          <el-table-column prop="biaoti" label="标题" align="center"></el-table-column>
          <el-table-column prop="taocanmingcheng" label="套餐" align="center"></el-table-column>
          <el-table-column prop="xingming" label="客户" align="center" width="90"></el-table-column>
          <el-table-column label="成品预览" align="center" width="120">
            <template slot-scope="scope">
              <img v-if="scope.row.tupian" :src="getImg(scope.row.tupian)" width="60" height="60"
                   style="object-fit: cover;border-radius:6px;"/>
            </template>
          </el-table-column>
          <el-table-column prop="shangxiajia" label="状态" align="center" width="90">
            <template slot-scope="scope">
              <el-tag :type="scope.row.shangxiajia === '上架' ? 'success' : 'info'">{{ scope.row.shangxiajia }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="addtime" label="上传时间" align="center" width="160"></el-table-column>
          <el-table-column width="180" label="操作" align="center">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="addOrUpdateHandler(scope.row.id,'info')">详情</el-button>
              <el-button v-if="isAuth('chengpin','修改')" type="text" size="small"
                         @click="addOrUpdateHandler(scope.row.id)">修改
              </el-button>
              <el-button v-if="isAuth('chengpin','删除')" type="text" size="small"
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
    <add-or-update v-if="addOrUpdateFlag" :parent="this" ref="addOrUpdate"></add-or-update>
  </div>
</template>

<script>
import AddOrUpdate from "./add-or-update";

export default {
  data() {
    return {
      searchForm: {dingdanbianhao: "", biaoti: ""},
      showFlag: true,
      addOrUpdateFlag: false,
      dataList: [],
      pageIndex: 1,
      pageSize: 10,
      totalPage: 0,
      dataListLoading: false,
      dataListSelections: []
    };
  },
  components: {AddOrUpdate},
  created() {
    this.getDataList();
  },
  methods: {
    getImg(val) {
      return this.$base.url + val.split(",")[0];
    },
    search() {
      this.pageIndex = 1;
      this.getDataList();
    },
    getDataList() {
      this.dataListLoading = true;
      let params = {page: this.pageIndex, limit: this.pageSize, sort: "addtime", order: "desc"};
      if (this.searchForm.dingdanbianhao) params["dingdanbianhao"] = "%" + this.searchForm.dingdanbianhao + "%";
      if (this.searchForm.biaoti) params["biaoti"] = "%" + this.searchForm.biaoti + "%";
      this.$http({url: "chengpin/page", method: "get", params}).then(({data}) => {
        if (data && data.code === 0) {
          this.dataList = data.data.list;
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
    addOrUpdateHandler(id, type) {
      this.showFlag = false;
      this.addOrUpdateFlag = true;
      if (type != "info") type = "else";
      this.$nextTick(() => {
        this.$refs.addOrUpdate.init(id, type);
      });
    },
    deleteHandler(id) {
      var ids = id ? [Number(id)] : this.dataListSelections.map(item => Number(item.id));
      this.$confirm(`确定进行[${id ? "删除" : "批量删除"}]操作?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        this.$http({url: "chengpin/delete", method: "post", data: ids}).then(({data}) => {
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
