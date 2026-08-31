<template>
	<sales-shell active="material" title="行业素材库" subtitle="按行业快速找到适合客户的案例">
		<view slot="search" class="searchbar">
			<text class="s-ico"></text>
			<input v-model="keyword" class="s-input" placeholder="搜索行业、业态或关键词" @confirm="search" />
		</view>
		<view slot="actions" class="total">全部 {{ total }} 条 ›</view>

		<view class="library">
			<view class="industry-row">
				<view v-for="(g,i) in topGroups" :key="g.id" class="industry-card"
					:class="['g'+i,{on:currentGroup&&currentGroup.id===g.id}]" @click="chooseGroup(g)">
					<image class="industry-bg" :src="$img(groupCover(g, i))" mode="aspectFill"></image>
					<view class="industry-mask"></view>
					<view class="industry-copy">
						<view><text class="industry-name">{{ g.name }}</text><text class="industry-count">{{ groupCount(g) }}条</text></view>
						<text class="industry-subs">{{ childNames(g) }}</text>
					</view>
					<view v-if="currentGroup&&currentGroup.id===g.id" class="industry-check">✓</view>
				</view>
			</view>

			<view class="category-head">
				<text class="category-title">{{ currentGroup ? currentGroup.name : '行业' }}分类</text>
				<text class="category-path">全部行业　/　{{ currentGroup ? currentGroup.name : '' }}</text>
			</view>

			<view class="category-area">
				<view class="category-grid">
					<view v-for="(c,i) in currentChildren" :key="c.id" class="category-card"
						:class="['c'+i,{on:currentCategory&&currentCategory.id===c.id}]" @click="chooseCategory(c)">
						<image class="category-bg" :src="$img(categoryCover(c, i))" mode="aspectFill"></image>
						<view class="category-mask"></view>
						<view class="category-copy">
							<text class="category-name">{{ c.name }}</text>
							<text class="category-count">{{ subCount(c) }}</text>
						</view>
						<text v-if="i===0" class="hot">热门</text>
						<view v-if="i===0" class="play"></view>
					</view>
				</view>

				<view class="featured card">
					<view class="featured-head">
						<text class="featured-title">更多</text>
						<text class="featured-close" @click.stop>×</text>
					</view>
					<view class="featured-list">
						<view v-for="m in moreFeatured" :key="m.id" class="featured-item">
							<image class="featured-img" :src="$media(m, 'cover')" mode="aspectFill"></image>
							<view class="featured-play"></view>
							<text class="featured-duration">{{ dur(m.duration) }}</text>
						</view>
						<view v-if="moreFeatured.length===0" class="featured-empty">暂无预览</view>
					</view>
					<view class="btn btn-danger featured-btn" :class="{disabled:!hasMoreChildren}" @click="openMore">
						查看更多分类　›
					</view>
				</view>
			</view>

			<!-- 更多小类：当前页放不下的分类 -->
			<view v-if="showMorePopup" class="more-mask" @click="closeMore">
				<view class="more-panel card" @click.stop>
					<view class="more-head">
						<view>
							<text class="more-title">{{ currentGroup ? currentGroup.name : '' }}分类</text>
							<text class="more-path">全部行业　/　{{ currentGroup ? currentGroup.name : '' }}　/　更多</text>
						</view>
						<text class="more-close" @click="closeMore">×</text>
					</view>
					<scroll-view scroll-y class="more-scroll">
						<view v-for="(chunk, bi) in moreChildChunks" :key="bi" class="more-block">
							<view class="more-category-grid">
								<view v-for="(c, i) in chunk" :key="c.id" class="category-card"
									:class="'c' + i" @click="pickMoreCategory(c)">
									<image class="category-bg" :src="$img(categoryCover(c, i + 7 + bi * 7))" mode="aspectFill"></image>
									<view class="category-mask"></view>
									<view class="category-copy">
										<text class="category-name">{{ c.name }}</text>
										<text class="category-count">{{ subCount(c) }}</text>
									</view>
									<text v-if="i===0" class="hot">热门</text>
									<view v-if="i===0" class="play"></view>
								</view>
							</view>
						</view>
					</scroll-view>
				</view>
			</view>

			<view class="recent card">
				<text class="recent-title">最近使用</text>
				<view v-for="m in recent" :key="m.id" class="recent-item">
					<image class="recent-img" :src="$media(m, 'cover')" mode="aspectFill"></image>
					<text class="recent-name">{{ m.industrySub || m.title }}</text>
					<text class="recent-clock">◷</text>
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
			total: 0,
			groups: [],
			materials: [],
			currentGroup: null,
			currentCategory: null,
			showMorePopup: false
		}
	},
	computed: {
		topGroups() {
			return this.groups.slice(0, 3)
		},
		currentChildren() {
			return this.currentGroup ? (this.currentGroup.children || []).slice(0, 7) : []
		},
		moreChildren() {
			return this.currentGroup ? (this.currentGroup.children || []).slice(7) : []
		},
		hasMoreChildren() {
			return this.moreChildren.length > 0
		},
		moreChildChunks() {
			const list = this.moreChildren
			const chunks = []
			for (let i = 0; i < list.length; i += 7) {
				chunks.push(list.slice(i, i + 7))
			}
			return chunks
		},
		moreFeatured() {
			const pool = this.moreChildren.length
				? this.moreChildren
				: (this.currentGroup ? (this.currentGroup.children || []).slice(6) : [])
			const names = pool.map(c => c.name)
			if (!names.length) {
				const all = this.currentGroup ? (this.currentGroup.children || []).map(c => c.name) : []
				return this.materials.filter(m => all.includes(m.industrySub)).slice(0, 3)
			}
			return this.materials.filter(m => names.includes(m.industrySub)).slice(0, 3)
		},
		recent() {
			return this.materials.slice(0, 3)
		}
	},
	onLoad(opt) {
		this.keyword = opt.keyword ? decodeURIComponent(opt.keyword) : ''
		this.initialBiztype = opt.biztype ? decodeURIComponent(opt.biztype) : ''
	},
	onShow() {
		if (!this.$api.auth()) return
		Promise.all([
			this.$api.list('hyIndustry', {}),
			this.$api.page('hyMaterial', { page: 1, limit: 100, status: '上架' })
		]).then(([treeRes, materialRes]) => {
			const all = treeRes.data || []
			this.materials = (materialRes.data && materialRes.data.list) || []
			this.total = (materialRes.data && materialRes.data.total) || this.materials.length
			this.groups = all.filter(i => i.level === 1).map(g => ({
				...g,
				children: all.filter(c => c.parentId === g.id)
			}))
			let group = this.groups.find(g => (g.children || []).some(c => c.name === this.initialBiztype))
			if (!group) group = this.groups[0]
			this.chooseGroup(group)
			if (this.initialBiztype) {
				const biztype = this.initialBiztype
				this.initialBiztype = ''
				const child = (group.children || []).find(c => c.name === biztype)
				if (child) {
					this.currentCategory = child
					this.enterCategory(child)
				}
			}
		})
	},
	methods: {
		chooseGroup(g) {
			if (!g) return
			this.currentGroup = g
			this.currentCategory = (g.children || [])[0] || null
			this.showMorePopup = false
		},
		chooseCategory(c) {
			if (!c) return
			this.enterCategory(c)
		},
		enterCategory(c) {
			if (!c) return
			const cid = uni.getStorageSync('hyActiveCustomerId')
			let url = `/pages/selection/selection?biztype=${encodeURIComponent(c.name)}`
			if (cid) url += `&customerId=${cid}`
			uni.navigateTo({ url })
		},
		groupMaterials(g) {
			return this.materials.filter(m => m.industryBig === g.name)
		},
		groupCover(g, i) {
			const m = this.groupMaterials(g)[0]
			return (m && m.cover) || `upload/studio_cover_${(i % 4) + 1}.jpg`
		},
		categoryCover(c, i) {
			const m = this.materials.find(x => x.industrySub === c.name)
			return (m && m.cover) || `upload/studio_cover_${(i % 4) + 1}.jpg`
		},
		groupCount(g) {
			return this.groupMaterials(g).length
		},
		subCount(c) {
			return this.materials.filter(m => m.industrySub === c.name).length
		},
		materialCount(name) {
			return this.materials.filter(m => m.industrySub === name).length
		},
		childNames(g) {
			return (g.children || []).slice(0, 4).map(c => c.name).join(' · ')
		},
		dur(s) {
			if (!s) return ''
			return `0:${String(s % 60).padStart(2, '0')}`
		},
		search() {
			const k = this.keyword.trim()
			if (!k) return
			const child = this.groups.reduce((out, g) => out.concat(g.children || []), []).find(c => c.name.indexOf(k) >= 0)
			if (child) {
				const group = this.groups.find(g => (g.children || []).some(c => c.id === child.id))
				this.currentGroup = group
				this.currentCategory = child
				this.enterCategory(child)
			}
		},
		openMore() {
			if (!this.hasMoreChildren) {
				uni.showToast({ title: '当前分类已全部展示', icon: 'none' })
				return
			}
			this.showMorePopup = true
		},
		closeMore() {
			this.showMorePopup = false
		},
		pickMoreCategory(c) {
			this.closeMore()
			this.enterCategory(c)
		}
	}
}
</script>

<style lang="scss" scoped>
.searchbar { width:560rpx; height:64rpx; background:#F4F6FA; border-radius:999rpx; display:flex; align-items:center; padding:0 24rpx; }
.s-ico { font-size:28rpx; margin-right:12rpx; }
.s-input { flex:1; font-size:25rpx; }
.total { font-size:24rpx; color:$ink-2; }
.library { flex:1; min-height:0; display:flex; flex-direction:column; gap:18rpx; }
.industry-row { height:300rpx; display:flex; gap:18rpx; flex-shrink:0; }
.industry-card { position:relative; overflow:hidden; border-radius:18rpx; border:1rpx solid $line; box-shadow:0 4rpx 14rpx rgba(31,39,51,.035); }
.industry-card.on { border-color:rgba(47,107,255,.5); box-shadow:0 0 0 3rpx rgba(47,107,255,.08),0 6rpx 18rpx rgba(31,39,51,.055); }
.industry-card.g0 { flex:668; }
.industry-card.g1 { flex:311; }
.industry-card.g2 { flex:238; }
.industry-bg,.category-bg { position:absolute; inset:0; width:100%; height:100%; }
.industry-mask { position:absolute; inset:0; background:linear-gradient(90deg,rgba(255,255,255,.94),rgba(255,255,255,.15)); }
.industry-copy { position:absolute; left:30rpx; top:28rpx; z-index:2; display:flex; flex-direction:column; }
.industry-name { font-size:42rpx; font-weight:700; color:$ink; letter-spacing:-1rpx; }
.industry-count { font-size:27rpx; color:$brand; margin-left:14rpx; }
.industry-subs { margin-top:12rpx; font-size:24rpx; color:$ink-2; }
.industry-check { position:absolute; right:18rpx; top:16rpx; z-index:3; width:38rpx; height:38rpx; border-radius:50%; background:#fff; color:$brand; display:flex; align-items:center; justify-content:center; }
.category-head { display:flex; align-items:center; height:48rpx; flex-shrink:0; }
.category-title { font-size:30rpx; font-weight:700; }
.category-path { margin-left:28rpx; font-size:22rpx; color:$muted; }
.category-area { flex:1; min-height:0; display:flex; gap:18rpx; }
/* 14 列可同时整除上排 2 张(各 4 列)与下排 4 张(各 2 列)，大卡占 6 列两行 */
.category-grid { flex:825; min-width:0; display:grid; grid-template-columns:repeat(14,1fr); grid-template-rows:1fr 1fr; gap:12rpx; }
.category-card { position:relative; overflow:hidden; border-radius:14rpx; border:1rpx solid $line; }
.category-card.on { border-color:rgba(47,107,255,.45); box-shadow:inset 0 0 0 2rpx rgba(47,107,255,.08); }
.category-card.c0 { grid-column:span 6; grid-row:span 2; }
.category-card.c1,.category-card.c2 { grid-column:span 4; }
.category-card.c3,.category-card.c4,.category-card.c5,.category-card.c6 { grid-column:span 2; }
.category-mask { position:absolute; inset:0; background:linear-gradient(180deg,rgba(255,255,255,.86),rgba(255,255,255,.05) 58%); }
.category-copy { position:absolute; left:18rpx; top:16rpx; z-index:2; display:flex; flex-direction:column; }
.category-name { font-size:28rpx; font-weight:700; color:$ink; }
.category-count { font-size:22rpx; color:$ink-2; }
.category-card.c0 .category-name { font-size:32rpx; }
.category-card.c3,.category-card.c4,.category-card.c5,.category-card.c6 {
	.category-copy { left:12rpx; top:10rpx; }
	.category-name { font-size:24rpx; }
	.category-count { font-size:20rpx; }
}
.hot { position:absolute; right:12rpx; top:12rpx; z-index:3; padding:4rpx 12rpx; border-radius:999rpx; background:#FF7A59; color:#fff; font-size:18rpx; }
.play,.featured-play { position:absolute; left:50%; top:50%; transform:translate(-50%,-50%); width:54rpx; height:54rpx; border:2rpx solid rgba(255,255,255,.9); border-radius:50%; background:rgba(20,28,40,.18); box-shadow:0 3rpx 12rpx rgba(0,0,0,.16); z-index:3; }
.play::after,.featured-play::after { content:""; position:absolute; left:21rpx; top:15rpx; border-left:16rpx solid #fff; border-top:11rpx solid transparent; border-bottom:11rpx solid transparent; }
.featured { flex:467; min-width:0; padding:22rpx; display:flex; flex-direction:column; }
.featured-head { display:flex; align-items:center; }
.featured-title { font-size:28rpx; font-weight:700; flex:1; }
.featured-close { margin-left:auto; font-size:32rpx; color:$muted; }
.featured-list { flex:1; min-height:0; display:flex; gap:12rpx; margin:16rpx 0; }
.featured-item { flex:1; position:relative; overflow:hidden; border-radius:12rpx; background:#EEF1F5; }
.featured-item::after { content:""; position:absolute; inset:0; background:linear-gradient(180deg,transparent 55%,rgba(16,24,36,.34)); pointer-events:none; }
.featured-img { width:100%; height:100%; }
.featured-duration { position:absolute; right:8rpx; bottom:6rpx; color:#fff; font-size:18rpx; }
.featured-btn { height:70rpx; font-size:24rpx; flex-shrink:0; }
.featured-btn.disabled { opacity:.45; }
.featured-empty { flex:1; display:flex; align-items:center; justify-content:center; color:$muted; font-size:22rpx; background:#EEF1F5; border-radius:12rpx; }
.recent { height:100rpx; flex-shrink:0; padding:14rpx 24rpx; display:flex; align-items:center; gap:18rpx; }
.recent-title { width:120rpx; font-size:28rpx; font-weight:700; }
.recent-item { flex:1; height:100%; position:relative; overflow:hidden; border-radius:12rpx; background:#EEF1F5; }
.recent-item::after { content:""; position:absolute; inset:0; background:linear-gradient(90deg,rgba(16,24,36,.18),rgba(16,24,36,.04)); pointer-events:none; }
.recent-img { width:100%; height:100%; }
.recent-name { position:absolute; left:50%; top:50%; transform:translate(-50%,-50%); color:#fff; font-size:24rpx; font-weight:600; white-space:nowrap; text-shadow:0 2rpx 8rpx rgba(0,0,0,.5); z-index:2; }
.recent-clock { position:absolute; right:10rpx; top:8rpx; color:#fff; z-index:2; }

.more-mask {
	position:fixed; inset:0; z-index:999;
	background:rgba(31,39,51,.42);
	display:flex; align-items:center; justify-content:center;
	padding:40rpx;
}
.more-panel {
	width:920rpx; max-width:94%; max-height:80vh;
	padding:28rpx 28rpx 24rpx;
	display:flex; flex-direction:column;
}
.more-head { display:flex; align-items:flex-start; margin-bottom:20rpx; }
.more-title { font-size:30rpx; font-weight:700; display:block; }
.more-path { font-size:22rpx; color:$muted; margin-top:8rpx; display:block; }
.more-close { font-size:36rpx; color:$muted; line-height:1; padding:0 8rpx; }
.more-scroll { flex:1; min-height:0; max-height:66vh; }
.more-block + .more-block { margin-top:18rpx; }
.more-category-grid {
	display:grid;
	grid-template-columns:repeat(14,1fr);
	grid-template-rows:1fr 1fr;
	gap:12rpx;
	min-height:320rpx;
}

@media #{$pad-mq-landscape} {
	.searchbar { width:25vw; height:5.2vh; padding:0 1.2vw; }
	.s-input,.total { font-size:clamp(12px,.95vw,15px); }
	.library { gap:1.1vh; }
	.industry-row { height:30.5%; gap:.75vw; }
	.industry-card { border-radius:13px; }
	.industry-copy { left:1.5vw; top:1.7vh; }
	.industry-name { font-size:clamp(25px,2.1vw,34px); }
	.industry-count { font-size:clamp(16px,1.25vw,20px); }
	.industry-subs { margin-top:.7vh; font-size:clamp(12px,.95vw,15px); }
	.category-head { height:4.8vh; }
	.category-title { font-size:clamp(16px,1.3vw,21px); }
	.category-path { margin-left:1.4vw; font-size:clamp(11px,.85vw,14px); }
	.category-area { gap:.75vw; }
	.category-grid { gap:.55vw; }
	.category-card { border-radius:10px; }
	.category-copy { left:.9vw; top:1vh; }
	.category-name { font-size:clamp(15px,1.2vw,19px); }
	.category-count { font-size:clamp(11px,.85vw,14px); }
	.category-card.c0 .category-name { font-size:clamp(17px,1.35vw,22px); }
	.category-card.c3,.category-card.c4,.category-card.c5,.category-card.c6 {
		.category-copy { left:.6vw; top:.8vh; }
		.category-name { font-size:clamp(12px,.95vw,15px); }
		.category-count { font-size:clamp(10px,.75vw,12px); }
	}
	.featured { padding:1.2vh 1vw; border-radius:13px; }
	.featured-title { font-size:clamp(15px,1.2vw,19px); }
	.featured-list { gap:.55vw; margin:1vh 0; }
	.featured-item { border-radius:8px; }
	.featured-btn { height:5.2vh; font-size:clamp(12px,.95vw,15px); }
	.recent { height:9.4vh; padding:1vh 1.2vw; gap:.8vw; border-radius:13px; }
	.recent-title { width:7vw; font-size:clamp(16px,1.3vw,21px); }
	.recent-name { font-size:clamp(12px,.95vw,15px); }
	.more-panel { width:52vw; padding:1.6vh 1.4vw; border-radius:13px; }
	.more-title { font-size:clamp(16px,1.3vw,21px); }
	.more-path { font-size:clamp(11px,.85vw,14px); margin-top:.4vh; }
	.more-category-grid { gap:.55vw; min-height:28vh; }
	.more-block + .more-block { margin-top:1vh; }
}

@media #{$pad-mq-portrait} {
	.industry-row {
		height: auto;
		flex-wrap: nowrap;
		overflow-x: auto;
	}
	.industry-card {
		flex: none !important;
		width: 72vw;
		height: 18vh;
	}
	.category-area {
		flex-direction: column;
	}
	.category-grid {
		flex: none;
		width: 100%;
		grid-template-columns: repeat(2, 1fr);
		grid-template-rows: auto;
		gap: 2vw;
	}
	.category-card.c0,
	.category-card.c1,
	.category-card.c2,
	.category-card.c3,
	.category-card.c4,
	.category-card.c5,
	.category-card.c6 {
		grid-column: span 1;
		grid-row: span 1;
		min-height: 14vh;
	}
	.featured {
		flex: none;
		width: 100%;
		margin-top: 2vh;
	}
	.recent {
		height: auto;
		flex-wrap: wrap;
	}
	.recent-item {
		min-width: 28vw;
		height: 10vh;
	}
	.more-panel {
		width: 92vw;
	}
	.more-category-grid {
		grid-template-columns: repeat(2, 1fr);
		grid-template-rows: auto;
		min-height: auto;
	}
	.more-category-grid .category-card.c0,
	.more-category-grid .category-card.c1,
	.more-category-grid .category-card.c2,
	.more-category-grid .category-card.c3,
	.more-category-grid .category-card.c4,
	.more-category-grid .category-card.c5,
	.more-category-grid .category-card.c6 {
		grid-column: span 1;
		grid-row: span 1;
		min-height: 14vh;
	}
}
</style>
