<template>
	<sales-shell active="order" title="订单执行" subtitle="让每一次拍摄按承诺完成">
		<view slot="search" class="searchbar">
			<text class="s-ico"></text>
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
					<view v-if="orders.length===0" class="empty">暂无{{ activeLabel }}订单</view>
					<view v-for="o in orders" :key="o.id" class="o-card" :class="{on:selected&&selected.id===o.id}"
						@click="select(o)">
						<view class="o-top">
							<image class="o-img" :src="orderAvatar(o)" mode="aspectFill"></image>
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
						<text class="d-readonly">销售只读 · 履约由制作侧更新</text>
					</view>
					<view class="d-cust">
						<image class="dc-img" :src="orderAvatar(selected)" mode="aspectFill"></image>
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
						<view class="radar">
							<svg class="radar-svg" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
								<polygon :points="radar.outerStr" fill="#FFFFFF" stroke="#D9E0EA" stroke-width="0.9"></polygon>
								<polygon :points="radar.midStr" fill="none" stroke="#E9ECF1" stroke-width="0.6"></polygon>
								<line v-for="(p,i) in radar.outer" :key="'ax'+i" x1="50" y1="50" :x2="p[0]" :y2="p[1]" stroke="#EDF0F4" stroke-width="0.5"></line>
								<polygon :points="radar.dataStr" fill="rgba(47,107,255,0.16)" stroke="#2F6BFF" stroke-width="1.2"></polygon>
								<circle v-for="(p,i) in radar.data" :key="'pt'+i" :cx="p[0]" :cy="p[1]" r="1.6" fill="#2F6BFF"></circle>
							</svg>
						</view>
						<view class="recipe-list">
							<view v-for="(a,i) in radar.axes" :key="i" class="rl-item">
								<text class="rl-dot" :style="{background: radarColors[i]}"></text>
								<text class="rl-name">{{ a }}</text>
								<text class="rl-num">{{ radar.counts[i] }}</text>
							</view>
						</view>
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
						<text class="cc-hint">正常制作阶段仅查看进度，不安排拍摄、不上传成品、不确认交付</text>
						<view class="btn btn-ghost" @click="viewList">查看本次拍摄清单 ›</view>
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
			items: [],
			customerAvatarMap: {},
			employeeAvatarMap: {},
			materialTypeMap: {},
			radarColors: ['#7C5CFF', '#2F6BFF', '#B9C0CC', '#22B07D', '#FF5A5F']
		}
	},
	computed: {
		activeLabel() {
			const t = this.tabs.find(t => t.key === this.activeTab)
			return t ? t.label : ''
		},
		radar() {
			// 轴顺序必须与选片页一致：厨过程 → 教知识 → 讲故事 → 说观点 → 硬广
			const axes = ['厨过程', '教知识', '讲故事', '说观点', '硬广']
			const counts = axes.map(a => this.distCount(a))
			const max = Math.max(1, ...counts)
			const cx = 50, cy = 50, R = 33
			const ang = i => (-90 + i * 72) * Math.PI / 180
			const P = (r, i) => [+(cx + r * Math.cos(ang(i))).toFixed(2), +(cy + r * Math.sin(ang(i))).toFixed(2)]
			const outer = axes.map((a, i) => P(R, i))
			const mid = axes.map((a, i) => P(R * 0.6, i))
			const data = counts.map((v, i) => P((Math.max(0, v) / max) * R, i))
			const str = pts => pts.map(p => p.join(',')).join(' ')
			return { axes, counts, outer, data, outerStr: str(outer), midStr: str(mid), dataStr: str(data) }
		}
	},
	onShow() {
		if (!this.$api.auth()) return
		this.loadCounts()
		this.loadOrders()
	},
	methods: {
		managerQuery(extra) {
			const q = Object.assign({ page: 1, limit: 50 }, extra || {})
			const empId = uni.getStorageSync('empId')
			if (empId) q.managerId = empId
			return q
		},
		loadCounts() {
			this.tabs.forEach(t => {
				this.$api.page('hyOrder', this.managerQuery({ page: 1, limit: 1, status: t.status })).then(res => {
					t.count = (res.data && res.data.total) || 0
				})
			})
		},
		loadOrders() {
			const t = this.tabs.find(t => t.key === this.activeTab)
			const q = this.managerQuery({ status: t.status })
			if (this.keyword) q.customerName = '%' + this.keyword + '%'
			this.$api.page('hyOrder', q).then(res => {
				this.orders = (res.data && res.data.list) || []
				this.loadOrderAvatars(this.orders)
				this.select(this.orders[0] || null)
			})
		},
		loadOrderAvatars(orders) {
			const customerIds = [...new Set((orders || []).map(o => o.customerId).filter(Boolean))]
			const managerIds = [...new Set((orders || []).map(o => o.managerId).filter(Boolean))]
			customerIds.forEach(id => this.fetchCustomerAvatar(id))
			managerIds.forEach(id => this.fetchEmployeeAvatar(id))
		},
		fetchCustomerAvatar(customerId) {
			if (!customerId || this.customerAvatarMap[customerId]) return
			this.$api.info('hyCustomer', customerId).then(res => {
				if (res.data && res.data.avatar) {
					this.$set(this.customerAvatarMap, customerId, res.data.avatar)
				}
			}).catch(() => {})
		},
		fetchEmployeeAvatar(empId) {
			if (!empId || this.employeeAvatarMap[empId]) return
			this.$api.info('hyEmployee', empId).then(res => {
				if (res.data && res.data.avatar) {
					this.$set(this.employeeAvatarMap, empId, res.data.avatar)
				}
			}).catch(() => {})
		},
		select(o) {
			if (!o) { this.selected = null; this.items = []; return }
			this.selected = o
			this.fetchCustomerAvatar(o.customerId)
			this.fetchEmployeeAvatar(o.managerId)
			this.$api.get(`hyOrder/detail/${o.id}`).then(res => {
				if (res && res.data) this.selected = Object.assign({}, o, res.data)
				this.items = (res && res.items) || []
				this.loadMaterialTypes(this.items)
			}).catch(() => { this.items = [] })
		},
		loadMaterialTypes(items) {
			const ids = [...new Set((items || []).map(it => it.materialRef).filter(Boolean))]
			ids.forEach(id => {
				if (this.materialTypeMap[id]) return
				this.$api.info('hyMaterial', id).then(res => {
					if (res.data && res.data.contentType) {
						this.$set(this.materialTypeMap, id, res.data.contentType)
					}
				}).catch(() => {})
			})
		},
		itemContentType(it) {
			if (!it) return ''
			if (it.materialRef && this.materialTypeMap[it.materialRef]) {
				return this.normalizeType(this.materialTypeMap[it.materialRef])
			}
			return this.normalizeType(it.contentType || '')
		},
		normalizeType(raw) {
			const t = String(raw || '').trim()
			if (!t) return ''
			if (t.indexOf('硬广') >= 0) return '硬广'
			if (t.indexOf('厨过程') >= 0 || t.indexOf('过程') >= 0) return '厨过程'
			if (t.indexOf('教知识') >= 0 || t.indexOf('知识') >= 0) return '教知识'
			if (t.indexOf('说观点') >= 0 || t.indexOf('观点') >= 0) return '说观点'
			if (t.indexOf('讲故事') >= 0 || t.indexOf('故事') >= 0) return '讲故事'
			return t
		},
		pickTab(t) {
			this.activeTab = t.key
			this.loadOrders()
		},
		distCount(axis) {
			const fromItems = (this.items || []).filter(it => this.itemContentType(it) === axis).length
			if ((this.items || []).length > 0) return fromItems
			// 清单为空时回退解析订单 recipe：「硬广2 厨过程1 ...」
			return this.parseRecipeCount(axis)
		},
		parseRecipeCount(axis) {
			const recipe = (this.selected && this.selected.recipe) || ''
			if (!recipe) return 0
			const re = new RegExp(axis + '(\\d+)')
			const m = recipe.match(re)
			return m ? Number(m[1]) || 0 : 0
		},
		orderAvatar(o) {
			if (!o) return ''
			const avatar = this.customerAvatarMap[o.customerId]
			if (avatar) return this.$img(avatar)
			return ''
		},
		ownerAvatar(o) {
			if (!o) return ''
			const avatar = this.employeeAvatarMap[o.managerId]
			if (avatar) return this.$img(avatar)
			return ''
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
		viewList() {
			const n = (this.items || []).length
			uni.showToast({ title: n ? `本次共 ${n} 条内容清单` : '暂无内容清单', icon: 'none' })
		}
	}
}
</script>

<style lang="scss" scoped>
.searchbar { width: 560rpx; height: 64rpx; background:#F4F6FA; border-radius:999rpx; display:flex; align-items:center; padding:0 24rpx; }
.s-ico { font-size:26rpx; margin-right:12rpx; }
.s-input { flex:1; font-size:26rpx; }

.tabs { display:flex; gap:16rpx; margin-bottom:22rpx; flex-shrink:0; }
.tab { padding:14rpx 36rpx; background:#fff; border:1rpx solid $line; border-radius:999rpx; font-size:26rpx; color:$ink-2; }
.tab.on { background:$brand; color:#fff; border:none; }

.om { flex:1; min-height:0; height:100%; display:flex; gap:24rpx; align-items:stretch; }
.col-list { flex:1.1; min-width:0; min-height:0; display:flex; flex-direction:column; }
.col-detail { flex:1.1; min-width:0; min-height:0; display:flex; flex-direction:column; gap:20rpx; overflow-y:auto; }
.cl-title { font-size:28rpx; font-weight:700; margin-bottom:16rpx; flex-shrink:0; }
.cl-scroll { flex:1; min-height:0; }

.o-card { background:#fff; border:1rpx solid $line; border-radius:18rpx; padding:24rpx; margin-bottom:18rpx; }
.o-card.on { border-color:#FF7A59; box-shadow:0 8rpx 22rpx rgba(255,90,95,.12); }
.o-top { display:flex; align-items:center; }
.o-img { width:112rpx; height:112rpx; border-radius:14rpx; background:#eee; flex-shrink:0; }
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
.d-title { font-size:28rpx; font-weight:600; }
.d-readonly { font-size:22rpx; color:$muted; }
.d-done { height:60rpx; padding:0 24rpx; font-size:24rpx; }
.d-cust { display:flex; margin-top:20rpx; }
.dc-img { width:110rpx; height:110rpx; border-radius:14rpx; background:#eee; }
.dc-info { flex:1; margin-left:18rpx; }
.dc-name { font-size:30rpx; font-weight:700; }
.dc-meta { display:flex; justify-content:space-between; font-size:22rpx; color:$muted; margin-top:10rpx; }
.deliver { margin-top:24rpx; background:#FFF6F4; border-radius:16rpx; padding:22rpx; }
.dl-title { font-size:27rpx; font-weight:600; }
.dl-title .dl-num { color:#FF5A5F; font-size:38rpx; }
.dl-sub { font-size:23rpx; color:$ink-2; display:block; margin-top:8rpx; }

.d-card { flex-shrink:0; }
.content-card { padding:24rpx; flex:1; min-height:0; display:flex; flex-direction:column; }
.cc-title { font-size:28rpx; font-weight:600; }
.recipe-row { display:flex; align-items:center; gap:26rpx; margin:18rpx 0; }
.radar { width:200rpx; height:200rpx; flex-shrink:0; }
.radar-svg { width:100%; height:100%; display:block; }
.recipe-list { flex:1; display:flex; flex-wrap:wrap; gap:12rpx 26rpx; }
.rl-item { display:flex; align-items:center; }
.rl-dot { width:16rpx; height:16rpx; border-radius:50%; margin-right:10rpx; }
.rl-name { font-size:24rpx; color:$ink-2; }
.rl-num { font-size:24rpx; font-weight:800; color:$ink; margin-left:8rpx; }
.item-grid { display:flex; flex-wrap:wrap; gap:14rpx; }
.it-cell { width: calc((100% - 70rpx) / 6); position:relative; display:flex; flex-direction:column; }
.it-img { width:100%; height:120rpx; border-radius:12rpx; background:#eee; box-shadow:0 3rpx 10rpx rgba(31,39,51,.05); }
.it-no { position:absolute; left:8rpx; bottom:42rpx; color:#fff; font-size:22rpx; font-weight:700; text-shadow:0 0 6rpx rgba(0,0,0,.6); }
.it-check { position:absolute; top:8rpx; right:8rpx; width:32rpx; height:32rpx; border-radius:50%; background:rgba(255,255,255,.85); border:2rpx solid #D7DCE3; display:flex; align-items:center; justify-content:center; font-size:22rpx; color:#fff; }
.it-check.done { background:#22B07D; border-color:#22B07D; }
.it-name { font-size:20rpx; color:$ink-2; margin-top:8rpx; text-align:center; }
.cc-foot { display:flex; flex-direction:column; gap:18rpx; margin-top:auto; padding-top:24rpx; }
.cc-hint { font-size:22rpx; color:$muted; line-height:1.5; }
.cc-foot .btn { height:84rpx; font-size:26rpx; }

.empty-center { display:flex; align-items:center; justify-content:center; color:$muted; min-height:400rpx; }
.empty { color:$muted; font-size:26rpx; text-align:center; padding:40rpx 0; }

/* 1-4 标注稿：订单列表 663、详情 609，卡片高约 210 */
@media #{$pad-mq-landscape} {
	.searchbar {
		width: 27.7vw;
		height: 5.2vh;
		padding: 0 1.2vw;
	}
	.s-input {
		font-size: clamp(12px, .95vw, 15px);
	}
	.tabs {
		height: 6vh;
		margin-bottom: 1.2vh;
		gap: .7vw;
	}
	.tab {
		width: 13.7vw;
		box-sizing: border-box;
		text-align: center;
		padding: .9vh .8vw;
		font-size: clamp(12px, .95vw, 15px);
	}
	.om {
		gap: .75vw;
	}
	.col-list {
		flex: 663;
	}
	.col-detail {
		flex: 609;
		gap: 1.1vh;
		overflow: hidden;
	}
	.cl-title {
		font-size: clamp(15px, 1.2vw, 19px);
		margin-bottom: .8vh;
	}
	.o-card {
		min-height: 18.5vh;
		box-sizing: border-box;
		padding: 1.3vh 1.1vw;
		margin-bottom: 1vh;
		border-radius: 13px;
	}
	.o-img {
		width: 5.5vw;
		height: 5.5vw;
		border-radius: 50%;
	}
	.o-info {
		margin-left: .9vw;
	}
	.o-name {
		font-size: clamp(16px, 1.3vw, 21px);
	}
	.o-pkg, .o-cnt, .ow-name, .o-prog-text, .o-days {
		font-size: clamp(11px, .86vw, 14px);
	}
	.o-days .d-num {
		font-size: clamp(23px, 1.9vw, 30px);
	}
	.o-bottom {
		margin: 1vh 0 .7vh;
	}
	.ow-av {
		width: 2.3vw;
		height: 2.3vw;
	}
	.o-bar {
		height: 6px;
	}
	.d-card {
		height: 28%;
		box-sizing: border-box;
		padding: 1.4vh 1.2vw;
		border-radius: 13px;
	}
	.d-head {
		align-items: flex-start;
		gap: .6vw;
	}
	.d-readonly {
		font-size: clamp(10px, .78vw, 13px);
		color: $muted;
		max-width: 42%;
		text-align: right;
		line-height: 1.35;
		flex-shrink: 1;
	}
	.d-title, .cc-title {
		font-size: clamp(15px, 1.2vw, 19px);
	}
	.d-done {
		height: 4.5vh;
		padding: 0 1.1vw;
		font-size: clamp(11px, .85vw, 14px);
	}
	.d-cust {
		margin-top: 1vh;
	}
	.dc-img {
		width: 5.4vw;
		height: 5.4vw;
		border-radius: 50%;
	}
	.dc-info {
		margin-left: 1vw;
	}
	.dc-name {
		font-size: clamp(18px, 1.5vw, 24px);
	}
	.dc-meta, .dl-sub {
		font-size: clamp(10px, .78vw, 13px);
		margin-top: .5vh;
	}
	.deliver {
		margin-top: 1vh;
		padding: 1vh 1vw;
		border-radius: 10px;
	}
	.dl-title {
		font-size: clamp(13px, 1vw, 16px);
	}
	.dl-title .dl-num {
		font-size: clamp(23px, 1.9vw, 30px);
	}
	.o-bar.big {
		height: 7px;
		margin-top: .7vh;
	}
	.content-card {
		padding: 1.4vh 1.2vw;
		border-radius: 13px;
	}
	.recipe-row {
		gap: 1.1vw;
		margin: .8vh 0;
		height: 12vh;
	}
	.radar {
		width: 7.8vw;
		height: 7.8vw;
	}
	.recipe-list {
		gap: .55vh 1vw;
	}
	.rl-dot {
		width: 8px;
		height: 8px;
		margin-right: .45vw;
	}
	.rl-name, .rl-num {
		font-size: clamp(11px, .86vw, 14px);
	}
	.item-grid {
		gap: .75vh .55vw;
	}
	.it-cell {
		width: calc((100% - 2.75vw) / 6);
	}
	.it-img {
		height: 8.2vh;
		border-radius: 8px;
	}
	.it-name {
		font-size: clamp(9px, .7vw, 11px);
		margin-top: .35vh;
	}
	.it-no {
		bottom: 2.6vh;
		font-size: 12px;
	}
	.it-check {
		width: 18px;
		height: 18px;
		font-size: 12px;
	}
	.cc-foot {
		padding-top: 1vh;
		gap: .8vw;
	}
	.cc-hint {
		font-size: clamp(10px, .78vw, 13px);
		line-height: 1.45;
		color: $muted;
	}
	.cc-foot .btn {
		height: 5.7vh;
		font-size: clamp(12px, .95vw, 15px);
	}
}

@media #{$pad-mq-portrait} {
	.om {
		flex-direction: column;
		overflow-y: auto;
	}
	.col-list, .col-detail {
		flex: none;
		width: 100%;
	}
	.col-detail {
		margin-top: 2vh;
	}
	.d-card {
		height: auto;
	}
	.it-cell {
		width: calc((100% - 28rpx) / 3);
	}
	.tabs {
		flex-wrap: wrap;
		height: auto;
	}
}
</style>
