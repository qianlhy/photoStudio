<template>
	<view class="page">
		<view class="aurora aurora-a"></view>
		<view class="aurora aurora-b"></view>
		<scroll-view scroll-y class="scroll" :show-scrollbar="false">
			<view class="head">
				<view>
					<text class="brand">{{ brandName }}</text>
					<text class="title">我的服务</text>
					<text class="subtitle">本次内容正在制作中</text>
				</view>
				<image class="avatar" :src="avatar" mode="aspectFill"></image>
			</view>

			<view class="reserve">
				<view class="rv-top">
					<text>储备 <text class="strong">{{ reservePct }}%</text></text>
					<text>可用{{ remain }}/{{ totalQuota }}条 · {{ producing }}条制作中</text>
				</view>
				<view class="rv-bar">
					<view class="rv-available" :style="{width: reservePct+'%'}"></view>
					<view class="rv-producing" :style="{width: producingPct+'%'}"></view>
				</view>
				<text class="rv-note">实心为可用 · 条纹为制作中</text>
			</view>

			<view class="cur glass-card">
				<view class="cur-copy">
					<text class="eyebrow">当前服务</text>
					<text class="cur-title">第{{ batch }}批 · {{ displayStatus }}</text>
					<text class="cur-sub" v-if="order.id">{{ order.videoCount || 0 }}条视频 · 预计{{ deliverText }}交付</text>
					<text class="cur-sub" v-else>暂无进行中的服务订单</text>
					<view class="cur-progress" v-if="order.id">
						<text>已完成 {{ order.completedCount || 0 }}/{{ order.videoCount || 0 }}</text>
						<view class="cp-bar"><view class="cp-in" :style="{width: donePct+'%'}"></view></view>
					</view>
				</view>
				<view class="cur-badge" v-if="order.id">{{ progressBadge }}</view>
				<view class="service-art">
					<view class="art-orb"></view>
					<view class="art-folder"><view class="art-play"></view></view>
					<view class="art-gear">✦</view>
				</view>
			</view>

			<view class="steps glass-card">
				<text class="block-title">服务进度</text>
				<view class="step-list">
					<view v-for="(s,i) in steps" :key="i" class="step">
						<view class="step-index">{{ i + 1 }}</view>
						<view class="st-axis">
							<view class="st-dot" :class="s.state"><text>{{ s.state === 'done' ? '✓' : (s.state === 'cur' ? i + 1 : '') }}</text></view>
							<view v-if="i < steps.length-1" class="st-line" :class="{done:s.state==='done'}"></view>
						</view>
						<view class="st-body">
							<text class="st-name" :class="{muted:s.state==='todo'}">{{ s.name }}</text>
							<text v-if="s.date" class="st-date">{{ s.date }}</text>
							<text v-if="s.state==='cur' && order.id" class="st-date">已完成 {{ order.completedCount || 0 }}/{{ order.videoCount || 0 }}</text>
						</view>
					</view>
				</view>
				<view class="steps-note"><view class="note-dot"></view><text>制作完成后，成品将自动进入「内容」</text></view>
			</view>

			<view class="info glass-card">
				<text class="block-title">本次服务信息</text>
				<view class="info-grid">
					<view class="info-item"><view class="info-icon video-icon"><view></view></view><text class="ig-label">本次内容</text><text class="ig-value">{{ order.videoCount || 0 }}条</text></view>
					<view class="info-item"><view class="info-icon calendar-icon"></view><text class="ig-label">拍摄日期</text><text class="ig-value">{{ shootText }}</text></view>
					<view class="info-item"><view class="info-icon clock-icon"></view><text class="ig-label">预计交付</text><text class="ig-value">{{ deliverText }}</text></view>
					<view class="info-item"><view class="info-icon list-icon"></view><text class="ig-label">服务编号</text><text class="ig-value small">{{ order.orderNo || '—' }}</text></view>
				</view>
			</view>

			<view class="manager glass-card">
				<text class="block-title">专属服务经理</text>
				<view class="manager-row">
					<image class="manager-avatar" :src="mgrAvatar" mode="aspectFill"></image>
					<view class="manager-info">
						<view class="manager-name-row"><text class="manager-name">{{ order.managerName || customer.managerName || '服务经理' }}</text><text class="manager-tag">你的服务经理</text></view>
						<text class="manager-time">工作日 09:00–18:00</text>
					</view>
					<view class="manager-action" @tap="callMgr"><view class="bubble-icon">•••</view><text>联系经理</text></view>
					<view class="manager-action" @tap="askQuestion"><view class="question-icon">?</view><text>提交问题</text></view>
				</view>
				<view class="manager-record" @tap="viewRecords"><view class="record-icon"></view><text>查看服务记录</text><text class="chevron">›</text></view>
			</view>
			<view class="bottom-space"></view>
		</scroll-view>
		<client-tabbar active="service" state-text="制作中"></client-tabbar>
	</view>
</template>

<script>
import clientTabbar from '@/components/client-tabbar/client-tabbar.vue'
export default {
	components: { clientTabbar },
	data() {
		return {
			brandName: '影集',
			avatar: '',
			mgrAvatar: '',
			customerId: null,
			customer: {},
			order: {},
			orderCount: 0,
			orderVideoTotal: 0
		}
	},
	computed: {
		remain() { return this.customer.remainCount || 0 },
		producing() {
			if (!this.order.videoCount) return 0
			const v = (this.order.videoCount || 0) - (this.order.completedCount || 0)
			return v > 0 ? v : 0
		},
		totalQuota() {
			const fromCrm = (this.customer.remainCount || 0) + (this.customer.publishedCount || 0)
			if (fromCrm > 0) return fromCrm
			if (this.orderVideoTotal > 0) return this.orderVideoTotal
			return Math.max(this.remain, 1)
		},
		producingPct() {
			if (!this.totalQuota) return 0
			return Math.min(100 - this.reservePct, Math.round((this.producing / this.totalQuota) * 100))
		},
		reservePct() {
			return Math.min(100, Math.round((this.remain / this.totalQuota) * 100))
		},
		donePct() {
			if (!this.order.videoCount) return 0
			return Math.round(((this.order.completedCount || 0) / this.order.videoCount) * 100)
		},
		batch() {
			const n = this.customer.dealCount || this.orderCount || 0
			return n > 0 ? n : 1
		},
		progressBadge() {
			if (this.order.abnormal) return '需关注'
			return '进度正常'
		},
		displayStatus() {
			const status = this.order.status || '暂无订单'
			if (status === '内容制作中') return '制作中'
			return status
		},
		shootText() { return this.md(this.order.shootDate) },
		deliverText() { return this.md(this.order.deliverDate) },
		steps() {
			const st = this.order.status || ''
			const order = ['服务已确认', '方案已确认', '拍摄已完成', '内容制作中', '等待交付', '服务完成']
			if (!st) {
				return order.map((name, i) => ({ name, date: '', state: 'todo' }))
			}
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
		this.avatar = this.$base.url + 'upload/avatar_1.jpg'
		this.mgrAvatar = this.$base.url + 'upload/avatar_2.jpg'
		this.customerId = uni.getStorageSync('hyCustomerId') || null
		this.load()
	},
	methods: {
		img(v) { return v ? (/^https?:/.test(v) ? v : this.$base.url + String(v).split(',')[0]) : '' },
		syncAvatar() {
			const a = this.customer.avatar
			if (a) this.avatar = this.img(a)
		},
		load() {
			const finish = (cid) => {
				this.$api.list('hyCustomer', { id: cid }).then(res => {
					this.customer = (res.data && res.data[0]) || {}
					this.syncAvatar()
				})
				this.$api.page('hyOrder', { customerId: cid, page: 1, limit: 20, sort: 'addtime', order: 'desc' }).then(res => {
					const list = (res.data && res.data.list) || []
					this.order = list[0] || {}
					this.orderCount = (res.data && res.data.total) || list.length
					this.orderVideoTotal = list.reduce((s, o) => s + (o.videoCount || 0), 0)
				})
			}
			this.bindThen(finish)
		},
		bindThen(finish) {
			const bindByPhone = () => {
				const table = uni.getStorageSync('nowTable') || 'yonghu'
				this.$api.session(table).then(res => {
					const u = res.data || {}
					const phone = u.shoujihaoma
					if (!phone) {
						uni.showToast({ title: '请先完善手机号以查看服务', icon: 'none' })
						return
					}
					uni.request({
						url: this.$base.url + 'hyCustomer/bindByPhone',
						method: 'GET',
						data: { phone },
						header: { Token: uni.getStorageSync('token') },
						success: (r) => {
							const body = r.data || {}
							if (body.code === 0 && body.data && body.data.id) {
								this.customerId = body.data.id
								uni.setStorageSync('hyCustomerId', body.data.id)
								finish(body.data.id)
							} else {
								uni.showToast({ title: body.msg || '未绑定服务账号', icon: 'none' })
							}
						}
					})
				}).catch(() => {
					uni.showToast({ title: '请先登录', icon: 'none' })
				})
			}
			const cached = uni.getStorageSync('hyCustomerId')
			if (!cached) {
				bindByPhone()
				return
			}
			// 旧缓存 ID（如种子 6001）在线上已不存在，校验失败则按手机号重绑
			this.$api.list('hyCustomer', { id: cached }).then(res => {
				const row = (res.data && res.data[0]) || null
				if (row && row.id) {
					this.customerId = cached
					finish(cached)
				} else {
					uni.removeStorageSync('hyCustomerId')
					bindByPhone()
				}
			}).catch(() => {
				uni.removeStorageSync('hyCustomerId')
				bindByPhone()
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
		viewRecords() {
			if (!this.customerId) {
				uni.showToast({ title: '暂无服务记录', icon: 'none' })
				return
			}
			this.$api.page('hyFollowRecord', { page: 1, limit: 5, customerId: this.customerId }).then(res => {
				const list = (res.data && res.data.list) || []
				if (!list.length) {
					uni.showToast({ title: '暂无服务记录', icon: 'none' })
					return
				}
				const text = list.map(t => (t.summary || t.contactResult || t.nextAction || '跟进记录')).slice(0, 3).join('\n')
				uni.showModal({ title: '服务记录', content: text, showCancel: false })
			})
		},
		goContent() { uni.redirectTo({ url: '/pages/hy-content/content' }) }
	}
}
</script>

<style lang="scss" scoped>
.page { position:relative; width:100%; height:100vh; overflow:hidden; background:linear-gradient(180deg,#F0FBFC 0%,#F7FCFC 48%,#F4FBFA 100%); color:#10244B; }
.aurora { position:absolute; border-radius:50%; filter:blur(8rpx); pointer-events:none; }
.aurora-a { top:-100rpx; right:-120rpx; width:500rpx; height:460rpx; background:radial-gradient(circle,rgba(110,229,220,.32),rgba(160,215,251,.12) 48%,transparent 70%); }
.aurora-b { top:230rpx; left:-200rpx; width:500rpx; height:360rpx; background:radial-gradient(circle,rgba(146,220,239,.18),transparent 70%); }
.scroll { position:relative; z-index:2; width:100%; height:100vh; padding:0 56rpx; box-sizing:border-box; }
.head { display:flex; justify-content:space-between; align-items:flex-start; padding-top:calc(var(--status-bar-height, 40rpx) + 18rpx); }
.head>view { display:flex; flex-direction:column; }
.brand { font-size:28rpx; font-weight:700; letter-spacing:1rpx; }
.title { margin-top:14rpx; font-size:48rpx; line-height:1.15; font-weight:700; letter-spacing:1rpx; }
.subtitle { margin-top:8rpx; color:#99A6B8; font-size:25rpx; }
.avatar { width:76rpx; height:76rpx; border:5rpx solid rgba(255,255,255,.86); border-radius:50%; background:#BCEFE9; box-shadow:0 8rpx 22rpx rgba(35,189,185,.28); }
.reserve { margin-top:38rpx; color:#8D99A9; font-size:21rpx; }
.rv-top { display:flex; justify-content:space-between; align-items:center; }
.strong { color:#59687D; }
.rv-bar { display:flex; width:100%; height:9rpx; margin:11rpx 0 10rpx; overflow:hidden; border-radius:6rpx; background:#E3EAED; }
.rv-available { height:100%; background:linear-gradient(90deg,#FF9A79,#FFC264); }
.rv-producing { height:100%; background:repeating-linear-gradient(125deg,#7ADDD5 0,#7ADDD5 5rpx,#C7F4EF 5rpx,#C7F4EF 10rpx); }
.rv-note { font-size:18rpx; color:#A6B0BF; }
.glass-card { position:relative; box-sizing:border-box; margin-top:20rpx; border:1rpx solid rgba(214,230,235,.78); border-radius:28rpx; background:rgba(255,255,255,.68); box-shadow:0 10rpx 30rpx rgba(68,103,119,.055),inset 0 1rpx 0 rgba(255,255,255,.86); backdrop-filter:blur(18rpx); }
.cur { height:302rpx; padding:28rpx 34rpx; overflow:hidden; background:linear-gradient(135deg,rgba(251,255,255,.85),rgba(226,250,250,.55)); }
.cur-copy { position:relative; z-index:3; display:flex; flex-direction:column; }
.eyebrow { font-size:24rpx; color:#77879C; }
.cur-title { margin-top:14rpx; font-size:38rpx; font-weight:700; color:#122956; }
.cur-sub { margin-top:8rpx; font-size:22rpx; color:#8290A3; }
.cur-progress { width:330rpx; margin-top:30rpx; font-size:21rpx; color:#8996A8; }
.cp-bar { height:9rpx; margin-top:10rpx; overflow:hidden; border-radius:6rpx; background:#E3EBEE; }
.cp-in { height:100%; border-radius:6rpx; background:linear-gradient(90deg,#38C9DC,#44DDC8); }
.cur-badge { position:absolute; z-index:4; right:32rpx; top:28rpx; padding:8rpx 20rpx; border:1rpx solid rgba(69,209,198,.28); border-radius:24rpx; background:rgba(237,255,251,.76); color:#43BFB7; font-size:20rpx; }
.service-art { position:absolute; right:18rpx; bottom:20rpx; width:245rpx; height:190rpx; }
.art-orb { position:absolute; right:15rpx; top:5rpx; width:170rpx; height:150rpx; border-radius:50%; background:radial-gradient(circle at 45% 40%,rgba(255,255,255,.95),rgba(92,222,232,.28) 52%,transparent 70%); }
.art-folder { position:absolute; left:42rpx; top:50rpx; width:126rpx; height:100rpx; border:4rpx solid rgba(255,255,255,.9); border-radius:18rpx; background:linear-gradient(145deg,#5DDAEA,#B6F5F2); box-shadow:0 16rpx 24rpx rgba(60,191,205,.22); transform:rotate(-6deg); }
.art-folder::before { content:""; position:absolute; left:10rpx; top:-22rpx; width:60rpx; height:28rpx; border-radius:12rpx 12rpx 0 0; background:#8AE8EF; }
.art-play { position:absolute; left:51rpx; top:34rpx; border-left:30rpx solid rgba(255,255,255,.95); border-top:20rpx solid transparent; border-bottom:20rpx solid transparent; filter:drop-shadow(0 3rpx 4rpx rgba(29,151,173,.18)); }
.art-gear { position:absolute; right:18rpx; bottom:0; width:78rpx; height:78rpx; display:flex; align-items:center; justify-content:center; border:7rpx dotted rgba(255,255,255,.95); border-radius:50%; background:#65DCE6; color:#fff; font-size:34rpx; box-shadow:0 9rpx 18rpx rgba(58,187,201,.22); }
.steps { height:486rpx; padding:26rpx 34rpx 20rpx; }
.block-title { display:block; font-size:27rpx; line-height:1.2; font-weight:700; color:#1A315E; }
.step-list { margin-top:16rpx; }
.step { display:flex; height:56rpx; }
.step-index { width:32rpx; padding-top:7rpx; font-size:20rpx; color:#6CCFC8; }
.st-axis { position:relative; width:42rpx; display:flex; flex-direction:column; align-items:center; }
.st-dot { position:relative; z-index:2; width:27rpx; height:27rpx; display:flex; align-items:center; justify-content:center; border-radius:50%; background:#DBE2E9; color:#fff; font-size:17rpx; }
.st-dot.done { background:#45D4C7; box-shadow:0 0 0 4rpx rgba(69,212,199,.11); }
.st-dot.cur { width:48rpx; height:48rpx; margin-top:-10rpx; background:linear-gradient(145deg,#5CDCE2,#60BFF3); font-size:23rpx; font-weight:700; box-shadow:0 0 0 10rpx rgba(80,210,224,.12),0 8rpx 16rpx rgba(74,190,217,.22); }
.st-line { position:absolute; top:27rpx; bottom:-29rpx; width:3rpx; background:#DDE4EA; }
.st-line.done { background:#63D9CE; }
.st-body { display:flex; flex-direction:column; padding:2rpx 0 0 18rpx; }
.st-name { font-size:24rpx; font-weight:600; color:#273C63; }
.st-name.muted { color:#B0BAC7; font-weight:400; }
.st-date { margin-top:2rpx; font-size:18rpx; color:#A7B1BF; }
.steps-note { position:absolute; left:34rpx; right:34rpx; bottom:20rpx; height:42rpx; display:flex; align-items:center; gap:12rpx; padding-top:12rpx; border-top:1rpx solid #DCEAEC; color:#8FA6B1; font-size:18rpx; }
.note-dot { width:8rpx; height:8rpx; border-radius:50%; background:#50D5CB; }
.info { height:168rpx; padding:24rpx 26rpx 16rpx; }
.info-grid { display:flex; margin-top:14rpx; }
.info-item { width:25%; display:flex; flex-direction:column; align-items:center; border-right:1rpx solid rgba(222,234,238,.8); }
.info-item:last-child { border-right:0; }
.info-icon { position:relative; width:34rpx; height:34rpx; border-radius:8rpx; background:linear-gradient(145deg,#65E2D1,#37C9D7); box-shadow:0 5rpx 12rpx rgba(63,204,205,.18); }
.video-icon view { position:absolute; left:13rpx; top:8rpx; border-left:12rpx solid #fff; border-top:9rpx solid transparent; border-bottom:9rpx solid transparent; }
.calendar-icon::before { content:""; position:absolute; left:7rpx; top:10rpx; width:20rpx; height:16rpx; border:2rpx solid #fff; border-radius:3rpx; box-shadow:inset 0 5rpx 0 rgba(255,255,255,.35); }
.clock-icon { border-radius:50%; }
.clock-icon::before { content:""; position:absolute; left:16rpx; top:7rpx; width:2rpx; height:11rpx; background:#fff; transform-origin:bottom; transform:rotate(-25deg); }
.list-icon::before { content:""; position:absolute; left:9rpx; top:8rpx; width:16rpx; height:2rpx; background:#fff; box-shadow:0 6rpx 0 #fff,0 12rpx 0 #fff; }
.ig-label { margin-top:6rpx; font-size:17rpx; color:#98A5B5; }
.ig-value { margin-top:2rpx; font-size:19rpx; font-weight:600; color:#53637B; white-space:nowrap; }
.ig-value.small { font-size:15rpx; }
.manager { height:196rpx; padding:24rpx 26rpx 0; }
.manager-row { display:flex; align-items:center; height:88rpx; }
.manager-avatar { width:66rpx; height:66rpx; border:3rpx solid #fff; border-radius:50%; background:#E8EFF1; box-shadow:0 4rpx 10rpx rgba(48,75,86,.12); }
.manager-info { flex:1; min-width:0; margin-left:14rpx; }
.manager-name-row { display:flex; align-items:center; white-space:nowrap; }
.manager-name { font-size:24rpx; font-weight:700; color:#263A5F; }
.manager-tag { margin-left:8rpx; padding:3rpx 9rpx; border-radius:10rpx; background:#EAFBFA; color:#53C3BE; font-size:14rpx; }
.manager-time { display:block; margin-top:4rpx; font-size:17rpx; color:#99A7B7; }
.manager-action { width:96rpx; display:flex; flex-direction:column; align-items:center; gap:3rpx; color:#8190A3; font-size:16rpx; }
.bubble-icon,.question-icon { width:38rpx; height:38rpx; display:flex; align-items:center; justify-content:center; border-radius:50%; color:#fff; font-weight:700; }
.bubble-icon { border-radius:14rpx; background:#53D7CA; letter-spacing:1rpx; font-size:14rpx; }
.question-icon { background:#69CEF2; font-size:23rpx; }
.manager-record { height:48rpx; display:flex; align-items:center; border-top:1rpx solid #E4EEF0; color:#8C99A9; font-size:18rpx; }
.record-icon { width:19rpx; height:23rpx; margin-right:10rpx; border:2rpx solid #A8B3C0; border-radius:3rpx; }
.chevron { margin-left:auto; font-size:28rpx; color:#A7B3C1; }
.bottom-space { height:155rpx; }
</style>
