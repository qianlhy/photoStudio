<template>
	<sales-shell active="order" title="订单执行" subtitle="让每一次拍摄按承诺完成">
		<view slot="search" class="searchbar">
			<text class="s-ico">🔍</text>
			<input v-model="keyword" class="s-input" placeholder="搜索客户或套餐" @confirm="loadOrders" />
		</view>

		<view class="tabs">
			<view v-for="t in tabs" :key="t.key" class="tab" :class="{on:t.key===activeTab}" @click="pickTab(t)">
				{{ t.label }} {{ t.count }}
			</view>
		</view>

		<view class="om">
			<!-- 左列表 -->
			<view class="col-list">
				<view class="cl-title">{{ activeLabel }}订单</view>
				<scroll-view scroll-y class="cl-scroll">
					<view v-for="o in orders" :key="o.id" class="o-card" :class="{on:selected&&selected.id===o.id}"
						@click="select(o)">
						<view class="o-top">
							<image class="o-img" :src="$img(coverOf(o))" mode="aspectFill"></image>
							<view class="o-info">
								<text class="o-name">{{ o.customerName }}</text>
								<text class="o-pkg">{{ o.packageName }}</text>
								<text class="o-cnt">{{ o.videoCount }}条视频</text>
							</view>
							<text class="o-days">剩 <text class="d-num">{{ daysLeft(o) }}</text> 天</text>
						</view>
						<view class="o-bottom">
							<view class="o-owner">
								<image class="ow-av" :src="ownerAvatar(o)" mode="aspectFill"></image>
								<text class="ow-name">负责人 {{ o.managerName }}</text>
							</view>
							<text class="o-prog-text">已完成 {{ o.completedCount }}/{{ o.videoCount }}</text>
						</view>
						<view class="o-bar"><view class="o-bar-in" :style="{width: pct(o)+'%', background: barColor(o)}"></view></view>
					</view>
				</scroll-view>
			</view>

			<!-- 右详情 -->
			<view class="col-detail" v-if="selected">
				<view class="card d-card">
					<view class="d-head">
						<text class="d-title">本次订单</text>
						<view class="btn btn-ghost d-done" @click="finishOrder">✓ 完成订单</view>
					</view>
					<view class="d-cust">
						<image class="dc-img" :src="$img(coverOf(selected))" mode="aspectFill"></image>
						<view class="dc-info">
							<text class="dc-name">{{ selected.customerName }}</text>
							<view class="dc-meta">
								<text>订单号 {{ selected.orderNo }}</text>
								<text>负责人 {{ selected.managerName }}</text>
							</view>
							<view class="dc-meta">
								<text>{{ selected.packageName }} · {{ selected.videoCount }}视频</text>
								<text>拍摄日期 {{ formatMd(selected.shootDate) }}</text>
							</view>
						</view>
					</view>

					<view class="deliver">
						<text class="dl-title">距离交付还有 <text class="dl-num">{{ daysLeft(selected) }}</text> 天</text>
						<text class="dl-sub">已完成 {{ selected.completedCount }}/{{ selected.videoCount }}</text>
						<view class="o-bar big"><view class="o-bar-in" :style="{width: pct(selected)+'%', background:'linear-gradient(90deg,#FF7A59,#FF4D7E)'}"></view></view>
					</view>
				</view>

				<view class="card content-card">
					<text class="cc-title">本次内容</text>
					<view class="recipe-row">
						<view class="pentagon"></view>
						<text class="recipe-text">{{ selected.recipe }}</text>
					</view>
					<view class="item-grid">
						<view v-for="(it,i) in items" :key="it.id" class="it-cell">
							<image class="it-img" :src="$img(it.cover)" mode="aspectFill"></image>
							<view class="it-no">{{ i+1 }}</view>
							<view class="it-check" :class="{done:it.status==1}">{{ it.status==1 ? '✓' : '' }}</view>
							<text class="it-name">{{ it.title }}</text>
						</view>
					</view>
					<view class="cc-foot">
						<view class="btn btn-ghost" @click="upload">⤴ 上传交付成品</view>
						<view class="btn btn-danger" @click="viewList">查看本次拍摄清单 ›</view>
					</view>
				</view>
			</view>
			<view class="col-detail empty-center" v-else><text>请选择左侧订单</text></view>
		</view>
	</sales-shell>
</template>

<script>
import salesShell from '@/components/sales-shell/sales-shell.vue'
export default {
	components: { salesShell },
	data() {
		return {
			keyword: '',
			activeTab: 'deliver',
			tabs: [
				{ key: 'shoot', label: '待拍摄', status: '待拍摄', count: 0 },
				{ key: 'deliver', label: '待交付', status: '待交付', count: 0 },
				{ key: 'done', label: '已完成', status: '已完成', count: 0 }
			],
			orders: [],
			selected: null,
			items: []
		}
	},
	computed: {
		activeLabel() {
			const t = this.tabs.find(t => t.key === this.activeTab)
			return t ? t.label : ''
		}
	},
	onShow() {
		if (!this.$api.auth()) return
		this.loadCounts()
		this.loadOrders()
	},
	methods: {
		loadCounts() {
			this.tabs.forEach(t => {
				this.$api.page('hyOrder', { page: 1, limit: 1, status: t.status }).then(res => {
					t.count = (res.data && res.data.total) || 0
				})
			})
		},
		loadOrders() {
			const t = this.tabs.find(t => t.key === this.activeTab)
			const q = { page: 1, limit: 50, status: t.status }
			if (this.keyword) q.customerName = '%' + this.keyword + '%'
			this.$api.page('hyOrder', q).then(res => {
				this.orders = (res.data && res.data.list) || []
				this.select(this.orders[0])
			})
		},
		select(o) {
			if (!o) { this.selected = null; this.items = []; return }
			this.selected = o
			this.$api.get(`hyOrder/detail/${o.id}`).then(res => {
				this.items = res.items || []
			})
		},
		pickTab(t) {
			this.activeTab = t.key
			this.loadOrders()
		},
		coverOf(o) {
			return o.cover || 'upload/studio_cover_1.jpg'
		},
		ownerAvatar(o) {
			return 'https://i.pravatar.cc/60?u=' + (o.managerId || o.managerName)
		},
		daysLeft(o) {
			if (!o.deliverDate) return 0
			const d = new Date(o.deliverDate.replace ? o.deliverDate.replace(/-/g, '/') : o.deliverDate)
			const diff = Math.ceil((d - new Date()) / 86400000)
			return diff < 0 ? 0 : diff
		},
		pct(o) {
			if (!o.videoCount) return 0
			return Math.round((o.completedCount / o.videoCount) * 100)
		},
		barColor(o) {
			const p = this.pct(o)
			return p >= 100 ? '#22B07D' : (this.activeTab === 'deliver' ? 'linear-gradient(90deg,#FF7A59,#FF4D7E)' : '#2F6BFF')
		},
		formatMd(t) {
			if (!t) return ''
			const d = new Date(t.replace ? t.replace(/-/g, '/') : t)
			return `${d.getMonth() + 1}月${d.getDate()}日`
		},
		finishOrder() {
			if (!this.selected) return
			this.$api.update('hyOrder', { id: this.selected.id, status: '已完成', completedCount: this.selected.videoCount }).then(() => {
				uni.showToast({ title: '订单已完成', icon: 'success' })
				this.loadCounts()
				this.loadOrders()
			})
		},
		upload() {
			uni.showToast({ title: '上传交付成品（演示）', icon: 'none' })
		},
		viewList() {
			uni.showToast({ title: '本次拍摄清单（演示）', icon: 'none' })
		}
	}
}
</script>

<style lang="scss" scoped>
.searchbar { width: 560rpx; height: 64rpx; background:#F4F6FA; border-radius:999rpx; display:flex; align-items:center; padding:0 24rpx; }
.s-ico { font-size:26rpx; margin-right:12rpx; }
.s-input { flex:1; font-size:26rpx; }

.tabs { display:flex; gap:16rpx; margin-bottom:22rpx; }
.tab { padding:14rpx 36rpx; background:#fff; border:1rpx solid $line; border-radius:999rpx; font-size:26rpx; color:$ink-2; }
.tab.on { background:$brand; color:#fff; border:none; }

.om { display:flex; gap:24rpx; align-items:flex-start; }
.col-list { flex:1.1; min-width:0; }
.col-detail { flex:1.1; min-width:0; display:flex; flex-direction:column; gap:20rpx; }
.cl-title { font-size:28rpx; font-weight:700; margin-bottom:16rpx; }
.cl-scroll { max-height: calc(100vh - 300rpx); }

.o-card { background:#fff; border:1rpx solid $line; border-radius:18rpx; padding:22rpx; margin-bottom:18rpx; }
.o-card.on { border-color:#FF7A59; box-shadow:0 8rpx 22rpx rgba(255,90,95,.12); }
.o-top { display:flex; align-items:center; }
.o-img { width:96rpx; height:96rpx; border-radius:14rpx; background:#eee; }
.o-info { flex:1; margin-left:16rpx; display:flex; flex-direction:column; min-width:0; }
.o-name { font-size:28rpx; font-weight:700; }
.o-pkg { font-size:23rpx; color:$ink-2; margin-top:4rpx; }
.o-cnt { font-size:22rpx; color:$muted; }
.o-days { font-size:24rpx; color:$ink-2; }
.o-days .d-num { color:#FF5A5F; font-size:34rpx; font-weight:800; }
.o-bottom { display:flex; justify-content:space-between; align-items:center; margin:16rpx 0 12rpx; }
.o-owner { display:flex; align-items:center; }
.ow-av { width:40rpx; height:40rpx; border-radius:50%; background:#eee; margin-right:10rpx; }
.ow-name { font-size:23rpx; color:$ink-2; }
.o-prog-text { font-size:23rpx; color:$ink-2; }
.o-bar { height:12rpx; background:#EEF1F5; border-radius:999rpx; overflow:hidden; }
.o-bar.big { height:18rpx; margin-top:16rpx; }
.o-bar-in { height:100%; border-radius:999rpx; }

/* 详情 */
.d-card { padding:24rpx; }
.d-head { display:flex; justify-content:space-between; align-items:center; }
.d-title { font-size:28rpx; font-weight:700; }
.d-done { height:60rpx; padding:0 24rpx; font-size:24rpx; }
.d-cust { display:flex; margin-top:20rpx; }
.dc-img { width:110rpx; height:110rpx; border-radius:14rpx; background:#eee; }
.dc-info { flex:1; margin-left:18rpx; }
.dc-name { font-size:30rpx; font-weight:800; }
.dc-meta { display:flex; justify-content:space-between; font-size:22rpx; color:$muted; margin-top:10rpx; }
.deliver { margin-top:24rpx; background:#FFF6F4; border-radius:16rpx; padding:22rpx; }
.dl-title { font-size:27rpx; font-weight:700; }
.dl-title .dl-num { color:#FF5A5F; font-size:38rpx; }
.dl-sub { font-size:23rpx; color:$ink-2; display:block; margin-top:8rpx; }

.content-card { padding:24rpx; }
.cc-title { font-size:28rpx; font-weight:700; }
.recipe-row { display:flex; align-items:center; gap:18rpx; margin:18rpx 0; }
.pentagon { width:80rpx; height:80rpx; flex-shrink:0; clip-path: polygon(50% 0%, 100% 38%, 82% 100%, 18% 100%, 0% 38%); background: conic-gradient(from -90deg,#7C5CFF,#2F6BFF,#B9C0CC,#22B07D,#FF5A5F,#7C5CFF); opacity:.85; }
.recipe-text { font-size:25rpx; color:$ink-2; }
.item-grid { display:flex; flex-wrap:wrap; gap:14rpx; }
.it-cell { width: calc((100% - 70rpx) / 6); position:relative; display:flex; flex-direction:column; }
.it-img { width:100%; height:120rpx; border-radius:12rpx; background:#eee; }
.it-no { position:absolute; left:8rpx; bottom:42rpx; color:#fff; font-size:22rpx; font-weight:700; text-shadow:0 0 6rpx rgba(0,0,0,.6); }
.it-check { position:absolute; top:8rpx; right:8rpx; width:32rpx; height:32rpx; border-radius:50%; background:rgba(255,255,255,.85); border:2rpx solid #D7DCE3; display:flex; align-items:center; justify-content:center; font-size:22rpx; color:#fff; }
.it-check.done { background:#22B07D; border-color:#22B07D; }
.it-name { font-size:20rpx; color:$ink-2; margin-top:8rpx; text-align:center; }
.cc-foot { display:flex; gap:18rpx; margin-top:24rpx; }
.cc-foot .btn { flex:1; height:84rpx; font-size:26rpx; }

.empty-center { display:flex; align-items:center; justify-content:center; color:$muted; min-height:400rpx; }
</style>
