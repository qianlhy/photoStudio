<template>
	<view class="client-tabbar">
		<view class="tab-pill" :class="{ active: active === 'service' }" @tap="goService">
			<view class="service-icon">
				<view class="service-play"></view>
			</view>
			<text>服务</text>
		</view>
		<view class="tab-state">
			<view class="state-dot" :class="active"></view>
			<text>{{ stateText }}</text>
		</view>
		<view class="tab-pill" :class="{ active: active === 'content' }" @tap="goContent">
			<view class="content-icon">
				<view class="content-play"></view>
			</view>
			<text>内容</text>
		</view>
	</view>
</template>

<script>
export default {
	name: 'client-tabbar',
	props: {
		active: { type: String, default: 'service' },
		stateText: { type: String, default: '制作中' }
	},
	methods: {
		goService() {
			if (this.active !== 'service') uni.redirectTo({ url: '/pages/hy-service/service' })
		},
		goContent() {
			if (this.active !== 'content') uni.redirectTo({ url: '/pages/hy-content/content' })
		}
	}
}
</script>

<style lang="scss" scoped>
.client-tabbar {
	position: fixed;
	z-index: 30;
	left: 28rpx;
	right: 28rpx;
	bottom: calc(18rpx + env(safe-area-inset-bottom));
	height: 103rpx;
	padding: 8rpx;
	box-sizing: border-box;
	display: flex;
	align-items: center;
	justify-content: space-between;
	border: 2rpx solid rgba(255,255,255,.9);
	border-radius: 58rpx;
	background: rgba(255,255,255,.82);
	box-shadow: 0 12rpx 36rpx rgba(45,52,79,.12);
	backdrop-filter: blur(18rpx);
}
.tab-pill {
	width: 241rpx;
	height: 84rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 16rpx;
	border-radius: 46rpx;
	font-size: 28rpx;
	color: #8190A4;
}
.tab-pill.active {
	color: #fff;
	font-weight: 600;
	background: linear-gradient(100deg,#50D6D1,#30C5E3);
	box-shadow: 0 8rpx 20rpx rgba(51,203,211,.24);
}
.tab-pill.active:last-child {
	background: linear-gradient(100deg,#A595F6,#9C89F4);
	box-shadow: 0 8rpx 20rpx rgba(135,111,236,.24);
}
.tab-state {
	min-width: 90rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 4rpx;
	color: #9DA8B7;
	font-size: 20rpx;
}
.state-dot {
	position: relative;
	width: 38rpx;
	height: 38rpx;
	border-radius: 12rpx;
	background: #54D1CB;
	box-shadow: 0 6rpx 14rpx rgba(84,209,203,.25);
}
.state-dot.content { background:#A692F4; box-shadow:0 6rpx 14rpx rgba(166,146,244,.25); }
.state-dot::before,.state-dot::after { content:""; position:absolute; background:#fff; border-radius:2rpx; }
.state-dot::before { left:10rpx; top:11rpx; width:18rpx; height:3rpx; box-shadow:0 6rpx 0 #fff,0 12rpx 0 #fff; }
.state-dot::after { display:none; }
.service-icon,.content-icon {
	position:relative;
	width:46rpx;
	height:38rpx;
	border-radius:8rpx;
	background:rgba(255,255,255,.28);
}
.service-icon::before {
	content:"";
	position:absolute;
	left:6rpx; right:6rpx; top:-6rpx;
	height:9rpx;
	border-radius:5rpx;
	background:rgba(255,255,255,.75);
	transform:rotate(-8deg);
}
.service-play,.content-play {
	position:absolute;
	left:18rpx; top:10rpx;
	border-left:12rpx solid currentColor;
	border-top:8rpx solid transparent;
	border-bottom:8rpx solid transparent;
}
.content-icon { border:3rpx solid currentColor; background:rgba(255,255,255,.2); }
.content-icon::before,.content-icon::after {
	content:""; position:absolute; top:-8rpx; width:3rpx; height:8rpx; background:currentColor;
}
.content-icon::before { left:9rpx; transform:rotate(-20deg); }
.content-icon::after { right:9rpx; transform:rotate(20deg); }
</style>
