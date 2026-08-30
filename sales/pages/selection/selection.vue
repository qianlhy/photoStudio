<template>
	<view class="sel">
		<!-- 顶栏 -->
		<view class="topbar">
			<view class="brand" hover-class="brand-hover" @tap.stop="goBack">
				<text class="brand-arrow">‹</text>
				<text class="brand-text">{{ brandName }}</text>
			</view>
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
					<video v-if="current.video" :key="'v-' + current.id + '-' + idx" class="video"
						:src="videoSrc(current)" :poster="$media(current, 'cover')"
						controls autoplay object-fit="contain" show-center-play-btn
						@error="onVideoError"></video>
					<image v-else class="video poster" :src="$media(current, 'cover')" mode="aspectFill"></image>
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
					<view class="pentagon-wrap">
						<svg class="pentagon-svg" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
							<polygon :points="pentagonOutlinePts" fill="#F1F3F6" />
							<polygon
								v-for="(w, i) in pentagonWedges"
								:key="pentagonKey + '-' + i"
								:points="w.points"
								:fill="w.color"
								fill-opacity="0.82"
							/>
						</svg>
					</view>
					<view class="advice">
						<text class="ad-title">{{ adviceTitle }}</text>
						<text class="ad-sub">{{ adviceSub }}</text>
					</view>
				</view>

				<view class="card sc3">
					<text class="sc-label">刚刚喜欢</text>
					<scroll-view scroll-x class="liked-row">
						<view v-for="m in likedItems.slice().reverse()" :key="m.id" class="lk">
							<image class="lk-img" :src="$media(m, 'cover')" mode="aspectFill"></image>
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
			_dirty: false,
			_saving: false,
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
		pentagonKey() {
			const c = this.counts
			return [c.process, c.knowledge, c.story, c.opinion, c.ad, this.liked.length].join('-')
		},
		pentagonOutlinePts() {
			const cx = 50
			const cy = 50
			const r = 44
			return [0, 1, 2, 3, 4].map(i => {
				const rad = (-90 + i * 72) * Math.PI / 180
				return `${(cx + r * Math.cos(rad)).toFixed(1)},${(cy + r * Math.sin(rad)).toFixed(1)}`
			}).join(' ')
		},
		pentagonWedges() {
			const keys = ['process', 'knowledge', 'story', 'opinion', 'ad']
			const max = Math.max(1, ...keys.map(k => this.counts[k] || 0))
			const cx = 50
			const cy = 50
			const minR = 10
			const maxR = 44
			const radius = (key) => minR + ((this.counts[key] || 0) / max) * (maxR - minR)
			return keys.map((key, i) => {
				const a1 = (-90 + i * 72) * Math.PI / 180
				const a2 = (-90 + (i + 1) * 72) * Math.PI / 180
				const r1 = radius(key)
				const r2 = radius(keys[(i + 1) % 5])
				const x1 = (cx + r1 * Math.cos(a1)).toFixed(1)
				const y1 = (cy + r1 * Math.sin(a1)).toFixed(1)
				const x2 = (cx + r2 * Math.cos(a2)).toFixed(1)
				const y2 = (cy + r2 * Math.sin(a2)).toFixed(1)
				return {
					points: `${cx},${cy} ${x1},${y1} ${x2},${y2}`,
					color: this.groups[i].color
				}
			})
		}
	},
	onLoad(opt) {
		const brand = uni.getStorageSync('brand')
		if (brand && brand.brandName) this.brandName = brand.brandName
		this.sessionId = opt.sessionId || null
		this.customerId = opt.customerId || uni.getStorageSync('hyActiveCustomerId') || null
		if (opt.customerName) this.customerName = decodeURIComponent(opt.customerName)
		if (this.customerId) {
			uni.setStorageSync('hyActiveCustomerId', this.customerId)
			if (this.customerName) uni.setStorageSync('hyActiveCustomerName', this.customerName)
		}
		if (this.sessionId) {
			this.loadSession()
		} else if (this.customerId) {
			this.loadCustomer()
		} else {
			this.loadMaterials()
		}
	},
	onUnload() {
		if (this._dirty) this.saveProgress()
	},
	onBackPress() {
		this.goBack()
		return true
	},
	methods: {
		videoSrc(m) {
			return m ? this.$media(m, 'video') : ''
		},
		onVideoError() {
			const m = this.current
			if (m) {
				this.$materialCache.invalidate(m.id, 'video')
			}
			uni.showToast({ title: '视频加载失败，正在尝试在线播放', icon: 'none' })
		},
		prefetchAround() {
			if (!this.$materialCache.isAppPlus()) return
			const start = Math.max(0, this.idx)
			const slice = this.materials.slice(start, start + 3)
			this.$materialCache.prefetchList(slice, this.$base.url, 3)
		},
		loadSession() {
			return this.$api.info('hySelectionSession', this.sessionId).then(res => {
				if (res.data) this.applySession(res.data)
				return this.restoreLikedItems()
			}).then(() => this.loadMaterials())
		},
		loadCustomer() {
			return this.$api.info('hyCustomer', this.customerId).then(res => {
				const c = res.data
				if (c) {
					this.customerName = c.name
					this.industry = c.industry
					this.biztype = c.biztype
				}
				return this.tryResumeSession()
			})
		},
		tryResumeSession() {
			if (this.sessionId) return this.loadSession()
			return this.$api.page('hySelectionSession', {
				page: 1,
				limit: 1,
				customerId: this.customerId,
				status: '进行中',
				sort: 'addtime',
				order: 'desc'
			}).then(res => {
				const s = (res.data && res.data.list && res.data.list[0]) || null
				if (s) {
					this.sessionId = s.id
					this.applySession(s)
					return this.restoreLikedItems()
				}
			}).then(() => this.loadMaterials())
		},
		applySession(s) {
			this.customerId = s.customerId
			this.customerName = s.customerName
			this.industry = s.industry
			this.biztype = s.biztype
			this.targetCount = s.targetCount || 15
			this.liked = s.liked ? String(s.liked).split(',').filter(Boolean) : []
			this.disliked = s.disliked ? String(s.disliked).split(',').filter(Boolean) : []
		},
		rebuildCounts() {
			const counts = { process: 0, knowledge: 0, story: 0, opinion: 0, ad: 0 }
			this.likedItems.forEach(m => {
				if (!m) return
				const key = this.typeKey(m.contentType)
				counts[key] = (counts[key] || 0) + 1
			})
			this.counts = counts
		},
		restoreLikedItems() {
			if (!this.liked.length) {
				this.likedItems = []
				this.rebuildCounts()
				return Promise.resolve()
			}
			return this.$api.page('hyMaterial', { page: 1, limit: 200, status: '上架' }).then(res => {
				const all = (res.data && res.data.list) || []
				const map = {}
				all.forEach(m => { map[String(m.id)] = m })
				this.likedItems = this.liked.map(id => map[String(id)]).filter(Boolean)
				this.rebuildCounts()
			})
		},
		loadMaterials() {
			const q = { page: 1, limit: 50, status: '上架' }
			if (this.biztype) q.industrySub = this.biztype
			else if (this.industry) q.industryBig = this.industry
			this.$api.page('hyMaterial', q).then(res => {
				let list = (res.data && res.data.list) || []
				// 过滤已选/已弃
				list = list.filter(m => {
					const id = String(m.id)
					return this.liked.indexOf(id) < 0 && this.disliked.indexOf(id) < 0
				})
				this.materials = list
				this.idx = 0
				this.buildChips()
				this.prefetchAround()
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
				list = list.filter(m => {
					const id = String(m.id)
					return this.liked.indexOf(id) < 0 && this.disliked.indexOf(id) < 0
				})
				this.materials = list
				this.idx = 0
				this.prefetchAround()
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
			this.rebuildCounts()
			this._dirty = true
			this.saveProgress()
			this.next()
		},
		dislike() {
			const m = this.current
			if (!m) return
			this.$api.get(`hyMaterial/dislike/${m.id}`).catch(() => {})
			this.disliked.push(String(m.id))
			this._dirty = true
			this.saveProgress()
			this.next()
		},
		skip() {
			this.next()
		},
		next() {
			if (this.idx < this.materials.length) this.idx++
			this.prefetchAround()
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
			const v = this.counts[key] || 0
			if (v === 0) return '0'
			return Math.max(12, Math.round((v / this.maxCount) * 60)) + 'rpx'
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
		saveProgress() {
			if (!this.customerId || this._saving) return Promise.resolve()
			this.rebuildCounts()
			this._saving = true
			const payload = this.buildPayload('进行中')
			const req = this.sessionId
				? this.$api.update('hySelectionSession', payload)
				: this.$api.save('hySelectionSession', payload)
			return req.then(res => {
				if (!this.sessionId && res && res.id) this.sessionId = res.id
				this._dirty = false
			}).catch(() => {}).then(() => {
				this._saving = false
			})
		},
		finish() {
			if (this.liked.length === 0) {
				uni.showToast({ title: '请至少选择一条素材', icon: 'none' })
				return
			}
			if (!this.customerId) {
				uni.showToast({ title: '请先从客户页进入选片，再生成方案', icon: 'none' })
				return
			}
			const runFinish = () => {
				this.rebuildCounts()
				const payload = this.buildPayload('已结束')
				const saveSession = this.sessionId
					? this.$api.update('hySelectionSession', payload)
					: this.$api.save('hySelectionSession', payload)
				saveSession.then(res => {
					const sid = this.sessionId || (res && res.id)
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
						uni.showToast({ title: '方案已生成，客户进入待付款', icon: 'success' })
						setTimeout(() => {
							uni.redirectTo({ url: `/pages/customer/customer?id=${this.customerId}` })
						}, 600)
					})
				})
			}
			if (this.liked.length > this.likedItems.length) {
				this.restoreLikedItems().then(runFinish)
			} else {
				runFinish()
			}
		},
		goBack() {
			if (this._dirty) this.saveProgress()
			const url = this.customerId
				? `/pages/customer/customer?id=${this.customerId}`
				: '/pages/workbench/workbench'
			// H5 下页面栈与浏览器历史易错位，统一用 reLaunch 保证每次都能返回
			uni.reLaunch({ url })
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
	box-shadow: 0 1rpx 0 rgba(31,39,51,.025);
}
.brand {
	display: flex;
	align-items: center;
	flex-shrink: 0;
	padding: 8rpx 24rpx 8rpx 0;
	position: relative;
	z-index: 10;
	cursor: pointer;
	user-select: none;
}
.brand-arrow {
	font-size: 36rpx;
	font-weight: 700;
	color: $ink;
	line-height: 1;
	margin-right: 6rpx;
}
.brand-text {
	font-size: 30rpx;
	font-weight: 700;
	color: $ink;
}
.brand-hover {
	opacity: 0.72;
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
	font-weight: 700;
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
	box-shadow: 0 8rpx 26rpx rgba(18,25,35,.12);
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
	font-weight: 700;
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
	box-shadow: 0 5rpx 16rpx rgba(31,39,51,.055);
	transition: transform .16s ease, box-shadow .16s ease, opacity .16s ease;
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
	box-shadow: 0 12rpx 28rpx rgba(47,107,255,.28);
}
.act:active { transform:scale(.97); opacity:.9; }
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
	padding: 24rpx 24rpx 28rpx;
	overflow: visible;
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
	padding-bottom: 4rpx;
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
	line-height: 1.35;
	text-align: center;
	white-space: nowrap;
}

.sc2 {
	display: flex;
	align-items: center;
	gap: 22rpx;
}
.pentagon-wrap {
	position: relative;
	width: 110rpx;
	height: 110rpx;
	flex-shrink: 0;
}
.pentagon-svg {
	width: 100%;
	height: 100%;
	display: block;
}
.advice {
	display: flex;
	flex-direction: column;
	flex: 1;
	min-width: 0;
}
.ad-title {
	font-size: 28rpx;
	font-weight: 700;
	line-height: 1.35;
}
.ad-sub {
	font-size: 24rpx;
	color: $muted;
	margin-top: 8rpx;
	line-height: 1.45;
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
	margin-top: auto;
	flex-shrink: 0;
}

/* 1-2 标注稿：主舞台 1027、右栏 374、栏间距 16 */
@media #{$pad-mq-landscape} {
	.topbar {
		height: 8vh;
		padding: 0 4.2vw;
		box-sizing: border-box;
	}
	.brand {
		padding: .6vh .9vw .6vh 0;
	}
	.brand-arrow {
		font-size: clamp(22px, 1.9vw, 30px);
		margin-right: .35vw;
	}
	.cust {
		font-size: clamp(19px, 1.65vw, 26px);
	}
	.counter {
		font-size: clamp(13px, 1.05vw, 17px);
	}
	.avatar {
		width: 3.2vw;
		height: 3.2vw;
	}
	.chips {
		height: 7.2vh;
		box-sizing: border-box;
		padding: 1.2vh 4.2vw .8vh;
		gap: .7vw;
		flex-wrap: nowrap;
	}
	.chip {
		min-width: 5.5vw;
		box-sizing: border-box;
		text-align: center;
		padding: .8vh 1.35vw;
		font-size: clamp(12px, .95vw, 15px);
	}
	.main {
		gap: 1vw;
		padding: 0 4.2vw 2.1vh;
	}
	.stage {
		flex: 2.746;
	}
	.side {
		flex: 1;
		gap: 1.1vh;
	}
	.player {
		min-height: 0;
		border-radius: 15px;
	}
	.overlay {
		left: 1.8vw;
		bottom: 10vh;
	}
	.ov-title {
		font-size: clamp(24px, 2.25vw, 36px);
	}
	.ov-tag {
		margin-top: 1vh;
		padding: .55vh .9vw;
		font-size: clamp(12px, .9vw, 15px);
	}
	.actions {
		height: 17.6vh;
		box-sizing: border-box;
		padding: 0;
		gap: 1.2vw;
	}
	.act.dislike {
		width: 8.8vh;
		height: 8.8vh;
	}
	.act.like {
		width: 10.8vh;
		height: 10.8vh;
	}
	.act.skip {
		height: 5.5vh;
		padding: 0 1.3vw;
	}
	.act .act-ic {
		font-size: clamp(21px, 1.9vw, 30px);
	}
	.act .act-tx, .hint {
		font-size: clamp(11px, .85vw, 14px);
	}
	.act.skip .act-ic,
	.act.skip .act-tx {
		font-size: clamp(11px, .9vw, 15px);
	}
	.side .card {
		padding: 1.6vh 1.15vw 1.5vh;
		box-sizing: border-box;
		border-radius: 13px;
		overflow: visible;
		flex-shrink: 0;
	}
	.sc1, .sc2, .sc3 {
		height: auto;
		min-height: 0;
	}
	.sc2 {
		gap: 1vw;
		align-items: center;
	}
	.sc-label {
		font-size: clamp(12px, .95vw, 15px);
	}
	.sc-big {
		font-size: clamp(16px, 1.3vw, 21px);
		margin: .5vh 0 1.2vh;
	}
	.sc-big .num {
		font-size: clamp(27px, 2.5vw, 40px);
	}
	.rp-num {
		font-size: clamp(13px, 1.05vw, 17px);
		margin-bottom: .4vh;
	}
	.rp-bar {
		width: 1.3vw;
		height: 4.6vh;
	}
	.rp-label {
		font-size: clamp(10px, .76vw, 12px);
		margin-top: .5vh;
		line-height: 1.4;
	}
	.pentagon-wrap {
		width: 7.1vw;
		height: 7.1vw;
	}
	.ad-title {
		font-size: clamp(16px, 1.3vw, 21px);
		line-height: 1.35;
	}
	.ad-sub {
		font-size: clamp(12px, .92vw, 15px);
		margin-top: .6vh;
		line-height: 1.45;
	}
	.liked-row {
		margin-top: .8vh;
	}
	.lk {
		width: 5.4vw;
		height: 8.2vh;
		margin-right: .55vw;
		border-radius: 8px;
	}
	.finish {
		height: 7.4vh;
		font-size: clamp(16px, 1.3vw, 21px);
		border-radius: 13px;
	}
}

@media #{$pad-mq-portrait} {
	.main {
		flex-direction: column;
		overflow-y: auto;
		padding-bottom: 2vh;
	}
	.stage, .side {
		flex: none;
		width: 100%;
		min-width: 0;
	}
	.player {
		min-height: 48vh;
	}
	.chips {
		flex-wrap: nowrap;
		overflow-x: auto;
	}
	.side {
		overflow-y: visible;
	}
	.side .card {
		min-height: auto;
	}
	.sc1, .sc2, .sc3 {
		height: auto !important;
	}
	.actions {
		flex-wrap: wrap;
		height: auto;
		padding: 2vh 0;
		gap: 3vw;
	}
	.hint {
		width: 100%;
		text-align: center;
		order: 10;
	}
}
</style>
