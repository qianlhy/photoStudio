<template>
	<view class="page">
		<view class="bg"></view>
		<scroll-view scroll-y class="scroll">
			<!-- 头部 -->
			<view class="head">
				<view class="brand">{{ brandName }}</view>
				<image class="avatar" :src="avatar" mode="aspectFill"></image>
			</view>
			<view class="title">我的服务</view>
			<view class="subtitle">本次内容正在制作中</view>

			<!-- 储备条 -->
			<view class="reserve">
				<view class="rv-top">
					<text class="rv-l">储备 {{ reservePct }}%</text>
					<text class="rv-r">可用 {{ remain }}/{{ totalQuota }}条 · {{ producing }}条制作中</text>
				</view>
				<view class="rv-bar"><view class="rv-in" :style="{width: reservePct+'%'}"></view></view>
				<text class="rv-note">实心为可用 · 条纹为制作中</text>
			</view>

			<!-- 当前服务 -->
			<view class="cur card">
				<view class="cur-row">
					<text class="cur-tag">当前服务</text>
					<text class="cur-badge">进度正常</text>
				</view>
				<view class="cur-title">第{{ batch }}批 · {{ order.status || '制作中' }}</view>
				<text class="cur-sub">{{ order.videoCount||0 }}条视频 · 预计{{ deliverText }}交付</text>
				<view class="cur-prog">
					<text class="cp-l">已完成 {{ order.completedCount||0 }}/{{ order.videoCount||0 }}</text>
					<view class="cp-bar"><view class="cp-in" :style="{width: donePct+'%'}"></view></view>
				</view>
			</view>

			<!-- 服务进度 -->
			<view class="steps card">
				<text class="blk-title">服务进度</text>
				<view v-for="(s,i) in steps" :key="i" class="step">
					<view class="st-left">
						<view class="st-dot" :class="{done:s.state==='done', cur:s.state==='cur'}">
							<text v-if="s.state==='done'">✓</text>
							<text v-else>{{ i+1 }}</text>
						</view>
						<view v-if="i<steps.length-1" class="st-line" :class="{done:s.state==='done'}"></view>
					</view>
					<view class="st-body">
						<text class="st-name" :class="{muted:s.state==='todo'}">{{ s.name }}</text>
						<text v-if="s.date" class="st-date">{{ s.date }}</text>
					</view>
				</view>
				<text class="steps-note">制作完成后，成品将自动进入「内容」</text>
			</view>

			<!-- 本次服务信息 -->
			<view class="info card">
				<text class="blk-title">本次服务信息</text>
				<view class="info-grid">
					<view class="ig"><text class="ig-ic">🎬</text><text class="ig-l">本次内容</text><text class="ig-v">{{ order.videoCount||0 }}条</text></view>
					<view class="ig"><text class="ig-ic">📅</text><text class="ig-l">拍摄日期</text><text class="ig-v">{{ shootText }}</text></view>
					<view class="ig"><text class="ig-ic">⏰</text><text class="ig-l">预计交付</text><text class="ig-v">{{ deliverText }}</text></view>
					<view class="ig"><text class="ig-ic">🧾</text><text class="ig-l">服务编号</text><text class="ig-v sm">{{ order.orderNo||'—' }}</text></view>
				</view>
			</view>

			<!-- 服务经理 -->
			<view class="mgr card">
				<text class="blk-title">专属服务经理</text>
				<view class="mgr-row">
					<image class="mgr-av" :src="mgrAvatar" mode="aspectFill"></image>
					<view class="mgr-info">
						<text class="mgr-name">{{ order.managerName || '服务经理' }} <text class="mgr-tag">你的服务经理</text></text>
						<text class="mgr-time">09:00-18:00</text>
					</view>
					<view class="mgr-btns">
						<view class="mgr-btn" @click="callMgr">💬 联系经理</view>
						<view class="mgr-btn" @click="askQuestion">❓ 提交问题</view>
					</view>
				</view>
				<view class="mgr-foot" @click="viewRecords">📄 查看服务记录 ›</view>
			</view>

			<view style="height:160rpx"></view>
		</scroll-view>

		<!-- 底部切换 -->
		<view class="tabbar">
			<view class="tab active">🎬 服务</view>
			<view class="tab mid">制作中</view>
			<view class="tab" @click="goContent">📺 内容</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			brandName: '影集',
			avatar: 'https://i.pravatar.cc/100?img=32',
			mgrAvatar: 'https://i.pravatar.cc/100?img=12',
			customerId: null,
			customer: {},
			order: {},
			totalQuota: 15,
			batch: 2
		}
	},
	computed: {
		remain() { return this.customer.remainCount || 0 },
		producing() {
			const v = (this.order.videoCount || 0) - (this.order.completedCount || 0)
			return v > 0 ? v : 0
		},
		reservePct() {
			return Math.min(100, Math.round((this.remain / this.totalQuota) * 100))
		},
		donePct() {
			if (!this.order.videoCount) return 0
			return Math.round((this.order.completedCount / this.order.videoCount) * 100)
		},
		shootText() { return this.md(this.order.shootDate) },
		deliverText() { return this.md(this.order.deliverDate) },
		steps() {
			const st = this.order.status || '内容制作中'
			const order = ['服务已确认', '方案已确认', '拍摄已完成', '内容制作中', '等待交付', '服务完成']
			const idxMap = { '待拍摄': 2, '待交付': 4, '已完成': 5, '内容制作中': 3 }
			const cur = idxMap[st] !== undefined ? idxMap[st] : 3
			return order.map((name, i) => ({
				name,
				date: i <= cur ? this.stepDate(i) : '',
				state: i < cur ? 'done' : (i === cur ? 'cur' : 'todo')
			}))
		}
	},
	onLoad() {
		this.customerId = uni.getStorageSync('hyCustomerId') || null
		this.load()
	},
	methods: {
		load() {
			const finish = (cid) => {
				this.$api.list('hyCustomer', { id: cid }).then(res => {
					this.customer = (res.data && res.data[0]) || {}
				})
				this.$api.page('hyOrder', { customerId: cid, page: 1, limit: 1, sort: 'addtime', order: 'desc' }).then(res => {
					const list = (res.data && res.data.list) || []
					this.order = list[0] || {}
				})
			}
			if (this.customerId) { finish(this.customerId); return }
			// 兜底：取第一个客户用于演示
			this.$api.page('hyCustomer', { page: 1, limit: 1 }).then(res => {
				const c = (res.data && res.data.list && res.data.list[0]) || {}
				this.customerId = c.id
				if (c.id) { uni.setStorageSync('hyCustomerId', c.id); finish(c.id) }
			})
		},
		md(t) {
			if (!t) return '—'
			const d = new Date(String(t).replace(/-/g, '/'))
			return `${d.getMonth() + 1}月${d.getDate()}日`
		},
		stepDate(i) {
			if (this.order.shootDate && i === 2) return this.md(this.order.shootDate)
			if (this.order.deliverDate && i >= 4) return this.md(this.order.deliverDate)
			return ''
		},
		callMgr() { uni.showToast({ title: '已通知服务经理', icon: 'none' }) },
		askQuestion() {
			uni.showModal({
				title: '提交问题', editable: true, placeholderText: '请描述您的问题',
				success: (r) => {
					if (r.confirm && r.content) {
						this.$api.save('hyMessage', {
							customerId: this.customerId, customerName: this.customer.name,
							title: r.content, content: r.content, type: '客户问题', fromRole: '客户'
						}).then(() => uni.showToast({ title: '已提交', icon: 'success' }))
					}
				}
			})
		},
		viewRecords() { uni.showToast({ title: '服务记录（演示）', icon: 'none' }) },
		goContent() { uni.redirectTo({ url: '/pages/hy-content/content' }) }
	}
}
</script>

<style lang="scss" scoped>
.page { width: 100%; height: 100vh; position: relative; background: #EAF6F4; }
.bg { position: absolute; top: 0; left: 0; right: 0; height: 420rpx; background: linear-gradient(160deg, #DDF3EF 0%, #EAF1FF 60%, #F4F6FA 100%); }
.scroll { position: relative; height: 100vh; padding: 0 28rpx; box-sizing: border-box; }
.head { display: flex; justify-content: space-between; align-items: center; padding-top: 90rpx; }
.brand { font-size: 30rpx; font-weight: 700; color: #1F2733; }
.avatar { width: 76rpx; height: 76rpx; border-radius: 50%; }
.title { font-size: 56rpx; font-weight: 800; color: #1F2733; margin-top: 18rpx; }
.subtitle { font-size: 26rpx; color: #6B7785; margin-top: 6rpx; }

.reserve { margin-top: 28rpx; }
.rv-top { display: flex; justify-content: space-between; }
.rv-l { font-size: 24rpx; color: #1F2733; font-weight: 600; }
.rv-r { font-size: 22rpx; color: #6B7785; }
.rv-bar { height: 14rpx; background: rgba(255,255,255,.6); border-radius: 999rpx; margin: 12rpx 0 8rpx; overflow: hidden; }
.rv-in { height: 100%; background: linear-gradient(90deg, #FFB37A, #4FD0C0); border-radius: 999rpx; }
.rv-note { font-size: 20rpx; color: #9AA6B2; }

.card { background: #fff; border-radius: 24rpx; padding: 28rpx; margin-top: 24rpx; box-shadow: 0 8rpx 24rpx rgba(31,39,51,.05); }
.cur { background: linear-gradient(135deg, #EFFaF8, #F2F8FF); }
.cur-row { display: flex; justify-content: space-between; align-items: center; }
.cur-tag { font-size: 24rpx; color: #6B7785; }
.cur-badge { font-size: 22rpx; color: #22B07D; background: #E8F7F0; padding: 4rpx 16rpx; border-radius: 999rpx; }
.cur-title { font-size: 40rpx; font-weight: 800; color: #1F2733; margin: 14rpx 0 8rpx; }
.cur-sub { font-size: 24rpx; color: #6B7785; }
.cur-prog { margin-top: 22rpx; }
.cp-l { font-size: 22rpx; color: #6B7785; }
.cp-bar { height: 14rpx; background: #E7ECF0; border-radius: 999rpx; margin-top: 10rpx; overflow: hidden; }
.cp-in { height: 100%; background: linear-gradient(90deg, #4FD0C0, #2F6BFF); border-radius: 999rpx; }

.blk-title { font-size: 30rpx; font-weight: 700; color: #1F2733; }
.step { display: flex; margin-top: 20rpx; }
.st-left { display: flex; flex-direction: column; align-items: center; margin-right: 18rpx; }
.st-dot { width: 44rpx; height: 44rpx; border-radius: 50%; background: #D7DCE3; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 22rpx; }
.st-dot.done { background: #22B07D; }
.st-dot.cur { background: #2F6BFF; box-shadow: 0 0 0 6rpx rgba(47,107,255,.18); }
.st-line { width: 3rpx; flex: 1; background: #E5E8EC; margin: 4rpx 0; min-height: 30rpx; }
.st-line.done { background: #22B07D; }
.st-body { display: flex; flex-direction: column; padding-bottom: 10rpx; }
.st-name { font-size: 28rpx; color: #1F2733; font-weight: 600; }
.st-name.muted { color: #B9C0CC; font-weight: 400; }
.st-date { font-size: 22rpx; color: #9AA6B2; margin-top: 4rpx; }
.steps-note { font-size: 22rpx; color: #4FD0C0; margin-top: 16rpx; display: block; }

.info-grid { display: flex; flex-wrap: wrap; margin-top: 16rpx; }
.ig { width: 25%; display: flex; flex-direction: column; align-items: center; padding: 12rpx 0; }
.ig-ic { font-size: 32rpx; }
.ig-l { font-size: 20rpx; color: #9AA6B2; margin-top: 8rpx; }
.ig-v { font-size: 24rpx; font-weight: 700; color: #1F2733; margin-top: 4rpx; }
.ig-v.sm { font-size: 18rpx; }

.mgr-row { display: flex; align-items: center; margin-top: 16rpx; }
.mgr-av { width: 80rpx; height: 80rpx; border-radius: 50%; }
.mgr-info { flex: 1; margin-left: 16rpx; }
.mgr-name { font-size: 28rpx; font-weight: 700; }
.mgr-tag { font-size: 20rpx; color: #2F6BFF; background: #EAF1FF; padding: 2rpx 12rpx; border-radius: 999rpx; margin-left: 8rpx; }
.mgr-time { font-size: 22rpx; color: #9AA6B2; display: block; margin-top: 6rpx; }
.mgr-btns { display: flex; gap: 12rpx; }
.mgr-btn { font-size: 22rpx; color: #2F6BFF; background: #F2F6FF; padding: 12rpx 18rpx; border-radius: 14rpx; }
.mgr-foot { margin-top: 18rpx; font-size: 24rpx; color: #6B7785; }

.tabbar { position: absolute; left: 0; right: 0; bottom: 0; height: 120rpx; background: #fff; display: flex; align-items: center; justify-content: space-around; box-shadow: 0 -4rpx 18rpx rgba(0,0,0,.05); }
.tab { font-size: 26rpx; color: #9AA6B2; padding: 16rpx 40rpx; border-radius: 999rpx; }
.tab.active { background: linear-gradient(90deg, #4FD0C0, #6FE0C8); color: #fff; font-weight: 700; }
.tab.mid { font-size: 22rpx; }
</style>
