<template>
	<view class="reg-page">
		<view class="brand">
			<view class="brand-logo"><text class="cuIcon-camera"></text></view>
			<text class="brand-name">注册账号</text>
			<text class="brand-sub">加入云漫 · 照相馆</text>
		</view>

		<view class="card">
			<!-- 用户注册 -->
			<block v-if="tableName == 'yonghu'">
				<view class="field">
					<input v-model="ruleForm.zhanghao" type="text" class="field-input" placeholder="账号" placeholder-class="ph" />
				</view>
				<view class="field">
					<input v-model="ruleForm.mima" type="password" class="field-input" placeholder="密码" placeholder-class="ph" />
				</view>
				<view class="field">
					<input v-model="ruleForm.mima2" type="password" class="field-input" placeholder="确认密码" placeholder-class="ph" />
				</view>
				<view class="field">
					<input v-model="ruleForm.xingming" type="text" class="field-input" placeholder="姓名" placeholder-class="ph" />
				</view>
				<view class="field">
					<picker @change="yonghuxingbieChange" :value="yonghuxingbieIndex" :range="yonghuxingbieOptions">
						<view class="field-picker" :class="{ ph: !ruleForm.xingbie }">
							{{ ruleForm.xingbie || '请选择性别' }}
						</view>
					</picker>
				</view>
				<view class="field">
					<input v-model="ruleForm.youxiang" type="text" class="field-input" placeholder="邮箱（选填）" placeholder-class="ph" />
				</view>
				<view class="field">
					<input v-model="ruleForm.shoujihaoma" type="text" class="field-input" placeholder="手机号码（选填）" placeholder-class="ph" />
				</view>
			</block>

			<!-- 策划师注册 -->
			<block v-if="tableName == 'cehuashi'">
				<view class="field">
					<input v-model="ruleForm.cehuazhanghao" type="text" class="field-input" placeholder="策划账号" placeholder-class="ph" />
				</view>
				<view class="field">
					<input v-model="ruleForm.mima" type="password" class="field-input" placeholder="密码" placeholder-class="ph" />
				</view>
				<view class="field">
					<input v-model="ruleForm.mima2" type="password" class="field-input" placeholder="确认密码" placeholder-class="ph" />
				</view>
				<view class="field">
					<input v-model="ruleForm.cehuaxingming" type="text" class="field-input" placeholder="策划姓名" placeholder-class="ph" />
				</view>
				<view class="field">
					<picker @change="cehuashixingbieChange" :value="cehuashixingbieIndex" :range="cehuashixingbieOptions">
						<view class="field-picker" :class="{ ph: !ruleForm.xingbie }">
							{{ ruleForm.xingbie || '请选择性别' }}
						</view>
					</picker>
				</view>
				<view class="field">
					<input v-model="ruleForm.youxiang" type="text" class="field-input" placeholder="邮箱（选填）" placeholder-class="ph" />
				</view>
				<view class="field">
					<input v-model="ruleForm.lianxishouji" type="text" class="field-input" placeholder="联系手机" placeholder-class="ph" />
				</view>
			</block>

			<button class="btn-submit" @tap="register">注 册</button>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				yonghuxingbieOptions: [],
				yonghuxingbieIndex: 0,
				cehuashixingbieOptions: [],
				cehuashixingbieIndex: 0,
				ruleForm: {},
				emailcode: "",
				tableName: ""
			}
		},
		async onLoad() {
			let table = uni.getStorageSync("loginTable");
			this.tableName = table;
			if (this.tableName == 'yonghu') {
				this.yonghuxingbieOptions = "男,女".split(',');
				this.$set(this.ruleForm, 'xingbie', this.yonghuxingbieOptions[0]);
			}
			if (this.tableName == 'cehuashi') {
				this.cehuashixingbieOptions = "男,女".split(',');
				this.$set(this.ruleForm, 'xingbie', this.cehuashixingbieOptions[0]);
			}
		},
		methods: {
			yonghuxingbieChange(e) {
				this.yonghuxingbieIndex = e.target.value
				this.$set(this.ruleForm, 'xingbie', this.yonghuxingbieOptions[this.yonghuxingbieIndex]);
			},
			cehuashixingbieChange(e) {
				this.cehuashixingbieIndex = e.target.value
				this.$set(this.ruleForm, 'xingbie', this.cehuashixingbieOptions[this.cehuashixingbieIndex]);
			},
			getUUID() {
				return new Date().getTime();
			},
			async register() {
				if ((!this.ruleForm.zhanghao) && `yonghu` == this.tableName) {
					this.$utils.msg(`账号不能为空`);
					return
				}
				if ((!this.ruleForm.mima) && `yonghu` == this.tableName) {
					this.$utils.msg(`密码不能为空`);
					return
				}
				if (`yonghu` == this.tableName && (this.ruleForm.mima != this.ruleForm.mima2)) {
					this.$utils.msg(`两次密码输入不一致`);
					return
				}
				if ((!this.ruleForm.xingming) && `yonghu` == this.tableName) {
					this.$utils.msg(`姓名不能为空`);
					return
				}
				if (`yonghu` == this.tableName && this.ruleForm.youxiang && (!this.$validate.isEmail(this.ruleForm
						.youxiang))) {
					this.$utils.msg(`邮箱应输入邮件格式`);
					return
				}
				if (`yonghu` == this.tableName && this.ruleForm.shoujihaoma && (!this.$validate.isMobile(this.ruleForm
						.shoujihaoma))) {
					this.$utils.msg(`手机号码应输入手机格式`);
					return
				}
				if ((!this.ruleForm.cehuazhanghao) && `cehuashi` == this.tableName) {
					this.$utils.msg(`策划账号不能为空`);
					return
				}
				if ((!this.ruleForm.mima) && `cehuashi` == this.tableName) {
					this.$utils.msg(`密码不能为空`);
					return
				}
				if (`cehuashi` == this.tableName && (this.ruleForm.mima != this.ruleForm.mima2)) {
					this.$utils.msg(`两次密码输入不一致`);
					return
				}
				if ((!this.ruleForm.cehuaxingming) && `cehuashi` == this.tableName) {
					this.$utils.msg(`策划姓名不能为空`);
					return
				}
				if (`cehuashi` == this.tableName && this.ruleForm.youxiang && (!this.$validate.isEmail(this.ruleForm
						.youxiang))) {
					this.$utils.msg(`邮箱应输入邮件格式`);
					return
				}
				if (`cehuashi` == this.tableName && this.ruleForm.lianxishouji && (!this.$validate.isMobile(this
						.ruleForm.lianxishouji))) {
					this.$utils.msg(`联系手机应输入手机格式`);
					return
				}
				await this.$api.register(`${this.tableName}`, this.ruleForm, this.emailcode);
				this.$utils.msgBack('注册成功');
			}
		}
	}
</script>

<style lang="scss" scoped>
	.reg-page {
		min-height: 100vh;
		box-sizing: border-box;
		padding: 0 56rpx 60rpx;
		background: linear-gradient(180deg, #F3EEE4 0%, #FAF8F4 38%, #FFFFFF 100%);
	}

	.brand {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 90rpx 0 50rpx;
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

		.cuIcon-camera {
			color: #fff;
			font-size: 58rpx;
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
		padding: 44rpx 40rpx 50rpx;
		box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.06);
	}

	.field {
		height: 92rpx;
		padding: 0 28rpx;
		margin-bottom: 24rpx;
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
		letter-spacing: 8rpx;
		box-shadow: 0 10rpx 24rpx rgba(180, 154, 107, 0.3);
	}

	.btn-submit::after {
		border: none;
	}
</style>
