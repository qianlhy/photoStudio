<template>
	<view class="home">
		<!-- 搜索 + 品类切换 -->
		<view class="top-bar">
			<view class="search-box">
				<text class="cuIcon-search"></text>
				<input class="search-input" v-model="keyword" placeholder="搜索风格/套餐/关键词" confirm-type="search"
					@confirm="loadCards" />
			</view>
			<view class="pinlei-tabs">
				<view class="ptab" :class="{ active: pinlei === '' }" @tap="setPinlei('')">全部</view>
				<view class="ptab" :class="{ active: pinlei === '写真' }" @tap="setPinlei('写真')">写真</view>
				<view class="ptab" :class="{ active: pinlei === '宣传片' }" @tap="setPinlei('宣传片')">宣传片</view>
			</view>
		</view>

		<!-- 滑卡区 -->
		<view class="swipe-area">
			<tc-swiper ref="swiper" :cards="cards" :baseUrl="baseUrl" @swiperight="onLike" @swipeleft="onNope"
				@cardtap="goDetail" />
		</view>

		<!-- 操作按钮 -->
		<view class="actions" v-if="cards.length">
			<view class="act-btn nope" @tap="tapNope"><text class="cuIcon-close"></text></view>
			<view class="act-btn like" @tap="tapLike"><text class="cuIcon-favor"></text></view>
		</view>

		<!-- 快捷入口 -->
		<view class="quick-links">
			<text @tap="goStoreup">我的收藏</text>
			<text class="sep">|</text>
			<text @tap="goTaocanGrid">套餐列表</text>
		</view>

		<!-- 套餐网格（次要入口） -->
		<view class="section" v-if="showGrid">
			<view class="section-head">
				<view class="section-title"><text class="bar"></text>全部套系</view>
			</view>
			<view class="grid">
				<view class="g-card" v-for="(t, i) in taocans" :key="i" @tap="goDetail(t)">
					<image class="g-img" :src="t.cover" mode="aspectFill"></image>
					<view class="g-body">
						<text class="g-name">{{ t.taocanmingcheng }}</text>
						<text class="g-price">¥{{ t.priceText }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import http from '@/api/http.js'
	export default {
		data() {
			return {
				cards: [],
				taocans: [],
				keyword: '',
				pinlei: '',
				showGrid: false,
				user: {}
			};
		},
		computed: {
			baseUrl() {
				return this.$base.url;
			}
		},
		onShow() {
			this.checkAuth();
			this.loadCards();
		},
		methods: {
			async checkAuth() {
				let token = uni.getStorageSync('token');
				if (!token) {
					uni.navigateTo({ url: '../login/login' });
					return;
				}
				try {
					let res = await this.$api.session('yonghu');
					this.user = res.data || {};
					if (this.user.sfsh === '否') {
						uni.redirectTo({ url: '../apply/apply?pending=1' });
					} else if (this.user.sfsh === '驳回') {
						uni.redirectTo({ url: '../apply/apply?rejected=1' });
					}
				} catch (e) {}
			},
			setPinlei(p) {
				this.pinlei = p;
				this.loadCards();
			},
			async loadCards() {
				try {
					let params = {};
					if (this.pinlei) params.pinlei = this.pinlei;
					if (this.keyword) params.keyword = this.keyword;
					let res = await http.get('taocan/cards', params);
					this.cards = res.data || [];
				} catch (e) {
					this.cards = [];
				}
			},
			async loadGrid() {
				this.showGrid = true;
				let res = await this.$api.list('taocan', {
					page: 1, limit: 20, shangxiajia: '上架', sort: 'paixu', order: 'asc'
				});
				let list = (res.data && res.data.list) || [];
				this.taocans = list.map(t => ({
					...t,
					cover: this.full(t.fengmian),
					priceText: t.xianxiabiaojia || '0'
				}));
			},
			full(p) {
				if (!p) return '';
				return this.baseUrl + p.split(',')[0];
			},
			removeTop() {
				if (this.cards.length) this.cards.shift();
			},
			async onLike(card) {
				await this.saveStoreup(card, '1');
				this.$utils.msg('已收藏');
				this.removeTop();
			},
			async onNope(card) {
				await this.saveStoreup(card, '22');
				this.removeTop();
			},
			tapLike() {
				if (this.$refs.swiper) this.$refs.swiper.swipe('right');
			},
			tapNope() {
				if (this.$refs.swiper) this.$refs.swiper.swipe('left');
			},
			async saveStoreup(card, type) {
				try {
					await this.$api.add('storeup', {
						refid: card.id,
						tablename: 'taocan',
						name: card.taocanmingcheng,
						picture: card.fengmian,
						type: type
					});
				} catch (e) {}
			},
			goDetail(card) {
				uni.navigateTo({ url: '../taocan/detail?id=' + card.id });
			},
			goStoreup() {
				uni.navigateTo({ url: '../storeup/list' });
			},
			goTaocanGrid() {
				this.loadGrid();
			}
		}
	};
</script>

<style lang="scss" scoped>
	page { background: $brand-bg-soft; }
	.home { padding-bottom: 40rpx; }
	.top-bar { padding: 20rpx 24rpx 0; background: #fff; }
	.search-box {
		display: flex; align-items: center; background: $brand-bg-soft;
		border-radius: 999rpx; padding: 0 24rpx; height: 72rpx;
		.cuIcon-search { color: $brand-primary; margin-right: 12rpx; }
	}
	.search-input { flex: 1; font-size: 28rpx; }
	.pinlei-tabs { display: flex; margin-top: 20rpx; gap: 16rpx; }
	.ptab {
		padding: 12rpx 28rpx; border-radius: 999rpx; font-size: 26rpx;
		color: $brand-ink-2; background: $brand-bg-soft;
		&.active { background: $brand-primary-soft; color: $brand-primary-deep; font-weight: 600; }
	}
	.swipe-area { padding: 24rpx; }
	.actions {
		display: flex; justify-content: center; gap: 80rpx; margin-top: -20rpx;
		.act-btn {
			width: 100rpx; height: 100rpx; border-radius: 50%;
			display: flex; align-items: center; justify-content: center; font-size: 44rpx;
			box-shadow: $brand-shadow-card;
		}
		.nope { background: #fff; color: #E8423F; }
		.like { background: $brand-cta; color: #fff; }
	}
	.quick-links {
		text-align: center; margin: 30rpx 0; font-size: 26rpx; color: $brand-primary-deep;
		.sep { margin: 0 16rpx; color: #ccc; }
	}
	.section { padding: 0 24rpx; }
	.section-head { margin-bottom: 20rpx; }
	.section-title {
		font-size: 32rpx; font-weight: 700; display: flex; align-items: center;
		.bar { width: 6rpx; height: 28rpx; background: $brand-primary; border-radius: 4rpx; margin-right: 12rpx; }
	}
	.grid { display: flex; flex-wrap: wrap; gap: 20rpx; }
	.g-card {
		width: calc(50% - 10rpx); background: #fff; border-radius: $brand-radius-lg;
		overflow: hidden; box-shadow: $brand-shadow-card;
	}
	.g-img { width: 100%; height: 240rpx; }
	.g-body { padding: 16rpx; }
	.g-name { font-size: 28rpx; font-weight: 600; display: block; }
	.g-price { color: $brand-price; font-size: 28rpx; margin-top: 8rpx; }
</style>
