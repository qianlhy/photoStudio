<template>
	<view class="login-page">
		<!-- 品牌区 -->
		<view class="brand">
			<view class="brand-logo">
				<text class="cuIcon-camera"></text>
			</view>
			<text class="brand-name">云漫 · 照相馆</text>
			<text class="brand-slogan">记录每一刻值得珍藏的光影</text>
		</view>

		<!-- 登录卡片 -->
		<view class="card">
			<view class="tabs">
				<view class="tab" :class="{ active: loginType === 'wx' }" @tap="switchType('wx')">
					微信登录
				</view>
				<view class="tab" :class="{ active: loginType === 'phone' }" @tap="switchType('phone')">
					手机号登录
				</view>
			</view>

			<!-- 微信登录 -->
			<view v-if="loginType === 'wx'" class="pane">
				<button class="btn-wx" @tap="wxLogin">
					<text class="cuIcon-weixin"></text>
					<text class="btn-wx-text">微信一键登录</text>
				</button>
				<view class="tip">使用微信账号快速登录 / 注册</view>
			</view>

			<!-- 手机号登录 -->
			<view v-else class="pane">
				<view class="field">
					<text class="cuIcon-mobile field-icon"></text>
					<input v-model="phone" type="number" maxlength="11" class="field-input" placeholder="请输入手机号"
						placeholder-class="ph" />
				</view>
				<view class="field">
					<text class="cuIcon-lock field-icon"></text>
					<input v-model="smsCode" type="number" maxlength="6" class="field-input code-input" placeholder="请输入验证码"
						placeholder-class="ph" />
					<view class="code-btn" :class="{ disabled: smsCountdown > 0 }" @tap="sendSms">
						{{ smsCountdown > 0 ? smsCountdown + 's' : '获取验证码' }}
					</view>
				</view>
				<button class="btn-primary" @tap="phoneLogin">登 录</button>
				<view class="tip">未注册的手机号验证后将自动创建账号</view>
			</view>
		</view>

		<!-- 开发体验挡板：一键登录，免校验 -->
		<view class="dev-login" @tap="devLogin">一键体验登录（开发用）</view>

		<view class="agreement">登录即代表同意《用户协议》与《隐私政策》</view>
	</view>
</template>

<script>
	import http from '@/api/http.js'
	export default {
		data() {
			return {
				loginType: 'wx',
				phone: '',
				smsCode: '',
				smsCountdown: 0,
				timer: null
			}
		},
		onUnload() {
			if (this.timer) clearInterval(this.timer)
		},
		methods: {
			switchType(t) {
				this.loginType = t
			},
			wxLogin() {
				uni.login({
					provider: 'weixin',
					success: async (r) => {
						if (!r || !r.code) {
							this.$utils.msg('微信登录失败，请重试')
							return
						}
						try {
							const res = await http.get('yonghu/wxlogin', {
								code: r.code
							})
							await this.afterLogin(res)
						} catch (e) {}
					},
					fail: () => {
						this.$utils.msg('微信登录失败，请重试')
					}
				})
			},
			async sendSms() {
				if (this.smsCountdown > 0) return
				if (!/^1\d{10}$/.test(this.phone)) {
					this.$utils.msg('请输入正确的手机号')
					return
				}
				try {
					const res = await http.get('yonghu/sendSmsCode', {
						phone: this.phone
					})
					this.$utils.msg((res && res.msg) || '验证码已发送')
					this.smsCountdown = 60
					if (this.timer) clearInterval(this.timer)
					this.timer = setInterval(() => {
						this.smsCountdown--
						if (this.smsCountdown <= 0) clearInterval(this.timer)
					}, 1000)
				} catch (e) {}
			},
			async phoneLogin() {
				if (!this.phone) {
					this.$utils.msg('请输入手机号')
					return
				}
				if (!this.smsCode) {
					this.$utils.msg('请输入验证码')
					return
				}
				try {
					const res = await http.get('yonghu/smslogin', {
						phone: this.phone,
						code: this.smsCode
					})
					await this.afterLogin(res)
				} catch (e) {}
			},
			// 开发体验挡板：固定手机号 + 模拟验证码 123456 一键登录
			async devLogin() {
				try {
					const res = await http.get('yonghu/smslogin', {
						phone: '13800138000',
						code: '123456'
					})
					await this.afterLogin(res)
				} catch (e) {
					this.$utils.msg('体验登录失败，请确认后端已启动')
				}
			},
			async afterLogin(res) {
				if (!res || !res.token) return
				uni.removeStorageSync('useridTag')
				uni.setStorageSync('token', res.token)
				uni.setStorageSync('nowTable', 'yonghu')
				uni.setStorageSync('role', '用户')
				const s = await this.$api.session('yonghu')
				uni.setStorageSync('userid', s.data.id)
				uni.setStorageSync('nickname', s.data.xingming || s.data.zhanghao || '')
				if (s.data.vip) {
					uni.setStorageSync('vip', s.data.vip)
				}
				if (res.needPreference || !s.data.pianhao) {
					uni.navigateTo({
						url: '../preference/preference',
						fail: () => {
							this.$utils.tab('../index/index')
						}
					})
					return
				}
				this.$utils.tab('../index/index')
			}
		}
	}
</script>

<style lang="scss" scoped>
	.login-page {
		min-height: 100vh;
		box-sizing: border-box;
		padding: 0 56rpx;
		display: flex;
		flex-direction: column;
		background: linear-gradient(180deg, #F3EEE4 0%, #FAF8F4 38%, #FFFFFF 100%);
	}

	.brand {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding-top: 150rpx;
		padding-bottom: 70rpx;
	}

	.brand-logo {
		width: 132rpx;
		height: 132rpx;
		border-radius: 50%;
		background: linear-gradient(135deg, #C7AE80 0%, #B49A6B 100%);
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 12rpx 30rpx rgba(180, 154, 107, 0.35);

		.cuIcon-camera {
			color: #fff;
			font-size: 66rpx;
		}
	}

	.brand-name {
		margin-top: 28rpx;
		font-size: 42rpx;
		font-weight: 600;
		letter-spacing: 4rpx;
		color: $brand-ink;
	}

	.brand-slogan {
		margin-top: 14rpx;
		font-size: 24rpx;
		color: $brand-ink-3;
		letter-spacing: 2rpx;
	}

	.card {
		background: #fff;
		border-radius: 28rpx;
		padding: 50rpx 44rpx 56rpx;
		box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.06);
	}

	.tabs {
		display: flex;
		justify-content: center;
		margin-bottom: 56rpx;
	}

	.tab {
		position: relative;
		margin: 0 36rpx;
		font-size: 30rpx;
		color: $brand-ink-3;
		padding-bottom: 16rpx;
	}

	.tab.active {
		color: $brand-ink;
		font-weight: 600;
	}

	.tab.active::after {
		content: '';
		position: absolute;
		left: 50%;
		bottom: 0;
		transform: translateX(-50%);
		width: 44rpx;
		height: 5rpx;
		border-radius: 5rpx;
		background: $brand-primary;
	}

	.pane {
		display: flex;
		flex-direction: column;
	}

	.field {
		display: flex;
		align-items: center;
		height: 92rpx;
		padding: 0 28rpx;
		margin-bottom: 28rpx;
		background: $brand-bg-soft;
		border-radius: 16rpx;
	}

	.field-icon {
		font-size: 36rpx;
		color: $brand-ink-3;
		margin-right: 16rpx;
	}

	.field-input {
		flex: 1;
		font-size: 28rpx;
		color: $brand-ink;
	}

	.ph {
		color: #bbb;
	}

	.code-btn {
		font-size: 26rpx;
		color: $brand-primary-deep;
		padding-left: 24rpx;
		margin-left: 8rpx;
		border-left: 1rpx solid #e2e2e2;
		white-space: nowrap;
	}

	.code-btn.disabled {
		color: #c0c0c0;
	}

	.btn-primary {
		margin-top: 16rpx;
		height: 92rpx;
		line-height: 92rpx;
		border-radius: 16rpx;
		background: linear-gradient(135deg, #B49A6B 0%, #A6885A 100%);
		color: #fff;
		font-size: 32rpx;
		letter-spacing: 8rpx;
		box-shadow: 0 10rpx 24rpx rgba(180, 154, 107, 0.3);
	}

	.btn-primary::after {
		border: none;
	}

	.btn-wx {
		display: flex;
		align-items: center;
		justify-content: center;
		height: 92rpx;
		border-radius: 16rpx;
		background: #07c160;
		color: #fff;
		font-size: 32rpx;
		box-shadow: 0 10rpx 24rpx rgba(7, 193, 96, 0.25);

		.cuIcon-weixin {
			font-size: 40rpx;
			margin-right: 14rpx;
		}
	}

	.btn-wx::after {
		border: none;
	}

	.btn-wx-text {
		letter-spacing: 2rpx;
	}

	.tip {
		text-align: center;
		margin-top: 30rpx;
		font-size: 24rpx;
		color: $brand-ink-3;
	}

	.dev-login {
		margin: 40rpx auto 0;
		text-align: center;
		font-size: 26rpx;
		color: $brand-primary-deep;
		text-decoration: underline;
		padding: 16rpx 0;
	}

	.agreement {
		margin-top: auto;
		text-align: center;
		padding: 30rpx 0 40rpx;
		font-size: 22rpx;
		color: #b9b9b9;
	}
</style>
