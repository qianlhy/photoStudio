<template>
	<view class="page">
		<view class="list">
			<view class="msg-card" :class="{ unread: item.isread !== '是' }" v-for="(item, idx) in list" :key="item.id"
				@tap="readMsg(item)">
				<view class="msg-head">
					<view class="msg-title">
						<text class="dot" v-if="item.isread !== '是'"></text>
						<text>{{ item.biaoti }}</text>
					</view>
					<text class="msg-type">{{ item.leixing }}</text>
				</view>
				<view class="msg-content">{{ item.neirong }}</view>
				<text class="msg-time">{{ item.addtime }}</text>
			</view>
			<view class="empty" v-if="list.length === 0">
				<text>暂无消息</text>
			</view>
		</view>
	</view>
</template>

<script>
	import http from '@/api/http.js'
	export default {
		data() {
			return {
				list: []
			};
		},
		onShow() {
			this.loadList();
		},
		methods: {
			async loadList() {
				let res = await this.$api.list('message', {
					page: 1,
					limit: 100,
					sort: 'addtime',
					order: 'desc'
				});
				this.list = (res.data && res.data.list) || [];
			},
			async readMsg(item) {
				if (item.isread === '是') return;
				try {
					await http.get(`message/read/${item.id}`, {});
					item.isread = '是';
				} catch (e) {}
			}
		}
	};
</script>

<style lang="scss" scoped>
	page {
		background: $brand-bg-soft;
	}

	.list {
		padding: 20rpx 24rpx;
	}

	.msg-card {
		background: #fff;
		border-radius: $brand-radius-lg;
		padding: 24rpx;
		margin-bottom: 18rpx;
		box-shadow: $brand-shadow-card;
	}

	.msg-card.unread {
		border-left: 6rpx solid $brand-primary;
	}

	.msg-head {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.msg-title {
		font-size: 30rpx;
		font-weight: 600;
		color: #333;
		display: flex;
		align-items: center;
	}

	.dot {
		width: 14rpx;
		height: 14rpx;
		border-radius: 50%;
		background: #ff6364;
		margin-right: 12rpx;
	}

	.msg-type {
		font-size: 22rpx;
		color: $brand-primary-deep;
		background: $brand-primary-soft;
		border-radius: 8rpx;
		padding: 2rpx 12rpx;
	}

	.msg-content {
		font-size: 26rpx;
		color: #666;
		line-height: 40rpx;
		margin: 14rpx 0;
	}

	.msg-time {
		font-size: 22rpx;
		color: #bbb;
	}

	.empty {
		text-align: center;
		color: #bbb;
		font-size: 26rpx;
		padding: 120rpx 0;
	}
</style>
