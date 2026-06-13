<template>
	<view class="ui-page">
		<!-- 头像 -->
		<view class="avatar-card" @tap="tableName=='cehuashi' ? cehuashixiangpianTap() : yonghuxiangpianTap()">
			<image class="avatar" v-if="ruleForm.xiangpian" :src="baseUrl + ruleForm.xiangpian" mode="aspectFill"></image>
			<view class="avatar" v-else>
				<text class="cuIcon-people avatar-ph"></text>
			</view>
			<text class="avatar-tip">点击更换头像</text>
		</view>

		<!-- 用户表单 -->
		<view class="card" v-if="tableName == 'yonghu'">
			<view class="row">
				<text class="label">账号</text>
				<input class="input" disabled v-model="ruleForm.zhanghao" placeholder="账号" placeholder-class="ph" />
			</view>
			<view class="row">
				<text class="label">密码</text>
				<input class="input" type="password" v-model="ruleForm.mima" placeholder="密码" placeholder-class="ph" />
			</view>
			<view class="row">
				<text class="label">姓名</text>
				<input class="input" v-model="ruleForm.xingming" placeholder="姓名" placeholder-class="ph" />
			</view>
			<view class="row">
				<text class="label">性别</text>
				<picker class="picker-wrap" @change="yonghuxingbieChange" :value="yonghuxingbieIndex" :range="yonghuxingbieOptions">
					<view class="input" :class="{ ph: !ruleForm.xingbie }">{{ ruleForm.xingbie || '请选择性别' }}</view>
				</picker>
			</view>
			<view class="row">
				<text class="label">邮箱</text>
				<input class="input" v-model="ruleForm.youxiang" placeholder="邮箱" placeholder-class="ph" />
			</view>
			<view class="row last">
				<text class="label">手机号码</text>
				<input class="input" v-model="ruleForm.shoujihaoma" placeholder="手机号码" placeholder-class="ph" />
			</view>
		</view>

		<!-- 策划师表单 -->
		<view class="card" v-if="tableName == 'cehuashi'">
			<view class="row">
				<text class="label">策划账号</text>
				<input class="input" disabled v-model="ruleForm.cehuazhanghao" placeholder="策划账号" placeholder-class="ph" />
			</view>
			<view class="row">
				<text class="label">密码</text>
				<input class="input" type="password" v-model="ruleForm.mima" placeholder="密码" placeholder-class="ph" />
			</view>
			<view class="row">
				<text class="label">策划姓名</text>
				<input class="input" v-model="ruleForm.cehuaxingming" placeholder="策划姓名" placeholder-class="ph" />
			</view>
			<view class="row">
				<text class="label">性别</text>
				<picker class="picker-wrap" @change="cehuashixingbieChange" :value="cehuashixingbieIndex" :range="cehuashixingbieOptions">
					<view class="input" :class="{ ph: !ruleForm.xingbie }">{{ ruleForm.xingbie || '请选择性别' }}</view>
				</picker>
			</view>
			<view class="row">
				<text class="label">邮箱</text>
				<input class="input" v-model="ruleForm.youxiang" placeholder="邮箱" placeholder-class="ph" />
			</view>
			<view class="row last">
				<text class="label">联系手机</text>
				<input class="input" v-model="ruleForm.lianxishouji" placeholder="联系手机" placeholder-class="ph" />
			</view>
		</view>

		<button class="btn-save" @tap="update">保存修改</button>
		<view class="btn-logout" @tap="logout">退出登录</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				ruleForm: {},
				tableName: "",
				yonghuxingbieOptions: [],
				yonghuxingbieIndex: 0,
				cehuashixingbieOptions: [],
				cehuashixingbieIndex: 0,
			}
		},
		computed: {
			baseUrl() {
				return this.$base.url;
			}
		},
		async onLoad() {
			let table = uni.getStorageSync("nowTable");
			let res = await this.$api.session(table);
			this.ruleForm = res.data;
			this.tableName = table;
			if (this.tableName == 'yonghu') {
				this.yonghuxingbieOptions = "男,女".split(',');
				this.yonghuxingbieOptions.forEach((item, index) => {
					if (item == this.ruleForm.xingbie) {
						this.yonghuxingbieIndex = index;
					}
				});
			}
			if (this.tableName == 'cehuashi') {
				this.cehuashixingbieOptions = "男,女".split(',');
				this.cehuashixingbieOptions.forEach((item, index) => {
					if (item == this.ruleForm.xingbie) {
						this.cehuashixingbieIndex = index;
					}
				});
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
			logout() {
				uni.setStorageSync('token', '');
				this.$utils.jump('../login/login');
			},
			async update() {
				if ((!this.ruleForm.zhanghao) && `yonghu` == this.tableName) {
					this.$utils.msg(`账号不能为空`);
					return
				}
				if ((!this.ruleForm.mima) && `yonghu` == this.tableName) {
					this.$utils.msg(`密码不能为空`);
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
				let table = uni.getStorageSync("nowTable");
				await this.$api.update(table, this.ruleForm);
				this.$utils.msgBack('修改成功');
			},
			yonghuxiangpianTap() {
				let _this = this;
				this.$api.upload(function(res) {
					_this.$set(_this.ruleForm, 'xiangpian', 'upload/' + res.file);
				});
			},
			cehuashixiangpianTap() {
				let _this = this;
				this.$api.upload(function(res) {
					_this.$set(_this.ruleForm, 'xiangpian', 'upload/' + res.file);
				});
			}
		}
	}
</script>

<style lang="scss" scoped>
	page {
		background: $brand-bg-soft;
	}

	.ui-page {
		min-height: 100vh;
		padding-bottom: 60rpx;
	}

	.avatar-card {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 50rpx 0 40rpx;
		background: linear-gradient(135deg, #C9B796 0%, #B49A6B 100%);
	}

	.avatar {
		width: 140rpx;
		height: 140rpx;
		border-radius: 50%;
		overflow: hidden;
		background: rgba(255, 255, 255, 0.25);
		border: 4rpx solid rgba(255, 255, 255, 0.7);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.avatar-ph {
		color: #fff;
		font-size: 72rpx;
	}

	.avatar-tip {
		margin-top: 18rpx;
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.9);
	}

	.card {
		margin: -30rpx 24rpx 0;
		background: #fff;
		border-radius: $brand-radius-lg;
		padding: 6rpx 30rpx;
		box-shadow: $brand-shadow-card;
	}

	.row {
		display: flex;
		align-items: center;
		min-height: 100rpx;
		border-bottom: 1rpx solid #f3f3f3;
	}

	.row.last {
		border-bottom: none;
	}

	.label {
		width: 170rpx;
		font-size: 28rpx;
		color: $brand-ink-2;
	}

	.input {
		flex: 1;
		font-size: 28rpx;
		color: $brand-ink;
	}

	.picker-wrap {
		flex: 1;
	}

	.ph {
		color: #bbb;
	}

	.btn-save {
		margin: 40rpx 24rpx 0;
		height: 92rpx;
		line-height: 92rpx;
		border-radius: 16rpx;
		background: linear-gradient(135deg, #B49A6B 0%, #A6885A 100%);
		color: #fff;
		font-size: 32rpx;
		letter-spacing: 4rpx;
		box-shadow: 0 10rpx 24rpx rgba(180, 154, 107, 0.3);
	}

	.btn-save::after {
		border: none;
	}

	.btn-logout {
		margin: 24rpx 24rpx 0;
		height: 92rpx;
		line-height: 92rpx;
		text-align: center;
		border-radius: 16rpx;
		background: #fff;
		color: $brand-ink-2;
		font-size: 30rpx;
		box-shadow: $brand-shadow-card;
	}
</style>
