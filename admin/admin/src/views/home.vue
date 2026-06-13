<template>
  <div class="dashboard">
    <div class="welcome">欢迎使用 {{ this.$project.projectName }}<span class="revenue" v-if="revenue"> · 营收汇总 ¥{{ revenue.toFixed(0) }}</span></div>

    <el-row :gutter="16" class="stat-row">
      <el-col :span="4" v-for="card in cards" :key="card.label">
        <div class="stat-card" :style="{ background: card.bg }">
          <i class="stat-icon" :class="card.icon"></i>
          <div class="stat-text">
            <div class="stat-num">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="never">
          <div slot="header"><b>订单状态分布</b></div>
          <div ref="statusChart" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div slot="header"><b>品类订单占比</b></div>
          <div ref="pinleiChart" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div slot="header"><b>套餐热度 Top10</b></div>
          <div ref="hotChart" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import router from '@/router/router-static'
import echarts from 'echarts'

export default {
  data() {
    return {
      cards: [
        {label: '套餐总数', value: 0, icon: 'el-icon-goods', bg: 'linear-gradient(135deg,#C4AB7C,#9C8559)'},
        {label: '订单总数', value: 0, icon: 'el-icon-tickets', bg: 'linear-gradient(135deg,#A8B892,#8FA67A)'},
        {label: '待排队', value: 0, icon: 'el-icon-time', bg: 'linear-gradient(135deg,#E0B173,#C98F4B)'},
        {label: '成品总数', value: 0, icon: 'el-icon-picture-outline', bg: 'linear-gradient(135deg,#8C9BAE,#69788A)'},
        {label: '注册用户', value: 0, icon: 'el-icon-user', bg: 'linear-gradient(135deg,#B49A6B,#8C7348)'},
        {label: '待审核', value: 0, icon: 'el-icon-s-check', bg: 'linear-gradient(135deg,#D4A574,#B8844E)'}
      ],
      revenue: 0,
      brandPalette: ['#B49A6B', '#A8B892', '#E0B173', '#8C9BAE', '#C98F4B', '#8FA67A', '#D9C3A0'],
      statusChart: null,
      pinleiChart: null,
      hotChart: null
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      if (this.$storage.get('Token')) {
        this.$http({url: `${this.$storage.get('sessionTable')}/session`, method: "get"}).then(({data}) => {
          if (data && data.code != 0) {
            router.push({name: 'login'});
          } else {
            this.loadDashboard();
          }
        });
      } else {
        router.push({name: 'login'});
      }
    },
    loadDashboard() {
      this.$http({url: "taocan/page", method: "get", params: {page: 1, limit: 1}}).then(({data}) => {
        if (data && data.code === 0) this.cards[0].value = data.data.total;
      });
      this.$http({url: "chengpin/page", method: "get", params: {page: 1, limit: 1}}).then(({data}) => {
        if (data && data.code === 0) this.cards[3].value = data.data.total;
      });
      this.$http({url: "dingdan/page", method: "get", params: {page: 1, limit: 1000}}).then(({data}) => {
        if (data && data.code === 0) {
          let list = data.data.list || [];
          this.cards[1].value = data.data.total;
          let statusCount = {};
          let pinleiCount = {};
          let revenue = 0;
          list.forEach(o => {
            let s = o.zhuangtai || '未知';
            statusCount[s] = (statusCount[s] || 0) + 1;
            let p = o.pinlei || '其他';
            pinleiCount[p] = (pinleiCount[p] || 0) + 1;
            if (s !== '已取消' && o.xianxiabiaojia) {
              revenue += parseFloat(o.xianxiabiaojia) || 0;
            }
          });
          this.cards[2].value = statusCount['待排队'] || 0;
          this.revenue = revenue;
          this.renderStatusChart(statusCount);
          this.renderPinleiChart(pinleiCount);
        }
      });
      this.$http({url: "yonghu/page", method: "get", params: {page: 1, limit: 1000}}).then(({data}) => {
        if (data && data.code === 0) {
          let list = data.data.list || [];
          this.cards[4].value = data.data.total;
          this.cards[5].value = list.filter(u => u.sfsh === '否').length;
        }
      });
      this.$http({
        url: "taocan/page",
        method: "get",
        params: {page: 1, limit: 10, sort: "clicknum", order: "desc"}
      }).then(({data}) => {
        if (data && data.code === 0) this.renderHotChart(data.data.list || []);
      });
    },
    renderStatusChart(statusCount) {
      if (!this.statusChart) this.statusChart = echarts.init(this.$refs.statusChart);
      let arr = Object.keys(statusCount).map(k => ({name: k, value: statusCount[k]}));
      this.statusChart.setOption({
        color: this.brandPalette,
        tooltip: {trigger: 'item', formatter: '{b}: {c} ({d}%)'},
        legend: {bottom: 0},
        series: [{
          type: 'pie',
          radius: ['40%', '65%'],
          center: ['50%', '45%'],
          data: arr,
          label: {formatter: '{b}\n{c}'}
        }]
      });
    },
    renderPinleiChart(pinleiCount) {
      if (!this.pinleiChart) this.pinleiChart = echarts.init(this.$refs.pinleiChart);
      let arr = Object.keys(pinleiCount).map(k => ({name: k, value: pinleiCount[k]}));
      this.pinleiChart.setOption({
        color: this.brandPalette,
        tooltip: {trigger: 'item', formatter: '{b}: {c} ({d}%)'},
        legend: {bottom: 0},
        series: [{
          type: 'pie',
          radius: ['40%', '65%'],
          center: ['50%', '45%'],
          data: arr,
          label: {formatter: '{b}\n{c}'}
        }]
      });
    },
    renderHotChart(list) {
      if (!this.hotChart) this.hotChart = echarts.init(this.$refs.hotChart);
      let names = list.map(i => i.taocanmingcheng);
      let values = list.map(i => i.clicknum || 0);
      this.hotChart.setOption({
        tooltip: {trigger: 'axis'},
        grid: {left: '3%', right: '6%', bottom: '3%', containLabel: true},
        xAxis: {type: 'value'},
        yAxis: {type: 'category', data: names.reverse()},
        series: [{
          type: 'bar',
          data: values.reverse(),
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              {offset: 0, color: '#D9C3A0'},
              {offset: 1, color: '#B49A6B'}
            ]),
            barBorderRadius: [0, 6, 6, 0]
          },
          barWidth: '50%'
        }]
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 4px;
}

.welcome {
  font-size: 22px;
  font-weight: 700;
  color: #222;
  margin-bottom: 20px;

  &::before {
    content: "";
    display: inline-block;
    width: 4px;
    height: 20px;
    border-radius: 2px;
    background: #B49A6B;
    margin-right: 10px;
    vertical-align: -2px;
  }

  .revenue {
    font-size: 16px;
    font-weight: 500;
    color: #8C7348;
    margin-left: 8px;
  }
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 14px;
  padding: 22px 24px;
  color: #fff;
  display: flex;
  align-items: center;
  box-shadow: 0 10px 24px rgba(60, 50, 30, 0.12);

  .stat-icon {
    font-size: 30px;
    width: 56px;
    height: 56px;
    line-height: 56px;
    text-align: center;
    border-radius: 14px;
    background: rgba(255, 255, 255, 0.18);
    margin-right: 16px;
  }

  .stat-num {
    font-size: 32px;
    font-weight: 700;
    line-height: 1.1;
  }

  .stat-label {
    font-size: 14px;
    margin-top: 6px;
    opacity: 0.95;
  }
}
</style>
