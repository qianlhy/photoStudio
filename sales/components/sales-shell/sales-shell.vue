<template>
	<view class="shell" :class="{ 'shell-center': align === 'center' }">
		<!-- 顶栏 -->
		<view class="topbar">
			<view class="brand-wrap">
				<view class="brand-mark">合</view>
				<text class="brand">{{ brandName }}</text>
			</view>
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
				<view class="bell" aria-label="消息通知"><view class="bell-shape"></view><view class="dot"></view></view>
				<image class="avatar" :src="avatar" mode="aspectFill"></image>
			</view>
		</view>
		<!-- 主体：左导航 + 内容 -->
		<view class="body">
			<view class="nav">
				<view v-for="item in navs" :key="item.key" class="nav-item" :class="{active: item.key===active}"
					@click="go(item)">
					<view class="nav-icon" :class="'icon-' + item.key"><view class="icon-core"></view></view>
					<text class="nav-label">{{ item.label }}</text>
				</view>
			</view>
			<view class="content">
				<slot></slot>
			</view>
		</view>
		<dev-portrait-toggle />
	</view>
</template>

<script>
import devPortraitToggle from '@/components/dev-portrait-toggle/dev-portrait-toggle.vue'
export default {
	name: 'sales-shell',
	components: { devPortraitToggle },
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
				{ key: 'workbench', label: '工作台', url: '/pages/workbench/workbench' },
				{ key: 'customer', label: '客户', url: '/pages/customer/customer' },
				{ key: 'order', label: '订单', url: '/pages/order/order' },
				{ key: 'material', label: '素材库', url: '/pages/material/library' },
				{ key: 'message', label: '消息', url: '/pages/message/message' }
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
	height: 112rpx;
	background: #fff;
	border-bottom: 1rpx solid $line;
	display: flex;
	align-items: center;
	padding: 0 40rpx;
	flex-shrink: 0;
	box-shadow: 0 1rpx 0 rgba(31, 39, 51, .025);
}
.brand {
	font-size: 34rpx;
	font-weight: 700;
	color: $ink;
	letter-spacing: 2rpx;
}
.brand-wrap {
	display: flex;
	align-items: center;
	flex-shrink: 0;
}
.brand-mark {
	display: none;
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
	font-weight: 700;
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
	width: 42rpx;
	height: 42rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	color: #667085;
}
.bell-shape {
	position: relative;
	width: 19rpx;
	height: 22rpx;
	border: 3rpx solid currentColor;
	border-top-left-radius: 12rpx;
	border-top-right-radius: 12rpx;
	border-bottom: 0;
}
.bell-shape::before {
	content: "";
	position: absolute;
	left: -6rpx;
	bottom: -5rpx;
	width: 25rpx;
	height: 3rpx;
	border-radius: 3rpx;
	background: currentColor;
}
.bell-shape::after {
	content: "";
	position: absolute;
	left: 6rpx;
	bottom: -10rpx;
	width: 7rpx;
	height: 7rpx;
	border-radius: 50%;
	background: currentColor;
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
	border: 2rpx solid #fff;
	box-shadow: 0 3rpx 12rpx rgba(31, 39, 51, .12);
}

.body {
	flex: 1;
	display: flex;
	overflow: hidden;
}
.nav {
	width: 150rpx;
	flex-shrink: 0;
	margin: 28rpx 0 28rpx 28rpx;
	padding: 24rpx 14rpx;
	background: #fff;
	border-radius: 28rpx;
	border: 1rpx solid rgba(31, 39, 51, .035);
	box-shadow: 0 6rpx 24rpx rgba(31, 39, 51, .055);
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}
.nav-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 22rpx 0;
	border-radius: 24rpx;
	color: $muted;
	transition: background-color .18s ease, color .18s ease, transform .18s ease;
}
.nav-item .nav-icon {
	position: relative;
	width: 40rpx;
	height: 40rpx;
	color: #98A2B3;
}
.nav-icon .icon-core,
.nav-icon::before,
.nav-icon::after,
.nav-icon .icon-core::before,
.nav-icon .icon-core::after {
	position: absolute;
	box-sizing: border-box;
	content: "";
}
.nav-icon .icon-core {
	left: 0;
	right: 0;
	top: 0;
	bottom: 0;
}
.icon-workbench::before, .icon-workbench::after,
.icon-workbench .icon-core::before, .icon-workbench .icon-core::after {
	width: 15rpx;
	height: 15rpx;
	border: 3rpx solid currentColor;
	border-radius: 4rpx;
}
.icon-workbench::before { left: 2rpx; top: 2rpx; }
.icon-workbench::after { right: 2rpx; top: 2rpx; }
.icon-workbench .icon-core::before { left: 2rpx; bottom: 2rpx; }
.icon-workbench .icon-core::after { right: 2rpx; bottom: 2rpx; }
.icon-customer::before {
	left: 14rpx; top: 2rpx; width: 14rpx; height: 14rpx;
	border: 3rpx solid currentColor; border-radius: 50%;
}
.icon-customer::after {
	left: 8rpx; bottom: 2rpx; width: 26rpx; height: 17rpx;
	border: 3rpx solid currentColor; border-radius: 16rpx 16rpx 6rpx 6rpx;
}
.icon-order::before {
	left: 7rpx; top: 1rpx; width: 27rpx; height: 36rpx;
	border: 3rpx solid currentColor; border-radius: 4rpx;
}
.icon-order::after {
	left: 14rpx; top: 11rpx; width: 14rpx; height: 3rpx;
	background: currentColor; box-shadow: 0 8rpx 0 currentColor, 0 16rpx 0 currentColor;
}
.icon-material::before {
	left: 3rpx; top: 6rpx; width: 34rpx; height: 28rpx;
	border: 3rpx solid currentColor; border-radius: 5rpx;
}
.icon-material::after {
	left: 16rpx; top: 14rpx;
	border-left: 10rpx solid currentColor;
	border-top: 6rpx solid transparent;
	border-bottom: 6rpx solid transparent;
}
.icon-message::before {
	left: 3rpx; top: 5rpx; width: 34rpx; height: 27rpx;
	border: 3rpx solid currentColor; border-radius: 8rpx;
}
.icon-message::after {
	left: 10rpx; bottom: 2rpx; width: 10rpx; height: 10rpx;
	border-left: 3rpx solid currentColor; transform: skewY(-35deg);
}
.nav-item .nav-label {
	font-size: 22rpx;
	margin-top: 10rpx;
}
.nav-item.active {
	background: $brand;
	color: #fff;
	box-shadow: 0 10rpx 24rpx rgba(47, 107, 255, .24);
}
.nav-item.active .nav-icon {
	color: #fff;
}
.nav-item.active .nav-label {
	color: #fff;
}

.content {
	flex: 1;
	min-width: 0;
	min-height: 0;
	height: 100%;
	overflow: hidden;
	padding: 28rpx 36rpx;
	box-sizing: border-box;
	display: flex;
	flex-direction: column;
}

/*
 * Pad 横屏：设计基准 1280×800（16:10）
 */
@include pad-landscape {
	.topbar {
		height: 10vh;
		padding: 0 2.1vw;
	}
	.brand {
		font-size: clamp(20px, 1.7vw, 27px);
	}
	.page-title {
		margin-left: 1.2vw;
		max-width: 16vw;
	}
	.page-title .pt-title {
		font-size: clamp(18px, 1.55vw, 25px);
		line-height: 1.2;
	}
	.page-title .pt-sub {
		font-size: clamp(11px, .85vw, 14px);
	}
	.center .title {
		font-size: clamp(18px, 1.55vw, 25px);
	}
	.center .subtitle {
		font-size: clamp(12px, .92vw, 15px);
	}
	.right {
		gap: 1.25vw;
	}
	.right .date {
		font-size: clamp(12px, .95vw, 15px);
	}
	.bell {
		width: clamp(26px, 2.2vw, 34px);
		height: clamp(26px, 2.2vw, 34px);
	}
	.avatar {
		width: clamp(34px, 3vw, 46px);
		height: clamp(34px, 3vw, 46px);
	}
	.nav {
		width: 8.9vw;
		box-sizing: border-box;
		margin: .5vh 0 1.2vh .8vw;
		padding: 2vh .7vw;
		border-radius: 18px;
		gap: 1.1vh;
	}
	.nav-item {
		padding: 1.5vh 0;
		border-radius: 14px;
	}
	.nav-item .nav-icon {
		transform: scale(.88);
	}
	.nav-item .nav-label {
		font-size: clamp(12px, .95vw, 15px);
		margin-top: .7vh;
	}
	.content {
		padding: .5vh 2vw 1.2vh .65vw;
	}
}

/*
 * Pad 竖屏：顶栏精简 + 底栏五导航（对齐 1800×2880 设计）
 * 不改横屏 DOM/业务，仅样式
 */
@include pad-portrait {
	.shell {
		height: 100vh;
		background: $page-bg;
	}
	.topbar {
		height: auto;
		min-height: p-px(52);
		padding: p-px(12) $p-pad-x p-px(8);
		flex-wrap: nowrap;
		align-items: center;
		background: #fff;
		border-bottom: 1px solid $line;
		box-shadow: none;
	}
	/* 工作台竖屏：顶栏只留品牌 + 铃铛，问候下沉到页面内容 */
	.shell-center .topbar {
		background: transparent;
		border-bottom: none;
		padding: p-px(14) $p-pad-x p-px(4);
		min-height: p-px(48);
	}
	.shell-center .center {
		display: none;
	}
	.brand-wrap {
		gap: p-px(10);
	}
	.brand-mark {
		display: flex;
		align-items: center;
		justify-content: center;
		width: p-px(32);
		height: p-px(32);
		border-radius: 50%;
		background: $brand;
		color: #fff;
		font-size: p-px(15);
		font-weight: 700;
		letter-spacing: 0;
		flex-shrink: 0;
	}
	.brand {
		font-size: p-px(18);
		font-weight: 700;
		color: $ink;
		flex-shrink: 0;
		letter-spacing: 1px;
	}
	.page-title {
		margin-left: p-px(16);
		max-width: p-px(280);
		flex: 1;
		min-width: 0;
	}
	.page-title .pt-title {
		font-size: p-px(18);
		font-weight: 600;
	}
	.page-title .pt-sub {
		display: none;
	}
	.center {
		flex: 1;
		min-width: 0;
		margin-left: p-px(16);
		align-items: flex-start;
	}
	.center .title {
		font-size: p-px(20);
		font-weight: 700;
		text-align: left;
	}
	.center .subtitle {
		font-size: p-px(12);
		color: $muted;
		text-align: left;
		margin-top: p-px(2);
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
		max-width: p-px(420);
	}
	.search-slot {
		order: 0;
		flex: 1;
		width: auto;
		max-width: p-px(360);
		padding: 0;
		justify-content: flex-end;
		margin: 0 p-px(16);
	}
	.right {
		gap: p-px(16);
		flex-shrink: 0;
		margin-left: auto;
	}
	.right .date {
		display: none;
	}
	.bell {
		width: p-px(28);
		height: p-px(28);
	}
	.avatar {
		width: p-px(40);
		height: p-px(40);
		border-radius: 50%;
	}
	.body {
		flex-direction: column;
		position: relative;
		padding-bottom: calc(#{$p-nav-h} + env(safe-area-inset-bottom));
		box-sizing: border-box;
		min-height: 0;
	}
	.nav {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 100;
		width: auto;
		height: calc(#{$p-nav-h} + env(safe-area-inset-bottom));
		margin: 0;
		padding: p-px(8) $p-pad-x calc(#{p-px(10)} + env(safe-area-inset-bottom));
		border-radius: p-px(16) p-px(16) 0 0;
		flex-direction: row;
		justify-content: space-between;
		align-items: center;
		gap: 0;
		box-sizing: border-box;
		background: #fff;
		box-shadow: 0 -4px 20px rgba(32, 41, 56, .06);
		border-top: 1px solid $line;
		border-left: none;
		border-right: none;
		border-bottom: none;
	}
	.nav-item {
		flex: 1;
		height: 100%;
		max-width: $p-nav-gap;
		padding: 0;
		margin: 0 auto;
		border-radius: p-px(10);
		gap: p-px(4);
		background: transparent !important;
		box-shadow: none !important;
		color: $muted;
	}
	.nav-item.active {
		background: transparent !important;
		box-shadow: none !important;
		color: $brand;
	}
	.nav-item.active .nav-label {
		color: $brand !important;
		font-weight: 600;
	}
	.nav-item.active .icon-core,
	.nav-item.active .nav-icon,
	.nav-item.active .nav-icon::before,
	.nav-item.active .nav-icon::after {
		color: $brand !important;
	}
	.right .avatar {
		display: none;
	}
	.nav-item .nav-icon {
		width: p-px(24);
		height: p-px(24);
		transform: none;
		color: #98A2B3;
	}
	.nav-item .nav-label {
		font-size: p-px(12);
		line-height: 1.2;
		margin-top: 0;
		color: $muted;
	}
	/* 图标用固定 px，避免 rpx 在宽屏浏览器里被撑乱 */
	.icon-workbench::before, .icon-workbench::after,
	.icon-workbench .icon-core::before, .icon-workbench .icon-core::after {
		width: 8px;
		height: 8px;
		border-width: 2px;
		border-radius: 2px;
	}
	.icon-customer::before {
		left: 7px; top: 0; width: 8px; height: 8px; border-width: 2px;
	}
	.icon-customer::after {
		left: 3px; bottom: 0; width: 16px; height: 10px; border-width: 2px;
	}
	.icon-order::before {
		left: 4px; top: 0; width: 14px; height: 18px; border-width: 2px;
	}
	.icon-order::after {
		left: 7px; top: 5px; width: 8px; height: 2px;
		box-shadow: 0 4px 0 currentColor, 0 8px 0 currentColor;
	}
	.icon-material::before {
		left: 1px; top: 3px; width: 20px; height: 16px; border-width: 2px;
	}
	.icon-material::after {
		left: 8px; top: 7px;
		border-left-width: 7px;
		border-top-width: 4px;
		border-bottom-width: 4px;
	}
	.icon-message::before {
		left: 1px; top: 2px; width: 20px; height: 15px; border-width: 2px;
	}
	.icon-message::after {
		left: 5px; bottom: 0; width: 6px; height: 6px; border-left-width: 2px;
	}
	.content {
		flex: 1;
		width: 100%;
		padding: p-px(12) $p-pad-x p-px(16);
		overflow-y: auto;
		box-sizing: border-box;
	}
}
</style>
