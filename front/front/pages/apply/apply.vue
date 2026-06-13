<template>
	<view class="page">
		<view class="hd" v-if="pending">
			<text class="hd-title">审核中</text>
			<text class="hd-sub">您的注册申请已提交，请耐心等待商家审核。审核结果将通过消息中心通知您。</text>
		</view>
		<view class="hd" v-else-if="rejected">
			<text class="hd-title">审核未通过</text>
			<text class="hd-sub reject-reason" v-if="reason">原因：{{ reason }}</text>
			<text class="hd-sub">请修改信息后重新提交申请</text>
		</view>
		<view class="hd" v-else>
			<text class="hd-title">注册申请</text>
			<text class="hd-sub">提交后需商家人工审核，通过后方可使用小程序</text>
		</view>

		<view class="card">
			<view class="field">
				<text class="label">手机号</text>
				<input v-model="form.shoujihaoma" type="number" maxlength="11" placeholder="请输入手机号" />
			</view>
			<view class="field">
				<text class="label">姓名</text>
				<input v-model="form.xingming" placeholder="请输入真实姓名" />
			</view>
			<view class="field">
				<text class="label">意向品类</text>
				<view class="chips">
					<view class="chip" :class="{ active: form.yixiangpinlei === '写真' }" @tap="form.yixiangpinlei='写真'">写真</view>
					<view class="chip" :class="{ active: form.yixiangpinlei === '宣传片' }" @tap="form.yixiangpinlei='宣传片'">宣传片</view>
					<view class="chip" :class="{ active: form.yixiangpinlei === '都看看' }" @tap="form.yixiangpinlei='都看看'">都看看</view>
				</view>
			</view>
			<view class="field col">
				<text class="label">备注说明</text>
				<textarea v-model="form.beizhu" placeholder="可填写拍摄需求、人数、时间意向等" />
			</view>
		</view>

		<button class="submit" v-if="!pending" @tap="submit">提交申请</button>
		<view class="back" @tap="goLogin">返回登录</view>
	</view>
</template>

<script>
	import http from '@/api/http.js'
	export default {
		data() {
			return {
				pending: false,
				rejected: false,
				reason: '',
				form: { shoujihaoma: '', xingming: '', yixiangpinlei: '写真', beizhu: '', openid: '' }
			};
		},
		onLoad(opt) {
			this.pending = opt.pending === '1';
			this.rejected = opt.rejected === '1';
			if (opt.openid) this.form.openid = opt.openid;
			this.loadUser();
		},
		methods: {
			async loadUser() {
				try {
					let res = await this.$api.session('yonghu');
					if (res.data) {
						this.form.shoujihaoma = res.data.shoujihaoma || '';
						this.form.xingming = res.data.xingming || '';
						this.form.yixiangpinlei = res.data.yixiangpinlei || '写真';
						this.form.beizhu = res.data.beizhu || '';
						this.reason = res.data.shhf || '';
						if (res.data.sfsh === '否') this.pending = true;
						if (res.data.sfsh === '驳回') this.rejected = true;
					}
				} catch (e) {}
			},
			async submit() {
				if (!this.form.shoujihaoma || !this.form.xingming) {
					this.$utils.msg('请填写手机号和姓名');
					return;
				}
				try {
					let res = await http.post('yonghu/apply', this.form);
					this.$utils.msg(res.msg || '申请已提交');
					this.pending = true;
					this.rejected = false;
				} catch (e) {}
			},
			goLogin() {
				uni.navigateTo({ url: '../login/login' });
			}
		}
	};
</script>

<style lang="scss" scoped>
	page { background: $brand-bg-soft; }
	.page { padding: 30rpx 24rpx; }
	.hd { padding: 20rpx 10rpx 30rpx; }
	.hd-title { display: block; font-size: 40rpx; font-weight: 700; color: $brand-ink; }
	.hd-sub { display: block; font-size: 26rpx; color: $brand-ink-3; margin-top: 12rpx; line-height: 1.6; }
	.reject-reason { color: $brand-price; }
	.card { background: #fff; border-radius: $brand-radius-lg; padding: 10rpx 24rpx; box-shadow: $brand-shadow-card; }
	.field { display: flex; align-items: center; padding: 24rpx 0; border-bottom: 1rpx solid $brand-line;
		&.col { flex-direction: column; align-items: flex-start; }
	}
	.label { width: 160rpx; font-size: 28rpx; color: $brand-ink-2; flex-shrink: 0; }
	input, textarea { flex: 1; font-size: 28rpx; width: 100%; }
	textarea { min-height: 160rpx; margin-top: 12rpx; }
	.chips { display: flex; flex-wrap: wrap; gap: 16rpx; }
	.chip {
		padding: 10rpx 24rpx; border-radius: 999rpx; font-size: 26rpx;
		background: $brand-bg-soft; color: $brand-ink-2;
		&.active { background: $brand-primary-soft; color: $brand-primary-deep; }
	}
	.submit {
		margin-top: 40rpx; height: 88rpx; line-height: 88rpx;
		background: linear-gradient(135deg, $brand-primary, $brand-primary-deep);
		color: #fff; border-radius: $brand-radius-lg; font-size: 30rpx; border: none;
	}
	.back { text-align: center; margin-top: 30rpx; font-size: 26rpx; color: $brand-primary-deep; }
</style>
