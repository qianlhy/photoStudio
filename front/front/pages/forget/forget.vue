<template>
	<view class="forget-page">
		<view class="brand">
			<view class="brand-logo"><text class="cuIcon-lock"></text></view>
			<text class="brand-name">重置密码</text>
			<text class="brand-sub">重置后初始密码为 123456</text>
		</view>

		<view class="card">
			<view class="field">
				<input v-model="username" type="text" class="field-input" placeholder="请输入您的账号" placeholder-class="ph" />
			</view>
			<view class="field">
				<picker @change="optionsChange" :value="index" :range="options">
					<view class="field-picker" :class="{ ph: index === 0 }">{{ options[index] }}</view>
				</picker>
			</view>
			<button class="btn-submit" @tap="onResetPass">重置密码</button>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				options: [
					'请选择登陆用户类型',
					'用户',
					'策划师',
				],
				optionsValues: [
					'',
					'yonghu',
					'cehuashi',
				],
				index: 0,
				username: ''
			}
		},
		methods: {
			async onResetPass() {
				if (!this.username) {
					this.$utils.msg('请输入账号')
					return;
				}
				if (this.optionsValues[this.index] == "") {
					this.$utils.msg('请选择角色')
					return;
				}
				await this.$api.resetPass(`${this.optionsValues[this.index]}`, this.username)
				this.$utils.msgBack("重置密码成功,原始密码为:123456")
			},
			optionsChange(e) {
				this.index = e.target.value
			}
		}
	}
</script>

<style lang="scss" scoped>
	.forget-page {
		min-height: 100vh;
		box-sizing: border-box;
		padding: 0 56rpx;
		background: linear-gradient(180deg, #F3EEE4 0%, #FAF8F4 38%, #FFFFFF 100%);
	}

	.brand {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 130rpx 0 60rpx;
	}

	.brand-logo {
		width: 116rpx;
		height: 116rpx;
		border-radius: 50%;
		background: linear-gradient(135deg, #C7AE80 0%, #B49A6B 100%);
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 12rpx 30rpx rgba(180, 154, 107, 0.35);

		.cuIcon-lock {
			color: #fff;
			font-size: 56rpx;
		}
	}

	.brand-name {
		margin-top: 24rpx;
		font-size: 40rpx;
		font-weight: 600;
		letter-spacing: 4rpx;
		color: $brand-ink;
	}

	.brand-sub {
		margin-top: 12rpx;
		font-size: 24rpx;
		color: $brand-ink-3;
	}

	.card {
		background: #fff;
		border-radius: 28rpx;
		padding: 50rpx 40rpx;
		box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.06);
	}

	.field {
		height: 92rpx;
		padding: 0 28rpx;
		margin-bottom: 28rpx;
		background: $brand-bg-soft;
		border-radius: 16rpx;
		display: flex;
		align-items: center;
	}

	.field-input {
		flex: 1;
		font-size: 28rpx;
		color: $brand-ink;
	}

	.field-picker {
		width: 100%;
		font-size: 28rpx;
		color: $brand-ink;
		line-height: 92rpx;
	}

	.ph {
		color: #bbb;
	}

	.btn-submit {
		margin-top: 16rpx;
		height: 92rpx;
		line-height: 92rpx;
		border-radius: 16rpx;
		background: linear-gradient(135deg, #B49A6B 0%, #A6885A 100%);
		color: #fff;
		font-size: 32rpx;
		letter-spacing: 4rpx;
		box-shadow: 0 10rpx 24rpx rgba(180, 154, 107, 0.3);
	}

	.btn-submit::after {
		border: none;
	}
</style>
