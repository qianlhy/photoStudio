<template>
	<view class="home">
		<!-- 顶部大图轮播 -->
		<view class="hero">
			<swiper class="hero-swiper" :indicator-dots="banners.length > 1" indicator-color="rgba(255,255,255,0.5)"
				indicator-active-color="#ffffff" :autoplay="true" :interval="4000" :circular="true" :duration="600">
				<swiper-item v-for="(b, i) in banners" :key="i" @tap="onBannerTap(b)">
					<image class="hero-img" :src="b.img" mode="aspectFill"></image>
				</swiper-item>
			</swiper>
			<view v-if="banners.length === 0" class="hero-empty">云漫 · 照相馆</view>
		</view>

		<!-- 风格分类 -->
		<view class="cates" v-if="categories.length">
			<view class="cate" v-for="(c, i) in categories" :key="i" @tap="onCate(c)"
				:class="{ active: curFengge === c.name }">
				<view class="cate-circle">
					<image v-if="c.img" class="cate-img" :src="c.img" mode="aspectFill"></image>
					<text v-else class="cate-char">{{ c.first }}</text>
				</view>
				<text class="cate-name">{{ c.name }}</text>
			</view>
		</view>

		<!-- 服务套系 -->
		<view class="section">
			<view class="section-head">
				<view class="section-title">
					<text class="bar"></text>服务套系
				</view>
				<text class="more" @tap="goTaocanAll">全部 ›</text>
			</view>
			<view class="grid">
				<view class="g-card" v-for="(t, i) in taocans" :key="i" @tap="goTaocan(t)">
					<image class="g-img" :src="t.cover" mode="aspectFill"></image>
					<view class="g-body">
						<text class="g-name">{{ t.taocanmingcheng }}</text>
						<view class="g-meta">
							<text class="g-price">¥{{ t.priceText }}</text>
							<text v-if="t.pinlei" class="g-tag">{{ t.pinlei }}</text>
						</view>
					</view>
				</view>
			</view>
			<view v-if="taocans.length === 0" class="empty">暂无套系</view>
		</view>

		<!-- 作品展示 -->
		<view class="section">
			<view class="section-head">
				<view class="section-title">
					<text class="bar"></text>作品展示
				</view>
				<text class="more" @tap="goChengpin">更多 ›</text>
			</view>
			<view class="grid">
				<view class="g-card" v-for="(w, i) in works" :key="i" @tap="goChengpin">
					<image class="g-img tall" :src="w.cover" mode="aspectFill"></image>
					<view class="g-body">
						<text class="g-name">{{ w.biaoti }}</text>
					</view>
				</view>
			</view>
			<view v-if="works.length === 0" class="empty">敬请期待</view>
		</view>

		<view class="foot">云漫 · 照相馆 · 用心记录每一刻</view>
	</view>
</template>

<script>
	import http from '@/api/http.js'
	export default {
		data() {
			return {
				user: {},
				banners: [],
				categories: [],
				taocans: [],
				works: [],
				curFengge: ''
			};
		},
		async onLoad() {
			let table = uni.getStorageSync('nowTable');
			if (table) {
				try {
					let res = await this.$api.session(table);
					this.user = res.data;
				} catch (e) {}
			}
			this.loadAll();
		},
		methods: {
			full(p) {
				if (!p) return '';
				let one = ('' + p).split(',')[0].trim();
				if (!one) return '';
				if (/^https?:\/\//.test(one)) return one;
				return this.$base.url + one;
			},
			async loadAll() {
				await Promise.all([this.loadTaocan(), this.loadCategories(), this.loadWorks()]);
				this.buildBanners();
				this.matchCategoryImages();
			},
			async loadCategories() {
				try {
					let res = await this.$api.list('leixing', {
						page: 1,
						limit: 20
					});
					let list = (res.data && res.data.list) || [];
					this.categories = list.map(it => {
						let name = it.leixing || '';
						return {
							name,
							first: name.substring(0, 1),
							img: '',
							raw: it
						};
					});
				} catch (e) {
					this.categories = [];
				}
			},
			async loadTaocan() {
				let params = {
					page: 1,
					limit: 6,
					shangxiajia: '上架'
				};
				if (this.curFengge) params.fengge = this.curFengge;
				try {
					let res = await this.$api.list('taocan', params);
					let list = (res.data && res.data.list) || [];
					this.taocans = list.map(it => {
						return Object.assign({}, it, {
							cover: this.full(it.fengmian),
							priceText: (it.xianxiabiaojia != null ? it.xianxiabiaojia : '面议')
						});
					});
				} catch (e) {
					this.taocans = [];
				}
			},
			async loadWorks() {
				try {
					let res = await this.$api.list('chengpin', {
						page: 1,
						limit: 4,
						shangxiajia: '上架'
					});
					let list = (res.data && res.data.list) || [];
					this.works = list.map(it => {
						return Object.assign({}, it, {
							cover: this.full(it.tupian)
						});
					});
				} catch (e) {
					this.works = [];
				}
			},
			buildBanners() {
				let imgs = [];
				this.taocans.forEach(t => {
					if (t.cover) imgs.push({
						img: t.cover,
						id: t.id
					});
				});
				this.banners = imgs.slice(0, 5);
			},
			matchCategoryImages() {
				this.categories = this.categories.map(c => {
					let hit = this.taocans.find(t => t.fengge === c.name);
					return Object.assign({}, c, {
						img: hit ? hit.cover : ''
					});
				});
			},
			onCate(c) {
				this.curFengge = (this.curFengge === c.name) ? '' : c.name;
				this.loadTaocan();
			},
			onBannerTap(b) {
				if (b && b.id) this.$utils.jump(`../taocan/detail?id=${b.id}`);
			},
			goTaocan(t) {
				if (t && t.id) this.$utils.jump(`../taocan/detail?id=${t.id}`);
			},
			goTaocanAll() {
				this.curFengge = '';
				this.loadTaocan();
			},
			goChengpin() {
				this.$utils.tab('../chengpin/list');
			}
		}
	};
</script>

<style lang="scss" scoped>
	page {
		background: $brand-bg-soft;
	}

	.home {
		min-height: 100vh;
		background: $brand-bg-soft;
		padding-bottom: 40rpx;
	}

	/* 顶部大图 */
	.hero {
		position: relative;
		margin: 20rpx 24rpx 0;
		border-radius: $brand-radius-lg;
		overflow: hidden;
		box-shadow: $brand-shadow;
	}

	.hero-swiper {
		width: 100%;
		height: 360rpx;
	}

	.hero-img {
		width: 100%;
		height: 360rpx;
		display: block;
	}

	.hero-empty {
		height: 360rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		color: #fff;
		letter-spacing: 6rpx;
		font-size: 40rpx;
		background: linear-gradient(135deg, #C7AE80, #B49A6B);
	}

	/* 风格分类 */
	.cates {
		display: flex;
		flex-wrap: nowrap;
		overflow-x: auto;
		padding: 36rpx 24rpx 12rpx;
	}

	.cate {
		flex: 0 0 auto;
		width: 150rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.cate-circle {
		width: 112rpx;
		height: 112rpx;
		border-radius: 50%;
		overflow: hidden;
		background: linear-gradient(135deg, #CDB595, #B49A6B);
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 6rpx 16rpx rgba(180, 154, 107, 0.25);
		border: 4rpx solid transparent;
	}

	.cate.active .cate-circle {
		border-color: $brand-primary;
	}

	.cate-img {
		width: 100%;
		height: 100%;
	}

	.cate-char {
		color: #fff;
		font-size: 40rpx;
		font-weight: 600;
	}

	.cate-name {
		margin-top: 14rpx;
		font-size: 24rpx;
		color: $brand-ink-2;
	}

	.cate.active .cate-name {
		color: $brand-primary-deep;
		font-weight: 600;
	}

	/* 区块 */
	.section {
		margin: 24rpx 24rpx 0;
		background: #fff;
		border-radius: $brand-radius-lg;
		padding: 28rpx 24rpx 8rpx;
		box-shadow: $brand-shadow-card;
	}

	.section-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 24rpx;
	}

	.section-title {
		display: flex;
		align-items: center;
		font-size: 32rpx;
		font-weight: 600;
		color: $brand-ink;
	}

	.bar {
		width: 8rpx;
		height: 30rpx;
		border-radius: 4rpx;
		background: $brand-primary;
		margin-right: 16rpx;
	}

	.more {
		font-size: 24rpx;
		color: $brand-ink-3;
	}

	/* 网格 */
	.grid {
		display: flex;
		flex-wrap: wrap;
		justify-content: space-between;
	}

	.g-card {
		width: 332rpx;
		margin-bottom: 24rpx;
		background: #fff;
		border-radius: $brand-radius;
		overflow: hidden;
	}

	.g-img {
		width: 332rpx;
		height: 240rpx;
		display: block;
		background: #f0f0f0;
	}

	.g-img.tall {
		height: 300rpx;
	}

	.g-body {
		padding: 14rpx 8rpx 6rpx;
	}

	.g-name {
		font-size: 28rpx;
		color: $brand-ink;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		display: block;
	}

	.g-meta {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-top: 10rpx;
	}

	.g-price {
		color: $brand-price;
		font-size: 30rpx;
		font-weight: 600;
	}

	.g-tag {
		font-size: 20rpx;
		color: $brand-primary-deep;
		background: $brand-primary-soft;
		padding: 4rpx 14rpx;
		border-radius: $brand-radius-pill;
	}

	.empty {
		text-align: center;
		color: $brand-ink-3;
		font-size: 26rpx;
		padding: 30rpx 0 40rpx;
	}

	.foot {
		text-align: center;
		color: #c4c4c4;
		font-size: 22rpx;
		letter-spacing: 2rpx;
		padding: 40rpx 0 10rpx;
	}
</style>
