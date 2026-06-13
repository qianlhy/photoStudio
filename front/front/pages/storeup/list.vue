<template>
	<view class="page">
		<view class="list">
			<view class="fav-card" v-for="(item, idx) in list" :key="item.id" @tap="goDetail(item)">
				<image class="fav-img" :src="coverOf(item)" mode="aspectFill"></image>
				<view class="fav-body">
					<text class="fav-name">{{ item.name }}</text>
					<text class="fav-time">{{ fmt(item.addtime) }}</text>
				</view>
				<text class="cuIcon-right"></text>
			</view>
			<view class="empty" v-if="list.length === 0">
				<text class="empty-text">暂无收藏，在首页右滑套餐即可收藏</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() { return { list: [] }; },
		computed: {
			baseUrl() { return this.$base.url; }
		},
		onShow() { this.loadList(); },
		methods: {
			async loadList() {
				try {
					let res = await this.$api.list('storeup', { page: 1, limit: 100, type: '1', tablename: 'taocan' });
					this.list = (res.data && res.data.list) || [];
				} catch (e) { this.list = []; }
			},
			coverOf(item) {
				if (!item.picture) return '';
				return this.baseUrl + item.picture.split(',')[0];
			},
			fmt(d) { return d ? d.toString().split(' ')[0] : ''; },
			goDetail(item) {
				uni.navigateTo({ url: '../taocan/detail?id=' + item.refid });
			}
		}
	};
</script>

<style lang="scss" scoped>
	page { background: $brand-bg-soft; }
	.page { padding: 20rpx 24rpx; }
	.fav-card {
		display: flex; align-items: center; background: #fff; border-radius: $brand-radius-lg;
		padding: 20rpx; margin-bottom: 20rpx; box-shadow: $brand-shadow-card;
	}
	.fav-img { width: 120rpx; height: 120rpx; border-radius: $brand-radius; flex-shrink: 0; }
	.fav-body { flex: 1; margin-left: 20rpx; }
	.fav-name { font-size: 30rpx; font-weight: 600; display: block; }
	.fav-time { font-size: 24rpx; color: $brand-ink-3; margin-top: 8rpx; }
	.empty { text-align: center; padding: 100rpx 0; }
	.empty-text { color: $brand-ink-3; font-size: 28rpx; }
</style>
