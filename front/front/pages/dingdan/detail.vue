<template>
	<view class="page" v-if="detail.id">
		<!-- 状态条 -->
		<view class="status-banner" :class="statusClassName">
			<text class="status-text">{{ detail.zhuangtai }}</text>
			<text class="status-sub" v-if="needQueue(detail.zhuangtai)">当前排队序号：第 {{ detail.paiduixuhao }} 位</text>
			<text class="status-sub" v-else-if="detail.zhuangtai === '成品已上线'">您的成品已上线，可前往成品专区查看</text>
		</view>

		<!-- 套餐快照 -->
		<view class="card body">
			<image class="cover" :src="coverOf(detail)" mode="aspectFill"></image>
			<view class="info">
				<text class="title">{{ detail.taocanmingcheng }}</text>
				<text class="tag">{{ detail.pinlei }} · {{ detail.fengge }}</text>
				<text class="price">￥{{ detail.xianxiabiaojia }}</text>
			</view>
		</view>

		<!-- 订单信息 -->
		<view class="card">
			<view class="row"><text class="label">订单编号</text><text class="val">{{ detail.dingdanbianhao }}</text></view>
			<view class="row"><text class="label">下单时间</text><text class="val">{{ detail.addtime }}</text></view>
			<view class="row"><text class="label">联系人</text><text class="val">{{ detail.xingming }}</text></view>
			<view class="row"><text class="label">手机号</text><text class="val">{{ detail.shoujihaoma }}</text></view>
			<view class="row"><text class="label">拍摄人数</text><text class="val">{{ detail.paisherenshu }} 人</text></view>
			<view class="row"><text class="label">意向档期</text><text class="val">{{ fmt(detail.yixiangdangqi) }}</text></view>
			<view class="row" v-if="detail.yugudangqi"><text class="label">预估档期</text><text class="val plan">{{ fmt(detail.yugudangqi) }}</text></view>
			<view class="row"><text class="label">缴费锁档</text><text class="val">{{ detail.jiaofeisuoding }}</text></view>
			<view class="row col" v-if="detail.beizhu"><text class="label">个性化需求</text><text class="val full">{{ detail.beizhu }}</text></view>
			<view class="row col" v-if="detail.quxiaoyuanyin"><text class="label">取消原因</text><text class="val full">{{ detail.quxiaoyuanyin }}</text></view>
		</view>

		<view class="rule-tip">缴费即锁定档期；取消/排队规则以门店须知为准。</view>

		<view class="footbar" v-if="canCancel(detail.zhuangtai) || detail.zhuangtai === '成品已上线' || canReschedule(detail.zhuangtai)">
			<button class="btn ghost" v-if="canReschedule(detail.zhuangtai)" @tap="contactReschedule">联系客服改期</button>
			<button class="btn cancel" v-if="canCancel(detail.zhuangtai)" @tap="cancelOrder">取消预约</button>
			<button class="btn primary" v-if="detail.zhuangtai === '成品已上线'" @tap="goChengpin">查看成品</button>
		</view>
	</view>
</template>

<script>
	import http from '@/api/http.js'
	export default {
		data() {
			return {
				id: '',
				detail: {}
			};
		},
		computed: {
			baseUrl() {
				return this.$base.url;
			},
			// 小程序端 :class 不支持方法调用，改用计算属性
			statusClassName() {
				let s = this.detail.zhuangtai;
				if (s === '已取消') return 'gray';
				if (s === '成品已上线') return 'green';
				return 'orange';
			}
		},
		onLoad(options) {
			this.id = options.id;
		},
		onShow() {
			if (this.id) this.loadDetail();
		},
		methods: {
			async loadDetail() {
				let res = await this.$api.info('dingdan', this.id);
				this.detail = res.data || {};
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
			canReschedule(s) {
				return ['待排队', '已排期', '待拍摄'].indexOf(s) > -1;
			},
			statusClass(s) {
				if (s === '已取消') return 'gray';
				if (s === '成品已上线') return 'green';
				return 'orange';
			},
			goChengpin() {
				uni.switchTab({
					url: '../chengpin/list',
					fail: () => this.$utils.jump('../chengpin/list')
				});
			},
			cancelOrder() {
				let _this = this;
				uni.showModal({
					title: '取消预约',
					editable: true,
					placeholderText: '请填写取消原因（选填）',
					content: '确认取消该预约？已缴费锁档的订单将按门店扣费规则处理。',
					success: async function(res) {
						if (res.confirm) {
							let reason = res.content || '用户主动取消';
							await http.post(`dingdan/cancel?id=${_this.id}&reason=${encodeURIComponent(reason)}`, {});
							_this.$utils.msg('已取消');
							_this.loadDetail();
						}
					}
				});
			},
			contactReschedule() {
				uni.showModal({
					title: '联系客服改期',
					editable: true,
					placeholderText: '请说明希望调整到的日期/时段',
					success: async (r) => {
						if (!r.confirm) return;
						try {
							await http.get('message/feedback', {
								content: r.content || '申请改期',
								orderNo: this.detail.dingdanbianhao,
								leixing: '档期'
							});
							this.$utils.msg('改期申请已提交');
						} catch (e) {}
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

	.page {
		padding-bottom: 140rpx;
	}

	.status-banner {
		padding: 40rpx 40rpx;
		color: #fff;
		display: flex;
		flex-direction: column;
	}

	.status-banner.orange {
		background: linear-gradient(135deg, #C9B796, #B49A6B);
	}

	.status-banner.green {
		background: linear-gradient(135deg, #B0C293, #8FA67A);
	}

	.status-banner.gray {
		background: linear-gradient(90deg, #bbb, #999);
	}

	.status-text {
		font-size: 40rpx;
		font-weight: 700;
	}

	.status-sub {
		font-size: 24rpx;
		margin-top: 10rpx;
		opacity: 0.95;
	}

	.card {
		background: #fff;
		margin: 20rpx 24rpx;
		border-radius: 20rpx;
		padding: 24rpx 30rpx;
	}

	.body {
		display: flex;
		margin-top: -30rpx;
		position: relative;
		z-index: 5;
	}

	.cover {
		width: 160rpx;
		height: 160rpx;
		border-radius: 12rpx;
		margin-right: 20rpx;
		background: #eee;
	}

	.info {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: center;
	}

	.title {
		font-size: 32rpx;
		font-weight: 700;
		color: #333;
	}

	.tag {
		font-size: 24rpx;
		color: #999;
		margin: 10rpx 0;
	}

	.price {
		color: $brand-price;
		font-size: 32rpx;
		font-weight: 700;
	}

	.row {
		display: flex;
		justify-content: space-between;
		padding: 16rpx 0;
		border-bottom: 1rpx solid #f5f5f5;
	}

	.row.col {
		flex-direction: column;
	}

	.label {
		font-size: 26rpx;
		color: #999;
	}

	.val {
		font-size: 26rpx;
		color: #333;
	}

	.val.full {
		margin-top: 10rpx;
		line-height: 40rpx;
	}

	.val.plan {
		color: $brand-primary-deep;
	}

	.rule-tip {
		color: #aaa;
		font-size: 22rpx;
		padding: 0 40rpx;
	}

	.footbar {
		position: fixed;
		left: 0;
		bottom: 0;
		width: 100%;
		height: 110rpx;
		background: #fff;
		box-shadow: 0 -2rpx 16rpx rgba(0, 0, 0, 0.06);
		display: flex;
		align-items: center;
		padding: 0 30rpx;
		box-sizing: border-box;
	}

	.btn {
		flex: 1;
		height: 84rpx;
		line-height: 84rpx;
		font-size: 30rpx;
		font-weight: 600;
		border-radius: 42rpx;
		margin: 0 10rpx;
	}

	.btn.cancel {
		background: #f3f3f3;
		color: #666;
	}

	.btn.primary {
		background: linear-gradient(135deg, #B0C293, #9BB07C);
		color: #fff;
	}

	.btn.ghost {
		background: #fff;
		color: $brand-primary-deep;
		border: 2rpx solid $brand-primary;
	}
</style>
