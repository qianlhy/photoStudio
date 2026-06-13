<template>
	<view class="page">
		<view class="hd">
			<text class="hd-title">告诉我们你的拍摄偏好</text>
			<text class="hd-sub">我们会优先为你推荐更合适的套餐</text>
		</view>

		<view class="card">
			<view class="sec-title">意向拍摄品类</view>
			<view class="chip-wrap">
				<view class="chip" :class="{ active: pinlei === '写真' }" @tap="pinlei = '写真'">写真</view>
				<view class="chip" :class="{ active: pinlei === '宣传片' }" @tap="pinlei = '宣传片'">宣传片</view>
				<view class="chip" :class="{ active: pinlei === '都看看' }" @tap="pinlei = '都看看'">都看看</view>
			</view>
		</view>

		<view class="card">
			<view class="sec-title">偏好风格（可多选）</view>
			<view class="chip-wrap">
				<view class="chip" :class="{ active: selected.indexOf(item.leixing) > -1 }"
					v-for="(item, idx) in fenggeList" :key="idx" @tap="toggle(item.leixing)">{{ item.leixing }}</view>
			</view>
		</view>

		<button class="save-btn" @tap="save">保存偏好</button>
		<view class="skip" @tap="skip">暂时跳过</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				user: {},
				pinlei: '',
				selected: [],
				fenggeList: []
			};
		},
		async onLoad() {
			let table = uni.getStorageSync('nowTable');
			if (table) {
				try {
					let res = await this.$api.session(table);
					this.user = res.data;
					this.pinlei = this.user.yixiangpinlei || '';
					if (this.user.pianhao) {
						this.selected = this.user.pianhao.split(',').filter(i => i);
					}
				} catch (e) {}
			}
			this.loadFengge();
		},
		watch: {
			pinlei() { this.loadFengge(); }
		},
		methods: {
			async loadFengge() {
				let params = { page: 1, limit: 100 };
				if (this.pinlei && this.pinlei !== '都看看') params.pinlei = this.pinlei;
				let res = await this.$api.list('leixing', params);
				this.fenggeList = (res.data && res.data.list) || [];
			},
			toggle(name) {
				let idx = this.selected.indexOf(name);
				if (idx > -1) {
					this.selected.splice(idx, 1);
				} else {
					this.selected.push(name);
				}
			},
			async save() {
				if (!this.user || !this.user.id) {
					this.$utils.msg('请先登录');
					return;
				}
				let table = uni.getStorageSync('nowTable');
				await this.$api.update(table, {
					id: this.user.id,
					yixiangpinlei: this.pinlei,
					pianhao: this.selected.join(',')
				});
				this.$utils.msg('偏好已保存');
				setTimeout(() => {
					uni.switchTab({
						url: '../index/index',
						fail: () => this.$utils.jump('../index/index')
					});
				}, 800);
			},
			skip() {
				uni.switchTab({
					url: '../index/index',
					fail: () => this.$utils.jump('../index/index')
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
		padding: 30rpx 24rpx;
	}

	.hd {
		padding: 20rpx 10rpx 30rpx;
	}

	.hd-title {
		display: block;
		font-size: 40rpx;
		font-weight: 700;
		color: #333;
	}

	.hd-sub {
		display: block;
		font-size: 26rpx;
		color: #999;
		margin-top: 10rpx;
	}

	.card {
		background: #fff;
		border-radius: 20rpx;
		padding: 28rpx 24rpx;
		margin-bottom: 24rpx;
	}

	.sec-title {
		font-size: 30rpx;
		font-weight: 700;
		color: $brand-ink;
		margin-bottom: 24rpx;
		padding-left: 16rpx;
		border-left: 6rpx solid $brand-primary;
	}

	.chip-wrap {
		display: flex;
		flex-wrap: wrap;
	}

	.chip {
		font-size: 26rpx;
		color: #666;
		background: #f3f3f3;
		border-radius: 36rpx;
		padding: 14rpx 36rpx;
		margin: 0 18rpx 18rpx 0;
	}

	.chip.active {
		background: $brand-primary;
		color: #fff;
		font-weight: 600;
	}

	.save-btn {
		margin-top: 20rpx;
		height: 88rpx;
		line-height: 88rpx;
		background: linear-gradient(135deg, #B49A6B 0%, #A6885A 100%);
		color: #fff;
		font-size: 32rpx;
		font-weight: 700;
		border-radius: 44rpx;
	}

	.save-btn::after {
		border: none;
	}

	.skip {
		text-align: center;
		color: #aaa;
		font-size: 26rpx;
		padding: 30rpx 0;
	}
</style>
