<template>
	<sales-shell active="message" title="销售行动中心" subtitle="只处理真正需要你介入的事情">
		<view slot="search" class="searchbar">
			<text class="s-ico"></text>
			<input v-model="keyword" class="s-input" placeholder="搜索客户或事项" />
		</view>
		<view slot="actions" class="done-link" @click="viewDone"><text class="clock-ico"></text>已完成</view>

		<view class="ac">
			<view class="col-main">
				<view class="todo-head">待我处理 <text class="th-num">{{ actions.length }}</text></view>
				<view class="filters">
					<view v-for="f in filters" :key="f.key" class="f" :class="{on:f.key===activeFilter}" @click="activeFilter=f.key">
						{{ f.label }} {{ countOf(f) }}
					</view>
				</view>

				<scroll-view scroll-y class="a-scroll">
				<view v-for="a in shownActions" :key="a.id" class="a-card">
					<image class="a-thumb" :src="$img(coverOfAction(a))" mode="aspectFill"></image>
					<view class="a-content">
					<view class="a-top">
						<text class="a-tag" :class="tagClass(a.type)">{{ tagLabel(a) }}</text>
						<text class="a-cust">{{ a.customerName }}</text>
					</view>
					<view class="a-mid">
						<view class="a-left">
							<text class="a-title">{{ a.title }}</text>
							<text class="a-meta" v-if="a.type==='待付款'">意向：{{ a.intention }} · 负责人：{{ a.ownerName }} · 今天跟进</text>
							<text class="a-meta" v-else-if="a.type==='客诉'">编导已收到修改意见，销售需确认客户情绪与处理结果</text>
							<!-- 制作预警 进度条 -->
							<view v-if="a.type==='制作预警'" class="thresh">
								<view class="th-bar">
									<view class="th-in" :style="{width:(a.innerProgress||0)+'%'}"></view>
									<view class="th-mk" style="left:50%"></view>
									<view class="th-mk active" :style="{left:(a.innerProgress||0)+'%'}"></view>
									<view class="th-mk" style="left:100%"></view>
								</view>
								<view class="th-labels">
									<text>内部进度50%</text>
									<text>内部进度2/3</text>
									<text>对客承诺30天</text>
								</view>
								<text class="th-note">对客承诺30天 · 内部要求15天完成</text>
							</view>
						</view>
						<view class="a-actions">
							<template v-if="a.type==='待付款'">
								<view class="btn btn-danger ab" @click="confirmPay(a)">确认收款</view>
								<view class="btn btn-ghost ab" @click="contact(a)">联系客户</view>
								<view class="btn btn-ghost ab" @click="recordFollow(a)">记录跟进</view>
								<view class="btn btn-ghost ab" @click="viewPlan(a)">查看内容方案</view>
							</template>
							<template v-else-if="a.type==='制作预警'">
								<view v-if="a.canIntervene==1" class="btn ab intervene" @click="resolve(a,'已发起内部干预')">↗ 发起内部干预</view>
								<text v-else class="a-watch">已过半 · 黄色关注（未达 2/3 或进度未落后）</text>
								<view class="btn btn-ghost ab" @click="viewProgress(a)">查看制作进度</view>
							</template>
							<template v-else-if="a.type==='客诉'">
								<view class="btn btn-danger ab" @click="resolve(a,'已受理投诉')">受理投诉</view>
								<view class="btn btn-ghost ab" @click="contact(a)">联系客户</view>
								<view class="btn btn-ghost ab" @click="viewProgress(a)">查看处理进度</view>
							</template>
							<template v-else>
								<view class="btn ab renew" @click="resolve(a,'已发起续拍跟进')">↗ 发起续拍跟进</view>
								<view class="btn btn-ghost ab" @click="viewPref(a)">查看客户偏好</view>
							</template>
						</view>
					</view>
					</view>
					<text class="a-chev">›</text>
				</view>
				<view v-if="shownActions.length===0" class="empty">暂无需要处理的事项</view>
				</scroll-view>

				<!-- 闭环步骤：随待办数量推进，无事项时全灰 -->
				<view class="loop card">
					<view class="lp" v-for="(s,i) in loopSteps" :key="i">
						<view class="lp-dot" :class="{on: i < loopActive, cur: i === loopActive && loopActive < loopSteps.length}">
							{{ i < loopActive ? '✓' : (i === loopActive && loopActive < loopSteps.length ? '◉' : '') }}
						</view>
						<text class="lp-l">{{ s }}</text>
						<view v-if="i<loopSteps.length-1" class="lp-line" :class="{on: i < loopActive}"></view>
					</view>
				</view>
			</view>

			<!-- 右：客户消息 + 智能摘要 -->
			<view class="col-side">
				<view class="card msg-card grow">
					<view class="msg-head"><text class="chat-ico"></text>客户消息 <text class="m-num">{{ shownMessages.length }}</text> <text class="m-dot"></text></view>
					<view v-for="m in shownMessages" :key="m.id" class="msg">
						<image class="msg-av" :src="$img(coverOfMsg(m))" mode="aspectFill"></image>
						<view class="msg-body">
							<text class="msg-name">{{ m.customerName }}：</text>
							<text class="msg-text">{{ m.title || m.content }}</text>
							<text class="msg-flag" :class="m.belong==='编导'?'editor':'sales'">{{ m.belong==='编导' ? '已自动分配编导' : '销售处理' }}</text>
						</view>
					</view>
					<view v-if="shownMessages.length===0" class="msg-empty">暂无匹配的客户消息</view>
					<text class="msg-foot">普通制作作为通自动分流 ⓘ</text>
				</view>

				<view class="card sum-card">
					<view class="sum-title">今日智能摘要 <text class="spark">✦</text></view>
					<text class="sum-text">{{ summary }}</text>
				</view>
			</view>
		</view>

		<!-- 已完成记录（仅覆盖层，不影响待办主列表） -->
		<view v-if="doneVisible" class="done-mask" @click="closeDone">
			<view class="done-panel" @click.stop>
				<view class="done-hd">
					<text class="done-title">已完成记录</text>
					<text class="done-count" v-if="!doneLoading">共 {{ doneTotal }} 条</text>
					<text class="done-close" @click="closeDone">关闭</text>
				</view>
				<scroll-view scroll-y class="done-scroll">
					<view v-if="doneLoading" class="done-empty">加载中…</view>
					<view v-else-if="doneActions.length===0" class="done-empty">暂无已完成事项</view>
					<view
						v-for="a in doneActions"
						:key="'d'+a.id"
						class="done-row"
						@click="openDoneItem(a)"
					>
						<view class="done-tags">
							<text class="a-tag green">已完成</text>
							<text class="a-tag" :class="tagClass(a.type)">{{ doneTypeLabel(a.type) }}</text>
						</view>
						<view class="done-body">
							<text class="done-name">{{ a.customerName || '客户' }}</text>
							<text class="done-desc">{{ doneTitle(a) }}</text>
						</view>
						<view class="done-meta">
							<text class="done-time">创建于 {{ formatDoneTime(a.addtime) }}</text>
							<text class="done-go">查看 ›</text>
						</view>
					</view>
				</scroll-view>
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
			activeFilter: 'all',
			filters: [
				{ key: 'all', label: '全部' },
				{ key: 'deal', label: '成交', type: '待付款' },
				{ key: 'complaint', label: '客诉', type: '客诉' },
				{ key: 'warn', label: '制作预警', type: '制作预警' },
				{ key: 'renew', label: '续拍', type: '库存不足' }
			],
			actions: [],
			messages: [],
			loopSteps: ['事件分流', '创建责任任务', '达到条件通知销售', '处理完成', '写入客户时间线'],
			doneVisible: false,
			doneLoading: false,
			doneActions: [],
			doneTotal: 0
		}
	},
	computed: {
		shownActions() {
			let list = this.actions
			if (this.activeFilter !== 'all') {
				const f = this.filters.find(f => f.key === this.activeFilter)
				if (f && f.type) list = list.filter(a => a.type === f.type)
			}
			const kw = (this.keyword || '').trim().toLowerCase()
			if (!kw) return list
			return list.filter(a => {
				const name = String(a.customerName || '').toLowerCase()
				const title = String(a.title || '').toLowerCase()
				const type = String(a.type || '').toLowerCase()
				return name.indexOf(kw) >= 0 || title.indexOf(kw) >= 0 || type.indexOf(kw) >= 0
			})
		},
		shownMessages() {
			const kw = (this.keyword || '').trim().toLowerCase()
			if (!kw) return this.messages
			return this.messages.filter(m => {
				const name = String(m.customerName || '').toLowerCase()
				const title = String(m.title || '').toLowerCase()
				const content = String(m.content || '').toLowerCase()
				return name.indexOf(kw) >= 0 || title.indexOf(kw) >= 0 || content.indexOf(kw) >= 0
			})
		},
		summary() {
			const n = this.actions.length
			const pay = this.actions.filter(a => a.type === '待付款').length
			const warn = this.actions.filter(a => a.type === '制作预警').length
			const comp = this.actions.filter(a => a.type === '客诉').length
			const renew = this.actions.filter(a => a.type === '库存不足').length
			return `${n}项待处理：${pay}笔待付款、${warn}项制作预警、${comp}起客诉、${renew}位客户适合续拍。`
		},
		/** 0=全灰；有待办时停在「达到条件通知销售」(index 2)；全部处理完可到最后 */
		loopActive() {
			if (!this.actions || this.actions.length === 0) return -1
			const allDone = this.actions.every(a => a.status === '已完成')
			if (allDone) return this.loopSteps.length
			return 2
		}
	},
	onShow() {
		if (!this.$api.auth()) return
		this.load()
	},
	methods: {
		load() {
			this.$api.get('hyActionItem/refreshProgress').finally(() => {
				this.$api.page('hyActionItem', { page: 1, limit: 50, belong: '销售', status: '待处理' }).then(res => {
					this.actions = (res.data && res.data.list) || []
					this.$api.page('hyActionItem', { page: 1, limit: 50, belong: '销售', status: '处理中' }).then(res2 => {
						const proc = (res2.data && res2.data.list) || []
						this.actions = this.actions.concat(proc)
					})
				})
			})
			this.$api.page('hyMessage', { page: 1, limit: 20 }).then(res => {
				this.messages = (res.data && res.data.list) || []
			})
		},
		countOf(f) {
			if (f.key === 'all') return this.actions.length
			return this.actions.filter(a => a.type === f.type).length
		},
		tagLabel(a) {
			if (a.type === '制作预警') return a.canIntervene == 1 ? '允许干预' : '黄色关注'
			return ({ '待付款': '待付款', '客诉': '客户投诉', '库存不足': '内容库存不足' })[a.type] || a.type
		},
		tagText(t) {
			return ({ '待付款': '待付款', '制作预警': '制作预警', '客诉': '客户投诉', '库存不足': '内容库存不足' })[t] || t
		},
		tagClass(t) { return ({ '待付款': 'orange', '制作预警': 'green', '客诉': 'red', '库存不足': 'blue' })[t] || 'blue' },
		coverOfMsg(m) { return 'upload/studio_cover_1.jpg' },
		coverOfAction(a) {
			if (a.cover) return a.cover
			const n = (Number(a.customerId) || 0) % 4 + 1
			return `upload/studio_cover_${n}.jpg`
		},
		resolve(a, msg) {
			this.$api.update('hyActionItem', { id: a.id, status: '已完成' }).then(() => {
				uni.showToast({ title: msg, icon: 'success' })
				this.load()
			})
		},
		confirmPay(a) {
			uni.showModal({
				title: '确认收款',
				content: `确认「${a.customerName}」已付款？将自动生成订单。`,
				success: (r) => {
					if (!r.confirm) return
					const doPay = (planId) => {
						const body = { customerId: a.customerId }
						if (planId) body.planId = planId
						this.$api.post('hyCustomer/confirmPay', body).then(res => {
							const no = (res.data && res.data.orderNo) || ''
							uni.showToast({ title: no ? `订单已生成 ${no}` : '订单已生成', icon: 'success' })
							this.load()
						})
					}
					this.$api.page('hyContentPlan', {
						customerId: a.customerId, page: 1, limit: 1, sort: 'addtime', order: 'desc'
					}).then(res => {
						const plan = (res.data && res.data.list && res.data.list[0]) || null
						doPay(plan && plan.id)
					}).catch(() => doPay(null))
				}
			})
		},
		contact(a) {
			uni.navigateTo({ url: `/pages/customer/customer?id=${a.customerId}` })
		},
		recordFollow(a) { uni.navigateTo({ url: `/pages/customer/follow?customerId=${a.customerId}&customerName=${encodeURIComponent(a.customerName)}` }) },
		viewPlan(a) { uni.navigateTo({ url: `/pages/customer/customer?id=${a.customerId}` }) },
		viewProgress(a) { uni.navigateTo({ url: `/pages/order/order` }) },
		viewPref(a) { uni.navigateTo({ url: `/pages/customer/customer?id=${a.customerId}` }) },
		viewDone() {
			this.doneVisible = true
			this.doneLoading = true
			this.doneActions = []
			this.doneTotal = 0
			this.$api.page('hyActionItem', {
				page: 1,
				limit: 50,
				belong: '销售',
				status: '已完成',
				sort: 'addtime',
				order: 'desc'
			}).then(res => {
				this.doneActions = (res.data && res.data.list) || []
				this.doneTotal = (res.data && res.data.total) || this.doneActions.length
			}).catch(() => {
				this.doneActions = []
				this.doneTotal = 0
			}).finally(() => {
				this.doneLoading = false
			})
		},
		closeDone() {
			this.doneVisible = false
		},
		openDoneItem(a) {
			if (!a || !a.customerId) return
			this.closeDone()
			uni.navigateTo({ url: `/pages/customer/customer?id=${a.customerId}` })
		},
		formatDoneTime(t) {
			if (!t) return ''
			const s = String(t).replace('T', ' ')
			return s.length >= 16 ? s.substr(0, 16) : s
		},
		/** 已完成列表：type 是事项分类，不是当前业务状态 */
		doneTypeLabel(t) {
			return ({
				'待付款': '成交跟进',
				'制作预警': '制作预警',
				'客诉': '客户投诉',
				'库存不足': '续拍跟进'
			})[t] || t || '事项'
		},
		doneTitle(a) {
			if (!a) return ''
			if (a.type === '待付款') return '待付款跟进已处理（收款或关闭）'
			if (a.type === '客诉') return '客诉已受理/关闭'
			if (a.type === '制作预警') return '制作预警已处理'
			if (a.type === '库存不足') return '续拍跟进已发起/关闭'
			const raw = a.title ? String(a.title) : this.tagText(a.type)
			return raw + '（已处理）'
		}
	}
}
</script>

<style lang="scss" scoped>
.searchbar { width:520rpx; height:64rpx; background:#F4F6FA; border-radius:999rpx; display:flex; align-items:center; padding:0 24rpx; }
.s-ico { font-size:26rpx; margin-right:12rpx; }
.s-input { flex:1; font-size:26rpx; }
.done-link { font-size:25rpx; color:$ink-2; display:flex; align-items:center; gap:8rpx; }
.clock-ico { width:22rpx; height:22rpx; border:2rpx solid currentColor; border-radius:50%; position:relative; }
.clock-ico::before { content:""; position:absolute; left:9rpx; top:4rpx; width:2rpx; height:7rpx; background:currentColor; transform-origin:bottom; transform:rotate(-25deg); }

.done-mask {
	position: fixed;
	left: 0; right: 0; top: 0; bottom: 0;
	background: rgba(15, 23, 42, .42);
	z-index: 1000;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 40rpx;
	box-sizing: border-box;
}
.done-panel {
	width: min(920rpx, 92vw);
	max-height: 78vh;
	background: #fff;
	border-radius: 20rpx;
	display: flex;
	flex-direction: column;
	overflow: hidden;
	box-shadow: 0 16rpx 48rpx rgba(15, 23, 42, .18);
}
.done-hd {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 28rpx 32rpx;
	border-bottom: 1rpx solid #EEF1F5;
}
.done-title { font-size: 32rpx; font-weight: 700; color: $ink; }
.done-count { flex: 1; font-size: 24rpx; color: $muted; }
.done-close { font-size: 26rpx; color: $brand; padding: 8rpx 4rpx; }
.done-scroll { flex: 1; min-height: 240rpx; max-height: 62vh; padding: 8rpx 0 20rpx; box-sizing: border-box; }
.done-empty { padding: 80rpx 32rpx; text-align: center; color: $muted; font-size: 26rpx; }
.done-row {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 22rpx 32rpx;
	border-bottom: 1rpx solid #F3F5F8;
}
.done-row:active { background: #F7F9FC; }
.done-tags { display: flex; flex-direction: column; gap: 8rpx; flex-shrink: 0; }
.done-body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6rpx; }
.done-name { font-size: 28rpx; font-weight: 600; color: $ink; }
.done-desc { font-size: 24rpx; color: $ink-2; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.done-meta { display: flex; flex-direction: column; align-items: flex-end; gap: 6rpx; flex-shrink: 0; }
.done-time { font-size: 22rpx; color: $muted; }
.done-go { font-size: 24rpx; color: $brand; }

.ac { flex:1; min-height:0; height:100%; display:flex; gap:24rpx; align-items:stretch; }
.col-main { flex:2; min-width:0; min-height:0; display:flex; flex-direction:column; }
.col-side { flex:1; min-width:0; min-height:0; display:flex; flex-direction:column; justify-content:space-between; gap:20rpx; overflow-y:auto; }
.a-scroll { flex:1; min-height:0; }

.todo-head { font-size:30rpx; font-weight:800; margin-bottom:16rpx; flex-shrink:0; }
.th-num { color:$brand; }
.filters { display:flex; gap:14rpx; margin-bottom:20rpx; flex-wrap:wrap; flex-shrink:0; }
.f { padding:10rpx 26rpx; background:#fff; border:1rpx solid $line; border-radius:999rpx; font-size:24rpx; color:$ink-2; }
.f.on { background:$brand; color:#fff; border:none; }

.a-card { background:#fff; border:1rpx solid $line; border-radius:18rpx; padding:24rpx; margin-bottom:18rpx; display:flex; align-items:flex-start; box-shadow:0 3rpx 12rpx rgba(31,39,51,.025); }
.a-thumb { width:96rpx; height:96rpx; border-radius:14rpx; background:#eee; flex-shrink:0; margin-right:20rpx; }
.a-content { flex:1; min-width:0; }
.a-chev { color:#C7CDD8; font-size:34rpx; margin-left:12rpx; align-self:center; }
.a-top { display:flex; align-items:center; gap:14rpx; margin-bottom:14rpx; }
.a-tag { padding:6rpx 18rpx; border-radius:999rpx; font-size:21rpx; }
.a-tag.orange { background:#FFF1E6; color:#FF8A3D; }
.a-tag.green { background:#E8F7F0; color:#22B07D; }
.a-tag.red { background:#FDECEC; color:#FF5A5F; }
.a-tag.blue { background:#EAF1FF; color:#2F6BFF; }
.a-cust { font-size:25rpx; color:$ink-2; }
.a-mid { display:flex; justify-content:space-between; align-items:flex-start; gap:24rpx; }
.a-left { flex:1; min-width:0; }
.a-title { font-size:28rpx; font-weight:700; display:block; }
.a-meta { font-size:23rpx; color:$muted; display:block; margin-top:10rpx; }
.a-actions { display:flex; flex-direction:row; flex-wrap:wrap; justify-content:flex-end; gap:12rpx; flex-shrink:0; max-width:440rpx; }
.ab { height:64rpx; padding:0 26rpx; font-size:24rpx; }
.intervene { background:#E8F7F0; color:#22B07D; }
.renew { background:#EAF1FF; color:#2F6BFF; }
.a-watch { font-size:22rpx; color:#B8791F; max-width:220rpx; line-height:1.4; }

.thresh { margin-top:18rpx; }
.th-bar { position:relative; height:10rpx; background:#EEF1F5; border-radius:999rpx; margin:16rpx 0; }
.th-in { height:100%; background:#FF8A3D; border-radius:999rpx; }
.th-mk { position:absolute; top:50%; width:18rpx; height:18rpx; border-radius:50%; background:#D7DCE3; transform:translate(-50%,-50%); }
.th-mk.active { background:#FF8A3D; border:3rpx solid #fff; }
.th-labels { display:flex; justify-content:space-between; font-size:20rpx; color:$muted; }
.th-note { font-size:21rpx; color:$muted; margin-top:10rpx; display:block; }

.loop { display:flex; align-items:center; padding:26rpx; margin-top:16rpx; flex-shrink:0; }
.lp { display:flex; align-items:center; flex:1; }
.lp-dot { width:40rpx; height:40rpx; border-radius:50%; background:#E5E8EC; color:#fff; display:flex; align-items:center; justify-content:center; font-size:22rpx; flex-shrink:0; }
.lp-dot.on { background:#22B07D; }
.lp-dot.cur { background:$brand; }
.lp-l { font-size:21rpx; color:$ink-2; margin:0 12rpx; white-space:nowrap; }
.lp-line { flex:1; height:2rpx; background:#E5E8EC; }
.lp-line.on { background:#22B07D; }

.msg-card { padding:24rpx; flex-shrink:0; }
.msg-card.grow { max-height:70%; overflow-y:auto; }
.sum-card { flex-shrink:0; }
.msg-head { font-size:27rpx; font-weight:700; display:flex; align-items:center; gap:8rpx; }
.chat-ico { width:25rpx; height:20rpx; border:2rpx solid $brand; border-radius:6rpx; position:relative; flex-shrink:0; }
.chat-ico::after { content:""; position:absolute; left:4rpx; bottom:-6rpx; width:7rpx; height:7rpx; border-left:2rpx solid $brand; transform:skewY(-35deg); }
.m-num { color:$brand; }
.msg { display:flex; margin-top:18rpx; }
.msg-av { width:70rpx; height:70rpx; border-radius:14rpx; background:#eee; flex-shrink:0; }
.msg-body { margin-left:14rpx; flex:1; }
.msg-name { font-size:24rpx; font-weight:700; }
.msg-text { font-size:24rpx; color:$ink-2; }
.msg-flag { display:inline-block; margin-top:8rpx; padding:4rpx 14rpx; border-radius:999rpx; font-size:20rpx; }
.msg-flag.sales { background:#FDECEC; color:#FF5A5F; }
.msg-flag.editor { background:#EAF1FF; color:#2F6BFF; }
.msg-foot { font-size:21rpx; color:$muted; display:block; margin-top:18rpx; }
.msg-empty { font-size:24rpx; color:$muted; padding:24rpx 0 8rpx; }

.sum-card { padding:24rpx; }
.sum-title { font-size:27rpx; font-weight:700; }
.spark { color:#FF8A3D; }
.sum-text { font-size:25rpx; color:$ink-2; line-height:1.6; display:block; margin-top:14rpx; }
.empty { color:$muted; text-align:center; padding:40rpx 0; }

/* 1-6 标注稿：主行动区约 1014，右侧消息栏约 254 */
@include pad-landscape {
	.searchbar {
		width: 22vw;
		height: 5.2vh;
		padding: 0 1.2vw;
	}
	.s-input, .done-link {
		font-size: clamp(12px, .95vw, 15px);
	}
	.ac {
		gap: .75vw;
	}
	.col-main {
		flex: 1014;
	}
	.col-side {
		flex: 254;
		gap: 1.2vh;
	}
	.todo-head {
		font-size: clamp(17px, 1.4vw, 22px);
		margin-bottom: .6vh;
	}
	.filters {
		gap: .55vw;
		margin-bottom: 1vh;
		flex-wrap: nowrap;
	}
	.f {
		padding: .55vh 1.15vw;
		font-size: clamp(11px, .85vw, 14px);
	}
	.a-card {
		min-height: 13.8vh;
		box-sizing: border-box;
		padding: 1.15vh .9vw;
		margin-bottom: .75vh;
		border-radius: 12px;
		align-items: center;
	}
	.a-thumb {
		width: 4.8vw;
		height: 4.8vw;
		margin-right: .9vw;
		border-radius: 50%;
	}
	.a-top {
		gap: .65vw;
		margin-bottom: .55vh;
	}
	.a-tag {
		padding: .3vh .75vw;
		font-size: clamp(10px, .76vw, 12px);
	}
	.a-cust, .a-meta {
		font-size: clamp(11px, .85vw, 14px);
	}
	.a-title {
		font-size: clamp(16px, 1.28vw, 20px);
	}
	.a-mid {
		gap: 1vw;
	}
	.a-actions {
		max-width: 25vw;
		gap: .55vw;
	}
	.ab {
		height: 4.8vh;
		padding: 0 1vw;
		font-size: clamp(11px, .85vw, 14px);
	}
	.thresh {
		margin-top: .9vh;
	}
	.th-bar {
		height: 5px;
		margin: .7vh 0;
	}
	.th-labels, .th-note {
		font-size: clamp(9px, .68vw, 11px);
	}
	.loop {
		height: 10.8vh;
		box-sizing: border-box;
		padding: 1.4vh 1.2vw;
		margin-top: 1vh;
		border-radius: 13px;
	}
	.lp-dot {
		width: 30px;
		height: 30px;
		font-size: 14px;
	}
	.lp-l {
		font-size: clamp(10px, .76vw, 12px);
		margin: 0 .55vw;
	}
	.msg-card {
		padding: 1.5vh 1vw;
		border-radius: 13px;
	}
	.msg-card.grow {
		height: 73%;
		max-height: none;
		box-sizing: border-box;
	}
	.msg-head, .sum-title {
		font-size: clamp(14px, 1.1vw, 18px);
	}
	.msg {
		margin-top: 1.2vh;
	}
	.msg-av {
		width: 3.7vw;
		height: 3.7vw;
		border-radius: 50%;
	}
	.msg-body {
		margin-left: .65vw;
	}
	.msg-name, .msg-text {
		font-size: clamp(11px, .85vw, 14px);
	}
	.msg-flag, .msg-foot {
		font-size: clamp(9px, .7vw, 11px);
	}
	.sum-card {
		height: 14%;
		box-sizing: border-box;
		padding: 1.2vh 1vw;
		border-radius: 13px;
	}
	.sum-text {
		font-size: clamp(10px, .78vw, 13px);
		margin-top: .55vh;
	}
}

@include pad-portrait {
	.searchbar {
		/* 无独立 08 marks；对齐通用搜索高与 05 量级 */
		width: $p-search-05;
		max-width: 100%;
		height: p-px(44);
		min-height: p-px(44);
		padding: 0 p-px(14);
		box-sizing: border-box;
	}
	.s-input {
		font-size: p-px(14);
	}
	.done-link {
		font-size: p-px(13);
	}
	.ac {
		flex-direction: column;
		overflow-y: auto;
		gap: p-px(16);
	}
	.col-main, .col-side {
		flex: none;
		width: 100%;
	}
	.col-side {
		margin-top: p-px(8);
	}
	.todo-head {
		font-size: p-px(18);
		margin-bottom: p-px(10);
	}
	.th-num {
		font-size: p-px(18);
	}
	.filters {
		flex-wrap: nowrap;
		overflow-x: auto;
		gap: p-px(10);
		margin-bottom: p-px(12);
	}
	.f {
		padding: p-px(8) p-px(14);
		font-size: p-px(13);
		border-radius: 999px;
		white-space: nowrap;
	}
	.a-card {
		padding: p-px(14) p-px(16);
		border-radius: $p-radius;
		margin-bottom: p-px(12);
		box-sizing: border-box;
	}
	.a-thumb {
		width: p-px(72);
		height: p-px(72);
		border-radius: p-px(12);
	}
	.a-title {
		font-size: p-px(15);
	}
	.a-meta {
		font-size: p-px(12);
	}
	.msg-card.grow, .sum-card {
		height: auto;
		max-height: none;
		padding: p-px(16) p-px(18);
		border-radius: $p-radius;
		box-sizing: border-box;
	}
	.a-actions {
		max-width: none;
		flex-wrap: wrap;
		gap: p-px(10);
	}
	.a-actions .btn,
	.ab {
		height: $p-ctrl-h;
		min-height: $p-ctrl-h;
		padding: 0 p-px(16);
		font-size: p-px(14);
		border-radius: 999px;
		box-sizing: border-box;
	}
}
</style>
