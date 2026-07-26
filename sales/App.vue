<script>
export default {
	onLaunch: function() {
		// 拉取品牌配置，写入全局
		const api = this.$api
		api.config().then(res => {
			if (res && res.data) {
				uni.setStorageSync('brand', res.data)
			}
		}).catch(() => {})
	},
	onShow: function() {},
	onHide: function() {}
}
</script>

<style lang="scss">
/* 全局基础样式 */
page {
	background: $page-bg;
	color: $ink;
	font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif;
	font-size: 28rpx;
}

view, text {
	box-sizing: border-box;
}

.flex {
	display: flex;
}
.flex-col {
	display: flex;
	flex-direction: column;
}
.items-center {
	align-items: center;
}
.justify-between {
	justify-content: space-between;
}
.flex-1 {
	flex: 1;
}

.card {
	background: $card;
	border-radius: $radius;
	border: 1rpx solid $line;
}

/* 通用按钮 */
.btn {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	border-radius: 999rpx;
	font-weight: 600;
}
.btn-primary {
	background: $brand;
	color: #fff;
}
.btn-danger {
	background: linear-gradient(90deg, #FF7A59, #FF4D7E);
	color: #fff;
}
.btn-ghost {
	background: #fff;
	color: $ink-2;
	border: 1rpx solid $line;
}

.tag {
	display: inline-flex;
	align-items: center;
	padding: 4rpx 16rpx;
	border-radius: 999rpx;
	font-size: 22rpx;
}
</style>
