<template>
	<view class="article">
		<text class="title">{{ detail.title }}</text>
		<view class="meta">
			<text class="meta-brand">云漫 · 照相馆</text>
			<text class="meta-time">{{ fmt(detail.addtime) }}</text>
		</view>
		<view class="divider"></view>
		<view class="content">
			<rich-text :nodes="detail.content"></rich-text>
		</view>
	</view>
</template>
<script>
	export default {
		data() {
			return {
				detail: {},
				id: '',
			}
		},
		async onLoad(options) {
			this.id = options.id;
			let res = await this.$api.info('news', options.id)
			this.detail = res.data || {};
			if (this.detail.content) {
				this.detail.content = this.detail.content.replace(/<img/g, '<img style="width: 100%;border-radius:12rpx;margin:10rpx 0;"');
			}
		},
		methods: {
			fmt(d) {
				if (!d) return '';
				return d.toString().split(' ')[0];
			}
		}
	}
</script>

<style scoped>
	page {
		background: #ffffff;
	}

	.article {
		padding: 36rpx 40rpx 60rpx;
	}

	.title {
		display: block;
		font-size: 44rpx;
		font-weight: 700;
		color: #222222;
		line-height: 64rpx;
	}

	.meta {
		display: flex;
		align-items: center;
		margin-top: 22rpx;
	}

	.meta-brand {
		font-size: 24rpx;
		color: #B49A6B;
		font-weight: 600;
	}

	.meta-time {
		font-size: 24rpx;
		color: #bbbbbb;
		margin-left: 20rpx;
	}

	.divider {
		height: 1rpx;
		background: #eeeeee;
		margin: 28rpx 0;
	}

	.content {
		font-size: 30rpx;
		line-height: 52rpx;
		letter-spacing: 1rpx;
		color: #555555;
	}
</style>
