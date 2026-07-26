<template>
	<view class="sel">
		<!-- 顶栏 -->
		<view class="topbar">
			<text class="brand" @click="goBack">‹ {{ brandName }}</text>
			<view class="center">
				<text class="cust">{{ customerName }}</text>
			</view>
			<view class="right">
				<text class="counter">已选 <text class="hot">{{ liked.length }}</text> / 目标 {{ targetCount }}</text>
				<image class="avatar" :src="avatar" mode="aspectFill"></image>
			</view>
		</view>

		<!-- 筛选 chips -->
		<view class="chips">
			<view v-for="c in chips" :key="c.key" class="chip" :class="{on: c.key===activeChip}" @click="pickChip(c)">
				{{ c.label }}
			</view>
		</view>

		<view class="main">
			<!-- 视频区 -->
			<view class="stage">
				<view v-if="current" class="player">
					<video v-if="current.video" class="video" :src="$img(current.video)" :poster="$img(current.cover)"
						controls></video>
					<image v-else class="video poster" :src="$img(current.cover)" mode="aspectFill"></image>
					<view class="overlay">
						<text class="ov-title">{{ current.title }}</text>
						<view class="ov-tag">{{ current.contentType }}<text v-if="current.tags"> · {{ firstTag(current.tags) }}</text></view>
					</view>
				</view>
				<view v-else class="player empty-stage">
					<text>本轮素材已看完，可结束选片生成方案</text>
				</view>

				<!-- 操作 -->
				<view class="actions">
					<view class="act dislike" @click="dislike">
						<text class="act-ic">✕</text>
						<text class="act-tx">不喜欢</text>
					</view>
					<text class="hint">左右滑动也可以选择</text>
					<view class="act like" @click="like">
						<text class="act-ic">♥</text>
						<text class="act-tx">喜欢</text>
					</view>
					<view class="act skip" @click="skip">
						<text class="act-ic">⏭</text>
						<text class="act-tx">暂时跳过</text>
					</view>
				</view>
			</view>

			<!-- 右栏 -->
			<view class="side">
				<view class="card sc1">
					<text class="sc-label">本次选择</text>
					<view class="sc-big">已选 <text class="num">{{ liked.length }}</text> 条</view>
					<view class="recipe">
						<view v-for="(g,i) in groups" :key="g.key" class="rp">
							<text class="rp-num">{{ counts[g.key] }}</text>
							<view class="rp-bar"><view class="rp-bar-in" :style="{height: barH(g.key), background: g.color}"></view></view>
							<text class="rp-label">{{ g.label }}</text>
						</view>
					</view>
				</view>

				<view class="card sc2">
					<view class="pentagon" :style="pentagonStyle"></view>
					<view class="advice">
						<text class="ad-title">{{ adviceTitle }}</text>
						<text class="ad-sub">{{ adviceSub }}</text>
					</view>
				</view>

				<view class="card sc3">
					<text class="sc-label">刚刚喜欢</text>
					<scroll-view scroll-x class="liked-row">
						<view v-for="m in likedItems.slice().reverse()" :key="m.id" class="lk">
							<image class="lk-img" :src="$img(m.cover)" mode="aspectFill"></image>
							<view class="lk-heart">♥</view>
							<text class="lk-dur">{{ durText(m.duration) }}</text>
						</view>
						<view v-if="likedItems.length===0" class="lk-empty">还没有喜欢的素材</view>
					</scroll-view>
				</view>

				<view class="btn btn-danger finish" @click="finish">✦ 结束选片并生成方案</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			brandName: '合意传媒',
			avatar: 'https://i.pravatar.cc/100?img=47',
			sessionId: null,
			customerId: null,
			customerName: '客户',
			industry: '',
			biztype: '',
			targetCount: 15,
			activeChip: 'all',
			chips: [],
			materials: [],
			idx: 0,
			liked: [],
			likedItems: [],
			disliked: [],
			counts: { process: 0, knowledge: 0, story: 0, opinion: 0, ad: 0 },
			groups: [
				{ key: 'process', label: '厨过程', color: '#7C5CFF' },
				{ key: 'knowledge', label: '教知识', color: '#2F6BFF' },
				{ key: 'story', label: '讲故事', color: '#B9C0CC' },
				{ key: 'opinion', label: '说观点', color: '#22B07D' },
				{ key: 'ad', label: '硬广', color: '#FF5A5F' }
			]
		}
	},
	computed: {
		current() {
			return this.materials[this.idx] || null
		},
		maxCount() {
			return Math.max(1, this.counts.process, this.counts.knowledge, this.counts.story, this.counts.opinion, this.counts.ad)
		},
		adviceTitle() {
			const total = this.liked.length
			if (total === 0) return '开始选片'
			const entries = this.groups.map(g => ({ g, v: this.counts[g.key] }))
			entries.sort((a, b) => b.v - a.v)
			const max = entries[0]
			const min = entries[entries.length - 1]
			if (max.v === 0) return '内容均衡'
			return `${max.g.label}偏多`
		},
		adviceSub() {
			const total = this.liked.length
			if (total === 0) return '为客户挑选合适的对标内容'
			const entries = this.groups.map(g => ({ g, v: this.counts[g.key] }))
			entries.sort((a, b) => b.v - a.v)
			const min = entries[entries.length - 1]
			return `稍后建议补充${min.g.label}类`
		},
		pentagonStyle() {
			// 五边形配方填充强度（按已选总量映射颜色透明度）
			const t = Math.min(1, this.liked.length / Math.max(1, this.targetCount))
			return {
				background: `conic-gradient(from -90deg, #7C5CFF, #2F6BFF, #B9C0CC, #22B07D, #FF5A5F, #7C5CFF)`,
				opacity: (0.35 + t * 0.55).toFixed(2)
			}
		}
	},
	onLoad(opt) {
		const brand = uni.getStorageSync('brand')
		if (brand && brand.brandName) this.brandName = brand.brandName
		this.sessionId = opt.sessionId || null
		this.customerId = opt.customerId || null
		if (this.sessionId) {
			this.loadSession()
		} else if (this.customerId) {
			this.loadCustomer()
		} else {
			this.loadMaterials()
		}
	},
	methods: {
		loadSession() {
			this.$api.info('hySelectionSession', this.sessionId).then(res => {
				const s = res.data
				if (s) {
					this.customerId = s.customerId
					this.customerName = s.customerName
					this.industry = s.industry
					this.biztype = s.biztype
					this.targetCount = s.targetCount || 15
					this.counts = {
						process: s.cProcess || 0, knowledge: s.cKnowledge || 0,
						story: s.cStory || 0, opinion: s.cOpinion || 0, ad: s.cAd || 0
					}
					if (s.liked) this.liked = String(s.liked).split(',').filter(Boolean)
				}
				this.loadMaterials()
			})
		},
		loadCustomer() {
			this.$api.info('hyCustomer', this.customerId).then(res => {
				const c = res.data
				if (c) {
					this.customerName = c.name
					this.industry = c.industry
					this.biztype = c.biztype
				}
				this.loadMaterials()
			})
		},
		loadMaterials() {
			const q = { page: 1, limit: 50, status: '上架' }
			if (this.biztype) q.industrySub = this.biztype
			else if (this.industry) q.industryBig = this.industry
			this.$api.page('hyMaterial', q).then(res => {
				let list = (res.data && res.data.list) || []
				// 过滤已选/已弃
				list = list.filter(m => this.liked.indexOf(String(m.id)) < 0)
				this.materials = list
				this.idx = 0
				this.buildChips()
			})
		},
		buildChips() {
			const chips = [{ key: 'all', label: this.biztype || this.industry || '全部' }]
			chips.push({ key: 'hot', label: '热门案例' })
			this.groups.forEach(g => chips.push({ key: g.key, label: g.label }))
			chips.push({ key: 'filter', label: '⚲ 切换筛选' })
			this.chips = chips
			this.activeChip = 'all'
		},
		pickChip(c) {
			this.activeChip = c.key
			if (c.key === 'all' || c.key === 'hot' || c.key === 'filter') {
				this.loadMaterials()
				return
			}
			const label = this.groups.find(g => g.key === c.key).label
			const q = { page: 1, limit: 50, status: '上架', contentType: label }
			if (this.biztype) q.industrySub = this.biztype
			this.$api.page('hyMaterial', q).then(res => {
				let list = (res.data && res.data.list) || []
				list = list.filter(m => this.liked.indexOf(String(m.id)) < 0)
				this.materials = list
				this.idx = 0
			})
		},
		typeKey(ct) {
			const map = { '厨过程': 'process', '教知识': 'knowledge', '讲故事': 'story', '说观点': 'opinion', '硬广': 'ad' }
			return map[ct] || 'process'
		},
		like() {
			const m = this.current
			if (!m) return
			this.$api.get(`hyMaterial/like/${m.id}`).catch(() => {})
			this.liked.push(String(m.id))
			this.likedItems.push(m)
			this.counts[this.typeKey(m.contentType)]++
			this.next()
		},
		dislike() {
			const m = this.current
			if (!m) return
			this.$api.get(`hyMaterial/dislike/${m.id}`).catch(() => {})
			this.disliked.push(String(m.id))
			this.next()
		},
		skip() {
			this.next()
		},
		next() {
			if (this.idx < this.materials.length) this.idx++
		},
		firstTag(tags) {
			return String(tags).split(',')[0]
		},
		durText(s) {
			if (!s) return ''
			const m = Math.floor(s / 60)
			const ss = ('0' + (s % 60)).slice(-2)
			return `0${m}:${ss}`
		},
		barH(key) {
			const v = this.counts[key]
			return Math.max(8, Math.round((v / this.maxCount) * 60)) + 'rpx'
		},
		buildPayload(status) {
			return {
				id: this.sessionId || undefined,
				customerId: this.customerId,
				customerName: this.customerName,
				managerId: uni.getStorageSync('empId'),
				managerName: uni.getStorageSync('empName'),
				industry: this.industry,
				biztype: this.biztype,
				targetCount: this.targetCount,
				selectedCount: this.liked.length,
				liked: this.liked.join(','),
				disliked: this.disliked.join(','),
				cProcess: this.counts.process,
				cKnowledge: this.counts.knowledge,
				cStory: this.counts.story,
				cOpinion: this.counts.opinion,
				cAd: this.counts.ad,
				status: status
			}
		},
		finish() {
			if (this.liked.length === 0) {
				uni.showToast({ title: '请至少选择一条素材', icon: 'none' })
				return
			}
			const payload = this.buildPayload('已结束')
			const saveSession = this.sessionId
				? this.$api.update('hySelectionSession', payload)
				: this.$api.save('hySelectionSession', payload)
			saveSession.then(res => {
				const sid = this.sessionId || (res && res.id)
				// 生成内容方案
				const plan = {
					customerId: this.customerId,
					customerName: this.customerName,
					sessionId: sid,
					originalSelection: this.liked.join(','),
					rProcess: this.counts.process,
					rKnowledge: this.counts.knowledge,
					rStory: this.counts.story,
					rOpinion: this.counts.opinion,
					rAd: this.counts.ad,
					totalCount: this.liked.length,
					finalMaterials: this.liked.join(','),
					confirmed: 0
				}
				this.$api.save('hyContentPlan', plan).then(() => {
					uni.showToast({ title: '方案已生成', icon: 'success' })
					setTimeout(() => {
						uni.redirectTo({ url: `/pages/customer/customer?id=${this.customerId}` })
					}, 600)
				})
			})
		},
		goBack() {
			uni.navigateBack({ delta: 1, fail: () => uni.reLaunch({ url: '/pages/workbench/workbench' }) })
		}
	}
}
</script>

<style lang="scss" scoped>
.sel {
	width: 100%;
	height: 100vh;
	display: flex;
	flex-direction: column;
	background: $page-bg;
}
.topbar {
	height: 96rpx;
	background: #fff;
	border-bottom: 1rpx solid $line;
	display: flex;
	align-items: center;
	padding: 0 32rpx;
}
.brand {
	font-size: 30rpx;
	font-weight: 700;
	color: $ink;
}
.center {
	flex: 1;
	text-align: center;
}
.cust {
	font-size: 30rpx;
	font-weight: 700;
}
.right {
	display: flex;
	align-items: center;
	gap: 20rpx;
}
.counter {
	font-size: 26rpx;
	color: $ink-2;
}
.counter .hot {
	color: $brand;
	font-weight: 800;
}
.avatar {
	width: 56rpx;
	height: 56rpx;
	border-radius: 50%;
	background: #eee;
}

.chips {
	display: flex;
	gap: 16rpx;
	padding: 20rpx 32rpx 4rpx;
	flex-wrap: wrap;
}
.chip {
	padding: 10rpx 26rpx;
	background: #fff;
	border: 1rpx solid $line;
	border-radius: 999rpx;
	font-size: 24rpx;
	color: $ink-2;
}
.chip.on {
	background: $brand;
	color: #fff;
	border-color: $brand;
}

.main {
	flex: 1;
	display: flex;
	gap: 24rpx;
	padding: 20rpx 32rpx 28rpx;
	overflow: hidden;
}
.stage {
	flex: 2;
	display: flex;
	flex-direction: column;
	min-width: 0;
}
.player {
	flex: 1;
	position: relative;
	background: #000;
	border-radius: 20rpx;
	overflow: hidden;
	min-height: 420rpx;
}
.video {
	width: 100%;
	height: 100%;
}
.poster {
	display: block;
}
.overlay {
	position: absolute;
	left: 32rpx;
	bottom: 110rpx;
	color: #fff;
}
.ov-title {
	font-size: 40rpx;
	font-weight: 800;
	text-shadow: 0 2rpx 12rpx rgba(0,0,0,.5);
}
.ov-tag {
	display: inline-block;
	margin-top: 14rpx;
	background: rgba(255,255,255,.22);
	padding: 6rpx 18rpx;
	border-radius: 999rpx;
	font-size: 22rpx;
}
.empty-stage {
	display: flex;
	align-items: center;
	justify-content: center;
	color: #ccc;
	font-size: 28rpx;
}

.actions {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 40rpx;
	padding: 28rpx 0 4rpx;
}
.act {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	border-radius: 50%;
	background: #fff;
	border: 1rpx solid $line;
}
.act .act-ic {
	font-size: 40rpx;
}
.act .act-tx {
	font-size: 20rpx;
	margin-top: 4rpx;
	color: $muted;
}
.act.dislike {
	width: 120rpx;
	height: 120rpx;
	color: $ink-2;
}
.act.like {
	width: 150rpx;
	height: 150rpx;
	background: $brand;
	color: #fff;
	border: none;
	box-shadow: 0 14rpx 30rpx rgba(47,107,255,.35);
}
.act.like .act-tx {
	color: #fff;
}
.act.skip {
	width: auto;
	height: 80rpx;
	border-radius: 999rpx;
	flex-direction: row;
	padding: 0 28rpx;
	gap: 10rpx;
}
.act.skip .act-ic {
	font-size: 26rpx;
}
.act.skip .act-tx {
	margin-top: 0;
	font-size: 24rpx;
}
.hint {
	font-size: 22rpx;
	color: $muted;
}

/* 右栏 */
.side {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 20rpx;
	overflow-y: auto;
	min-width: 0;
}
.side .card {
	padding: 24rpx;
}
.sc-label {
	font-size: 24rpx;
	color: $muted;
}
.sc-big {
	font-size: 30rpx;
	font-weight: 700;
	margin: 8rpx 0 22rpx;
}
.sc-big .num {
	font-size: 48rpx;
	color: $brand;
}
.recipe {
	display: flex;
	justify-content: space-between;
	align-items: flex-end;
}
.rp {
	display: flex;
	flex-direction: column;
	align-items: center;
	flex: 1;
}
.rp-num {
	font-size: 26rpx;
	font-weight: 700;
	margin-bottom: 8rpx;
}
.rp-bar {
	width: 36rpx;
	height: 70rpx;
	background: #F1F3F6;
	border-radius: 8rpx;
	display: flex;
	align-items: flex-end;
	overflow: hidden;
}
.rp-bar-in {
	width: 100%;
	border-radius: 8rpx;
}
.rp-label {
	font-size: 20rpx;
	color: $muted;
	margin-top: 8rpx;
}

.sc2 {
	display: flex;
	align-items: center;
	gap: 22rpx;
}
.pentagon {
	width: 110rpx;
	height: 110rpx;
	clip-path: polygon(50% 0%, 100% 38%, 82% 100%, 18% 100%, 0% 38%);
	flex-shrink: 0;
}
.advice {
	display: flex;
	flex-direction: column;
}
.ad-title {
	font-size: 28rpx;
	font-weight: 700;
}
.ad-sub {
	font-size: 24rpx;
	color: $muted;
	margin-top: 8rpx;
}

.liked-row {
	white-space: nowrap;
	margin-top: 14rpx;
}
.lk {
	display: inline-block;
	position: relative;
	width: 130rpx;
	height: 160rpx;
	margin-right: 14rpx;
	border-radius: 12rpx;
	overflow: hidden;
	background: #eee;
}
.lk-img {
	width: 100%;
	height: 100%;
}
.lk-heart {
	position: absolute;
	top: 8rpx;
	right: 8rpx;
	color: #fff;
	font-size: 24rpx;
	text-shadow: 0 0 6rpx rgba(0,0,0,.4);
}
.lk-dur {
	position: absolute;
	bottom: 6rpx;
	left: 8rpx;
	color: #fff;
	font-size: 20rpx;
	text-shadow: 0 0 6rpx rgba(0,0,0,.6);
}
.lk-empty {
	display: inline-block;
	color: $muted;
	font-size: 24rpx;
	padding: 30rpx 0;
}

.finish {
	height: 100rpx;
	font-size: 30rpx;
	border-radius: 18rpx;
}
</style>
