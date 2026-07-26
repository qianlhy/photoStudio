<template>
	<view class="page">
		<view class="bg"></view>
		<scroll-view scroll-y class="scroll">
			<view class="head">
				<view class="brand">{{ brandName }}</view>
				<image class="avatar" :src="avatar" mode="aspectFill"></image>
			</view>
			<view class="title">{{ greeting }}</view>
			<view class="subtitle">你的内容都在这里</view>

			<!-- 储备 -->
			<view class="reserve">
				<view class="rv-top">
					<text class="rv-l">储备 {{ reservePct }}%</text>
					<text class="rv-r">可用 {{ remain }}/{{ totalQuota }}条 · 约{{ publishDays }}天</text>
				</view>
				<view class="rv-bar"><view class="rv-in" :style="{width: reservePct+'%'}"></view></view>
				<text class="rv-note">由服务经理根据实际发布情况更新</text>
			</view>

			<!-- 当前服务 -->
			<view class="cur card">
				<view class="cur-l">
					<text class="cur-tag">当前服务</text>
					<text class="cur-title">本批{{ list.length }}条已交付</text>
					<text class="cur-sub">{{ order.packageName || '增长套餐' }} · {{ deliverText }}</text>
				</view>
				<view class="cur-link" @click="goService">查看服务记录 ›</view>
			</view>

			<!-- 我的成品 -->
			<view class="sec-head">
				<text class="blk-title">我的成品</text>
				<text class="sec-cnt">共 {{ list.length }} 条</text>
			</view>
			<scroll-view scroll-x class="chips">
				<view v-for="t in tabs" :key="t.key" class="chip" :class="{on:t.key===activeTab}" @click="activeTab=t.key">
					{{ t.label }} {{ t.count }}
				</view>
			</scroll-view>

			<view class="grid">
				<view v-for="m in shown" :key="m.id" class="cell" @click="play(m)">
					<image class="cover" :src="img(m.cover)" mode="aspectFill"></image>
					<view class="cell-mask"></view>
					<view class="dl" :class="{done:m.downloadStatus==='已下载'}" @click.stop="download(m)">
						<text v-if="m.downloadStatus==='已下载'">✓</text>
						<text v-else>⤓</text>
					</view>
					<view v-if="m.downloadStatus==='已下载'" class="stamp">已下载</view>
					<view v-else class="play">▶</view>
					<view class="cell-foot">
						<text class="ctype">{{ m.contentType }}</text>
						<text class="cname">{{ idx2(m) }} {{ m.title }}</text>
					</view>
				</view>
			</view>

			<!-- 对标参考 -->
			<view class="ref card">
				<view class="ref-lock">🔒</view>
				<view class="ref-info">
					<text class="ref-title">对标参考 {{ refCount }}</text>
					<text class="ref-sub">交付前用于确认拍摄方向 · 只读</text>
					<text class="ref-lock-note">🔒 不可下载</text>
				</view>
				<view class="ref-thumb" @click="viewRef">查看 ›</view>
			</view>

			<view style="height:160rpx"></view>
		</scroll-view>

		<view class="tabbar">
			<view class="tab" @click="goService">🎬 服务</view>
			<view class="tab mid">已交付</view>
			<view class="tab active">📺 内容</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			brandName: '影集',
			avatar: 'https://i.pravatar.cc/100?img=45',
			customerId: null,
			customer: {},
			order: {},
			list: [],
			refCount: 0,
			totalQuota: 15,
			activeTab: 'all',
			types: ['硬广', '晒过程', '教知识', '说观点', '讲故事']
		}
	},
	computed: {
		greeting() {
			const h = new Date().getHours()
			const g = h < 11 ? '早上好' : (h < 14 ? '中午好' : (h < 18 ? '下午好' : '晚上好'))
			return `${g}，${this.customer.name || '客户'}`
		},
		remain() { return this.customer.remainCount || 0 },
		publishDays() { return this.customer.publishDays || 0 },
		reservePct() { return Math.min(100, Math.round((this.remain / this.totalQuota) * 100)) },
		deliverText() {
			const t = this.order.deliverDate
			if (!t) return '—'
			const d = new Date(String(t).replace(/-/g, '/'))
			return `${d.getMonth() + 1}月${d.getDate()}日`
		},
		tabs() {
			const arr = [{ key: 'all', label: '全部', count: this.list.length }]
			this.types.forEach(t => {
				const c = this.list.filter(m => m.contentType === t).length
				if (c > 0) arr.push({ key: t, label: t, count: c })
			})
			return arr
		},
		shown() {
			if (this.activeTab === 'all') return this.list
			return this.list.filter(m => m.contentType === this.activeTab)
		}
	},
	onLoad() {
		this.customerId = uni.getStorageSync('hyCustomerId') || null
		this.load()
	},
	methods: {
		img(v) { return v ? (/^https?:/.test(v) ? v : this.$base.url + String(v).split(',')[0]) : '' },
		idx2(m) {
			const i = this.list.indexOf(m) + 1
			return (i < 10 ? '0' + i : '' + i)
		},
		load() {
			const finish = (cid) => {
				this.$api.list('hyCustomer', { id: cid }).then(res => { this.customer = (res.data && res.data[0]) || {} })
				this.$api.page('hyDeliverable', { customerId: cid, page: 1, limit: 50, sort: 'sort', order: 'asc' }).then(res => {
					this.list = (res.data && res.data.list) || []
				})
				this.$api.page('hyOrder', { customerId: cid, page: 1, limit: 1, sort: 'addtime', order: 'desc' }).then(res => {
					this.order = (res.data && res.data.list && res.data.list[0]) || {}
				})
				this.$api.page('hyContentPlan', { customerId: cid, page: 1, limit: 1 }).then(res => {
					const p = (res.data && res.data.list && res.data.list[0]) || {}
					this.refCount = p.totalCount || 0
				})
			}
			if (this.customerId) { finish(this.customerId); return }
			this.$api.page('hyCustomer', { page: 1, limit: 1 }).then(res => {
				const c = (res.data && res.data.list && res.data.list[0]) || {}
				this.customerId = c.id
				if (c.id) { uni.setStorageSync('hyCustomerId', c.id); finish(c.id) }
			})
		},
		play(m) {
			if (m.video) {
				uni.navigateTo({ url: `/pages/hy-content/content` }) // 占位：可接视频全屏播放
				uni.showToast({ title: '播放：' + m.title, icon: 'none' })
			}
		},
		download(m) {
			uni.request({
				url: `${this.$base.url}hyDeliverable/download/${m.id}`,
				method: 'GET',
				header: { Token: uni.getStorageSync('token') },
				success: () => {
					m.downloadStatus = '已下载'
					this.$set(m, 'downloadStatus', '已下载')
					uni.showToast({ title: '已开始下载', icon: 'success' })
				}
			})
		},
		viewRef() { uni.showToast({ title: '对标参考为只读，不可下载', icon: 'none' }) },
		goService() { uni.redirectTo({ url: '/pages/hy-service/service' }) }
	}
}
</script>

<style lang="scss" scoped>
.page { width: 100%; height: 100vh; position: relative; background: #F4F2FA; }
.bg { position: absolute; top: 0; left: 0; right: 0; height: 360rpx; background: linear-gradient(160deg, #EFEAFB 0%, #F4F6FA 70%); }
.scroll { position: relative; height: 100vh; padding: 0 28rpx; box-sizing: border-box; }
.head { display: flex; justify-content: space-between; align-items: center; padding-top: 90rpx; }
.brand { font-size: 30rpx; font-weight: 700; color: #1F2733; }
.avatar { width: 76rpx; height: 76rpx; border-radius: 50%; }
.title { font-size: 52rpx; font-weight: 800; color: #1F2733; margin-top: 18rpx; }
.subtitle { font-size: 26rpx; color: #6B7785; margin-top: 6rpx; }

.reserve { margin-top: 24rpx; }
.rv-top { display: flex; justify-content: space-between; }
.rv-l { font-size: 24rpx; font-weight: 600; }
.rv-r { font-size: 22rpx; color: #6B7785; }
.rv-bar { height: 14rpx; background: rgba(255,255,255,.6); border-radius: 999rpx; margin: 12rpx 0 8rpx; overflow: hidden; }
.rv-in { height: 100%; background: linear-gradient(90deg, #FF8A3D, #7C5CFF, #2F6BFF); border-radius: 999rpx; }
.rv-note { font-size: 20rpx; color: #9AA6B2; }

.card { background: #fff; border-radius: 24rpx; padding: 24rpx; margin-top: 22rpx; box-shadow: 0 8rpx 24rpx rgba(31,39,51,.05); }
.cur { background: linear-gradient(135deg, #FFF0EC, #FFF6F3); display: flex; align-items: center; justify-content: space-between; }
.cur-tag { font-size: 22rpx; color: #6B7785; }
.cur-title { font-size: 32rpx; font-weight: 800; display: block; margin: 6rpx 0; }
.cur-sub { font-size: 22rpx; color: #6B7785; }
.cur-link { font-size: 24rpx; color: #FF5A5F; background: #fff; padding: 14rpx 22rpx; border-radius: 999rpx; }

.sec-head { display: flex; justify-content: space-between; align-items: baseline; margin-top: 30rpx; }
.blk-title { font-size: 32rpx; font-weight: 800; }
.sec-cnt { font-size: 24rpx; color: #9AA6B2; }
.chips { white-space: nowrap; margin: 18rpx 0; }
.chip { display: inline-block; padding: 10rpx 26rpx; background: #fff; border-radius: 999rpx; font-size: 24rpx; color: #6B7785; margin-right: 14rpx; }
.chip.on { background: #7C5CFF; color: #fff; }

.grid { display: flex; flex-wrap: wrap; justify-content: space-between; }
.cell { width: 31.5%; height: 320rpx; position: relative; border-radius: 16rpx; overflow: hidden; margin-bottom: 16rpx; background: #222; }
.cover { width: 100%; height: 100%; filter: grayscale(1); }
.cell-mask { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: linear-gradient(180deg, rgba(0,0,0,.1), rgba(0,0,0,.55)); }
.dl { position: absolute; top: 10rpx; right: 10rpx; width: 44rpx; height: 44rpx; border-radius: 50%; background: rgba(255,255,255,.85); display: flex; align-items: center; justify-content: center; font-size: 24rpx; color: #1F2733; }
.dl.done { background: #22B07D; color: #fff; }
.stamp { position: absolute; top: 50%; left: 50%; transform: translate(-50%,-50%) rotate(-12deg); border: 3rpx solid #fff; color: #fff; font-size: 26rpx; padding: 6rpx 18rpx; border-radius: 10rpx; opacity: .9; }
.play { position: absolute; top: 50%; left: 50%; transform: translate(-50%,-50%); width: 64rpx; height: 64rpx; border-radius: 50%; background: rgba(255,255,255,.85); display: flex; align-items: center; justify-content: center; color: #1F2733; }
.cell-foot { position: absolute; left: 12rpx; bottom: 12rpx; right: 12rpx; }
.ctype { font-size: 18rpx; color: #fff; background: rgba(124,92,255,.8); padding: 2rpx 12rpx; border-radius: 6rpx; }
.cname { display: block; font-size: 22rpx; color: #fff; margin-top: 8rpx; text-shadow: 0 2rpx 6rpx rgba(0,0,0,.6); }

.ref { display: flex; align-items: center; background: linear-gradient(135deg, #F0ECFB, #F4F6FF); }
.ref-lock { font-size: 44rpx; margin-right: 18rpx; }
.ref-info { flex: 1; }
.ref-title { font-size: 28rpx; font-weight: 700; }
.ref-sub { font-size: 22rpx; color: #6B7785; display: block; margin-top: 6rpx; }
.ref-lock-note { font-size: 20rpx; color: #9AA6B2; }
.ref-thumb { font-size: 24rpx; color: #7C5CFF; background: #fff; padding: 14rpx 22rpx; border-radius: 999rpx; }

.tabbar { position: absolute; left: 0; right: 0; bottom: 0; height: 120rpx; background: #fff; display: flex; align-items: center; justify-content: space-around; box-shadow: 0 -4rpx 18rpx rgba(0,0,0,.05); }
.tab { font-size: 26rpx; color: #9AA6B2; padding: 16rpx 40rpx; border-radius: 999rpx; }
.tab.active { background: linear-gradient(90deg, #7C5CFF, #9B7BFF); color: #fff; font-weight: 700; }
.tab.mid { font-size: 22rpx; }
</style>
