<template>
	<view class="tc-swiper">
		<view v-if="cards.length === 0" class="tc-empty">
			<image class="tc-empty-img" src="/static/gen/upload.png" mode="aspectFit"></image>
			<text class="tc-empty-text">{{ emptyText }}</text>
		</view>
		<view v-else class="tc-stack">
			<view
				v-for="(card, index) in renderCards"
				:key="card.id"
				class="tc-card"
				:style="cardStyles[index]"
				@touchstart="index === 0 ? onTouchStart($event) : null"
				@touchmove.stop.prevent="index === 0 ? onTouchMove($event) : null"
				@touchend="index === 0 ? onTouchEnd($event) : null"
				@tap="index === 0 ? onTap(card) : null">
				<!-- 顶层视频卡：真实 video 播放（同屏仅一个，避免卡顿） -->
				<video
					v-if="index === 0 && isVideo(card)"
					id="tcTopVideo"
					class="tc-card-media tc-card-video"
					:src="videoSrc(card)"
					:poster="coverOf(card)"
					:autoplay="true"
					:loop="true"
					:muted="true"
					object-fit="cover"
					:controls="false"
					:show-center-play-btn="false"
					:show-play-btn="false"
					:show-fullscreen-btn="false"
					:show-mute-btn="false"
					:enable-progress-gesture="false"
					:vslide-gesture="false"></video>
				<!-- 封面图：非视频卡 / 非顶层 / 拖动或飞出动画中（盖住视频，保证滑动顺滑） -->
				<image
					v-if="!isVideo(card) || index !== 0 || dragging || animating"
					class="tc-card-media tc-card-cover"
					:src="coverOf(card)"
					mode="aspectFill"></image>
				<!-- 视频标识 -->
				<view v-if="isVideo(card)" class="tc-video-flag"><text class="tc-play-tri"></text></view>
				<!-- 顶层透明手势层：防止原生 video 吞掉左右滑动手势 -->
				<view v-if="index === 0" class="tc-gesture"></view>
				<!-- 右滑收藏角标 -->
				<view v-if="index === 0 && moveX > 20" class="tc-badge tc-badge-like">收藏</view>
				<!-- 左滑屏蔽角标 -->
				<view v-if="index === 0 && moveX < -20" class="tc-badge tc-badge-nope">不喜欢</view>
				<view class="tc-card-mask"></view>
				<view class="tc-card-info">
					<view class="tc-card-tags">
						<text v-if="card.pinlei" class="tc-tag tc-tag-pinlei">{{ card.pinlei }}</text>
						<text v-if="card.fengge" class="tc-tag">{{ card.fengge }}</text>
					</view>
					<view class="tc-card-title">{{ card.taocanmingcheng }}</view>
					<view class="tc-card-brief">{{ card.jianjie }}</view>
					<view class="tc-card-price">
						<text class="tc-price-symbol">￥</text>
						<text class="tc-price-num">{{ card.xianxiabiaojia }}</text>
						<text class="tc-price-unit">线下标价</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: 'tc-swiper',
		props: {
			cards: {
				type: Array,
				default: () => []
			},
			baseUrl: {
				type: String,
				default: ''
			},
			emptyText: {
				type: String,
				default: '暂无更多套餐，去切换品类或调整偏好试试~'
			}
		},
		data() {
			return {
				moveX: 0,
				moveY: 0,
				startX: 0,
				startY: 0,
				dragging: false,
				animating: false
			};
		},
		computed: {
			// 最多渲染顶部 3 张，减少性能开销
			renderCards() {
				return this.cards.slice(0, 3);
			},
			// 小程序端 :style 不支持方法调用，改用计算属性数组（依赖 moveX 等变化自动重算）
			cardStyles() {
				return this.renderCards.map((card, index) => {
					if (index === 0) {
						let rotate = this.moveX / 18;
						let opacity = 1 - Math.min(Math.abs(this.moveX) / 600, 0.25);
						let transition = this.dragging ? 'none' : 'transform 0.3s ease, opacity 0.3s ease';
						return `transform: translate3d(${this.moveX}px, ${this.moveY}px, 0) rotate(${rotate}deg); opacity:${opacity}; z-index:30; transition:${transition};`;
					}
					let scale = 1 - index * 0.04;
					let translateY = index * 18;
					return `transform: scale(${scale}) translateY(${translateY}rpx); z-index:${30 - index}; transition: transform 0.3s ease;`;
				});
			}
		},
		methods: {
			coverOf(card) {
				if (!card.fengmian) return '';
				let first = card.fengmian.split(',')[0];
				return this.baseUrl + first;
			},
			isVideo(card) {
				return !!(card && card.shipin && String(card.shipin).trim());
			},
			videoSrc(card) {
				if (!this.isVideo(card)) return '';
				let first = String(card.shipin).split(',')[0];
				return this.baseUrl + first;
			},
			pauseTopVideo() {
				try {
					let ctx = uni.createVideoContext('tcTopVideo', this);
					if (ctx) ctx.pause();
				} catch (e) {}
			},
			playTopVideo() {
				try {
					let ctx = uni.createVideoContext('tcTopVideo', this);
					if (ctx) ctx.play();
				} catch (e) {}
			},
			onTouchStart(e) {
				if (this.animating) return;
				let t = e.touches[0] || e.changedTouches[0];
				this.startX = t.clientX;
				this.startY = t.clientY;
				this.dragging = true;
				// 拖动期间用封面图盖住并暂停视频，避免边解码边位移导致卡顿
				if (this.isVideo(this.cards[0])) this.pauseTopVideo();
			},
			onTouchMove(e) {
				if (!this.dragging) return;
				let t = e.touches[0] || e.changedTouches[0];
				this.moveX = t.clientX - this.startX;
				this.moveY = (t.clientY - this.startY) / 2;
			},
			onTouchEnd(e) {
				if (!this.dragging) return;
				this.dragging = false;
				let threshold = 100;
				if (this.moveX > threshold) {
					this.fly('right');
				} else if (this.moveX < -threshold) {
					this.fly('left');
				} else {
					this.reset();
				}
			},
			// 按钮触发：方向 right=收藏 left=屏蔽
			swipe(direction) {
				if (this.animating || this.cards.length === 0) return;
				this.fly(direction);
			},
			fly(direction) {
				this.animating = true;
				let card = this.cards[0];
				this.moveX = direction === 'right' ? 1000 : -1000;
				this.moveY = 60;
				setTimeout(() => {
					if (direction === 'right') {
						this.$emit('swiperight', card);
					} else {
						this.$emit('swipeleft', card);
					}
					this.reset();
					this.animating = false;
				}, 280);
			},
			reset() {
				this.moveX = 0;
				this.moveY = 0;
				// 卡片归位 / 切换到新顶卡后，恢复（或启动）顶层视频播放
				this.$nextTick(() => {
					setTimeout(() => {
						if (this.isVideo(this.cards[0])) this.playTopVideo();
					}, 60);
				});
			},
			onTap(card) {
				// 轻微滑动视为点击
				if (Math.abs(this.moveX) < 10) {
					this.$emit('cardtap', card);
				}
			}
		}
	};
</script>

<style lang="scss" scoped>
	.tc-swiper {
		position: relative;
		width: 100%;
		height: 920rpx;
	}

	.tc-stack {
		position: relative;
		width: 100%;
		height: 100%;
	}

	.tc-card {
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 900rpx;
		border-radius: 28rpx;
		overflow: hidden;
		background: #fff;
		box-shadow: 0 12rpx 40rpx rgba(0, 0, 0, 0.12);
		will-change: transform;
	}

	.tc-card-media {
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
	}

	/* 底层媒体（视频） */
	.tc-card-video {
		z-index: 1;
	}

	/* 封面图：拖动/飞出时盖住视频，或非视频卡的常态展示 */
	.tc-card-cover {
		z-index: 2;
	}

	/* 透明手势层：覆盖整张卡，保证滑动手势不被原生 video 吞掉 */
	.tc-gesture {
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		z-index: 3;
		background: transparent;
	}

	.tc-video-flag {
		position: absolute;
		top: 24rpx;
		right: 24rpx;
		width: 56rpx;
		height: 56rpx;
		border-radius: 50%;
		background: rgba(0, 0, 0, 0.45);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 6;
	}

	.tc-play-tri {
		width: 0;
		height: 0;
		margin-left: 6rpx;
		border-style: solid;
		border-width: 14rpx 0 14rpx 22rpx;
		border-color: transparent transparent transparent #fff;
	}

	.tc-card-mask {
		position: absolute;
		left: 0;
		bottom: 0;
		width: 100%;
		height: 380rpx;
		background: linear-gradient(to top, rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0));
		z-index: 4;
	}

	.tc-card-info {
		position: absolute;
		left: 0;
		bottom: 0;
		width: 100%;
		padding: 30rpx 36rpx 40rpx;
		box-sizing: border-box;
		color: #fff;
		z-index: 5;
	}

	.tc-card-tags {
		display: flex;
		flex-wrap: wrap;
		margin-bottom: 14rpx;
	}

	.tc-tag {
		font-size: 22rpx;
		padding: 4rpx 16rpx;
		border-radius: 20rpx;
		margin-right: 12rpx;
		background: rgba(255, 255, 255, 0.25);
		color: #fff;
	}

	.tc-tag-pinlei {
		background: #FFE16D;
		color: #6b4e00;
		font-weight: 600;
	}

	.tc-card-title {
		font-size: 40rpx;
		font-weight: 700;
		margin-bottom: 10rpx;
	}

	.tc-card-brief {
		font-size: 26rpx;
		opacity: 0.85;
		line-height: 36rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
	}

	.tc-card-price {
		margin-top: 16rpx;
		display: flex;
		align-items: baseline;
	}

	.tc-price-symbol {
		color: #FFE16D;
		font-size: 28rpx;
	}

	.tc-price-num {
		color: #FFE16D;
		font-size: 48rpx;
		font-weight: 700;
		margin-right: 12rpx;
	}

	.tc-price-unit {
		font-size: 22rpx;
		opacity: 0.7;
	}

	.tc-badge {
		position: absolute;
		top: 60rpx;
		padding: 10rpx 28rpx;
		font-size: 36rpx;
		font-weight: 800;
		border-radius: 12rpx;
		border: 6rpx solid;
		z-index: 5;
	}

	.tc-badge-like {
		right: 50rpx;
		color: #67c23a;
		border-color: #67c23a;
		transform: rotate(18deg);
	}

	.tc-badge-nope {
		left: 50rpx;
		color: #f56c6c;
		border-color: #f56c6c;
		transform: rotate(-18deg);
	}

	.tc-empty {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		height: 100%;
	}

	.tc-empty-img {
		width: 160rpx;
		height: 160rpx;
		opacity: 0.5;
	}

	.tc-empty-text {
		margin-top: 20rpx;
		color: #999;
		font-size: 26rpx;
	}
</style>
