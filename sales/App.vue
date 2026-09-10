<script>
import materialCache from './utils/materialCache.js'
import base from './api/base.js'

export default {
	onLaunch: function() {
		const api = this.$api
		api.config().then(res => {
			if (res && res.data) {
				uni.setStorageSync('brand', res.data)
			}
		}).catch(() => {})
		// 已登录：等 plus 就绪后静默同步（路径为 App 私有目录，无需客户提供）
		if (uni.getStorageSync('token') && materialCache.isAppPlus()) {
			materialCache.startSync(api, (this.$base && this.$base.url) || base.url, { silent: true })
		}
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
	font-family: "PingFang SC", "HarmonyOS Sans SC", "Noto Sans CJK SC", "Microsoft YaHei", sans-serif;
	font-size: 28rpx;
	font-weight: 400;
	-webkit-font-smoothing: antialiased;
	-moz-osx-font-smoothing: grayscale;
	text-rendering: optimizeLegibility;
}

view, text, button, input, textarea {
	box-sizing: border-box;
	font-family: inherit;
}

text {
	line-height: 1.35;
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
	box-shadow: 0 3rpx 12rpx rgba(31, 39, 51, .025);
}

/* 通用按钮 */
.btn {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	border-radius: 999rpx;
	font-weight: 600;
	letter-spacing: 0;
	transition: opacity .16s ease, transform .16s ease, box-shadow .16s ease;
}
.btn-primary {
	background: $brand;
	color: #fff;
	box-shadow: 0 8rpx 18rpx rgba(47, 107, 255, .18);
}
.btn-danger {
	background: linear-gradient(90deg, #FF7A59, #FF4D7E);
	color: #fff;
	box-shadow: 0 8rpx 18rpx rgba(255, 77, 126, .15);
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
	font-weight: 500;
	line-height: 1.25;
}

/* 高频控件的统一精修 */
.searchbar {
	border: 1rpx solid transparent;
	box-shadow: inset 0 1rpx 2rpx rgba(31, 39, 51, .025);
	transition: border-color .18s ease, background-color .18s ease, box-shadow .18s ease;
}
.searchbar:focus-within {
	background: #fff;
	border-color: rgba(47, 107, 255, .28);
	box-shadow: 0 0 0 5rpx rgba(47, 107, 255, .07);
}
.s-input {
	color: $ink;
}

/* 放大镜用 CSS 绘制，避免各端 emoji 字形与颜色不一致 */
.s-ico {
	position: relative;
	display: inline-block;
	width: 26rpx;
	height: 26rpx;
	flex-shrink: 0;
	color: $muted;
}
.s-ico::before {
	content: "";
	position: absolute;
	left: 0;
	top: 0;
	width: 18rpx;
	height: 18rpx;
	box-sizing: border-box;
	border: 3rpx solid currentColor;
	border-radius: 50%;
}
.s-ico::after {
	content: "";
	position: absolute;
	right: 1rpx;
	bottom: 1rpx;
	width: 9rpx;
	height: 3rpx;
	background: currentColor;
	border-radius: 3rpx;
	transform: rotate(45deg);
}
.tab, .f, .chip {
	font-weight: 500;
	transition: color .16s ease, background-color .16s ease, border-color .16s ease, box-shadow .16s ease;
}
/* 选中态底色各页不同（蓝 / 橙粉渐变），投影用中性色以免串色 */
.tab.on, .f.on, .chip.on {
	box-shadow: 0 5rpx 14rpx rgba(31, 39, 51, .12);
}
image {
	background-color: #EEF1F5;
}

/* H5 与 Android Pad 均保留清晰、克制的按压反馈 */
.btn:active,
.nav-item:active {
	opacity: .88;
	transform: scale(.985);
}

/* 横屏 Pad：卡片边缘和投影统一为轻量层级，不改变原有布局 */
/* 小米平板 6 Pro 横屏 2880×1800 */
@media #{$pad-mq-landscape} {
	.card {
		border-color: rgba(31, 39, 51, .055);
		box-shadow: 0 3px 12px rgba(31, 39, 51, .028);
	}
	.btn {
		font-weight: 600;
	}
	.searchbar {
		border-width: 1px;
	}
}

@media #{$pad-mq-portrait} {
	.card {
		border-color: rgba(31, 39, 51, .055);
		box-shadow: 0 3px 12px rgba(31, 39, 51, .028);
	}
}
</style>
