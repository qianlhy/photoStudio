<template>
	<sales-shell active="customer" title="客户经营" subtitle="管理客户的内容生命周期">
		<view slot="search" class="searchbar">
			<text class="s-ico"></text>
			<input v-model="keyword" class="s-input" placeholder="搜索客户、行业或门店" @confirm="loadCustomers" />
		</view>
		<view slot="actions" class="btn btn-danger new-btn" @click="newCustomer">＋ 新建客户</view>

		<!-- 筛选标签 -->
		<view class="tabs">
			<view v-for="t in tabs" :key="t.key" class="tab" :class="{on: t.key===activeTab}" @click="pickTab(t)">
				<text v-if="t.dot" class="t-dot" :style="{background:t.dot}"></text>
				{{ t.label }} {{ t.count }}
			</view>
		</view>

		<view class="cm">
			<!-- 左：今天最该联系 -->
			<view class="col-list">
				<view class="cl-title">今天最该联系</view>
				<scroll-view scroll-y class="cl-scroll">
					<view v-for="c in shownCustomers" :key="c.id" class="cl-card" :class="{on:selected&&selected.id===c.id}"
						@click="select(c)">
						<image class="cl-img" :src="$img(c.avatar)" mode="aspectFill"></image>
						<view class="cl-body">
							<view class="cl-row">
								<text class="cl-name">{{ c.name }}</text>
							</view>
							<view class="cl-tag" :class="tagClass(c)">{{ mainTag(c) }}</view>
							<view class="cl-foot">
								<text class="cl-status" :style="{color: statusColor(c)}">● {{ statusText(c) }}</text>
								<text class="cl-deadline" v-if="c.publishDeadline">预计发布至 {{ c.publishDeadline }}</text>
							</view>
						</view>
						<view class="cl-remain">
							<text class="cl-remain-num">剩 {{ c.remainCount||0 }} 条</text>
							<text class="cl-arrow">›</text>
						</view>
					</view>
				</scroll-view>
			</view>

			<!-- 中：客户画像/生命周期 -->
			<view class="col-center" v-if="selected">
				<view class="profile card">
					<image class="pf-cover" :src="$img(selected.avatar)" mode="aspectFill"></image>
					<view class="pf-head">
						<image class="pf-avatar" :src="$img(selected.avatar)" mode="aspectFill"></image>
						<view class="pf-name-block">
							<text class="pf-name">{{ selected.name }}</text>
							<view class="pf-tags">
								<text v-if="selected.biztype" class="chip-mini blue">{{ selected.biztype }}</text>
								<text v-for="(tg,i) in tagList(selected)" :key="i" class="chip-mini">{{ tg }}</text>
							</view>
						</view>
						<view class="btn btn-primary pf-follow" @click="goFollow">开始跟进</view>
					</view>
					<view class="pf-pref">
						<text class="pf-pref-label">内容偏好</text>
						<view class="pf-pref-chips">
							<text v-for="(p,i) in prefList(selected)" :key="i" class="chip-mini">{{ p }}</text>
						</view>
					</view>
				</view>

				<!-- 项目完成度 -->
				<view class="card progress-card">
					<view class="pc-head">
						<text class="pc-title">项目完成度</text>
						<text class="pc-pct">{{ completePct }}%</text>
					</view>
					<view class="steps">
						<view class="step done"><view class="st-dot">✓</view><text class="st-l">已选 {{ selected.selectedCount||0 }}</text></view>
						<view class="step-line done"></view>
						<view class="step done"><view class="st-dot">✓</view><text class="st-l">已拍 {{ selected.shotCount||0 }}</text></view>
						<view class="step-line done"></view>
						<view class="step done"><view class="st-dot">✓</view><text class="st-l">已交付 {{ selected.deliveredCount||0 }}</text></view>
						<view class="step-line"></view>
						<view class="step"><view class="st-dot grey"></view><text class="st-l">已发布 {{ selected.publishedCount||0 }}</text></view>
					</view>
				</view>

				<!-- 内容生命值 -->
				<view class="card life-card">
					<view class="life-top">
						<view class="life-left">
							<view class="ring" :style="ringStyle">
								<view class="ring-hole">
									<text class="ring-num">剩 <text class="big">{{ selected.remainCount||0 }}</text></text>
									<text class="ring-sub">预计可发布 {{ selected.publishDays||0 }} 天</text>
								</view>
							</view>
						</view>
						<view class="life-right">
							<text class="life-label">内容预计发布至</text>
							<text class="life-date">{{ selected.publishDeadline || '—' }}</text>
							<text v-if="(selected.publishDays||0)<=8" class="life-warn">⚠ 即将断更</text>
							<view class="btn btn-danger life-btn" @click="recommendRenew">推荐续拍方案</view>
						</view>
					</view>
					<view class="life-week">
						<view v-for="(d,i) in weekDays" :key="i" class="lw">
							<text class="lw-l">{{ d.label }}</text>
							<text class="lw-d">{{ d.day }}</text>
							<view class="lw-dot" :style="{background:d.color}"></view>
						</view>
					</view>
				</view>
			</view>
			<view class="col-center empty-center" v-else>
				<text>请选择左侧客户查看详情</text>
			</view>

			<!-- 右：下一步行动 / 时间线 / 建议 -->
			<view class="col-side" v-if="selected">
				<view class="card act-card">
					<text class="sc-title">下一步行动</text>
					<view class="act-box">
						<text class="act-main">{{ nextAction.title }}</text>
						<text class="act-sub">{{ nextAction.sub }}</text>
					</view>
					<view v-if="selected.followStatus==='待付款'" class="btn btn-danger act-btn" @click="confirmPay">确认收款并生成订单</view>
					<view class="btn btn-danger act-btn" :class="{'btn-ghost': selected.followStatus==='待付款'}" @click="goFollow">记录本次跟进</view>
				</view>

				<view class="card tl-card grow">
					<text class="sc-title">跟进时间线</text>
					<view v-if="timeline.length===0" class="empty sm">暂无跟进记录</view>
					<view v-for="(t,i) in timeline" :key="t.id" class="tl-item">
						<view class="tl-dot" :style="{background: dotColor(i)}"></view>
						<view class="tl-body">
							<text class="tl-date">{{ formatMd(t.addtime) }}</text>
							<text class="tl-text">{{ t.summary || t.contactResult || t.nextAction }}</text>
						</view>
					</view>
				</view>

				<view class="card sug-card">
					<view class="sc-title">智能建议 <text class="spark">✦</text></view>
					<text class="sug-text">{{ suggestion }}</text>
				</view>
			</view>
		</view>
	</sales-shell>
</template>

<script>
import salesShell from '@/components/sales-shell/sales-shell.vue'
export default {
	components: { salesShell },
	data() {
		return {
			keyword: '',
			activeTab: 'today',
			customers: [],
			selected: null,
			timeline: [],
			tabs: [
				{ key: 'today', label: '今天要跟进', count: 0 },
				{ key: 'unselect', label: '选片未完成', count: 0 },
				{ key: 'ending', label: '内容将发完', count: 0, dot: '#FF8A3D' },
				{ key: 'repurchase', label: '适合复购', count: 0, dot: '#22B07D' },
				{ key: 'all', label: '全部客户', count: 0 }
			]
		}
	},
	computed: {
		shownCustomers() {
			let list = this.customers.slice()
			if (this.activeTab === 'unselect') list = list.filter(c => (c.deliveredCount || 0) < (c.selectedCount || 0))
			else if (this.activeTab === 'ending') list = list.filter(c => (c.remainCount || 0) <= 4)
			else if (this.activeTab === 'repurchase') list = list.filter(c => (c.dealCount || 0) >= 1 && (c.remainCount || 0) <= 6)
			else if (this.activeTab === 'today') list = list.filter(c => c.intention === '高' || c.followStatus === '跟进中' || c.followStatus === '待付款')
			return list
		},
		completePct() {
			if (!this.selected || !this.selected.selectedCount) return 0
			return Math.round(((this.selected.deliveredCount || 0) / this.selected.selectedCount) * 100)
		},
		ringStyle() {
			const remain = (this.selected && this.selected.remainCount) || 0
			const total = Math.max(remain, (this.selected && this.selected.selectedCount) || 10)
			const pct = Math.min(100, Math.round((remain / total) * 100))
			const color = remain <= 4 ? '#FF8A3D' : '#2F6BFF'
			return { background: `conic-gradient(${color} ${pct}%, #EEF1F5 0)` }
		},
		nextAction() {
			if (!this.selected) return { title: '', sub: '' }
			const s = this.selected
			if (s.followStatus === '待付款') return { title: '提醒客户完成付款', sub: '内容方案已确认，等待付款' }
			if ((s.remainCount || 0) <= 8) return { title: '今天联系客户确认续拍', sub: `现有内容在 ${s.publishDays || 0} 天后发完` }
			return { title: '保持跟进', sub: '关注客户内容发布进度' }
		},
		suggestion() {
			if (!this.selected) return ''
			const prefs = this.prefList(this.selected)
			const main = prefs[0] || '硬广'
			return `客户偏好${main}，建议下周增加 2 条讲故事，提升品牌温度`
		},
		weekDays() {
			const days = (this.selected && this.selected.publishDays) || 0
			const wk = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
			const out = []
			for (let i = 0; i < 8; i++) {
				const d = new Date()
				d.setDate(d.getDate() + i)
				const label = i === 0 ? '今天' : (i === 1 ? '明天' : (i === 2 ? '后天' : wk[d.getDay()]))
				let color = '#22B07D'
				if (i >= days) color = '#E2E6EC'
				else if (i >= days - 2) color = '#FF8A3D'
				out.push({ label, day: d.getDate(), color })
			}
			return out
		}
	},
	onLoad(opt) {
		this.preselectId = opt.id || null
	},
	onShow() {
		if (!this.$api.auth()) return
		this.loadCustomers()
	},
	methods: {
		loadCustomers() {
			const q = { page: 1, limit: 100 }
			if (this.keyword) q.name = '%' + this.keyword + '%'
			this.$api.page('hyCustomer', q).then(res => {
				const list = (res.data && res.data.list) || []
				this.customers = list
				this.tabs[0].count = list.filter(c => c.intention === '高' || c.followStatus === '跟进中' || c.followStatus === '待付款').length
				this.tabs[1].count = list.filter(c => (c.deliveredCount || 0) < (c.selectedCount || 0)).length
				this.tabs[2].count = list.filter(c => (c.remainCount || 0) <= 4).length
				this.tabs[3].count = list.filter(c => (c.dealCount || 0) >= 1 && (c.remainCount || 0) <= 6).length
				this.tabs[4].count = (res.data && res.data.total) || list.length
				let sel = null
				if (this.preselectId) sel = list.find(c => String(c.id) === String(this.preselectId))
				this.select(sel || this.shownCustomers[0] || list[0])
			})
		},
		select(c) {
			if (!c) { this.selected = null; return }
			this.selected = c
			this.loadTimeline(c.id)
		},
		loadTimeline(cid) {
			this.$api.page('hyFollowRecord', { page: 1, limit: 8, customerId: cid }).then(res => {
				this.timeline = (res.data && res.data.list) || []
			})
		},
		pickTab(t) {
			this.activeTab = t.key
			if (!this.shownCustomers.find(c => this.selected && c.id === this.selected.id)) {
				this.select(this.shownCustomers[0])
			}
		},
		tagList(c) {
			return (c.tags ? String(c.tags).split(',') : []).filter(Boolean).slice(0, 2)
		},
		prefList(c) {
			return (c.preference ? String(c.preference).split(',') : []).filter(Boolean)
		},
		mainTag(c) {
			const t = this.tagList(c)[0]
			return t || (c.intention === '高' ? '重点客户' : '普通客户')
		},
		tagClass(c) {
			const t = this.mainTag(c)
			if (t.indexOf('重点') >= 0) return 'red'
			if (t.indexOf('成长') >= 0) return 'green'
			return 'blue'
		},
		statusText(c) {
			if (c.followStatus === '待付款') return '待付款'
			if (c.intention === '高') return '今日跟进'
			return c.followStatus || '跟进中'
		},
		statusColor(c) {
			if (c.followStatus === '待付款') return '#FF8A3D'
			if (c.intention === '高') return '#FF5A5F'
			return '#22B07D'
		},
		dotColor(i) {
			return ['#FF5A5F', '#2F6BFF', '#22B07D', '#FF8A3D', '#7C5CFF'][i % 5]
		},
		formatMd(t) {
			if (!t) return ''
			const d = new Date(t.replace ? t.replace(/-/g, '/') : t)
			return `${d.getMonth() + 1}月${d.getDate()}日`
		},
		newCustomer() {
			uni.showToast({ title: '新建客户（演示）', icon: 'none' })
		},
		goFollow() {
			uni.navigateTo({ url: `/pages/customer/follow?customerId=${this.selected.id}&customerName=${encodeURIComponent(this.selected.name)}` })
		},
		confirmPay() {
			if (!this.selected) return
			uni.showModal({
				title: '确认收款',
				content: `确认「${this.selected.name}」已付款？将自动生成订单（待拍摄）并回写内容库存。`,
				success: (r) => {
					if (!r.confirm) return
					this.$api.post('hyCustomer/confirmPay', { customerId: this.selected.id }).then(res => {
						const no = (res.data && res.data.orderNo) || ''
						uni.showToast({ title: no ? `订单已生成 ${no}` : '订单已生成', icon: 'success' })
						this.loadCustomers()
						setTimeout(() => uni.navigateTo({ url: '/pages/order/order' }), 700)
					})
				}
			})
		},
		recommendRenew() {
			uni.showToast({ title: '已生成续拍方案建议', icon: 'none' })
		}
	}
}
</script>

<style lang="scss" scoped>
.searchbar {
	width: 560rpx;
	height: 64rpx;
	background: #F4F6FA;
	border-radius: 999rpx;
	display: flex;
	align-items: center;
	padding: 0 24rpx;
}
.s-ico { font-size: 26rpx; margin-right: 12rpx; }
.s-input { flex: 1; font-size: 26rpx; }
.new-btn { height: 64rpx; padding: 0 28rpx; font-size: 26rpx; }

.tabs {
	display: flex;
	gap: 16rpx;
	margin-bottom: 22rpx;
	flex-wrap: wrap;
	flex-shrink: 0;
}
.tab {
	padding: 12rpx 28rpx;
	background: #fff;
	border: 1rpx solid $line;
	border-radius: 999rpx;
	font-size: 25rpx;
	color: $ink-2;
}
.tab.on {
	background: linear-gradient(90deg, #FF7A59, #FF4D7E);
	color: #fff;
	border: none;
}
.t-dot { display:inline-block; width:12rpx; height:12rpx; border-radius:50%; margin-right:8rpx; }

.cm {
	flex: 1;
	min-height: 0;
	height: 100%;
	display: flex;
	gap: 24rpx;
	align-items: stretch;
}
.col-list { flex: 1.1; min-width: 0; min-height: 0; display: flex; flex-direction: column; }
.col-center { flex: 1.4; min-width: 0; min-height: 0; display: flex; flex-direction: column; gap: 20rpx; overflow-y: auto; }
.col-side { flex: 1; min-width: 0; min-height: 0; display: flex; flex-direction: column; justify-content: space-between; gap: 20rpx; overflow-y: auto; }

.cl-title, .sc-title { font-size: 28rpx; font-weight: 600; margin-bottom: 16rpx; flex-shrink: 0; }
.cl-scroll { flex: 1; min-height: 0; }
.cl-card {
	display: flex;
	align-items: center;
	background: #fff;
	border: 1rpx solid $line;
	border-radius: 18rpx;
	padding: 18rpx;
	margin-bottom: 16rpx;
}
.cl-card.on { border-color: rgba(47,107,255,.55); background:#FBFDFF; box-shadow: 0 8rpx 22rpx rgba(47,107,255,.10); }
.cl-img { width: 110rpx; height: 110rpx; border-radius: 14rpx; background: #eee; flex-shrink:0; }
.cl-body { flex: 1; margin-left: 16rpx; min-width: 0; }
.cl-name { font-size: 27rpx; font-weight: 700; }
.cl-tag { display:inline-block; margin-top:8rpx; padding:4rpx 14rpx; border-radius:999rpx; font-size:20rpx; }
.cl-tag.red { background:#FDECEC; color:#FF5A5F; }
.cl-tag.green { background:#E8F7F0; color:#22B07D; }
.cl-tag.blue { background:#EAF1FF; color:#2F6BFF; }
.cl-foot { margin-top:10rpx; display:flex; flex-direction:column; }
.cl-status { font-size: 22rpx; }
.cl-deadline { font-size: 21rpx; color: $muted; margin-top:4rpx; }
.cl-remain { display:flex; flex-direction:column; align-items:flex-end; }
.cl-remain-num { font-size: 24rpx; color: $ink-2; font-weight:600; }
.cl-arrow { color:#C7CDD8; font-size:32rpx; margin-top:18rpx; }

/* 画像 */
.profile { overflow: hidden; }
.pf-cover { width: 100%; height: 180rpx; }
.pf-head { display:flex; align-items:center; padding: 0 24rpx; margin-top:-40rpx; }
.pf-avatar { width: 96rpx; height: 96rpx; border-radius: 50%; border: 4rpx solid #fff; background:#eee; box-shadow:0 4rpx 12rpx rgba(31,39,51,.12); }
.pf-name-block { flex:1; margin-left: 18rpx; }
.pf-name { font-size: 32rpx; font-weight: 700; }
.pf-tags { margin-top: 8rpx; }
.chip-mini { display:inline-block; padding:4rpx 14rpx; background:#F1F3F6; color:$ink-2; border-radius:999rpx; font-size:20rpx; margin-right:10rpx; }
.chip-mini.blue { background:#EAF1FF; color:#2F6BFF; }
.pf-follow { height: 64rpx; padding: 0 30rpx; font-size: 26rpx; }
.pf-pref { padding: 20rpx 24rpx 24rpx; }
.pf-pref-label { font-size: 24rpx; color: $muted; }
.pf-pref-chips { margin-top: 12rpx; }

/* 完成度 */
.progress-card { padding: 24rpx; }
.pc-head { display:flex; justify-content:space-between; align-items:center; margin-bottom: 24rpx; }
.pc-title { font-size: 27rpx; font-weight: 600; }
.pc-pct { font-size: 40rpx; font-weight: 800; color: #22B07D; }
.steps { display:flex; align-items:center; }
.step { display:flex; flex-direction:column; align-items:center; }
.st-dot { width:40rpx; height:40rpx; border-radius:50%; background:#22B07D; color:#fff; display:flex; align-items:center; justify-content:center; font-size:24rpx; }
.st-dot.grey { background:#D7DCE3; }
.st-l { font-size: 21rpx; color:$ink-2; margin-top:8rpx; }
.step-line { flex:1; height:4rpx; background:#E5E8EC; margin:0 4rpx 30rpx; }
.step-line.done { background:#22B07D; }

/* 生命值 */
.life-card { padding: 24rpx; display:flex; flex-direction:column; }
.life-top { display:flex; align-items:center; gap: 28rpx; }
.ring { width: 180rpx; height: 180rpx; border-radius: 50%; display:flex; align-items:center; justify-content:center; }
.ring-hole { width: 130rpx; height: 130rpx; border-radius:50%; background:#fff; display:flex; flex-direction:column; align-items:center; justify-content:center; }
.ring-num { font-size: 22rpx; color:$ink-2; }
.ring-num .big { font-size: 44rpx; font-weight:800; color:$ink; }
.ring-sub { font-size: 18rpx; color:$muted; margin-top:4rpx; text-align:center; }
.life-right { flex:1; display:flex; flex-direction:column; }
.life-label { font-size: 24rpx; color:$muted; }
.life-date { font-size: 48rpx; font-weight: 800; color: #FF5A5F; margin:4rpx 0; }
.life-warn { font-size: 22rpx; color:#FF5A5F; margin-bottom:12rpx; }
.life-btn { height: 72rpx; font-size: 26rpx; }
.life-week { display:flex; justify-content:space-between; margin-top:24rpx; padding-top:20rpx; border-top:1rpx solid #F0F2F5; }
.lw { display:flex; flex-direction:column; align-items:center; flex:1; }
.lw-l { font-size:19rpx; color:$muted; }
.lw-d { font-size:22rpx; color:$ink-2; margin:4rpx 0 8rpx; }
.lw-dot { width:14rpx; height:14rpx; border-radius:50%; }

/* 右栏 */
.act-card, .tl-card, .sug-card { padding: 24rpx; flex-shrink: 0; }
.tl-card.grow { max-height: 52%; overflow-y: auto; }
.act-box { background:#FDF1F0; border-radius:14rpx; padding:20rpx; margin:14rpx 0; }
.act-main { font-size:27rpx; font-weight:700; display:block; }
.act-sub { font-size:23rpx; color:$ink-2; margin-top:8rpx; display:block; }
.act-btn { height: 80rpx; font-size: 28rpx; margin-top: 14rpx; }
.tl-item { display:flex; padding:14rpx 0; }
.tl-dot { width:14rpx; height:14rpx; border-radius:50%; margin-top:8rpx; margin-right:16rpx; flex-shrink:0; }
.tl-date { font-size:22rpx; color:$muted; display:block; }
.tl-text { font-size:24rpx; color:$ink; display:block; margin-top:4rpx; }
.spark { color:#FF8A3D; }
.sug-text { font-size: 25rpx; color:$ink-2; line-height: 1.6; }
.empty { color:$muted; font-size:26rpx; text-align:center; padding:30rpx 0; }
.empty.sm { padding: 20rpx 0; }
.empty-center { display:flex; align-items:center; justify-content:center; color:$muted; min-height:400rpx; }

/* 1-3 标注稿：左/中/右约 337 / 591 / 353 */
@media #{$pad-mq-landscape} {
	.searchbar {
		width: 25vw;
		height: 5.2vh;
		padding: 0 1.2vw;
	}
	.s-input, .new-btn {
		font-size: clamp(12px, .95vw, 15px);
	}
	.new-btn {
		height: 5.4vh;
		padding: 0 1.5vw;
	}
	.tabs {
		height: 6.4vh;
		margin-bottom: 1.2vh;
		gap: .75vw;
		flex-wrap: nowrap;
	}
	.tab {
		flex: 1;
		box-sizing: border-box;
		text-align: center;
		padding: .9vh .8vw;
		font-size: clamp(12px, .95vw, 15px);
	}
	.cm {
		gap: .75vw;
	}
	.col-list {
		flex: 337;
	}
	.col-center {
		flex: 591;
		gap: 1.2vh;
		overflow: hidden;
	}
	.col-side {
		flex: 353;
		gap: 1.2vh;
		overflow: hidden;
	}
	.cl-title, .sc-title {
		font-size: clamp(15px, 1.2vw, 19px);
		margin-bottom: .8vh;
	}
	.cl-card {
		height: 16.2vh;
		box-sizing: border-box;
		border-radius: 12px;
		padding: 1.2vh .9vw;
		margin-bottom: 1vh;
	}
	.cl-img {
		width: 5.1vw;
		height: 10.8vh;
		border-radius: 9px;
	}
	.cl-body {
		margin-left: .8vw;
	}
	.cl-name {
		font-size: clamp(13px, 1.05vw, 17px);
	}
	.cl-tag {
		margin-top: .55vh;
		padding: .3vh .65vw;
		font-size: clamp(10px, .76vw, 12px);
	}
	.cl-foot {
		margin-top: .65vh;
	}
	.cl-status, .cl-deadline {
		font-size: clamp(10px, .78vw, 13px);
	}
	.cl-remain-num {
		font-size: clamp(12px, .92vw, 15px);
	}

	/* 画像卡按内容自适应，避免固定高度叠加 overflow:hidden 把姓名和偏好裁掉 */
	.profile {
		height: auto;
		flex: none;
		padding-bottom: .4vh;
	}
	.pf-cover {
		height: 10.5vh;
	}
	/* 只让头像压住封面，姓名与按钮对齐到封面下方，避免被封面遮住 */
	.pf-head {
		padding: 0 1.2vw;
		margin-top: -2.6vh;
		align-items: flex-end;
	}
	.pf-avatar {
		width: 4.8vw;
		height: 4.8vw;
	}
	.pf-name {
		font-size: clamp(17px, 1.45vw, 23px);
	}
	.pf-follow {
		height: 5.2vh;
		padding: 0 1.35vw;
		font-size: clamp(12px, .95vw, 15px);
	}
	.pf-pref {
		padding: 1.2vh 1.2vw;
	}
	.pf-pref-label {
		font-size: clamp(11px, .85vw, 14px);
	}
	.pf-pref-chips {
		margin-top: .65vh;
	}
	.chip-mini {
		font-size: clamp(10px, .76vw, 12px);
	}
	.progress-card {
		height: auto;
		box-sizing: border-box;
		padding: 1.4vh 1.2vw;
		flex: none;
	}
	.pc-head {
		margin-bottom: 1.2vh;
	}
	.pc-title {
		font-size: clamp(14px, 1.1vw, 18px);
	}
	.pc-pct {
		font-size: clamp(24px, 2vw, 32px);
	}
	.st-dot {
		width: 26px;
		height: 26px;
		font-size: 14px;
	}
	.st-l {
		font-size: clamp(10px, .75vw, 12px);
	}
	.life-card {
		flex: 1;
		min-height: 0;
		padding: 1.4vh 1.2vw;
	}
	/* 环形图与右侧文案贴着卡片顶部，多余高度留给底部周历 */
	.life-top {
		flex: none;
		align-items: center;
		gap: 1.4vw;
	}
	.ring {
		width: 9.5vw;
		height: 9.5vw;
	}
	.ring-hole {
		width: 6.8vw;
		height: 6.8vw;
	}
	.ring-num {
		font-size: clamp(12px, .9vw, 15px);
	}
	.ring-num .big {
		font-size: clamp(27px, 2.25vw, 36px);
	}
	.ring-sub {
		font-size: clamp(10px, .72vw, 12px);
	}
	.life-label {
		font-size: clamp(12px, .9vw, 15px);
	}
	.life-date {
		font-size: clamp(29px, 2.5vw, 40px);
	}
	.life-btn {
		height: 5.4vh;
		font-size: clamp(12px, .95vw, 15px);
	}
	.life-week {
		margin-top: auto;
		padding-top: 1vh;
	}
	.lw-l, .lw-d {
		font-size: clamp(9px, .7vw, 11px);
	}

	.act-card {
		height: 28%;
		box-sizing: border-box;
	}
	.tl-card.grow {
		height: 33%;
		max-height: none;
		box-sizing: border-box;
		overflow-y: auto;
	}
	.sug-card {
		height: 25%;
		box-sizing: border-box;
	}
	.act-card, .tl-card, .sug-card {
		padding: 1.5vh 1.15vw;
		border-radius: 13px;
	}
	.act-box {
		padding: 1.2vh 1vw;
		margin: .8vh 0;
	}
	.act-main {
		font-size: clamp(14px, 1.12vw, 18px);
	}
	.act-sub, .tl-text, .sug-text {
		font-size: clamp(11px, .86vw, 14px);
	}
	.act-btn {
		height: 5.4vh;
		font-size: clamp(13px, 1vw, 16px);
	}
	.tl-item {
		padding: .8vh 0;
	}
	.tl-date {
		font-size: clamp(10px, .76vw, 12px);
	}
}
</style>
