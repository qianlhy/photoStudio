<template>
	<view class="page">
		<view class="list">
			<view class="cp-card" v-for="(item, idx) in list" :key="item.id">
				<view class="cp-head">
					<text class="cp-title">{{ item.taocanmingcheng || item.biaoti }}</text>
					<text class="cp-date">{{ fmt(item.addtime) }}</text>
				</view>
				<text class="cp-no" v-if="item.dingdanbianhao">关联订单：{{ item.dingdanbianhao }}</text>
				<view class="cp-imgs">
					<image v-for="(img, i) in imgsOf(item)" :key="i" class="cp-img" :src="baseUrl + img"
						mode="aspectFill" @tap="preview(item, i)"></image>
				</view>
				<view class="cp-foot">
					<text class="cp-count">共 {{ imgsOf(item).length }} 张</text>
					<view class="cp-btns">
						<button class="mini-btn" @tap="saveAll(item)">保存到相册</button>
						<button class="mini-btn primary" v-if="item.shipin" @tap="playVideo(item)">看视频</button>
					</view>
				</view>
			</view>

			<view class="empty" v-if="list.length === 0">
				<image class="empty-img" src="/static/gen/upload.png" mode="aspectFit"></image>
				<text class="empty-text">暂无成品，拍摄完成上线后会在这里展示</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				list: [],
				user: {}
			};
		},
		computed: {
			baseUrl() {
				return this.$base.url;
			}
		},
		async onShow() {
			let table = uni.getStorageSync('nowTable');
			if (table) {
				try {
					let res = await this.$api.session(table);
					this.user = res.data;
				} catch (e) {}
			}
			this.loadList();
		},
		methods: {
			async loadList() {
				try {
					let params = {
						page: 1,
						limit: 100,
						sort: 'addtime',
						order: 'desc',
						shangxiajia: '上架'
					};
					let res = await this.$api.list('chengpin', params);
					this.list = (res.data && res.data.list) || [];
				} catch (e) {
					this.list = [];
				}
			},
			imgsOf(item) {
				if (!item.tupian) return [];
				return item.tupian.split(',').filter(i => i);
			},
			fmt(d) {
				if (!d) return '';
				return d.toString().split(' ')[0];
			},
			preview(item, idx) {
				uni.previewImage({
					current: idx,
					urls: this.imgsOf(item).map(i => this.baseUrl + i)
				});
			},
			saveAll(item) {
				let imgs = this.imgsOf(item);
				if (!imgs.length) return;
				uni.showLoading({
					title: '保存中'
				});
				let done = 0;
				imgs.forEach(img => {
					uni.downloadFile({
						url: this.baseUrl + img,
						success: (r) => {
							uni.saveImageToPhotosAlbum({
								filePath: r.tempFilePath,
								complete: () => {
									done++;
									if (done === imgs.length) {
										uni.hideLoading();
										this.$utils.msg('已保存到相册');
									}
								}
							});
						},
						fail: () => {
							done++;
							if (done === imgs.length) uni.hideLoading();
						}
					});
				});
			},
			playVideo(item) {
				let url = this.baseUrl + item.shipin.split(',')[0];
				// #ifdef H5
				window.open(url);
				// #endif
				// #ifndef H5
				uni.navigateTo({
					url: `../pay-confirm/pay-confirm?video=${encodeURIComponent(url)}`,
					fail: () => {}
				});
				// #endif
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

	.cp-card {
		background: #fff;
		border-radius: $brand-radius-lg;
		padding: 24rpx;
		margin-bottom: 20rpx;
		box-shadow: $brand-shadow-card;
	}

	.cp-head {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.cp-title {
		font-size: 30rpx;
		font-weight: 700;
		color: #333;
	}

	.cp-date {
		font-size: 22rpx;
		color: #bbb;
	}

	.cp-no {
		font-size: 22rpx;
		color: #999;
		margin: 8rpx 0 16rpx;
		display: block;
	}

	.cp-imgs {
		display: flex;
		flex-wrap: wrap;
	}

	.cp-img {
		width: 212rpx;
		height: 212rpx;
		border-radius: 12rpx;
		margin: 0 12rpx 12rpx 0;
		background: #eee;
	}

	.cp-foot {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-top: 10rpx;
	}

	.cp-count {
		font-size: 24rpx;
		color: #999;
	}

	.cp-btns {
		display: flex;
	}

	.mini-btn {
		font-size: 24rpx;
		color: #666;
		background: #f3f3f3;
		border-radius: 30rpx;
		padding: 0 26rpx;
		height: 56rpx;
		line-height: 56rpx;
		margin-left: 16rpx;
	}

	.mini-btn.primary {
		background: $brand-cta;
		color: #fff;
	}

	.empty {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 140rpx 0;
	}

	.empty-img {
		width: 160rpx;
		height: 160rpx;
		opacity: 0.5;
	}

	.empty-text {
		margin-top: 20rpx;
		color: #bbb;
		font-size: 26rpx;
	}
</style>
