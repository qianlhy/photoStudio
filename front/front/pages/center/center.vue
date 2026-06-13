<template>
	<view class="mine">
		<!-- 资料头 -->
		<view class="profile" @tap="onPageTap('../user-info/user-info')">
			<view class="avatar">
				<image v-if="user.xiangpian" :src="baseUrl + user.xiangpian" mode="aspectFill"></image>
				<text v-else class="cuIcon-people avatar-ph"></text>
			</view>
			<view class="profile-info">
				<view class="name-row">
					<text class="name">{{ displayName }}</text>
					<text v-if="user.vip && user.vip == '是'" class="vip">VIP</text>
				</view>
				<text class="sub">{{ subInfo }}</text>
			</view>
			<text class="cuIcon-right profile-arrow"></text>
		</view>

		<!-- 用户快捷入口 -->
		<view class="quick" v-if="tableName == 'yonghu'">
			<view class="quick-item" @tap="goTab('../dingdan/list')">
				<text class="cuIcon-form quick-icon"></text>
				<text class="quick-label">我的订单</text>
			</view>
			<view class="quick-item" @tap="goTab('../chengpin/list')">
				<text class="cuIcon-pic quick-icon"></text>
				<text class="quick-label">我的成品</text>
			</view>
			<view class="quick-item" @tap="goPage('../message/list')">
				<text class="cuIcon-notification quick-icon"></text>
				<text class="quick-label">消息</text>
				<text v-if="unread > 0" class="badge">{{ unread }}</text>
			</view>
			<view class="quick-item" @tap="goPage('../store/notice')">
				<text class="cuIcon-shop quick-icon"></text>
				<text class="quick-label">门店</text>
			</view>
		</view>

		<!-- 菜单 -->
		<view class="menu">
			<block v-if="tableName == 'yonghu'">
				<view class="row" hover-class="row-hover" @tap="goPage('../preference/preference')">
					<text class="cuIcon-newshot row-icon"></text>
					<text class="row-text">拍摄偏好</text>
					<text class="cuIcon-right row-arrow"></text>
				</view>
				<view class="row" hover-class="row-hover" @tap="goPage('../message/list')">
					<text class="cuIcon-message row-icon"></text>
					<text class="row-text">消息中心</text>
					<text v-if="unread > 0" class="row-badge">{{ unread }}</text>
					<text class="cuIcon-right row-arrow"></text>
				</view>
				<view class="row" hover-class="row-hover" @tap="goPage('../store/notice')">
					<text class="cuIcon-shop row-icon"></text>
					<text class="row-text">门店须知</text>
					<text class="cuIcon-right row-arrow"></text>
				</view>
			</block>

			<block v-for="item in menuList" v-bind:key="item.roleName">
				<block v-if="role == item.roleName" v-for="(menu, index) in item.backMenu" :key="index">
					<block v-for="(child, sort) in menu.child" :key="sort">
						<view class="row" hover-class="row-hover"
							v-if="['yifahuodingdan','yituikuandingdan','yiquxiaodingdan','weizhifudingdan','yizhifudingdan','yiwanchengdingdan'].indexOf(child.tableName) === -1"
							@tap="onPageTap('../' + child.tableName + '/list?userid=' + user.id)">
							<text class="row-icon" :class="child.appFrontIcon || 'cuIcon-list'"></text>
							<text class="row-text">{{ child.menu }}</text>
							<text class="cuIcon-right row-arrow"></text>
						</view>
					</block>
				</block>
			</block>
		</view>

		<view class="logout" @tap="logout">退出登录</view>
	</view>
</template>
<script>
	import menu from '@/utils/menu'
	import http from '@/api/http.js'
	export default {
		data() {
			return {
				user: {},
				tableName: '',
				role: '',
				unread: 0,
				menuList: []
			};
		},
		computed: {
			baseUrl() {
				return this.$base.url;
			},
			displayName() {
				if (this.tableName == 'cehuashi') return this.user.cehuazhanghao || '摄影师';
				return this.user.xingming || this.user.zhanghao || '未登录';
			},
			subInfo() {
				if (this.user.shoujihaoma) return this.user.shoujihaoma;
				if (this.user.yixiangpinlei) return '意向：' + this.user.yixiangpinlei;
				return '点击完善个人资料';
			}
		},
		async onShow() {
			uni.removeStorageSync("useridTag");
			this.role = uni.getStorageSync("role");
			let table = uni.getStorageSync("nowTable");
			try {
				let res = await this.$api.session(table);
				this.user = res.data;
			} catch (e) {}
			this.tableName = table;
			this.menuList = menu.list();
			this.loadUnread();
		},
		methods: {
			async loadUnread() {
				if (this.tableName !== 'yonghu') return;
				try {
					let res = await http.get('message/unread', {});
					this.unread = (res && res.count) || 0;
				} catch (e) {
					this.unread = 0;
				}
			},
			goTab(url) {
				uni.switchTab({
					url: url,
					fail: () => {
						uni.navigateTo({
							url: url
						});
					}
				});
			},
			goPage(url) {
				uni.navigateTo({
					url: url,
					fail: () => {
						uni.switchTab({
							url: url
						});
					}
				});
			},
			onPageTap(url) {
				uni.setStorageSync("useridTag", 1);
				uni.navigateTo({
					url: url,
					fail: () => {
						uni.switchTab({
							url: url
						});
					}
				});
			},
			logout() {
				uni.showModal({
					title: '退出登录',
					content: '确认退出当前账号？',
					success: (r) => {
						if (r.confirm) {
							uni.removeStorageSync('token');
							uni.removeStorageSync('nowTable');
							uni.removeStorageSync('role');
							uni.removeStorageSync('userid');
							uni.reLaunch({
								url: '../login/login'
							});
						}
					}
				});
			}
		}
	}
</script>

<style lang="scss" scoped>
	page {
		background: $brand-bg-soft;
	}

	.mine {
		min-height: 100vh;
		padding-bottom: 60rpx;
	}

	/* 资料头 */
	.profile {
		display: flex;
		align-items: center;
		padding: 60rpx 40rpx 70rpx;
		background: linear-gradient(135deg, #C9B796 0%, #B49A6B 100%);
	}

	.avatar {
		width: 128rpx;
		height: 128rpx;
		border-radius: 50%;
		overflow: hidden;
		background: rgba(255, 255, 255, 0.25);
		display: flex;
		align-items: center;
		justify-content: center;
		border: 4rpx solid rgba(255, 255, 255, 0.6);

		image {
			width: 100%;
			height: 100%;
		}
	}

	.avatar-ph {
		color: #fff;
		font-size: 66rpx;
	}

	.profile-info {
		flex: 1;
		margin-left: 28rpx;
		display: flex;
		flex-direction: column;
	}

	.name-row {
		display: flex;
		align-items: center;
	}

	.name {
		font-size: 38rpx;
		font-weight: 700;
		color: #fff;
	}

	.vip {
		margin-left: 16rpx;
		font-size: 20rpx;
		color: #8C7853;
		background: #fff;
		border-radius: $brand-radius-pill;
		padding: 2rpx 16rpx;
		font-weight: 600;
	}

	.sub {
		margin-top: 12rpx;
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.85);
	}

	.profile-arrow {
		color: rgba(255, 255, 255, 0.8);
		font-size: 32rpx;
	}

	/* 快捷入口 */
	.quick {
		display: flex;
		margin: -40rpx 24rpx 0;
		background: #fff;
		border-radius: $brand-radius-lg;
		padding: 34rpx 0;
		box-shadow: $brand-shadow-card;
	}

	.quick-item {
		position: relative;
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.quick-icon {
		font-size: 48rpx;
		color: $brand-primary;
	}

	.quick-label {
		margin-top: 12rpx;
		font-size: 24rpx;
		color: $brand-ink-2;
	}

	.badge {
		position: absolute;
		top: -8rpx;
		right: 38rpx;
		min-width: 30rpx;
		height: 30rpx;
		line-height: 30rpx;
		text-align: center;
		padding: 0 6rpx;
		background: $brand-price;
		color: #fff;
		font-size: 20rpx;
		border-radius: 30rpx;
	}

	/* 菜单 */
	.menu {
		margin: 24rpx 24rpx 0;
		background: #fff;
		border-radius: $brand-radius-lg;
		overflow: hidden;
		box-shadow: $brand-shadow-card;
	}

	.row {
		display: flex;
		align-items: center;
		height: 100rpx;
		padding: 0 30rpx;
		border-bottom: 1rpx solid #f4f4f4;
	}

	.row:last-child {
		border-bottom: none;
	}

	.row-hover {
		background: #faf8f4;
	}

	.row-icon {
		font-size: 38rpx;
		color: $brand-primary;
		width: 48rpx;
	}

	.row-text {
		flex: 1;
		font-size: 30rpx;
		color: $brand-ink;
		margin-left: 12rpx;
	}

	.row-badge {
		min-width: 34rpx;
		height: 34rpx;
		line-height: 34rpx;
		text-align: center;
		padding: 0 8rpx;
		background: $brand-price;
		color: #fff;
		font-size: 20rpx;
		border-radius: 34rpx;
		margin-right: 12rpx;
	}

	.row-arrow {
		color: #c8c8c8;
		font-size: 28rpx;
	}

	.logout {
		margin: 40rpx 24rpx 0;
		height: 92rpx;
		line-height: 92rpx;
		text-align: center;
		background: #fff;
		border-radius: $brand-radius-lg;
		color: $brand-ink-2;
		font-size: 30rpx;
		box-shadow: $brand-shadow-card;
	}
</style>
