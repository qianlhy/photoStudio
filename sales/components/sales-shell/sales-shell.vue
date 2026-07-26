<template>
	<view class="shell">
		<!-- 顶栏 -->
		<view class="topbar">
			<text class="brand">{{ brandName }}</text>
			<!-- 居中问候（工作台） -->
			<view v-if="align==='center'" class="center">
				<text class="title">{{ title }}</text>
				<text class="subtitle" v-if="subtitle">{{ subtitle }}</text>
			</view>
			<!-- 左对齐大标题（列表页） -->
			<view v-else class="page-title">
				<text class="pt-title">{{ title }}</text>
				<text class="pt-sub" v-if="subtitle">{{ subtitle }}</text>
			</view>
			<view class="search-slot" v-if="align!=='center'"><slot name="search"></slot></view>
			<view class="right">
				<slot name="actions"></slot>
				<text class="date" v-if="align==='center'">{{ dateText }}</text>
				<view class="bell"><text>🔔</text><view class="dot"></view></view>
				<image class="avatar" :src="avatar" mode="aspectFill"></image>
			</view>
		</view>
		<!-- 主体：左导航 + 内容 -->
		<view class="body">
			<view class="nav">
				<view v-for="item in navs" :key="item.key" class="nav-item" :class="{active: item.key===active}"
					@click="go(item)">
					<view class="nav-icon">{{ item.icon }}</view>
					<text class="nav-label">{{ item.label }}</text>
				</view>
			</view>
			<scroll-view class="content" scroll-y>
				<slot></slot>
			</scroll-view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'sales-shell',
	props: {
		active: { type: String, default: 'workbench' },
		title: { type: String, default: '' },
		subtitle: { type: String, default: '' },
		align: { type: String, default: 'left' }
	},
	data() {
		return {
			brandName: '合意传媒',
			avatar: 'https://i.pravatar.cc/100?img=47',
			navs: [
				{ key: 'workbench', label: '工作台', icon: '🧩', url: '/pages/workbench/workbench' },
				{ key: 'customer', label: '客户', icon: '👥', url: '/pages/customer/customer' },
				{ key: 'order', label: '订单', icon: '🧾', url: '/pages/order/order' },
				{ key: 'material', label: '素材库', icon: '🎬', url: '/pages/material/material' },
				{ key: 'message', label: '消息', icon: '💬', url: '/pages/message/message' }
			]
		}
	},
	computed: {
		dateText() {
			const d = new Date()
			const w = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][d.getDay()]
			return `${d.getMonth() + 1}月${d.getDate()}日 ${w}`
		}
	},
	created() {
		const brand = uni.getStorageSync('brand')
		if (brand && brand.brandName) this.brandName = brand.brandName
		const name = uni.getStorageSync('empName')
	},
	methods: {
		go(item) {
			if (item.key === this.active) return
			uni.reLaunch({ url: item.url })
		}
	}
}
</script>

<style lang="scss" scoped>
.shell {
	width: 100%;
	height: 100vh;
	display: flex;
	flex-direction: column;
	background: $page-bg;
}

.topbar {
	height: 96rpx;
	background: #fff;
	border-bottom: 1rpx solid $line;
	display: flex;
	align-items: center;
	padding: 0 32rpx;
	flex-shrink: 0;
}
.brand {
	font-size: 34rpx;
	font-weight: 800;
	color: $ink;
	letter-spacing: 2rpx;
}
.center {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
}
.center .title {
	font-size: 30rpx;
	font-weight: 700;
	color: $ink;
}
.center .subtitle {
	font-size: 22rpx;
	color: $muted;
	margin-top: 2rpx;
}
.page-title {
	display: flex;
	flex-direction: column;
	margin-left: 36rpx;
}
.page-title .pt-title {
	font-size: 36rpx;
	font-weight: 800;
	color: $ink;
}
.page-title .pt-sub {
	font-size: 22rpx;
	color: $muted;
	margin-top: 2rpx;
}
.search-slot {
	flex: 1;
	display: flex;
	justify-content: center;
	padding: 0 24rpx;
}
.right {
	display: flex;
	align-items: center;
	gap: 24rpx;
}
.right .date {
	font-size: 24rpx;
	color: $muted;
}
.bell {
	position: relative;
	font-size: 30rpx;
}
.bell .dot {
	position: absolute;
	top: -2rpx;
	right: -2rpx;
	width: 12rpx;
	height: 12rpx;
	border-radius: 50%;
	background: $danger;
}
.avatar {
	width: 56rpx;
	height: 56rpx;
	border-radius: 50%;
	background: #eee;
}

.body {
	flex: 1;
	display: flex;
	overflow: hidden;
}
.nav {
	width: 132rpx;
	flex-shrink: 0;
	padding: 24rpx 16rpx;
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}
.nav-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 16rpx 0;
	border-radius: 20rpx;
	color: $muted;
}
.nav-item .nav-icon {
	font-size: 38rpx;
	line-height: 1;
}
.nav-item .nav-label {
	font-size: 22rpx;
	margin-top: 8rpx;
}
.nav-item.active {
	background: $brand;
	color: #fff;
	box-shadow: 0 8rpx 20rpx rgba(47, 107, 255, .28);
}
.nav-item.active .nav-label {
	color: #fff;
}

.content {
	flex: 1;
	height: 100%;
	padding: 24rpx 28rpx 40rpx;
}
</style>
