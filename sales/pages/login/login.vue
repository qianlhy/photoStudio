<template>
	<view class="login">
		<view class="login-card">
			<view class="logo">{{ brandName }}</view>
			<view class="slogan">{{ slogan }}</view>
			<view class="form">
				<view class="field">
					<text class="ico ico-user"></text>
					<input v-model="username" class="input" placeholder="请输入账号 / 手机号" placeholder-class="ph" />
				</view>
				<view class="field">
					<text class="ico ico-lock"></text>
					<input v-model="password" class="input" password placeholder="请输入密码" placeholder-class="ph" />
				</view>
				<view class="btn-login" @click="doLogin">登 录</view>
				<view class="tip">演示账号：ajie / 123456（销售经理）· admin / 123456（管理员）</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			brandName: '合意传媒',
			slogan: '短视频 · 内容 · 增长',
			username: 'ajie',
			password: '123456'
		}
	},
	onLoad() {
		this.$api.config().then(res => {
			if (res && res.data) {
				if (res.data.brandName) this.brandName = res.data.brandName
				if (res.data.brandSlogan) this.slogan = res.data.brandSlogan
			}
		}).catch(() => {})
	},
	methods: {
		doLogin() {
			if (!this.username || !this.password) {
				uni.showToast({ title: '请输入账号和密码', icon: 'none' })
				return
			}
			this.$api.login({ username: this.username, password: this.password }).then(res => {
				uni.setStorageSync('token', res.token)
				uni.setStorageSync('role', res.role)
				uni.setStorageSync('empId', res.userId)
				uni.setStorageSync('empName', res.name)
				uni.showToast({ title: '登录成功', icon: 'success' })
				setTimeout(() => {
					uni.reLaunch({ url: '/pages/workbench/workbench' })
				}, 400)
			}).catch(() => {})
		}
	}
}
</script>

<style lang="scss" scoped>
.login {
	width: 100%;
	height: 100vh;
	background: linear-gradient(135deg, #F3EEE4 0%, #EAF1FF 100%);
	display: flex;
	align-items: center;
	justify-content: center;
}
.login-card {
	width: 720rpx;
	background: #fff;
	border-radius: 28rpx;
	padding: 64rpx 64rpx 56rpx;
	box-shadow: 0 24rpx 60rpx rgba(31, 39, 51, .12);
	text-align: center;
}
.logo {
	font-size: 52rpx;
	font-weight: 700;
	color: $ink;
	letter-spacing: 4rpx;
}
.slogan {
	font-size: 26rpx;
	color: $muted;
	margin-top: 12rpx;
	margin-bottom: 56rpx;
}
.field {
	display: flex;
	align-items: center;
	height: 96rpx;
	border: 1rpx solid $line;
	border-radius: 16rpx;
	padding: 0 28rpx;
	margin-bottom: 28rpx;
	background: #FAFBFD;
}
.field .ico {
	position: relative;
	display: inline-block;
	width: 32rpx;
	height: 32rpx;
	margin-right: 18rpx;
	flex-shrink: 0;
	color: #A6B0C0;
}
.ico-user::before {
	content: "";
	position: absolute;
	left: 10rpx;
	top: 3rpx;
	width: 13rpx;
	height: 13rpx;
	box-sizing: border-box;
	border: 3rpx solid currentColor;
	border-radius: 50%;
}
.ico-user::after {
	content: "";
	position: absolute;
	left: 4rpx;
	bottom: 3rpx;
	width: 25rpx;
	height: 14rpx;
	box-sizing: border-box;
	border: 3rpx solid currentColor;
	border-radius: 14rpx 14rpx 5rpx 5rpx;
}
.ico-lock::before {
	content: "";
	position: absolute;
	left: 9rpx;
	top: 3rpx;
	width: 15rpx;
	height: 13rpx;
	box-sizing: border-box;
	border: 3rpx solid currentColor;
	border-bottom: 0;
	border-radius: 8rpx 8rpx 0 0;
}
.ico-lock::after {
	content: "";
	position: absolute;
	left: 4rpx;
	bottom: 3rpx;
	width: 25rpx;
	height: 16rpx;
	box-sizing: border-box;
	border: 3rpx solid currentColor;
	border-radius: 4rpx;
}
.input {
	flex: 1;
	font-size: 30rpx;
	color: $ink;
}
.ph {
	color: #B9C0CC;
}
.btn-login {
	height: 96rpx;
	line-height: 96rpx;
	border-radius: 16rpx;
	background: $brand;
	color: #fff;
	font-size: 32rpx;
	font-weight: 700;
	margin-top: 16rpx;
	box-shadow: 0 12rpx 28rpx rgba(47, 107, 255, .28);
}
.tip {
	margin-top: 32rpx;
	font-size: 22rpx;
	color: $muted;
}

/* 小米平板 6 Pro 横屏登录页 */
@media #{$pad-mq-landscape} {
	.login-card {
		width: 520px;
		max-width: 36vw;
		padding: 48px 56px 40px;
		border-radius: 20px;
	}
	.logo {
		font-size: 34px;
	}
	.slogan {
		font-size: 16px;
		margin-bottom: 36px;
	}
	.field {
		height: 52px;
		margin-bottom: 18px;
		border-radius: 12px;
	}
	.field input {
		font-size: 16px;
	}
	.btn-login {
		height: 52px;
		line-height: 52px;
		font-size: 18px;
		border-radius: 12px;
	}
	.tip {
		font-size: 13px;
		margin-top: 20px;
	}
}
</style>
