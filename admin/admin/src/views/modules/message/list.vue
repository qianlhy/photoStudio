<template>
  <div class="main-content">
    <el-form :inline="true" :model="searchForm" class="form-content">
      <el-row :gutter="20" class="slt">
        <el-form-item label="标题">
          <el-input v-model="searchForm.biaoti" placeholder="标题" clearable></el-input>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.leixing" placeholder="全部" clearable>
            <el-option label="审核" value="审核"></el-option>
            <el-option label="档期" value="档期"></el-option>
            <el-option label="成品" value="成品"></el-option>
            <el-option label="活动" value="活动"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button icon="el-icon-search" type="success" @click="search()">查询</el-button>
        </el-form-item>
      </el-row>
      <el-row class="ad">
        <el-form-item>
          <el-button type="primary" icon="el-icon-s-promotion" @click="pushDialog">活动推送</el-button>
          <el-button v-if="isAuth('message','删除')" :disabled="dataListSelections.length <= 0" type="danger"
                     icon="el-icon-delete" @click="deleteHandler()">删除
          </el-button>
        </el-form-item>
      </el-row>
    </el-form>
    <div class="table-content">
      <el-table class="tables" :data="dataList" v-loading="dataListLoading" border
                @selection-change="selectionChangeHandler" style="width: 100%">
        <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
        <el-table-column prop="biaoti" label="标题" align="center" width="160"></el-table-column>
        <el-table-column prop="neirong" label="内容" align="center" show-overflow-tooltip></el-table-column>
        <el-table-column prop="leixing" label="类型" align="center" width="90"></el-table-column>
        <el-table-column prop="userid" label="接收用户id" align="center" width="120"></el-table-column>
        <el-table-column prop="isread" label="已读" align="center" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isread === '是' ? 'success' : 'info'">{{ scope.row.isread }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="addtime" label="时间" align="center" width="160"></el-table-column>
        <el-table-column width="100" label="操作" align="center">
          <template slot-scope="scope">
            <el-button v-if="isAuth('message','删除')" type="text" size="small"
                       @click="deleteHandler(scope.row.id)">删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle"
                     :current-page="pageIndex" :page-sizes="[10, 20, 50]" :page-size="pageSize"
                     :total="totalPage" layout="total, sizes, prev, pager, next, jumper"></el-pagination>
    </div>

    <el-dialog title="活动推送" :visible.sync="pushVisible" width="520px">
      <el-form :model="pushForm" label-width="100px">
        <el-form-item label="推送范围">
          <el-radio-group v-model="pushForm.scope">
            <el-radio label="all">全部已通过用户</el-radio>
            <el-radio label="one">指定用户ID</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="用户ID" v-if="pushForm.scope === 'one'">
          <el-input v-model="pushForm.userid" placeholder="输入用户 id"></el-input>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="pushForm.title" placeholder="活动标题"></el-input>
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" :rows="4" v-model="pushForm.content" placeholder="活动详情"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="pushVisible = false">取消</el-button>
        <el-button type="primary" :loading="pushing" @click="submitPush">发送</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      searchForm: {biaoti: "", leixing: ""},
      dataList: [],
      pageIndex: 1,
      pageSize: 10,
      totalPage: 0,
      dataListLoading: false,
      dataListSelections: [],
      pushVisible: false,
      pushing: false,
      pushForm: {scope: 'all', userid: '', title: '', content: ''}
    };
  },
  created() {
    this.getDataList();
  },
  methods: {
    search() {
      this.pageIndex = 1;
      this.getDataList();
    },
    getDataList() {
      this.dataListLoading = true;
      let params = {page: this.pageIndex, limit: this.pageSize, sort: "addtime", order: "desc"};
      if (this.searchForm.biaoti) params["biaoti"] = "%" + this.searchForm.biaoti + "%";
      if (this.searchForm.leixing) params["leixing"] = this.searchForm.leixing;
      this.$http({url: "message/page", method: "get", params}).then(({data}) => {
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
    pushDialog() {
      this.pushForm = {scope: 'all', userid: '', title: '', content: ''};
      this.pushVisible = true;
    },
    submitPush() {
      if (!this.pushForm.title || !this.pushForm.content) {
        this.$message.warning("请填写标题和内容");
        return;
      }
      if (this.pushForm.scope === 'one' && !this.pushForm.userid) {
        this.$message.warning("请填写用户ID");
        return;
      }
      this.pushing = true;
      let params = {
        title: this.pushForm.title,
        content: this.pushForm.content,
        leixing: '活动'
      };
      if (this.pushForm.scope === 'one') params.userid = this.pushForm.userid;
      this.$http({url: "message/push", method: "post", params}).then(({data}) => {
        this.pushing = false;
        if (data && data.code === 0) {
          this.$message.success("推送成功");
          this.pushVisible = false;
          this.search();
        } else {
          this.$message.error(data.msg);
        }
      }).catch(() => {
        this.pushing = false;
        this.$message.error("推送失败");
      });
    },
    deleteHandler(id) {
      var ids = id ? [Number(id)] : this.dataListSelections.map(item => Number(item.id));
      this.$confirm(`确定进行[${id ? "删除" : "批量删除"}]操作?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        this.$http({url: "message/delete", method: "post", data: ids}).then(({data}) => {
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
