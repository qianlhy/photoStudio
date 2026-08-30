<template>
  <div class="overview">
    <!-- 指标卡 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <div class="stat-card" :class="c.cls">
          <div class="stat-icon"><i :class="c.icon"></i></div>
          <div class="stat-text">
            <div class="stat-label">{{ c.label }}</div>
            <div class="stat-num">{{ c.value }}</div>
            <div class="stat-delta">较上月 <span :class="c.deltaUp?'up':'down'">{{ c.delta }}</span> <span :class="c.deltaUp?'up':'down'">{{ c.pct }}</span></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="business-row">
      <!-- 业务经理业绩 -->
      <el-col :span="12">
        <el-card shadow="never" class="blk">
          <div slot="header" class="blk-head"><b>业务经理业绩</b><span class="more">查看全部 ›</span></div>
          <el-table :data="perf" class="hy-table" :show-header="true">
            <el-table-column label="业务经理" min-width="170">
              <template slot-scope="s">
                <div class="pf-cell">
                  <span class="rank" :class="'r'+(s.$index+1)">{{ s.$index + 1 }}</span>
                  <span class="pf-av">{{ (s.row.name||'').charAt(0) }}</span>
                  <span class="pf-name">{{ s.row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="跟进中" width="110">
              <template slot-scope="s">
                <div class="pf-num">{{ s.row.following }}</div>
                <div class="pf-bar"><i :style="{width: barW(s.row.following, maxFollow), background:'#2F6BFF'}"></i></div>
              </template>
            </el-table-column>
            <el-table-column label="已成交" width="110">
              <template slot-scope="s">
                <div class="pf-num">{{ s.row.dealCust }}</div>
                <div class="pf-bar"><i :style="{width: barW(s.row.dealCust, maxDeal), background:'#22B07D'}"></i></div>
              </template>
            </el-table-column>
            <el-table-column prop="dealCount" label="成交数量" width="90" align="center"></el-table-column>
            <el-table-column label="成交额 (元)" width="120" align="right">
              <template slot-scope="s">{{ fmt(s.row.amount) }}</template>
            </el-table-column>
            <el-table-column label="预计提成 (元)" width="120" align="right">
              <template slot-scope="s"><span class="pf-comm">{{ fmt(s.row.commission) }}</span></template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 成交趋势 + 交付情况 -->
      <el-col :span="12">
        <el-card shadow="never" class="blk">
          <div slot="header" class="blk-head"><b>成交趋势</b></div>
          <div ref="trendChart" class="trend-chart"></div>
        </el-card>
        <el-card shadow="never" class="blk" style="margin-top:16px">
          <div slot="header" class="blk-head"><b>交付情况</b></div>
          <div class="deliver">
            <div class="rate-ring">
              <svg class="rate-svg" viewBox="0 0 120 120" aria-hidden="true">
                <defs>
                  <linearGradient id="deliverRateGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="#3DD598" />
                    <stop offset="100%" stop-color="#22B07D" />
                  </linearGradient>
                </defs>
                <circle class="rate-track" cx="60" cy="60" r="50" />
                <circle
                  class="rate-bar"
                  cx="60"
                  cy="60"
                  r="50"
                  transform="rotate(-90 60 60)"
                  :stroke-dasharray="ringCircumference"
                  :stroke-dashoffset="ringDashOffset"
                />
              </svg>
              <div class="rate-core">
                <div class="rate-value">{{ deliver.rate }}<span class="rate-unit">%</span></div>
                <div class="rate-name">完成率</div>
              </div>
            </div>
            <div class="deliver-stats">
              <div class="ds"><i class="ds-ic el-icon-date b1"></i><div><div class="ds-num">{{ deliver.total }}</div><div class="ds-l">本月应交付</div></div></div>
              <div class="ds"><i class="ds-ic el-icon-circle-check b2"></i><div><div class="ds-num green">{{ deliver.done }}</div><div class="ds-l">已完成 {{ deliver.donePct }}%</div></div></div>
              <div class="ds"><i class="ds-ic el-icon-time b3"></i><div><div class="ds-num orange">{{ deliver.undone }}</div><div class="ds-l">未完成 {{ deliver.undonePct }}%</div></div></div>
              <div class="ds"><i class="ds-ic el-icon-warning-outline b4"></i><div><div class="ds-num red">{{ deliver.abnormal }}</div><div class="ds-l">异常 {{ deliver.abnPct }}%</div></div></div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 重点待办 -->
    <el-row :gutter="16" class="todo-row">
      <el-col :span="14">
        <el-card shadow="never" class="blk">
          <div slot="header" class="blk-head"><b>重点待办 · 待付款客户（{{ unpaidList.length }}）</b></div>
          <el-table :data="unpaidList" class="hy-table">
            <el-table-column prop="name" label="客户名称" min-width="160"></el-table-column>
            <el-table-column prop="contact" label="客户联系人" width="120"></el-table-column>
            <el-table-column label="未付款金额 (元)" width="140">
              <template slot-scope="s"><span class="red">{{ fmt(s.row.unpaidAmount) }}</span></template>
            </el-table-column>
            <el-table-column label="最后跟进时间" width="150">
              <template slot-scope="s">{{ (s.row.lastFollowTime||'').substr(0,10) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="blk">
          <div slot="header" class="blk-head"><b>异常订单（{{ abnormalList.length }}）</b></div>
          <el-table :data="abnormalList" class="hy-table">
            <el-table-column prop="orderNo" label="订单号" min-width="150"></el-table-column>
            <el-table-column prop="customerName" label="客户名称" min-width="150"></el-table-column>
            <el-table-column prop="abnormal" label="问题类型" width="110">
              <template slot-scope="s"><el-tag size="mini" type="danger">{{ s.row.abnormal }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="managerName" label="负责人" width="90"></el-table-column>
          </el-table>
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
        {label: '客户总数', value: 0, icon: 'el-icon-user-solid', cls: 'c-blue', delta: '+32', pct: '+12.6%', deltaUp: true},
        {label: '待付款客户', value: 0, icon: 'el-icon-wallet', cls: 'c-amber', delta: '+3', pct: '+20.0%', deltaUp: true},
        {label: '异常订单', value: 0, icon: 'el-icon-warning', cls: 'c-red', delta: '-2', pct: '-33.3%', deltaUp: false},
        {label: '本月已交付', value: 0, icon: 'el-icon-success', cls: 'c-green', delta: '+8', pct: '+14.5%', deltaUp: true}
      ],
      perf: [],
      unpaidList: [],
      abnormalList: [],
      deliver: {total: 0, done: 0, undone: 0, abnormal: 0, donePct: 0, undonePct: 0, abnPct: 0, rate: 0},
      trendChart: null
    }
  },
  computed: {
    maxFollow() { return Math.max(1, ...this.perf.map(p => p.following || 0)) },
    maxDeal() { return Math.max(1, ...this.perf.map(p => p.dealCust || 0)) },
    ringCircumference() { return 2 * Math.PI * 50 },
    ringDashOffset() {
      const rate = Math.min(100, Math.max(0, Number(this.deliver.rate) || 0))
      return this.ringCircumference * (1 - rate / 100)
    }
  },
  mounted() {
    if (!this.$storage.get('Token')) {
      router.push({name: 'login'})
      return
    }
    this.load()
  },
  methods: {
    fmt(n) {
      n = Number(n || 0)
      return n.toLocaleString('zh-CN')
    },
    barW(v, max) { return Math.round((v || 0) / max * 100) + '%' },
    load() {
      // 客户
      this.$http({url: 'hyCustomer/page', method: 'get', params: {page: 1, limit: 1000}}).then(({data}) => {
        if (data.code !== 0) return
        const list = data.data.list || []
        this.cards[0].value = data.data.total
        this.cards[1].value = list.filter(c => c.followStatus === '待付款').length
        this.unpaidList = list.filter(c => (c.unpaidAmount || 0) > 0).slice(0, 8)
        this.buildPerfFromCustomers(list)
      })
      // 订单
      this.$http({url: 'hyOrder/page', method: 'get', params: {page: 1, limit: 1000}}).then(({data}) => {
        if (data.code !== 0) return
        const list = data.data.list || []
        const done = list.filter(o => o.status === '已完成').length
        const abn = list.filter(o => o.abnormal)
        this.cards[2].value = abn.length
        this.cards[3].value = done
        this.abnormalList = abn.slice(0, 8)
        this.buildDeliver(list, done, abn.length)
        this.buildPerfFromOrders(list)
        this.renderTrend(list)
      })
    },
    buildPerfFromCustomers(list) {
      const map = {}
      list.forEach(c => {
        const k = c.managerName || '未分配'
        if (!map[k]) map[k] = {name: k, following: 0, dealCust: 0, dealCount: 0, amount: 0, commission: 0}
        if (c.followStatus === '跟进中' || c.followStatus === '待付款') map[k].following++
        if ((c.dealCount || 0) > 0) map[k].dealCust++
      })
      this._perfMap = map
      this.flushPerf()
    },
    buildPerfFromOrders(list) {
      const map = this._perfMap || {}
      list.forEach(o => {
        const k = o.managerName || '未分配'
        if (!map[k]) map[k] = {name: k, following: 0, dealCust: 0, dealCount: 0, amount: 0, commission: 0}
        if (o.status === '已完成' || o.status === '待交付') {
          map[k].dealCount++
          map[k].amount += Number(o.amount || 0)
        }
      })
      this._perfMap = map
      this.flushPerf()
    },
    flushPerf() {
      const arr = Object.values(this._perfMap || {})
      arr.forEach(p => p.commission = Math.round(p.amount * 0.05))
      arr.sort((a, b) => b.amount - a.amount)
      this.perf = arr.slice(0, 6)
    },
    buildDeliver(list, done, abn) {
      const total = list.length
      const undone = total - done
      this.deliver = {
        total: total,
        done: done,
        undone: undone,
        abnormal: abn,
        donePct: total ? Math.round(done / total * 100) : 0,
        undonePct: total ? Math.round(undone / total * 100) : 0,
        abnPct: total ? Math.round(abn / total * 100) : 0,
        rate: total ? Math.round(done / total * 100) : 0
      }
    },
    renderTrend(list) {
      // 按日期聚合成交额/数量
      const byDay = {}
      list.forEach(o => {
        const d = (o.addtime || '').substr(0, 10)
        if (!d) return
        if (!byDay[d]) byDay[d] = {amount: 0, count: 0}
        byDay[d].amount += Number(o.amount || 0)
        byDay[d].count++
      })
      const days = Object.keys(byDay).sort()
      if (!this.trendChart) this.trendChart = echarts.init(this.$refs.trendChart)
      this.trendChart.setOption({
        tooltip: {trigger: 'axis'},
        legend: {data: ['成交额 (元)', '成交数量'], top: 0},
        grid: {left: '3%', right: '4%', bottom: '3%', containLabel: true},
        xAxis: {type: 'category', data: days.map(d => d.substr(5))},
        yAxis: [{type: 'value'}, {type: 'value'}],
        series: [
          {name: '成交数量', type: 'bar', yAxisIndex: 1, data: days.map(d => byDay[d].count), itemStyle: {color: '#9CC2FF', barBorderRadius: [4, 4, 0, 0]}, barWidth: '40%'},
          {name: '成交额 (元)', type: 'line', smooth: true, data: days.map(d => byDay[d].amount), itemStyle: {color: '#2F6BFF'}, areaStyle: {color: 'rgba(47,107,255,.08)'}}
        ]
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.overview { padding: 0; }
.stat-row { margin-bottom: 18px; }
.stat-card {
  min-height: 116px; box-sizing: border-box;
  background: #fff; border: 1px solid #E7ECF3; border-radius: 12px; padding: 20px 22px;
  display: flex; align-items: center; box-shadow: 0 6px 18px rgba(31,39,51,.05);
  border-top: 3px solid #2F6BFF;
}
.stat-card.c-blue { border-top-color: #2F6BFF; }
.stat-card.c-amber { border-top-color: #FF8A3D; }
.stat-card.c-red { border-top-color: #FF5A5F; }
.stat-card.c-green { border-top-color: #22B07D; }
.stat-icon { width: 56px; height: 56px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 28px; margin-right: 16px; }
.c-blue .stat-icon { background: #EAF1FF; color: #2F6BFF; }
.c-amber .stat-icon { background: #FFF1E6; color: #FF8A3D; }
.c-red .stat-icon { background: #FDECEC; color: #FF5A5F; }
.c-green .stat-icon { background: #E8F7F0; color: #22B07D; }
.stat-label { font-size: 13px; color: #8A94A6; }
.stat-num { font-size: 30px; font-weight: 800; color: #1F2733; line-height: 1.2; }
.stat-delta { font-size: 12px; color: #8A94A6; margin-top: 4px; }
.stat-delta .up { color: #22B07D; }
.stat-delta .down { color: #FF5A5F; }

.blk { margin-bottom: 0; border-radius: 10px; }
.business-row { margin-bottom: 16px; }
.trend-chart { height: 110px; }
.todo-row { margin-top: 0; }
.blk ::v-deep .el-card__header { padding: 14px 16px; }
.blk ::v-deep .el-card__body { padding: 10px 16px; }
.blk-head { display: flex; justify-content: space-between; align-items: center; }
.more { font-size: 12px; color: #2F6BFF; cursor: pointer; }
.pf-cell { display: flex; align-items: center; }
.rank { display: inline-block; width: 20px; height: 20px; line-height: 20px; text-align: center; border-radius: 6px; background: #EEF1F5; color: #8A94A6; font-size: 12px; margin-right: 8px; flex-shrink: 0; }
.rank.r1 { background: #FFE6B0; color: #B8791F; }
.rank.r2 { background: #E3E8EF; color: #6B7785; }
.rank.r3 { background: #F8D8C0; color: #C2683A; }
.pf-av { width: 28px; height: 28px; border-radius: 50%; background: linear-gradient(135deg,#4f8bff,#2F6BFF); color: #fff; font-size: 13px; display: inline-flex; align-items: center; justify-content: center; margin-right: 8px; flex-shrink: 0; }
.pf-name { font-weight: 600; }
.pf-num { font-weight: 600; color: #1F2733; line-height: 1.2; }
.pf-bar { height: 4px; border-radius: 3px; background: #EEF1F5; margin-top: 4px; overflow: hidden; }
.pf-bar i { display: block; height: 100%; border-radius: 3px; }
.pf-comm { color: #FF8A3D; font-weight: 600; }
.red { color: #FF5A5F; font-weight: 600; }

.deliver { display: flex; align-items: center; gap: 8px; padding: 4px 0; }
.rate-ring {
  position: relative;
  width: 100px;
  height: 100px;
  flex-shrink: 0;
}
.rate-svg {
  width: 100%;
  height: 100%;
  display: block;
  filter: drop-shadow(0 4px 10px rgba(34, 176, 125, 0.12));
}
.rate-track {
  fill: none;
  stroke: #EEF1F5;
  stroke-width: 10;
}
.rate-bar {
  fill: none;
  stroke: url(#deliverRateGrad);
  stroke-width: 10;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.65s cubic-bezier(0.4, 0, 0.2, 1);
}
.rate-core {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}
.rate-value {
  font-size: 24px;
  font-weight: 800;
  color: #1F2733;
  line-height: 1;
  letter-spacing: -0.5px;
}
.rate-unit {
  font-size: 13px;
  font-weight: 600;
  margin-left: 1px;
}
.rate-name {
  margin-top: 5px;
  font-size: 11px;
  color: #8A94A6;
  letter-spacing: 0.5px;
}
.deliver-stats { flex: 1; display: flex; flex-wrap: wrap; }
.ds { width: 50%; padding: 8px 12px; display: flex; align-items: center; gap: 10px; }
.ds-ic { width: 34px; height: 34px; border-radius: 9px; display: flex; align-items: center; justify-content: center; font-size: 17px; color: #fff; flex-shrink: 0; }
.ds-ic.b1 { background: #2F6BFF; } .ds-ic.b2 { background: #22B07D; } .ds-ic.b3 { background: #FF8A3D; } .ds-ic.b4 { background: #FF5A5F; }
.ds-num { font-size: 22px; font-weight: 800; color: #1F2733; line-height: 1.1; }
.ds-num.green { color: #22B07D; }
.ds-num.orange { color: #FF8A3D; }
.ds-num.red { color: #FF5A5F; }
.ds-l { font-size: 12px; color: #8A94A6; }
</style>
