<template>
	<view class="page" v-if="detail.id">
		<!-- 顶部大图 -->
		<swiper class="hero" :indicator-dots="coverList.length > 1" indicator-active-color="#ffffff"
			indicator-color="rgba(255,255,255,0.5)" circular :autoplay="true" interval="4500">
			<swiper-item v-for="(img, idx) in coverList" :key="idx">
				<image class="hero-img" :src="baseUrl + img" mode="aspectFill" @tap="previewCover(idx)"></image>
			</swiper-item>
		</swiper>

		<!-- 概要卡片 -->
		<view class="card head">
			<view class="title-row">
				<text class="title">{{ detail.taocanmingcheng }}</text>
				<view class="fav" @tap="toggleStoreup">
					<text :class="storeupFlag ? 'cuIcon-favorfill' : 'cuIcon-favor'"></text>
					<text class="fav-text">{{ storeupFlag ? '已收藏' : '收藏' }}</text>
				</view>
			</view>
			<view class="price-row">
				<text class="price-symbol">¥</text>
				<text class="price">{{ detail.xianxiabiaojia }}</text>
				<text class="price-tip">线下标价 · 到店缴费锁定档期</text>
			</view>
			<view class="meta-row">
				<text v-if="detail.pinlei" class="meta-tag">{{ detail.pinlei }}</text>
				<text v-if="detail.fengge" class="meta-tag">{{ detail.fengge }}</text>
				<text class="meta-hot">🔥 {{ detail.clicknum || 0 }} 人想拍</text>
			</view>
		</view>

		<!-- 功能图标网格 -->
		<view class="card feature-card">
			<view class="feature" v-for="(f, i) in features" :key="i">
				<text class="feature-icon" :class="f.icon"></text>
				<text class="feature-label">{{ f.label }}</text>
			</view>
		</view>

		<!-- 驼色套餐条 -->
		<view class="ribbon">
			<text class="ribbon-left">套餐内容</text>
			<text class="ribbon-right">{{ detail.taocanmingcheng }}</text>
		</view>

		<!-- 简介 -->
		<view class="card" v-if="detail.jianjie">
			<view class="sec-title">套餐介绍</view>
			<rich-text class="brief" :nodes="briefNodes"></rich-text>
		</view>

		<!-- 附加升级 -->
		<view class="card" v-if="detail.shengjixiangmu">
			<view class="sec-title">附加升级项目</view>
			<text class="upgrade-text">{{ detail.shengjixiangmu }}</text>
		</view>

		<!-- 实拍样片 -->
		<view class="card" v-if="shotList.length">
			<view class="sec-title">实拍样片</view>
			<image v-for="(img, idx) in shotList" :key="idx" class="shot-img" :src="baseUrl + img" mode="widthFix"
				@tap="preview(idx)"></image>
		</view>

		<!-- 服务说明 -->
		<view class="service">
			<view class="service-line"><text class="service-key">服务团队</text>专属摄影师 + 化妆师全程一对一服务</view>
			<view class="service-line"><text class="service-key">拍摄场地</text>专业摄影基地实景拍摄</view>
			<view class="service-line"><text class="service-key">出片说明</text>下单仅锁定档期，到店缴费，规则见“我的-门店须知”</view>
		</view>

		<view class="bottom-space"></view>

		<!-- 底部操作栏 -->
		<view class="footbar">
			<view class="foot-act" @tap="contactService">
				<text class="cuIcon-service foot-icon"></text>
				<text class="foot-label">客服</text>
			</view>
			<button class="foot-act foot-share" open-type="share">
				<text class="cuIcon-share foot-icon"></text>
				<text class="foot-label">分享</text>
			</button>
			<view class="book-btn" @tap="openForm">立即预约</view>
		</view>

		<!-- 预约表单弹层 -->
		<view v-if="showForm" class="mask" @tap="closeForm"></view>
		<view class="sheet" :class="{ show: showForm }">
			<view class="sheet-head">
				<text class="sheet-title">预约下单</text>
				<text class="cuIcon-close sheet-close" @tap="closeForm"></text>
			</view>
			<view class="sheet-sub">仅锁定档期，到店缴费</view>
			<view class="form-item">
				<text class="form-label">联系人</text>
				<input class="form-input" v-model="form.xingming" placeholder="请输入姓名" placeholder-class="ph" />
			</view>
			<view class="form-item">
				<text class="form-label">手机号</text>
				<input class="form-input" type="number" v-model="form.shoujihaoma" placeholder="请输入手机号"
					placeholder-class="ph" />
			</view>
			<view class="form-item">
				<text class="form-label">拍摄人数</text>
				<input class="form-input" type="number" v-model="form.paisherenshu" placeholder="如 1"
					placeholder-class="ph" />
			</view>
			<view class="form-item">
				<text class="form-label">意向档期</text>
				<picker mode="date" :value="form.yixiangdangqi" :start="today" @change="onDateChange">
					<view class="form-picker" :class="{ ph: !form.yixiangdangqi }">
						{{ form.yixiangdangqi || '请选择拍摄日期' }}
					</view>
				</picker>
			</view>
			<view class="form-item col">
				<text class="form-label">个性化需求</text>
				<textarea class="form-textarea" v-model="form.beizhu" placeholder="如希望的场景、造型、参考风格等"
					placeholder-class="ph" />
			</view>
			<view class="sheet-btn" @tap="submitOrder">确认预约</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				id: '',
				detail: {},
				user: {},
				storeupFlag: 0,
				today: '',
				showForm: false,
				form: {
					xingming: '',
					shoujihaoma: '',
					paisherenshu: '',
					yixiangdangqi: '',
					beizhu: ''
				}
			};
		},
		computed: {
			baseUrl() {
				return this.$base.url;
			},
			coverList() {
				if (!this.detail.fengmian) return [];
				return this.detail.fengmian.split(',').filter(i => i);
			},
			shotList() {
				if (!this.detail.shctp) return [];
				return this.detail.shctp.split(',').filter(i => i);
			},
			briefNodes() {
				let t = this.detail.jianjie || '';
				if (/<[a-z][\s\S]*>/i.test(t)) return t;
				return '<p style="font-size:26rpx;color:#666;line-height:44rpx;">' + t + '</p>';
			},
			features() {
				let d = this.detail;
				let arr = [];
				arr.push({
					icon: 'cuIcon-time',
					label: d.paishishichang || '全天拍摄'
				});
				arr.push({
					icon: 'cuIcon-pic',
					label: d.jingxiuzhangshu ? (d.jingxiuzhangshu + ' 张精修') : '专业精修'
				});
				arr.push({
					icon: 'cuIcon-goods',
					label: d.fuzhuangshuliang ? (d.fuzhuangshuliang + ' 套服装') : '服装提供'
				});
				arr.push({
					icon: 'cuIcon-female',
					label: '专业化妆'
				});
				arr.push({
					icon: 'cuIcon-friend',
					label: '一对一服务'
				});
				arr.push({
					icon: 'cuIcon-safe',
					label: '无隐形消费'
				});
				return arr;
			}
		},
		async onLoad(options) {
			this.id = options.id;
			let d = new Date();
			this.today = `${d.getFullYear()}-${this.pad(d.getMonth() + 1)}-${this.pad(d.getDate())}`;
			let table = uni.getStorageSync('nowTable');
			if (table) {
				try {
					let res = await this.$api.session(table);
					this.user = res.data;
					this.form.xingming = this.user.xingming || '';
					this.form.shoujihaoma = this.user.shoujihaoma || '';
				} catch (e) {}
			}
			await this.loadDetail();
			this.getStoreup();
		},
		onShareAppMessage() {
			return {
				title: this.detail.taocanmingcheng || '精选拍摄套餐',
				path: '/pages/taocan/detail?id=' + this.id
			};
		},
		methods: {
			pad(n) {
				return n < 10 ? '0' + n : '' + n;
			},
			async loadDetail() {
				let res = await this.$api.info('taocan', this.id);
				this.detail = res.data || {};
			},
			previewCover(idx) {
				uni.previewImage({
					current: idx,
					urls: this.coverList.map(i => this.baseUrl + i)
				});
			},
			preview(idx) {
				uni.previewImage({
					current: idx,
					urls: this.shotList.map(i => this.baseUrl + i)
				});
			},
			contactService() {
				uni.showModal({
					title: '联系客服',
					content: '拨打门店电话 400-000-0000 或在“我的-门店须知”查看更多联系方式',
					confirmText: '拨打',
					success: (r) => {
						if (r.confirm) {
							uni.makePhoneCall({
								phoneNumber: '4000000000',
								fail: () => {}
							});
						}
					}
				});
			},
			openForm() {
				if (!this.user || !this.user.id) {
					this.$utils.msg('请先登录');
					return;
				}
				this.showForm = true;
			},
			closeForm() {
				this.showForm = false;
			},
			onDateChange(e) {
				this.form.yixiangdangqi = e.detail.value;
			},
			async getStoreup() {
				if (!this.user || !this.user.id) return;
				let res = await this.$api.list('storeup', {
					page: 1,
					limit: 1,
					refid: this.id,
					tablename: 'taocan',
					userid: this.user.id,
					type: 1
				});
				this.storeupFlag = res.data.list.length;
			},
			async toggleStoreup() {
				if (!this.user || !this.user.id) {
					this.$utils.msg('请先登录');
					return;
				}
				let res = await this.$api.list('storeup', {
					page: 1,
					limit: 1,
					refid: this.id,
					tablename: 'taocan',
					userid: this.user.id,
					type: 1
				});
				if (res.data.list.length >= 1) {
					let storeupId = res.data.list[0].id;
					await this.$api.del('storeup', JSON.stringify([storeupId]));
					this.$utils.msg('已取消收藏');
					this.getStoreup();
					return;
				}
				let cover = this.coverList.length ? this.coverList[0] : '';
				await this.$api.add('storeup', {
					userid: this.user.id,
					name: this.detail.taocanmingcheng,
					picture: cover,
					refid: this.detail.id,
					tablename: 'taocan',
					type: 1,
					inteltype: this.detail.pinlei
				});
				this.$utils.msg('收藏成功');
				this.getStoreup();
			},
			async submitOrder() {
				if (!this.user || !this.user.id) {
					this.$utils.msg('请先登录');
					return;
				}
				if (!this.form.xingming) return this.$utils.msg('请填写联系人');
				if (!this.form.shoujihaoma) return this.$utils.msg('请填写手机号');
				if (!this.form.yixiangdangqi) return this.$utils.msg('请选择意向档期');
				let data = {
					taocanid: this.detail.id,
					zhanghao: this.user.zhanghao || this.user.username || '',
					xingming: this.form.xingming,
					shoujihaoma: this.form.shoujihaoma,
					paisherenshu: this.form.paisherenshu || 1,
					yixiangdangqi: this.form.yixiangdangqi,
					beizhu: this.form.beizhu
				};
				let res = await this.$api.add('dingdan', data);
				this.showForm = false;
				uni.showModal({
					title: '预约成功',
					content: `订单号：${res.dingdanbianhao}\n当前排队序号：${res.paiduixuhao}`,
					showCancel: false,
					success: () => {
						uni.switchTab({
							url: '../dingdan/list',
							fail: () => {
								this.$utils.jump('../dingdan/list');
							}
						});
					}
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
		padding-bottom: 130rpx;
	}

	.hero {
		width: 100%;
		height: 600rpx;
	}

	.hero-img {
		width: 100%;
		height: 100%;
	}

	.card {
		background: #fff;
		margin: 20rpx 24rpx;
		border-radius: $brand-radius-lg;
		padding: 30rpx;
		box-shadow: $brand-shadow-card;
	}

	.head {
		margin-top: -44rpx;
		position: relative;
		z-index: 5;
	}

	.title-row {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
	}

	.title {
		flex: 1;
		font-size: 38rpx;
		font-weight: 700;
		color: $brand-ink;
		line-height: 52rpx;
	}

	.fav {
		display: flex;
		flex-direction: column;
		align-items: center;
		color: $brand-primary;
		font-size: 40rpx;
		margin-left: 24rpx;
	}

	.fav-text {
		font-size: 20rpx;
		color: $brand-ink-3;
		margin-top: 2rpx;
	}

	.price-row {
		display: flex;
		align-items: baseline;
		margin-top: 18rpx;
	}

	.price-symbol {
		color: $brand-price;
		font-size: 30rpx;
		font-weight: 600;
	}

	.price {
		color: $brand-price;
		font-size: 52rpx;
		font-weight: 700;
		margin-right: 16rpx;
	}

	.price-tip {
		color: $brand-ink-3;
		font-size: 22rpx;
	}

	.meta-row {
		display: flex;
		align-items: center;
		margin-top: 18rpx;
	}

	.meta-tag {
		font-size: 22rpx;
		color: $brand-primary-deep;
		background: $brand-primary-soft;
		border-radius: $brand-radius-pill;
		padding: 4rpx 18rpx;
		margin-right: 14rpx;
	}

	.meta-hot {
		font-size: 22rpx;
		color: $brand-ink-3;
		margin-left: auto;
	}

	/* 功能网格 */
	.feature-card {
		display: flex;
		flex-wrap: wrap;
		padding: 36rpx 10rpx 14rpx;
	}

	.feature {
		width: 33.33%;
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-bottom: 30rpx;
	}

	.feature-icon {
		font-size: 50rpx;
		color: $brand-primary;
	}

	.feature-label {
		margin-top: 14rpx;
		font-size: 24rpx;
		color: $brand-ink-2;
	}

	/* 驼色条 */
	.ribbon {
		margin: 4rpx 24rpx 0;
		height: 84rpx;
		border-radius: $brand-radius;
		background: linear-gradient(90deg, #C9B796 0%, #B49A6B 100%);
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0 30rpx;
	}

	.ribbon-left {
		color: #fff;
		font-size: 28rpx;
		font-weight: 600;
		letter-spacing: 2rpx;
	}

	.ribbon-right {
		color: rgba(255, 255, 255, 0.9);
		font-size: 26rpx;
	}

	.sec-title {
		font-size: 30rpx;
		font-weight: 700;
		color: $brand-ink;
		margin-bottom: 20rpx;
		padding-left: 16rpx;
		border-left: 6rpx solid $brand-primary;
	}

	.brief {
		font-size: 26rpx;
		color: $brand-ink-2;
		line-height: 44rpx;
	}

	.upgrade-text {
		display: block;
		font-size: 26rpx;
		color: $brand-ink-2;
		line-height: 44rpx;
	}

	.shot-img {
		width: 100%;
		border-radius: $brand-radius;
		margin-bottom: 16rpx;
	}

	/* 服务说明 */
	.service {
		margin: 0 24rpx;
		padding: 6rpx 6rpx 0;
	}

	.service-line {
		font-size: 24rpx;
		color: $brand-ink-3;
		line-height: 44rpx;
	}

	.service-key {
		color: $brand-primary-deep;
		margin-right: 12rpx;
	}

	.bottom-space {
		height: 30rpx;
	}

	/* 底部操作栏 */
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
		padding: 0 24rpx;
		box-sizing: border-box;
	}

	.foot-act {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		width: 96rpx;
		color: $brand-ink-2;
		background: transparent;
		line-height: 1.2;
	}

	.foot-act::after {
		border: none;
	}

	.foot-share {
		padding: 0;
		margin: 0;
		font-size: inherit;
	}

	.foot-icon {
		font-size: 40rpx;
		color: $brand-ink-2;
	}

	.foot-label {
		font-size: 20rpx;
		color: $brand-ink-3;
		margin-top: 2rpx;
	}

	.book-btn {
		flex: 1;
		height: 84rpx;
		line-height: 84rpx;
		text-align: center;
		margin-left: 16rpx;
		border-radius: $brand-radius-pill;
		background: linear-gradient(135deg, #B0C293 0%, #9BB07C 100%);
		color: #fff;
		font-size: 32rpx;
		font-weight: 600;
		letter-spacing: 4rpx;
		box-shadow: 0 8rpx 20rpx rgba(155, 176, 124, 0.35);
	}

	/* 弹层 */
	.mask {
		position: fixed;
		left: 0;
		top: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.45);
		z-index: 20;
	}

	.sheet {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 21;
		background: #fff;
		border-radius: 28rpx 28rpx 0 0;
		padding: 30rpx 36rpx calc(40rpx + env(safe-area-inset-bottom));
		transform: translateY(110%);
		transition: transform 0.28s ease;
	}

	.sheet.show {
		transform: translateY(0);
	}

	.sheet-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.sheet-title {
		font-size: 34rpx;
		font-weight: 700;
		color: $brand-ink;
	}

	.sheet-close {
		font-size: 38rpx;
		color: $brand-ink-3;
	}

	.sheet-sub {
		font-size: 22rpx;
		color: $brand-ink-3;
		margin: 8rpx 0 20rpx;
	}

	.form-item {
		display: flex;
		align-items: center;
		padding: 22rpx 0;
		border-bottom: 1rpx solid #f3f3f3;
	}

	.form-item.col {
		flex-direction: column;
		align-items: flex-start;
		border-bottom: none;
	}

	.form-label {
		width: 160rpx;
		font-size: 28rpx;
		color: $brand-ink;
	}

	.form-input {
		flex: 1;
		font-size: 28rpx;
		color: $brand-ink;
	}

	.form-picker {
		font-size: 28rpx;
		color: $brand-ink;
	}

	.ph {
		color: #bbb;
	}

	.form-textarea {
		width: 100%;
		height: 150rpx;
		margin-top: 16rpx;
		font-size: 28rpx;
		background: $brand-bg-soft;
		border-radius: $brand-radius;
		padding: 18rpx;
		box-sizing: border-box;
	}

	.sheet-btn {
		margin-top: 30rpx;
		height: 92rpx;
		line-height: 92rpx;
		text-align: center;
		border-radius: $brand-radius;
		background: linear-gradient(135deg, #B0C293 0%, #9BB07C 100%);
		color: #fff;
		font-size: 32rpx;
		font-weight: 600;
		letter-spacing: 4rpx;
	}
</style>
