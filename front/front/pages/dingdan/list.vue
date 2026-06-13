<template>
	<view class="page">
		<!-- 状态筛选 -->
		<scroll-view class="status-bar" scroll-x>
			<view class="status-item" :class="{ active: status === '' }" @tap="onStatus('')">全部</view>
			<view class="status-item" :class="{ active: status === item }" v-for="(item, idx) in statusList" :key="idx"
				@tap="onStatus(item)">{{ item }}</view>
		</scroll-view>

		<view class="list">
			<view class="order-card" v-for="(item, idx) in orders" :key="item.id" @tap="goDetail(item.id)">
				<view class="order-head">
					<text class="order-no">单号 {{ item.dingdanbianhao }}</text>
					<text class="order-status" :class="item.statusCls">{{ item.zhuangtai }}</text>
				</view>
				<view class="order-body">
					<image class="order-img" :src="coverOf(item)" mode="aspectFill"></image>
					<view class="order-info">
						<text class="order-title">{{ item.taocanmingcheng }}</text>
						<text class="order-tag">{{ item.pinlei }} · {{ item.fengge }}</text>
						<view class="order-row" v-if="item.paiduixuhao && needQueue(item.zhuangtai)">
							<text class="queue">排队序号：第 {{ item.paiduixuhao }} 位</text>
						</view>
						<view class="order-row">
							<text class="order-date">意向档期：{{ fmt(item.yixiangdangqi) }}</text>
						</view>
						<view class="order-row" v-if="item.yugudangqi">
							<text class="order-date plan">预估档期：{{ fmt(item.yugudangqi) }}</text>
						</view>
					</view>
				</view>
				<view class="order-foot">
					<text class="price">￥{{ item.xianxiabiaojia }}</text>
					<view class="btns">
						<view class="lock-tag" v-if="item.jiaofeisuoding === '是' || item.jiaofeisuoding === '已缴费锁定档期'">已锁档</view>
						<button class="mini-btn" v-if="canCancel(item.zhuangtai)" @tap.stop="cancelOrder(item)">取消</button>
						<button class="mini-btn primary" v-if="item.zhuangtai === '成品已上线'" @tap.stop="goChengpin">看成品</button>
					</view>
				</view>
			</view>

			<view class="empty" v-if="orders.length === 0">
				<text>暂无订单</text>
			</view>
		</view>
	</view>
</template>

<script>
	import http from '@/api/http.js'
	export default {
		data() {
			return {
				orders: [],
				status: '',
				statusList: ['待排队', '已排期', '待拍摄', '拍摄完成', '制作中', '成品已上线', '已取消']
			};
		},
		computed: {
			baseUrl() {
				return this.$base.url;
			}
		},
		onShow() {
			this.loadOrders();
		},
		methods: {
			async loadOrders() {
				let params = {
					page: 1,
					limit: 100,
					sort: 'addtime',
					order: 'desc'
				};
				if (this.status) params.zhuangtai = this.status;
				let res = await this.$api.list('dingdan', params);
				let list = (res.data && res.data.list) || [];
				// 小程序端 :class 不支持方法调用，下发前预计算状态样式
				list.forEach(o => {
					o.statusCls = this.statusClass(o.zhuangtai);
				});
				this.orders = list;
			},
			onStatus(s) {
				this.status = s;
				this.loadOrders();
			},
			coverOf(item) {
				if (!item.fengmian) return '';
				return this.baseUrl + item.fengmian.split(',')[0];
			},
			fmt(d) {
				if (!d) return '待定';
				return d.toString().split(' ')[0];
			},
			needQueue(s) {
				return ['待排队', '已排期', '待拍摄'].indexOf(s) > -1;
			},
			canCancel(s) {
				return ['待排队', '已排期'].indexOf(s) > -1;
			},
			statusClass(s) {
				if (s === '已取消') return 'gray';
				if (s === '成品已上线') return 'green';
				return 'orange';
			},
			goDetail(id) {
				this.$utils.jump(`../dingdan/detail?id=${id}`);
			},
			goChengpin() {
				uni.switchTab({
					url: '../chengpin/list',
					fail: () => this.$utils.jump('../chengpin/list')
				});
			},
			cancelOrder(item) {
				let _this = this;
				uni.showModal({
					title: '取消预约',
					editable: true,
					placeholderText: '请填写取消原因（选填）',
					content: '确认取消该预约？已缴费锁档的订单将按门店扣费规则处理。',
					success: async function(res) {
						if (res.confirm) {
							let reason = res.content || '用户主动取消';
							await http.post(`dingdan/cancel?id=${item.id}&reason=${encodeURIComponent(reason)}`, {});
							_this.$utils.msg('已取消');
							_this.loadOrders();
						}
					}
				});
			}
		}
	};
</script>

<style lang="scss" scoped>
	page {
		background: #f6f6f6;
	}

	.status-bar {
		white-space: nowrap;
		background: #fff;
		padding: 20rpx 20rpx;
		position: sticky;
		top: 0;
		z-index: 10;
	}

	.status-item {
		display: inline-block;
		font-size: 26rpx;
		color: #666;
		padding: 8rpx 28rpx;
		margin-right: 12rpx;
		border-radius: 30rpx;
		background: #f3f3f3;
	}

	.status-item.active {
		background: $brand-primary;
		color: #fff;
		font-weight: 600;
	}

	.list {
		padding: 20rpx 24rpx;
	}

	.order-card {
		background: #fff;
		border-radius: 20rpx;
		padding: 24rpx;
		margin-bottom: 20rpx;
	}

	.order-head {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding-bottom: 18rpx;
		border-bottom: 1rpx solid #f3f3f3;
	}

	.order-no {
		font-size: 24rpx;
		color: #999;
	}

	.order-status {
		font-size: 26rpx;
		font-weight: 600;
	}

	.order-status.orange {
		color: #C99A3B;
	}

	.order-status.green {
		color: $brand-cta-deep;
	}

	.order-status.gray {
		color: #bbb;
	}

	.order-body {
		display: flex;
		padding: 20rpx 0;
	}

	.order-img {
		width: 160rpx;
		height: 160rpx;
		border-radius: 12rpx;
		margin-right: 20rpx;
		background: #eee;
	}

	.order-info {
		flex: 1;
		display: flex;
		flex-direction: column;
	}

	.order-title {
		font-size: 30rpx;
		font-weight: 600;
		color: #333;
	}

	.order-tag {
		font-size: 24rpx;
		color: #999;
		margin: 8rpx 0;
	}

	.order-row {
		margin-top: 4rpx;
	}

	.queue {
		font-size: 24rpx;
		color: $brand-primary-deep;
	}

	.order-date {
		font-size: 24rpx;
		color: #666;
	}

	.order-date.plan {
		color: #409eff;
	}

	.order-foot {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding-top: 18rpx;
		border-top: 1rpx solid #f3f3f3;
	}

	.price {
		color: $brand-price;
		font-size: 30rpx;
		font-weight: 700;
	}

	.btns {
		display: flex;
		align-items: center;
	}

	.lock-tag {
		font-size: 22rpx;
		color: $brand-cta-deep;
		border: 1rpx solid $brand-cta-deep;
		border-radius: 8rpx;
		padding: 2rpx 12rpx;
		margin-right: 16rpx;
	}

	.mini-btn {
		font-size: 24rpx;
		color: #666;
		background: #f3f3f3;
		border-radius: 30rpx;
		padding: 0 28rpx;
		height: 56rpx;
		line-height: 56rpx;
		margin-left: 16rpx;
	}

	.mini-btn.primary {
		background: $brand-cta;
		color: #fff;
	}

	.empty {
		text-align: center;
		color: #bbb;
		font-size: 26rpx;
		padding: 120rpx 0;
	}
</style>
