<template>
  <div class="main-content">
    <div v-if="showFlag">
      <el-form :inline="true" :model="searchForm" class="form-content">
        <el-row :gutter="20" class="slt">
          <el-form-item label="套餐名称">
            <el-input v-model="searchForm.taocanmingcheng" placeholder="套餐名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="品类">
            <el-select v-model="searchForm.pinlei" placeholder="全部" clearable>
              <el-option label="写真" value="写真"></el-option>
              <el-option label="宣传片" value="宣传片"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.shangxiajia" placeholder="全部" clearable>
              <el-option label="上架" value="上架"></el-option>
              <el-option label="下架" value="下架"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button icon="el-icon-search" type="success" @click="search()">查询</el-button>
          </el-form-item>
        </el-row>
        <el-row class="ad">
          <el-form-item>
            <el-button v-if="isAuth('taocan','新增')" type="success" icon="el-icon-plus" @click="addOrUpdateHandler()">新增
            </el-button>
            <el-button v-if="isAuth('taocan','删除')" :disabled="dataListSelections.length <= 0" type="danger"
                       icon="el-icon-delete" @click="deleteHandler()">删除
            </el-button>
          </el-form-item>
        </el-row>
      </el-form>
      <div class="table-content">
        <el-table class="tables" :data="dataList" v-loading="dataListLoading" border
                  @selection-change="selectionChangeHandler" style="width: 100%">
          <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
          <el-table-column label="索引" align="center" type="index" width="50"/>
          <el-table-column prop="fengmian" label="封面" align="center" width="90">
            <template slot-scope="scope">
              <img v-if="scope.row.fengmian" :src="getImg(scope.row.fengmian)" width="60" height="60"
                   style="object-fit: cover;border-radius:6px;"/>
            </template>
          </el-table-column>
          <el-table-column prop="taocanmingcheng" label="套餐名称" align="center"></el-table-column>
          <el-table-column prop="pinlei" label="品类" align="center" width="90"></el-table-column>
          <el-table-column prop="fengge" label="风格" align="center" width="100"></el-table-column>
          <el-table-column prop="xianxiabiaojia" label="线下标价" align="center" width="100"></el-table-column>
          <el-table-column prop="jingxiuzhangshu" label="精修张数" align="center" width="90"></el-table-column>
          <el-table-column prop="clicknum" label="热度" align="center" width="80"></el-table-column>
          <el-table-column prop="paixu" label="排序" align="center" width="70"></el-table-column>
          <el-table-column prop="shangxiajia" label="状态" align="center" width="90">
            <template slot-scope="scope">
              <el-tag :type="scope.row.shangxiajia === '上架' ? 'success' : 'info'">{{ scope.row.shangxiajia }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column width="240" label="操作" align="center">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="addOrUpdateHandler(scope.row.id,'info')">详情</el-button>
              <el-button v-if="isAuth('taocan','修改')" type="text" size="small"
                         @click="addOrUpdateHandler(scope.row.id)">修改
              </el-button>
              <el-button v-if="isAuth('taocan','修改')" type="text" size="small"
                         @click="toggleShelf(scope.row)">{{ scope.row.shangxiajia === '上架' ? '下架' : '上架' }}
              </el-button>
              <el-button v-if="isAuth('taocan','删除')" type="text" size="small"
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
      searchForm: {
        taocanmingcheng: "",
        pinlei: "",
        shangxiajia: ""
      },
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
  components: {
    AddOrUpdate
  },
  created() {
    this.getDataList();
  },
  methods: {
    getImg(val) {
      let first = val.split(",")[0];
      return this.$base.url + first;
    },
    search() {
      this.pageIndex = 1;
      this.getDataList();
    },
    getDataList() {
      this.dataListLoading = true;
      let params = {
        page: this.pageIndex,
        limit: this.pageSize,
        sort: "paixu",
        order: "asc"
      };
      if (this.searchForm.taocanmingcheng) {
        params["taocanmingcheng"] = "%" + this.searchForm.taocanmingcheng + "%";
      }
      if (this.searchForm.pinlei) params["pinlei"] = this.searchForm.pinlei;
      if (this.searchForm.shangxiajia) params["shangxiajia"] = this.searchForm.shangxiajia;
      this.$http({
        url: "taocan/page",
        method: "get",
        params: params
      }).then(({data}) => {
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
      if (type != "info") {
        type = "else";
      }
      this.$nextTick(() => {
        this.$refs.addOrUpdate.init(id, type);
      });
    },
    toggleShelf(row) {
      let next = row.shangxiajia === "上架" ? "下架" : "上架";
      this.$http({
        url: "taocan/update",
        method: "post",
        data: {id: row.id, shangxiajia: next}
      }).then(({data}) => {
        if (data && data.code === 0) {
          this.$message.success("操作成功");
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
        this.$http({
          url: "taocan/delete",
          method: "post",
          data: ids
        }).then(({data}) => {
          if (data && data.code === 0) {
            this.$message({
              message: "操作成功",
              type: "success",
              duration: 1500,
              onClose: () => {
                this.search();
              }
            });
          } else {
            this.$message.error(data.msg);
          }
        });
      });
    }
  }
};
</script>
