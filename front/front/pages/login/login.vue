<template>
	<view class="login-page">
		<view class="aurora aurora-a"></view>
		<view class="aurora aurora-b"></view>

		<view class="login-body">
			<!-- 品牌区 -->
			<view class="brand">
				<view class="brand-logo">
					<view class="logo-play"></view>
				</view>
				<text class="brand-tag">合意传媒</text>
				<text class="brand-name">影集 · 客户服务</text>
				<text class="brand-slogan">查看服务进度与内容交付</text>
			</view>

			<!-- 登录卡片 -->
			<view class="card glass-card">
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
					<view class="tip">未注册请先提交申请，审核通过后可登录</view>
				</view>
			</view>

			<view class="apply-link" @tap="goApply">没有账号？提交注册申请</view>

			<!-- 开发体验挡板：一键登录，免校验 -->
			<view class="dev-login" @tap="devLogin">一键体验登录（开发用）</view>

			<view class="agreement">登录即代表同意《用户协议》与《隐私政策》</view>
		</view>
	</view>
</template>

<script>
	// 与 db_full 用户 id=11、hy_customer id=6001 对齐，便于登录后绑定服务档案
	const DEV_PHONE = '13823888881'
	const DEV_CODE = '123456'

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
			requestLogin(url, data) {
				return new Promise((resolve, reject) => {
					uni.request({
						url: this.$base.url + url,
						method: 'GET',
						data,
						success: (response) => {
							const body = response.data || {}
							if (response.statusCode === 200 && body.code === 0) {
								resolve(body)
								return
							}
							reject(body)
						},
						fail: reject
					})
				})
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
							const res = await this.requestLogin('yonghu/wxlogin', {
								code: r.code
							})
							await this.afterLogin(res)
						} catch (e) {
							if (e && e.needApply) {
								uni.navigateTo({
									url: '../apply/apply?openid=' + encodeURIComponent(e.openid || '')
								})
								return
							}
							if (e && (e.sfsh === '否' || e.sfsh === '驳回')) {
								const q = e.sfsh === '驳回' ? 'rejected=1' : 'pending=1'
								uni.navigateTo({ url: '../apply/apply?' + q })
								return
							}
							this.$utils.msg((e && e.msg) || '微信登录失败')
						}
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
					const res = await this.requestLogin('yonghu/sendSmsCode', {
						phone: this.phone
					})
					this.$utils.msg((res && res.msg) || '验证码已发送')
					this.smsCountdown = 60
					if (this.timer) clearInterval(this.timer)
					this.timer = setInterval(() => {
						this.smsCountdown--
						if (this.smsCountdown <= 0) clearInterval(this.timer)
					}, 1000)
				} catch (e) {
					this.$utils.msg((e && e.msg) || '验证码发送失败')
				}
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
					const res = await this.requestLogin('yonghu/smslogin', {
						phone: this.phone,
						code: this.smsCode
					})
					await this.afterLogin(res)
				} catch (e) {
					if (e && e.needApply) {
						uni.navigateTo({ url: '../apply/apply' })
						return
					}
					if (e && (e.sfsh === '否' || e.sfsh === '驳回')) {
						const q = e.sfsh === '驳回' ? 'rejected=1' : 'pending=1'
						uni.navigateTo({ url: '../apply/apply?' + q })
						return
					}
					this.$utils.msg((e && e.msg) || '登录失败')
				}
			},
			goApply() {
				uni.login({
					provider: 'weixin',
					success: async (r) => {
						if (!r || !r.code) {
							uni.navigateTo({ url: '../apply/apply' })
							return
						}
						try {
							await this.requestLogin('yonghu/wxlogin', { code: r.code })
							// 已有账号则直接提示去登录
							this.$utils.msg('该微信已有账号，请直接登录')
						} catch (e) {
							const oid = (e && e.openid) ? encodeURIComponent(e.openid) : ''
							uni.navigateTo({ url: '../apply/apply' + (oid ? ('?openid=' + oid) : '') })
						}
					},
					fail: () => {
						uni.navigateTo({ url: '../apply/apply' })
					}
				})
			},
			async devLogin() {
				uni.removeStorageSync('hyCustomerId')
				try {
					const res = await this.requestLogin('yonghu/smslogin', {
						phone: DEV_PHONE,
						code: DEV_CODE
					})
					await this.afterLogin(res)
					return
				} catch (e) {}
				try {
					const res = await this.requestLogin('yonghu/login', {
						username: '账号1',
						password: DEV_CODE
					})
					await this.afterLogin(res)
					return
				} catch (e) {
					const msg = (e && e.msg) ? e.msg : '体验登录失败，请确认后端已启动'
					this.$utils.msg(msg)
				}
			},
			async afterLogin(res) {
				if (!res || !res.token) {
					if (res && res.sfsh) {
						if (res.sfsh === '否') uni.navigateTo({ url: '../apply/apply?pending=1' });
						if (res.sfsh === '驳回') uni.navigateTo({ url: '../apply/apply?rejected=1' });
					}
					return;
				}
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
							uni.reLaunch({ url: '../hy-service/service' });
						}
					})
					return
				}
				uni.reLaunch({ url: '../hy-service/service' });
			}
		}
	}
</script>

<style lang="scss" scoped>
	.login-page {
		position: relative;
		min-height: 100vh;
		overflow: hidden;
		background: linear-gradient(180deg, #F0FBFC 0%, #F7FCFC 48%, #F4FBFA 100%);
		color: #10244B;
	}

	.aurora {
		position: absolute;
		border-radius: 50%;
		filter: blur(8rpx);
		pointer-events: none;
	}

	.aurora-a {
		top: -120rpx;
		right: -140rpx;
		width: 520rpx;
		height: 480rpx;
		background: radial-gradient(circle, rgba(110, 229, 220, .32), rgba(160, 215, 251, .12) 48%, transparent 70%);
	}

	.aurora-b {
		top: 260rpx;
		left: -220rpx;
		width: 520rpx;
		height: 380rpx;
		background: radial-gradient(circle, rgba(146, 220, 239, .18), transparent 70%);
	}

	.login-body {
		position: relative;
		z-index: 2;
		min-height: 100vh;
		box-sizing: border-box;
		padding: 0 56rpx;
		display: flex;
		flex-direction: column;
	}

	.brand {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding-top: calc(var(--status-bar-height, 40rpx) + 120rpx);
		padding-bottom: 56rpx;
	}

	.brand-logo {
		width: 132rpx;
		height: 132rpx;
		border-radius: 50%;
		border: 5rpx solid rgba(255, 255, 255, .86);
		background: linear-gradient(145deg, #5DDAEA, #B6F5F2);
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 12rpx 30rpx rgba(58, 187, 201, .28);
	}

	.logo-play {
		margin-left: 8rpx;
		border-left: 34rpx solid rgba(255, 255, 255, .95);
		border-top: 22rpx solid transparent;
		border-bottom: 22rpx solid transparent;
		filter: drop-shadow(0 3rpx 4rpx rgba(29, 151, 173, .18));
	}

	.brand-tag {
		margin-top: 28rpx;
		font-size: 28rpx;
		font-weight: 700;
		color: #77879C;
		letter-spacing: 2rpx;
	}

	.brand-name {
		margin-top: 12rpx;
		font-size: 44rpx;
		font-weight: 700;
		letter-spacing: 2rpx;
		color: #122956;
	}

	.brand-slogan {
		margin-top: 14rpx;
		font-size: 25rpx;
		color: #99A6B8;
	}

	.glass-card {
		border: 1rpx solid rgba(214, 230, 235, .78);
		border-radius: 28rpx;
		background: rgba(255, 255, 255, .68);
		box-shadow: 0 10rpx 30rpx rgba(68, 103, 119, .055), inset 0 1rpx 0 rgba(255, 255, 255, .86);
		backdrop-filter: blur(18rpx);
	}

	.card {
		padding: 50rpx 44rpx 56rpx;
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
		color: #99A6B8;
		padding-bottom: 16rpx;
	}

	.tab.active {
		color: #122956;
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
		background: linear-gradient(90deg, #38C9DC, #44DDC8);
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
		background: rgba(255, 255, 255, .72);
		border: 1rpx solid rgba(214, 230, 235, .72);
		border-radius: 16rpx;
	}

	.field-icon {
		font-size: 36rpx;
		color: #8FA6B1;
		margin-right: 16rpx;
	}

	.field-input {
		flex: 1;
		font-size: 28rpx;
		color: #122956;
	}

	.ph {
		color: #B0BAC7;
	}

	.code-btn {
		font-size: 26rpx;
		color: #43BFB7;
		padding-left: 24rpx;
		margin-left: 8rpx;
		border-left: 1rpx solid rgba(214, 230, 235, .9);
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
		background: linear-gradient(135deg, #38C9DC, #44DDC8);
		color: #fff;
		font-size: 32rpx;
		letter-spacing: 8rpx;
		box-shadow: 0 10rpx 24rpx rgba(58, 187, 201, .28);
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
		box-shadow: 0 10rpx 24rpx rgba(7, 193, 96, .25);

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
		color: #99A6B8;
	}

	.apply-link {
		margin: 24rpx auto 0;
		text-align: center;
		font-size: 26rpx;
		color: #43BFB7;
	}

	.dev-login {
		margin: 20rpx auto 0;
		text-align: center;
		font-size: 26rpx;
		color: #69CEF2;
		text-decoration: underline;
		padding: 16rpx 0;
	}

	.agreement {
		margin-top: auto;
		text-align: center;
		padding: 30rpx 0 40rpx;
		font-size: 22rpx;
		color: #A7B1BF;
	}
</style>
