<template>
	<view class="page">
		<view class="card">
			<view class="store-name">{{ cfg.storeName || '照相馆' }}</view>
			<view class="store-row"><text class="lbl">地址</text><text class="val">{{ cfg.storeAddress }}</text></view>
			<view class="store-row"><text class="lbl">营业时间</text><text class="val">{{ cfg.storeBusinessHours }}</text></view>
			<view class="store-row"><text class="lbl">联系电话</text><text class="val phone" @tap="call">{{ cfg.storePhone }}</text></view>
			<button class="nav-btn" v-if="cfg.storeLng && cfg.storeLat" @tap="openMap">导航到店</button>
		</view>

		<view class="card">
			<view class="sec-title">取消与扣费规则</view>
			<text class="rule">{{ cfg.cancelRule || '暂未设置' }}</text>
		</view>

		<view class="card">
			<view class="sec-title">排队规则</view>
			<text class="rule">{{ cfg.queueRule || '暂未设置' }}</text>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				cfg: {}
			};
		},
		async onLoad() {
			let res = await this.$api.page('config', {
				page: 1,
				limit: 100
			});
			let map = {};
			((res.data && res.data.list) || []).forEach(i => {
				map[i.name] = i.value;
			});
			this.cfg = map;
		},
		methods: {
			call() {
				if (this.cfg.storePhone) {
					uni.makePhoneCall({
						phoneNumber: this.cfg.storePhone
					});
				}
			},
			openMap() {
				uni.openLocation({
					latitude: parseFloat(this.cfg.storeLat),
					longitude: parseFloat(this.cfg.storeLng),
					name: this.cfg.storeName || '照相馆',
					address: this.cfg.storeAddress || ''
				});
			}
		}
	};
</script>

<style lang="scss" scoped>
	page {
		background: $brand-bg-soft;
	}

	.page {
		padding: 20rpx 24rpx;
	}

	.card {
		background: #fff;
		border-radius: $brand-radius-lg;
		padding: 28rpx 30rpx;
		margin-bottom: 20rpx;
		box-shadow: $brand-shadow-card;
	}

	.store-name {
		font-size: 36rpx;
		font-weight: 700;
		color: #333;
		margin-bottom: 18rpx;
	}

	.store-row {
		display: flex;
		padding: 12rpx 0;
	}

	.lbl {
		width: 140rpx;
		color: #999;
		font-size: 26rpx;
	}

	.val {
		flex: 1;
		color: #333;
		font-size: 26rpx;
	}

	.phone {
		color: $brand-primary-deep;
	}

	.nav-btn {
		margin-top: 24rpx;
		height: 80rpx;
		line-height: 80rpx;
		background: linear-gradient(135deg, #C4AB7C, #B49A6B);
		color: #fff;
		font-size: 28rpx;
		border-radius: 40rpx;
		border: none;
	}

	.sec-title {
		font-size: 30rpx;
		font-weight: 700;
		color: $brand-ink;
		margin-bottom: 18rpx;
		padding-left: 16rpx;
		border-left: 6rpx solid $brand-primary;
	}

	.rule {
		font-size: 26rpx;
		color: #666;
		line-height: 44rpx;
	}
</style>
