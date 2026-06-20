<template>
	<view class="page">
		<view class="hd">
			<text class="hd-title">告诉我们你的拍摄偏好</text>
			<text class="hd-sub">我们会优先为你推荐更合适的样片与套餐</text>
		</view>

		<!-- 必选大类（可多选） -->
		<view class="card">
			<view class="sec-title">意向拍摄大类<text class="must">必选</text></view>
			<view class="sec-tip">可单选，也可多选</view>
			<view class="cat-wrap">
				<view class="cat" :class="{ active: catSelected(cat.value) }" v-for="cat in bigCats" :key="cat.value"
					@tap="toggleCat(cat.value)">
					<text class="cat-icon" :class="cat.icon"></text>
					<text class="cat-name">{{ cat.label }}</text>
					<text class="cat-check cuIcon-check" v-if="catSelected(cat.value)"></text>
				</view>
			</view>
		</view>

		<!-- 细分小类（随大类联动，可多选） -->
		<view class="card" v-if="selectedCats.length">
			<view class="sec-title">偏好细分风格<text class="opt">可多选</text></view>
			<block v-for="group in styleGroups" :key="group.cat">
				<view class="group-head">{{ group.label }}</view>
				<view class="chip-wrap" v-if="group.styles.length">
					<view class="chip" :class="{ active: styleSelected(item.leixing) }"
						v-for="(item, idx) in group.styles" :key="idx" @tap="toggleStyle(item.leixing)">
						{{ item.leixing }}
					</view>
				</view>
				<view class="group-empty" v-else>该大类暂无细分风格</view>
			</block>
		</view>
		<view class="card empty-hint" v-else>
			<text>请先选择上方拍摄大类，再挑选细分风格</text>
		</view>

		<button class="save-btn" @tap="save">保存偏好</button>
		<view class="skip" @tap="skip">暂时跳过</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				user: {},
				// 大类展示标签 -> 底层品类值（与 taocan/leixing 的 pinlei 一致）
				bigCats: [
					{ value: '写真', label: '个人写真', icon: 'cuIcon-camera' },
					{ value: '宣传片', label: '商业宣传片', icon: 'cuIcon-video' }
				],
				selectedCats: [],
				selectedStyles: [],
				allStyles: []
			};
		},
		computed: {
			// 仅展示已选大类对应的细分小类分组
			styleGroups() {
				return this.bigCats
					.filter(c => this.selectedCats.indexOf(c.value) > -1)
					.map(c => ({
						cat: c.value,
						label: c.label,
						styles: this.allStyles.filter(s => s.pinlei === c.value)
					}));
			}
		},
		async onLoad() {
			let table = uni.getStorageSync('nowTable');
			if (table) {
				try {
					let res = await this.$api.session(table);
					this.user = res.data || {};
					this.initFromUser();
				} catch (e) {}
			}
			this.loadStyles();
		},
		methods: {
			// 回填：把已保存的偏好还原到大类/细分选择
			initFromUser() {
				let yx = this.user.yixiangpinlei || '';
				if (yx === '都看看') {
					this.selectedCats = ['写真', '宣传片'];
				} else if (yx === '写真' || yx === '宣传片') {
					this.selectedCats = [yx];
				} else if (yx) {
					// 兼容历史逗号存储
					this.selectedCats = yx.split(',').filter(v => v === '写真' || v === '宣传片');
				}
				if (this.user.pianhao) {
					this.selectedStyles = this.user.pianhao.split(',').filter(i => i);
				}
			},
			async loadStyles() {
				// 一次性拉全部风格，前端按品类分组，避免每次切换大类都请求
				let res = await this.$api.list('leixing', { page: 1, limit: 100 });
				this.allStyles = (res.data && res.data.list) || [];
			},
			catSelected(v) {
				return this.selectedCats.indexOf(v) > -1;
			},
			toggleCat(v) {
				let idx = this.selectedCats.indexOf(v);
				if (idx > -1) {
					this.selectedCats.splice(idx, 1);
					// 取消大类时，移除其名下已选的细分风格
					let names = this.allStyles.filter(s => s.pinlei === v).map(s => s.leixing);
					this.selectedStyles = this.selectedStyles.filter(n => names.indexOf(n) === -1);
				} else {
					this.selectedCats.push(v);
				}
			},
			styleSelected(name) {
				return this.selectedStyles.indexOf(name) > -1;
			},
			toggleStyle(name) {
				let idx = this.selectedStyles.indexOf(name);
				if (idx > -1) {
					this.selectedStyles.splice(idx, 1);
				} else {
					this.selectedStyles.push(name);
				}
			},
			// 根据所选大类推导落库的 yixiangpinlei（两个都选=都看看）
			resolvePinlei() {
				if (this.selectedCats.length >= 2) return '都看看';
				if (this.selectedCats.length === 1) return this.selectedCats[0];
				return '';
			},
			async save() {
				if (!this.user || !this.user.id) {
					this.$utils.msg('请先登录');
					return;
				}
				if (!this.selectedCats.length) {
					this.$utils.msg('请至少选择一个拍摄大类');
					return;
				}
				let table = uni.getStorageSync('nowTable');
				await this.$api.update(table, {
					id: this.user.id,
					yixiangpinlei: this.resolvePinlei(),
					pianhao: this.selectedStyles.join(',')
				});
				this.$utils.msg('偏好已保存，将为你优先推荐');
				setTimeout(() => {
					uni.switchTab({
						url: '../index/index',
						fail: () => this.$utils.jump('../index/index')
					});
				}, 800);
			},
			skip() {
				uni.switchTab({
					url: '../index/index',
					fail: () => this.$utils.jump('../index/index')
				});
			}
		}
	};
</script>

<style lang="scss" scoped>
	page {
		background: $brand-bg-soft;
	}

	.page {
		padding: 30rpx 24rpx;
	}

	.hd {
		padding: 20rpx 10rpx 30rpx;
	}

	.hd-title {
		display: block;
		font-size: 40rpx;
		font-weight: 700;
		color: #333;
	}

	.hd-sub {
		display: block;
		font-size: 26rpx;
		color: #999;
		margin-top: 10rpx;
	}

	.card {
		background: #fff;
		border-radius: 20rpx;
		padding: 28rpx 24rpx;
		margin-bottom: 24rpx;
	}

	.sec-title {
		display: flex;
		align-items: center;
		font-size: 30rpx;
		font-weight: 700;
		color: $brand-ink;
		padding-left: 16rpx;
		border-left: 6rpx solid $brand-primary;
	}

	.must,
	.opt {
		font-size: 22rpx;
		font-weight: 500;
		margin-left: 14rpx;
		padding: 2rpx 14rpx;
		border-radius: 20rpx;
	}

	.must {
		color: #E8423F;
		background: #FDE2E2;
	}

	.opt {
		color: $brand-primary-deep;
		background: $brand-primary-soft;
	}

	.sec-tip {
		font-size: 24rpx;
		color: #aaa;
		margin: 12rpx 0 22rpx 16rpx;
	}

	/* 大类卡片 */
	.cat-wrap {
		display: flex;
		gap: 24rpx;
	}

	.cat {
		position: relative;
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		height: 168rpx;
		border-radius: 18rpx;
		background: #f6f4ef;
		border: 3rpx solid transparent;
	}

	.cat.active {
		background: $brand-primary-soft;
		border-color: $brand-primary;
	}

	.cat-icon {
		font-size: 56rpx;
		color: #b8a888;
	}

	.cat.active .cat-icon {
		color: $brand-primary-deep;
	}

	.cat-name {
		margin-top: 14rpx;
		font-size: 28rpx;
		color: $brand-ink-2;
		font-weight: 600;
	}

	.cat.active .cat-name {
		color: $brand-primary-deep;
	}

	.cat-check {
		position: absolute;
		top: 14rpx;
		right: 16rpx;
		font-size: 30rpx;
		color: $brand-primary;
	}

	/* 细分小类分组 */
	.group-head {
		font-size: 26rpx;
		color: $brand-ink-2;
		font-weight: 600;
		margin: 24rpx 0 16rpx;
	}

	.group-head:first-child {
		margin-top: 6rpx;
	}

	.group-empty {
		font-size: 24rpx;
		color: #bbb;
		margin-bottom: 10rpx;
	}

	.chip-wrap {
		display: flex;
		flex-wrap: wrap;
	}

	.chip {
		font-size: 26rpx;
		color: #666;
		background: #f3f3f3;
		border-radius: 36rpx;
		padding: 14rpx 36rpx;
		margin: 0 18rpx 18rpx 0;
	}

	.chip.active {
		background: $brand-primary;
		color: #fff;
		font-weight: 600;
	}

	.empty-hint {
		text-align: center;
		font-size: 26rpx;
		color: #bbb;
		padding: 50rpx 24rpx;
	}

	.save-btn {
		margin-top: 20rpx;
		height: 88rpx;
		line-height: 88rpx;
		background: linear-gradient(135deg, #B49A6B 0%, #A6885A 100%);
		color: #fff;
		font-size: 32rpx;
		font-weight: 700;
		border-radius: 44rpx;
	}

	.save-btn::after {
		border: none;
	}

	.skip {
		text-align: center;
		color: #aaa;
		font-size: 26rpx;
		padding: 30rpx 0;
	}
</style>
