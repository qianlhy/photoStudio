<template>
	<!-- H5 临时：浏览器竖屏预览开关，测完删除本组件引用即可 -->
	<!-- #ifdef H5 -->
	<view class="dev-portrait-btn" :class="{ on: forcePortrait }" @click="toggle">
		{{ forcePortrait ? '退出竖屏预览' : '竖屏预览' }}
	</view>
	<!-- #endif -->
</template>

<script>
import { applyForcePortrait, restoreForcePortrait } from '@/utils/forcePortrait.js'

export default {
	name: 'dev-portrait-toggle',
	data() {
		return {
			forcePortrait: false
		}
	},
	mounted() {
		// #ifdef H5
		this.forcePortrait = restoreForcePortrait()
		// #endif
	},
	methods: {
		toggle() {
			// #ifdef H5
			const next = !this.forcePortrait
			applyForcePortrait(next)
			this.forcePortrait = next
			uni.showToast({
				title: next ? '已切换到竖屏预览' : '已恢复横屏',
				icon: 'none'
			})
			// #endif
		}
	}
}
</script>

<style lang="scss" scoped>
/* #ifdef H5 */
.dev-portrait-btn {
	position: fixed;
	right: 16px;
	bottom: 88px;
	z-index: 99999;
	padding: 10px 14px;
	border-radius: 999px;
	background: rgba(32, 41, 56, .88);
	color: #fff;
	font-size: 13px;
	font-weight: 600;
	box-shadow: 0 8px 20px rgba(0, 0, 0, .22);
	user-select: none;
}
.dev-portrait-btn.on {
	background: $brand;
}
/* #endif */
</style>
