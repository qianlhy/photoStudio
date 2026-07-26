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
				<view class="reception">
					<view v-if="receptions.length===0" class="empty">今日暂无接待安排</view>
					<view v-for="(r,idx) in receptions" :key="r.id" class="rc-card" @click="openCustomer(r)">
						<view class="rc-time">{{ formatHm(r.taskTime) }}</view>
						<view class="rc-body">
							<image class="rc-img" :src="$img(r.cover)" mode="aspectFill"></image>
							<view class="rc-info">
								<text class="rc-name">{{ r.customerName }}</text>
								<text class="rc-status">{{ r.action }}</text>
							</view>
							<text class="rc-arrow">›</text>
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
						<view class="ov-item">
							<text class="ov-num" style="color:#FF8A3D">{{ overview.reception }}</text>
							<text class="ov-label">待接待</text>
						</view>
						<view class="ov-item">
							<text class="ov-num" style="color:#2F6BFF">{{ overview.follow }}</text>
							<text class="ov-label">待跟进</text>
						</view>
						<view class="ov-item">
							<text class="ov-num" style="color:#22B07D">{{ overview.unpaid }}</text>
							<text class="ov-label">待付款</text>
						</view>
					</view>
				</view>

				<!-- 待办事项 -->
				<view class="card side-card">
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
					<view class="sc-title">新内容已上线</view>
					<view class="new-grid">
						<view v-for="m in newMaterials" :key="m.id" class="ng-item" @click="goMaterial">
							<image class="ng-img" :src="$img(m.cover)" mode="aspectFill"></image>
							<text class="ng-name">{{ m.industrySub || m.industryBig }}</text>
							<text class="ng-cnt">{{ m.usedCount || m.viewCount || 0 }} 条</text>
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
			overview: { reception: 0, follow: 0, unpaid: 0 }
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
		this.loadData()
	},
	methods: {
		loadData() {
			const empId = uni.getStorageSync('empId')
			// 待办任务（用作今日接待 + 待办事项）
			this.$api.page('hyFollowTask', { page: 1, limit: 20, status: '待办' }).then(res => {
				const list = (res.data && res.data.list) || []
				this.todos = list.slice(0, 5)
				this.receptions = list.slice(0, 3)
				this.overview.reception = this.receptions.length
				this.overview.follow = list.length
			})
			// 待付款客户数
			this.$api.page('hyCustomer', { page: 1, limit: 100, followStatus: '待付款' }).then(res => {
				this.overview.unpaid = (res.data && res.data.total) || 0
			})
			// 新内容
			this.$api.page('hyMaterial', { page: 1, limit: 3 }).then(res => {
				this.newMaterials = (res.data && res.data.list) || []
			})
			// 进行中的选片
			this.$api.page('hySelectionSession', { page: 1, limit: 1, status: '进行中' }).then(res => {
				const list = (res.data && res.data.list) || []
				this.lastSession = list[0] || null
				if (this.lastSession && !this.lastSession.cover) {
					this.lastSession.cover = 'upload/studio_cover_3.jpg'
				}
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
		startReception() {
			uni.navigateTo({ url: '/pages/customer/customer' })
		},
		scanOldCustomer() {
			// #ifdef APP-PLUS || MP-WEIXIN
			uni.scanCode({ success: () => uni.navigateTo({ url: '/pages/customer/customer' }) })
			// #endif
			// #ifdef H5
			uni.navigateTo({ url: '/pages/customer/customer' })
			// #endif
		},
		openCustomer(r) {
			uni.navigateTo({ url: `/pages/customer/customer?id=${r.customerId}` })
		},
		continueSelection() {
			uni.navigateTo({ url: `/pages/selection/selection?sessionId=${this.lastSession.id}&customerId=${this.lastSession.customerId}` })
		},
		goMaterial() {
			uni.reLaunch({ url: '/pages/material/material' })
		}
	}
}
</script>

<style lang="scss" scoped>
.wb {
	display: flex;
	gap: 28rpx;
	align-items: flex-start;
}
.col-main {
	flex: 1.9;
	min-width: 0;
}
.col-side {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 24rpx;
}

/* Hero */
.hero {
	position: relative;
	background: linear-gradient(120deg, #EAF1FF 0%, #F2FBF6 100%);
	border-radius: 24rpx;
	padding: 48rpx;
	overflow: hidden;
	min-height: 280rpx;
}
.hero-glow {
	position: absolute;
	right: -60rpx;
	top: -60rpx;
	width: 300rpx;
	height: 300rpx;
	background: radial-gradient(circle, rgba(47,107,255,.18), transparent 70%);
	border-radius: 50%;
}
.hero-title {
	font-size: 44rpx;
	font-weight: 800;
	color: $ink;
}
.hero-sub {
	font-size: 26rpx;
	color: $ink-2;
	margin-top: 16rpx;
}
.hero-actions {
	display: flex;
	gap: 24rpx;
	margin-top: 40rpx;
}
.hero-start {
	height: 88rpx;
	padding: 0 56rpx;
	font-size: 30rpx;
	box-shadow: 0 12rpx 26rpx rgba(47,107,255,.3);
}
.hero-scan {
	height: 88rpx;
	padding: 0 36rpx;
	font-size: 28rpx;
}

.block-title {
	font-size: 30rpx;
	font-weight: 700;
	margin: 36rpx 0 20rpx;
}

/* 今日接待 */
.reception {
	display: flex;
	gap: 20rpx;
}
.rc-card {
	flex: 1;
	background: #fff;
	border: 1rpx solid $line;
	border-radius: 18rpx;
	padding: 20rpx;
}
.rc-time {
	font-size: 30rpx;
	font-weight: 800;
	color: #FF8A3D;
	margin-bottom: 14rpx;
}
.rc-body {
	display: flex;
	align-items: center;
}
.rc-img {
	width: 72rpx;
	height: 72rpx;
	border-radius: 14rpx;
	background: #f0f0f0;
}
.rc-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	margin-left: 16rpx;
	min-width: 0;
}
.rc-name {
	font-size: 26rpx;
	font-weight: 600;
	color: $ink;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}
.rc-status {
	font-size: 22rpx;
	color: $muted;
	margin-top: 6rpx;
}
.rc-arrow {
	color: #C7CDD8;
	font-size: 32rpx;
}

/* 继续选片 */
.continue {
	display: flex;
	align-items: center;
	padding: 22rpx 24rpx;
	margin-top: 24rpx;
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
	padding: 28rpx;
}
.sc-title {
	font-size: 28rpx;
	font-weight: 700;
	margin-bottom: 20rpx;
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
	border: 1rpx solid $line;
	border-radius: 16rpx;
	padding: 24rpx 0;
	margin: 0 8rpx;
}
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
	padding: 16rpx 0;
	border-bottom: 1rpx solid #F5F6F8;
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
	height: 130rpx;
	border-radius: 12rpx;
	background: #eee;
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
	color: $brand;
	margin-top: 20rpx;
}

.empty {
	color: $muted;
	font-size: 26rpx;
	padding: 40rpx 0;
	text-align: center;
	width: 100%;
}
.empty.sm {
	padding: 24rpx 0;
}
</style>
