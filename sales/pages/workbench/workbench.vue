<template>
	<sales-shell active="workbench" align="center" :title="greeting" :subtitle="subtitle">
		<view class="wb">
			<!-- 左主列 -->
			<view class="col-main">
				<!-- Hero -->
				<view class="hero">
					<view class="hero-text">
						<view class="hero-title">准备好开始今天的创作了吗？</view>
						<view class="hero-sub">为顾客找到真正适合他的内容方案</view>
						<view class="hero-actions">
							<view class="btn btn-primary hero-start" @click="startReception">＋ 开始接待</view>
							<view class="btn btn-ghost hero-scan" @click="scanOldCustomer">⊞ 扫码识别老客户</view>
						</view>
					</view>
					<view class="hero-glow"></view>
				</view>

				<!-- 今日接待 -->
				<view class="block-title">今日接待</view>
				<view v-if="receptions.length===0" class="empty grow">今日暂无接待安排</view>
				<view v-else class="reception">
					<view v-for="(r,idx) in receptions" :key="r.id" class="rc-col">
						<!-- 顶部时间线 -->
						<view class="rc-timeline">
							<view class="rc-dot" :class="{active: idx===0}"></view>
							<view v-if="idx < receptions.length-1" class="rc-track"></view>
						</view>
						<view class="rc-card" @click="openCustomer(r)">
							<view class="rc-time" :style="{color: statusColor(idx)}">{{ formatHm(r.taskTime) }}</view>
							<view class="rc-body">
								<image class="rc-img" :src="$img(r.cover)" mode="aspectFill"></image>
								<view class="rc-info">
									<text class="rc-name">{{ r.customerName }}</text>
									<text class="rc-status" :style="{color: statusColor(idx)}">{{ r.action }}</text>
								</view>
								<text class="rc-arrow">›</text>
							</view>
						</view>
					</view>
				</view>

				<!-- 继续上次选片 -->
				<view v-if="lastSession" class="continue card">
					<text class="ct-label">继续上次选片</text>
					<image class="ct-img" :src="$img(lastSession.cover)" mode="aspectFill"></image>
					<view class="ct-info">
						<text class="ct-name">{{ lastSession.customerName }}</text>
					</view>
					<view class="ct-count">已选 <text class="hot">{{ lastSession.selectedCount||0 }}</text> / 目标 {{ lastSession.targetCount||0 }}</view>
					<view class="btn btn-danger ct-btn" @click="continueSelection">继续选片 ›</view>
				</view>
			</view>

			<!-- 右列 -->
			<view class="col-side">
				<!-- 今日概览 -->
				<view class="card side-card">
					<view class="sc-title">今日概览</view>
					<view class="overview">
						<view class="ov-item o1">
							<text class="ov-num">{{ overview.reception }}</text>
							<text class="ov-label">待接待</text>
						</view>
						<view class="ov-item o2">
							<text class="ov-num">{{ overview.follow }}</text>
							<text class="ov-label">待跟进</text>
						</view>
						<view class="ov-item o3">
							<text class="ov-num">{{ overview.unpaid }}</text>
							<text class="ov-label">待付款</text>
						</view>
					</view>
				</view>

				<!-- 待办事项 -->
				<view class="card side-card grow">
					<view class="sc-title">待办事项</view>
					<view v-if="todos.length===0" class="empty sm">暂无待办</view>
					<view v-for="(t,i) in todos" :key="t.id" class="todo">
						<view class="todo-dot" :style="{background: dotColor(i)}"></view>
						<text class="todo-text">{{ t.action }}</text>
						<text class="todo-time">{{ formatHm(t.taskTime) }}</text>
						<text class="todo-arrow">›</text>
					</view>
				</view>

				<!-- 新内容已上线 -->
				<view class="card side-card">
					<view class="sc-title-row">
						<view class="sc-title">新内容已上线</view>
						<text v-if="cacheStatus.syncing" class="cache-tag syncing">同步中…</text>
						<text v-else class="cache-tag" @click="syncMaterials">本地 {{ cacheStatus.cachedVideo }}/{{ cacheStatus.remoteTotal || '—' }}</text>
					</view>
					<view class="new-grid">
						<view v-for="m in newMaterials" :key="m.id" class="ng-item" @click="goMaterial">
							<image v-if="materialThumb(m)" class="ng-img" :src="materialThumb(m)" mode="aspectFill"></image>
							<view v-else class="ng-img ng-vid-ph">
								<text class="ng-play">▶</text>
							</view>
							<text class="ng-name">{{ materialLabel(m) }}</text>
							<text class="ng-cnt">{{ materialMeta(m) }}</text>
						</view>
					</view>
					<view class="more" @click="goMaterial">查看素材库 ›</view>
				</view>
			</view>
		</view>
	</sales-shell>
</template>

<script>
import salesShell from '@/components/sales-shell/sales-shell.vue'
export default {
	components: { salesShell },
	data() {
		return {
			empName: '',
			receptions: [],
			todos: [],
			newMaterials: [],
			lastSession: null,
			overview: { reception: 0, follow: 0, unpaid: 0 },
			cacheStatus: { syncing: false, cachedVideo: 0, remoteTotal: 0 }
		}
	},
	computed: {
		greeting() {
			const h = new Date().getHours()
			const g = h < 11 ? '早上好' : (h < 14 ? '中午好' : (h < 18 ? '下午好' : '晚上好'))
			return `${g}，${this.empName || '伙伴'}`
		},
		subtitle() {
			return `今天有 ${this.overview.reception} 位客户等待接待`
		}
	},
	onShow() {
		if (!this.$api.auth()) return
		this.empName = uni.getStorageSync('empName') || ''
		this.refreshCacheStatus()
		this.loadData()
	},
	onUnload() {
		if (this._offCacheStatus) this._offCacheStatus()
	},
	onLoad() {
		this._offCacheStatus = this.$materialCache.onStatusChange(st => {
			this.cacheStatus = st
		})
	},
	methods: {
		refreshCacheStatus() {
			this.cacheStatus = this.$materialCache.getStatus()
		},
		syncMaterials() {
			if (!this.$materialCache.isAppPlus()) {
				uni.showToast({ title: '请在 Pad App 中使用本地缓存', icon: 'none' })
				return
			}
			this.$materialCache.startSync(this.$api, this.$base.url, { silent: false })
		},
		loadData() {
			const empId = uni.getStorageSync('empId')
			// 该经理名下客户（口径与后台客户管理一致）
			const cq = { page: 1, limit: 100 }
			if (empId) cq.managerId = empId
			this.$api.page('hyCustomer', cq).then(res => {
				const list = (res.data && res.data.list) || []
				const unpaid = list.filter(c => c.followStatus === '待付款')
				const following = list.filter(c => c.followStatus === '跟进中')
				// 待接待：跟进中/未设状态且尚未开始选片
				const toReceive = list.filter(c => (c.followStatus === '跟进中' || !c.followStatus) && (c.selectedCount || 0) === 0)
				this.overview.unpaid = unpaid.length
				this.overview.follow = following.length
				this.overview.reception = toReceive.length
				// 今日接待：优先待接待，再补待付款提醒
				const rec = toReceive.concat(unpaid).slice(0, 3)
				this.receptions = rec.map(c => ({
					id: c.id,
					customerId: c.id,
					customerName: c.name,
					action: c.followStatus === '待付款' ? '提醒完成付款' : '开始接待',
					taskTime: c.lastFollowTime || c.addtime,
					cover: c.avatar
				}))
			})
			// 待办事项：该经理待办任务
			const tq = { page: 1, limit: 20, status: '待办', sort: 'task_time', order: 'asc' }
			if (empId) tq.ownerId = empId
			this.$api.page('hyFollowTask', tq).then(res => {
				this.todos = ((res.data && res.data.list) || []).slice(0, 5)
			})
			// 新内容（最新上架）
			this.$api.page('hyMaterial', { page: 1, limit: 3, sort: 'addtime', order: 'desc' }).then(res => {
				this.newMaterials = (res.data && res.data.list) || []
				const remoteTotal = (res.data && res.data.total) || this.newMaterials.length
				this.cacheStatus = Object.assign({}, this.cacheStatus, { remoteTotal })
				if (this.$materialCache.isAppPlus()) {
					const st = this.$materialCache.getStatus()
					const stale = !st.lastSyncAt || (Date.now() - st.lastSyncAt > 30 * 60 * 1000)
					if (stale && !st.syncing) {
						this.$materialCache.startSync(this.$api, this.$base.url, { silent: true })
					}
				}
			})
			// 进行中的选片（限定当前经理，且客户仍存在）
			const sq = { page: 1, limit: 5, status: '进行中', sort: 'addtime', order: 'desc' }
			if (empId) sq.managerId = empId
			this.$api.page('hySelectionSession', sq).then(res => {
				const list = (res.data && res.data.list) || []
				const s = list[0] || null
				if (!s) {
					this.lastSession = null
					return
				}
				this.$api.info('hyCustomer', s.customerId).then(cr => {
					if (!cr.data) {
						this.lastSession = null
						return
					}
					this.lastSession = s
					if (!this.lastSession.cover) {
						this.lastSession.cover = cr.data.avatar || 'upload/studio_cover_3.jpg'
					}
				}).catch(() => { this.lastSession = null })
			})
		},
		formatHm(t) {
			if (!t) return ''
			const d = new Date(t.replace ? t.replace(/-/g, '/') : t)
			const hh = ('0' + d.getHours()).slice(-2)
			const mm = ('0' + d.getMinutes()).slice(-2)
			return `${hh}:${mm}`
		},
		dotColor(i) {
			return ['#FF5A5F', '#2F6BFF', '#22B07D', '#FF8A3D', '#7C5CFF'][i % 5]
		},
		statusColor(i) {
			return ['#FF5A5F', '#2F6BFF', '#22B07D'][i % 3]
		},
		startReception() {
			uni.navigateTo({ url: '/pages/customer/customer' })
		},
		scanOldCustomer() {
			const openCustomer = (raw) => {
				let id = String(raw || '').trim()
				const m = id.match(/hyCustomer[:/=]?(\d+)/i) || id.match(/customerId[=:](\d+)/i) || id.match(/^(\d{5,})$/)
				if (m) {
					uni.navigateTo({ url: `/pages/customer/customer?id=${m[1]}` })
					return
				}
				uni.showToast({ title: '未识别到客户码，已进入客户列表', icon: 'none' })
				uni.navigateTo({ url: '/pages/customer/customer' })
			}
			// #ifdef APP-PLUS || MP-WEIXIN
			uni.scanCode({
				success: (res) => openCustomer(res.result),
				fail: () => uni.navigateTo({ url: '/pages/customer/customer' })
			})
			// #endif
			// #ifdef H5
			uni.showModal({
				title: '扫码识别老客户',
				editable: true,
				placeholderText: '粘贴客户码，如 hyCustomer:6001',
				success: (r) => {
					if (r.confirm) openCustomer(r.content)
					else uni.navigateTo({ url: '/pages/customer/customer' })
				}
			})
			// #endif
		},
		openCustomer(r) {
			uni.navigateTo({ url: `/pages/customer/customer?id=${r.customerId}` })
		},
		continueSelection() {
			uni.navigateTo({ url: `/pages/selection/selection?sessionId=${this.lastSession.id}&customerId=${this.lastSession.customerId}` })
		},
		goMaterial() {
			uni.reLaunch({ url: '/pages/material/library' })
		},
		materialLabel(m) {
			const t = String((m && m.title) || '').trim()
			if (t) return t.length > 10 ? t.slice(0, 10) + '…' : t
			return (m && (m.industrySub || m.industryBig)) || '新素材'
		},
		materialMeta(m) {
			if (!m) return ''
			if (m.usedCount > 0) return m.usedCount + ' 次使用'
			if (m.heat > 0) return '热度 ' + m.heat
			return '新上架'
		},
		materialThumb(m) {
			if (!m || !m.cover) return ''
			if (/\.(mp4|mov|webm|m4v)(\?.*)?$/i.test(m.cover)) return ''
			const url = this.$media(m, 'cover')
			return url || ''
		}
	}
}
</script>

<style lang="scss" scoped>
.wb {
	flex: 1;
	min-height: 0;
	height: 100%;
	display: flex;
	gap: 32rpx;
	align-items: stretch;
}
.col-main {
	flex: 1.9;
	min-width: 0;
	min-height: 0;
	display: flex;
	flex-direction: column;
}
.col-side {
	flex: 1;
	min-width: 0;
	min-height: 0;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	gap: 24rpx;
	overflow-y: auto;
}

/* Hero */
.hero {
	position: relative;
	background: linear-gradient(120deg, #E6F0FF 0%, #EAF6FF 45%, #F0FBF5 100%);
	border-radius: 28rpx;
	padding: 44rpx 56rpx;
	overflow: hidden;
	/* 与设计稿一致：hero 与“今日接待”区高度约 2:1 */
	flex: 2 0 0;
	min-height: 0;
	display: flex;
	align-items: center;
	border: 1rpx solid rgba(47,107,255,.06);
	box-shadow: inset 0 1rpx 0 rgba(255,255,255,.72), 0 6rpx 20rpx rgba(31,39,51,.025);
}
.hero-glow {
	position: absolute;
	right: -80rpx;
	top: -60rpx;
	width: 420rpx;
	height: 420rpx;
	background: radial-gradient(circle, rgba(120,180,255,.28), transparent 68%);
	border-radius: 50%;
}
.hero-text {
	position: relative;
	z-index: 1;
}
.hero-title {
	font-size: 52rpx;
	font-weight: 700;
	color: $ink;
	letter-spacing: 1rpx;
}
.hero-sub {
	font-size: 28rpx;
	color: $ink-2;
	margin-top: 20rpx;
}
.hero-actions {
	display: flex;
	align-items: center;
	gap: 28rpx;
	margin-top: 56rpx;
}
.hero-start {
	height: 104rpx;
	padding: 0 72rpx;
	font-size: 34rpx;
	font-weight: 600;
	border-radius: 20rpx;
	box-shadow: 0 12rpx 26rpx rgba(47,107,255,.24);
}
.hero-scan {
	height: 104rpx;
	padding: 0 44rpx;
	font-size: 30rpx;
	border-radius: 20rpx;
}

.block-title {
	font-size: 32rpx;
	font-weight: 600;
	margin: 32rpx 0 20rpx;
	flex-shrink: 0;
}

/* 今日接待 */
.reception {
	display: flex;
	gap: 24rpx;
	flex: 1 0 0;
	min-height: 0;
	align-items: stretch;
}
.rc-col {
	flex: 1;
	display: flex;
	flex-direction: column;
	min-width: 0;
}
.rc-timeline {
	display: flex;
	align-items: center;
	height: 24rpx;
	margin-bottom: 18rpx;
	padding-left: 6rpx;
}
.rc-dot {
	width: 16rpx;
	height: 16rpx;
	border-radius: 50%;
	background: #C7CDD8;
	flex-shrink: 0;
}
.rc-dot.active {
	background: #FF5A5F;
	box-shadow: 0 0 0 8rpx rgba(255,90,95,.14);
}
.rc-track {
	flex: 1;
	height: 3rpx;
	background: #E2E6EC;
	margin-left: 10rpx;
}
.rc-card {
	background: #fff;
	border: 1rpx solid $line;
	border-radius: 20rpx;
	padding: 24rpx 22rpx;
	flex: 1;
	min-height: 0;
	display: flex;
	flex-direction: column;
	justify-content: center;
	box-shadow: 0 3rpx 12rpx rgba(31,39,51,.025);
}
.rc-time {
	font-size: 34rpx;
	font-weight: 700;
	margin-bottom: 20rpx;
}
.rc-body {
	display: flex;
	align-items: center;
}
.rc-img {
	width: 120rpx;
	height: 120rpx;
	border-radius: 16rpx;
	background: #f0f0f0;
	flex-shrink: 0;
}
.rc-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	margin-left: 20rpx;
	min-width: 0;
}
.rc-name {
	font-size: 28rpx;
	font-weight: 700;
	color: $ink;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}
.rc-status {
	font-size: 24rpx;
	margin-top: 8rpx;
}
.rc-arrow {
	color: #C7CDD8;
	font-size: 34rpx;
}

/* 继续选片 */
.continue {
	display: flex;
	align-items: center;
	padding: 24rpx 28rpx;
	margin-top: 22rpx;
	flex-shrink: 0;
}
.ct-label {
	font-size: 26rpx;
	font-weight: 600;
	color: $ink;
	margin-right: 20rpx;
}
.ct-img {
	width: 64rpx;
	height: 64rpx;
	border-radius: 12rpx;
	background: #eee;
}
.ct-info {
	margin-left: 16rpx;
	flex: 1;
}
.ct-name {
	font-size: 26rpx;
	color: $ink;
}
.ct-count {
	font-size: 24rpx;
	color: $ink-2;
	margin-right: 24rpx;
}
.ct-count .hot {
	color: $danger;
	font-weight: 700;
}
.ct-btn {
	height: 72rpx;
	padding: 0 32rpx;
	font-size: 26rpx;
}

/* 右列卡片 */
.side-card {
	padding: 32rpx;
	flex-shrink: 0;
}
.side-card.grow {
	max-height: 46%;
	overflow-y: auto;
}
.sc-title {
	font-size: 30rpx;
	font-weight: 600;
	margin-bottom: 24rpx;
}
.sc-title-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 24rpx;
}
.sc-title-row .sc-title {
	margin-bottom: 0;
}
.cache-tag {
	font-size: 22rpx;
	color: $brand;
	padding: 6rpx 16rpx;
	background: rgba(47, 107, 255, .08);
	border-radius: 999rpx;
}
.cache-tag.syncing {
	color: $muted;
	background: #F1F3F6;
}
.overview {
	display: flex;
	justify-content: space-between;
}
.ov-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	border-radius: 18rpx;
	border: 1rpx solid transparent;
	padding: 24rpx 0;
	margin: 0 8rpx;
}
.ov-item:first-child { margin-left: 0; }
.ov-item:last-child { margin-right: 0; }
.ov-item.o1 { background: #FFF8F3; border-color: #FFD9BE; }
.ov-item.o1 .ov-num { color: #FF8A3D; }
.ov-item.o2 { background: #F5F8FF; border-color: #C9DBFF; }
.ov-item.o2 .ov-num { color: #2F6BFF; }
.ov-item.o3 { background: #F3FBF7; border-color: #BFE9D5; }
.ov-item.o3 .ov-num { color: #22B07D; }
.ov-num {
	font-size: 52rpx;
	font-weight: 800;
}
.ov-label {
	font-size: 22rpx;
	color: $muted;
	margin-top: 8rpx;
}

.todo {
	display: flex;
	align-items: center;
	padding: 18rpx 0;
	border-bottom: 1rpx solid #F5F6F8;
}
.todo:last-child {
	border-bottom: none;
}
.todo-dot {
	width: 14rpx;
	height: 14rpx;
	border-radius: 50%;
	margin-right: 16rpx;
}
.todo-text {
	flex: 1;
	font-size: 26rpx;
	color: $ink;
}
.todo-time {
	font-size: 24rpx;
	color: $muted;
	margin-right: 10rpx;
}
.todo-arrow {
	color: #C7CDD8;
}

.new-grid {
	display: flex;
	gap: 16rpx;
}
.ng-item {
	flex: 1;
	display: flex;
	flex-direction: column;
}
.ng-img {
	width: 100%;
	height: 168rpx;
	border-radius: 14rpx;
	background: #eee;
}
.ng-vid-ph {
	display: flex;
	align-items: center;
	justify-content: center;
	background: linear-gradient(135deg, #2a3340 0%, #1a2030 100%);
}
.ng-play {
	font-size: 40rpx;
	color: rgba(255, 255, 255, 0.88);
}
.ng-name {
	font-size: 24rpx;
	color: $ink;
	margin-top: 10rpx;
}
.ng-cnt {
	font-size: 22rpx;
	color: $muted;
}
.more {
	text-align: center;
	font-size: 24rpx;
	color: $ink-2;
	margin-top: 20rpx;
	padding: 16rpx 0;
	border: 1rpx solid $line;
	border-radius: 14rpx;
	background: #FAFBFD;
}

.empty {
	color: $muted;
	font-size: 26rpx;
	padding: 40rpx 0;
	text-align: center;
	width: 100%;
}
.empty.grow {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
}
.empty.sm {
	padding: 24rpx 0;
}

/* 1280×800 横屏 Pad：按 PxCook 标注比例 */
@media #{$pad-mq-landscape} {
	.wb {
		gap: .65vw;
	}
	.col-main {
		flex: 1.743;
	}
	.col-side {
		flex: 1;
		gap: 0;
		justify-content: space-between;
	}

	/* 原稿标注：hero 819×341，屏幕正文高度约 780 */
	.hero {
		flex: none;
		height: 43.7%;
		min-height: 0;
		box-sizing: border-box;
		border-radius: 14px;
		padding: 0 5.1%;
	}
	.hero-glow {
		right: -3vw;
		top: -6vh;
		width: 34vw;
		height: 34vw;
	}
	.hero-title {
		font-size: clamp(25px, 2.35vw, 37px);
		letter-spacing: 0;
	}
	.hero-sub {
		font-size: clamp(14px, 1.2vw, 19px);
		margin-top: 1.3vh;
	}
	.hero-actions {
		gap: 1.4vw;
		margin-top: 3.7vh;
	}
	.hero-start,
	.hero-scan {
		height: 7.2vh;
		border-radius: 12px;
	}
	.hero-start {
		padding: 0 3.6vw;
		font-size: clamp(17px, 1.45vw, 23px);
	}
	.hero-scan {
		padding: 0 2.2vw;
		font-size: clamp(15px, 1.25vw, 20px);
	}

	.block-title {
		font-size: clamp(17px, 1.4vw, 22px);
		margin: 2.2vh 0 1.2vh;
	}
	.reception {
		flex: 1;
		gap: .7vw;
	}
	.rc-timeline {
		height: 1.8vh;
		margin-bottom: .7vh;
		padding-left: .45vw;
	}
	.rc-dot {
		width: 10px;
		height: 10px;
	}
	.rc-dot.active {
		box-shadow: 0 0 0 4px rgba(255,90,95,.14);
	}
	.rc-track {
		height: 2px;
		margin-left: .45vw;
	}
	.rc-card {
		border-radius: 13px;
		padding: 1.5vh 1vw;
	}
	.rc-time {
		font-size: clamp(18px, 1.45vw, 23px);
		margin-bottom: 1.1vh;
	}
	.rc-img {
		width: 4.2vw;
		height: 4.2vw;
		border-radius: 50%;
	}
	.rc-info {
		margin-left: .85vw;
	}
	.rc-name {
		font-size: clamp(13px, 1.05vw, 17px);
	}
	.rc-status {
		font-size: clamp(12px, .9vw, 15px);
		margin-top: .45vh;
	}
	.rc-arrow {
		font-size: clamp(20px, 1.7vw, 27px);
	}

	/* 原稿标注：继续选片条高 122，底部留白 33 */
	.continue {
		height: 15.6%;
		box-sizing: border-box;
		padding: 1.4vh 1.25vw;
		margin-top: 1.7vh;
		border-radius: 13px;
	}
	.ct-label,
	.ct-name {
		font-size: clamp(13px, 1.05vw, 17px);
	}
	.ct-label {
		margin-right: 1.1vw;
	}
	.ct-img {
		width: 4.5vw;
		height: 4.5vw;
		border-radius: 50%;
	}
	.ct-info {
		margin-left: .85vw;
	}
	.ct-count {
		font-size: clamp(12px, .95vw, 15px);
		margin-right: 1.2vw;
	}
	.ct-btn {
		height: 5.7vh;
		padding: 0 1.7vw;
		font-size: clamp(13px, 1.05vw, 17px);
		border-radius: 999px;
	}

	/* 原稿标注：右栏宽 470；三块高度 208 / 225 / 286 */
	.side-card {
		box-sizing: border-box;
		padding: 2vh 1.25vw;
		border-radius: 13px;
	}
	.col-side .side-card:nth-child(1) {
		height: 26.7%;
	}
	.col-side .side-card:nth-child(2) {
		height: 28.8%;
		max-height: none;
	}
	.col-side .side-card:nth-child(3) {
		height: 36.7%;
	}
	.sc-title {
		font-size: clamp(16px, 1.25vw, 20px);
		margin-bottom: 1.4vh;
	}
	.ov-item {
		padding: 1.8vh 0;
		margin: 0 .35vw;
		border-radius: 999px;
	}
	.ov-num {
		font-size: clamp(25px, 2.25vw, 36px);
	}
	.ov-label {
		font-size: clamp(12px, .92vw, 15px);
		margin-top: .45vh;
	}
	.todo {
		padding: .9vh .55vw;
		border: 1px solid #EDF0F4;
		border-radius: 9px;
		margin-bottom: .75vh;
	}
	.todo:last-child {
		border-bottom: 1px solid #EDF0F4;
		margin-bottom: 0;
	}
	.todo-dot {
		width: 8px;
		height: 8px;
		margin-right: .7vw;
	}
	.todo-text {
		font-size: clamp(12px, 1vw, 16px);
	}
	.todo-time {
		font-size: clamp(11px, .88vw, 14px);
		margin-right: .5vw;
	}
	.new-grid {
		gap: .5vw;
	}
	.ng-img {
		height: 9.8vh;
		border-radius: 9px;
	}
	.ng-name {
		font-size: clamp(12px, .95vw, 15px);
		margin-top: .5vh;
	}
	.ng-cnt {
		font-size: clamp(11px, .85vw, 14px);
	}
	.more {
		font-size: clamp(12px, .92vw, 15px);
		margin-top: .7vh;
		padding: .8vh 0;
		border-radius: 8px;
	}
}

@media #{$pad-mq-portrait} {
	.wb {
		flex-direction: column;
		overflow-y: auto;
	}
	.col-main, .col-side {
		flex: none;
		width: 100%;
	}
	.col-side {
		gap: 2vh;
	}
	.hero {
		height: auto;
		min-height: 22vh;
		padding: 3vh 5vw;
	}
	.reception {
		flex-direction: column;
	}
	.rc-col {
		width: 100%;
	}
	.continue {
		height: auto;
		flex-direction: column;
		align-items: flex-start;
		gap: 1.5vh;
		padding: 2vh 3vw;
	}
	.col-side .side-card:nth-child(1),
	.col-side .side-card:nth-child(2),
	.col-side .side-card:nth-child(3) {
		height: auto;
		min-height: 0;
	}
}
</style>
