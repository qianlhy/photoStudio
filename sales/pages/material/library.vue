<template>
	<sales-shell active="material" title="行业素材库" subtitle="按行业快速找到适合客户的案例">
		<view slot="search" class="searchbar">
			<text class="s-ico">⌕</text>
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
							<text class="category-count">{{ c.materialCount || materialCount(c.name) }}</text>
						</view>
						<text v-if="i===0" class="hot">热门</text>
						<view v-if="i===0" class="play">▷</view>
					</view>
				</view>

				<view class="featured card">
					<view class="featured-head">
						<text class="featured-title">{{ currentCategory ? currentCategory.name : '精选案例' }}</text>
						<text class="featured-sub">· 精选案例</text>
						<text class="featured-close">×</text>
					</view>
					<view class="featured-list">
						<view v-for="m in featured" :key="m.id" class="featured-item">
							<image class="featured-img" :src="$img(m.cover)" mode="aspectFill"></image>
							<view class="featured-play">▷</view>
							<text class="featured-duration">{{ dur(m.duration) }}</text>
						</view>
					</view>
					<view class="btn btn-danger featured-btn" @click="viewExamples">
						查看 {{ currentCategory ? (currentCategory.materialCount || featured.length) : featured.length }} 条案例　›
					</view>
				</view>
			</view>

			<view class="recent card">
				<text class="recent-title">最近使用</text>
				<view v-for="m in recent" :key="m.id" class="recent-item">
					<image class="recent-img" :src="$img(m.cover)" mode="aspectFill"></image>
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
			currentCategory: null
		}
	},
	computed: {
		topGroups() {
			return this.groups.slice(0, 3)
		},
		currentChildren() {
			return this.currentGroup ? (this.currentGroup.children || []).slice(0, 7) : []
		},
		featured() {
			const name = this.currentCategory && this.currentCategory.name
			let list = name ? this.materials.filter(m => m.industrySub === name) : this.materials
			return list.slice(0, 3)
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
				const child = this.currentChildren.find(c => c.name === this.initialBiztype)
				if (child) this.chooseCategory(child)
			}
		})
	},
	methods: {
		chooseGroup(g) {
			if (!g) return
			this.currentGroup = g
			this.currentCategory = (g.children || [])[0] || null
		},
		chooseCategory(c) {
			this.currentCategory = c
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
			const sum = (g.children || []).reduce((n, c) => n + (Number(c.materialCount) || 0), 0)
			return sum || this.groupMaterials(g).length
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
				this.chooseGroup(group)
				this.chooseCategory(child)
			}
		},
		viewExamples() {
			if (!this.currentCategory) return
			uni.navigateTo({ url: `/pages/selection/selection?biztype=${encodeURIComponent(this.currentCategory.name)}` })
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
.industry-card { position:relative; overflow:hidden; border-radius:18rpx; border:1rpx solid $line; }
.industry-card.g0 { flex:668; }
.industry-card.g1 { flex:311; }
.industry-card.g2 { flex:238; }
.industry-bg,.category-bg { position:absolute; inset:0; width:100%; height:100%; }
.industry-mask { position:absolute; inset:0; background:linear-gradient(90deg,rgba(255,255,255,.94),rgba(255,255,255,.15)); }
.industry-copy { position:absolute; left:30rpx; top:28rpx; z-index:2; display:flex; flex-direction:column; }
.industry-name { font-size:42rpx; font-weight:800; color:$ink; }
.industry-count { font-size:27rpx; color:$brand; margin-left:14rpx; }
.industry-subs { margin-top:12rpx; font-size:24rpx; color:$ink-2; }
.industry-check { position:absolute; right:18rpx; top:16rpx; z-index:3; width:38rpx; height:38rpx; border-radius:50%; background:#fff; color:$brand; display:flex; align-items:center; justify-content:center; }
.category-head { display:flex; align-items:center; height:48rpx; flex-shrink:0; }
.category-title { font-size:30rpx; font-weight:800; }
.category-path { margin-left:28rpx; font-size:22rpx; color:$muted; }
.category-area { flex:1; min-height:0; display:flex; gap:18rpx; }
/* 14 列可同时整除上排 2 张(各 4 列)与下排 4 张(各 2 列)，大卡占 6 列两行 */
.category-grid { flex:825; min-width:0; display:grid; grid-template-columns:repeat(14,1fr); grid-template-rows:1fr 1fr; gap:12rpx; }
.category-card { position:relative; overflow:hidden; border-radius:14rpx; border:1rpx solid $line; }
.category-card.c0 { grid-column:span 6; grid-row:span 2; }
.category-card.c1,.category-card.c2 { grid-column:span 4; }
.category-card.c3,.category-card.c4,.category-card.c5,.category-card.c6 { grid-column:span 2; }
.category-mask { position:absolute; inset:0; background:linear-gradient(180deg,rgba(255,255,255,.86),rgba(255,255,255,.05) 58%); }
.category-copy { position:absolute; left:18rpx; top:16rpx; z-index:2; display:flex; flex-direction:column; }
.category-name { font-size:28rpx; font-weight:800; color:$ink; }
.category-count { font-size:22rpx; color:$ink-2; }
.category-card.c0 .category-name { font-size:32rpx; }
.category-card.c3,.category-card.c4,.category-card.c5,.category-card.c6 {
	.category-copy { left:12rpx; top:10rpx; }
	.category-name { font-size:24rpx; }
	.category-count { font-size:20rpx; }
}
.hot { position:absolute; right:12rpx; top:12rpx; z-index:3; padding:4rpx 12rpx; border-radius:999rpx; background:#FF7A59; color:#fff; font-size:18rpx; }
.play,.featured-play { position:absolute; left:50%; top:50%; transform:translate(-50%,-50%); width:54rpx; height:54rpx; border:2rpx solid #fff; border-radius:50%; color:#fff; display:flex; align-items:center; justify-content:center; z-index:3; }
.featured { flex:467; min-width:0; padding:22rpx; display:flex; flex-direction:column; }
.featured-head { display:flex; align-items:center; }
.featured-title { font-size:28rpx; font-weight:800; white-space:nowrap; }
.featured-sub { font-size:22rpx; color:$ink-2; margin-left:8rpx; white-space:nowrap; }
.featured-close { margin-left:auto; font-size:32rpx; color:$muted; }
.featured-list { flex:1; min-height:0; display:flex; gap:12rpx; margin:16rpx 0; }
.featured-item { flex:1; position:relative; overflow:hidden; border-radius:12rpx; }
.featured-img { width:100%; height:100%; }
.featured-duration { position:absolute; right:8rpx; bottom:6rpx; color:#fff; font-size:18rpx; }
.featured-btn { height:70rpx; font-size:24rpx; flex-shrink:0; }
.recent { height:100rpx; flex-shrink:0; padding:14rpx 24rpx; display:flex; align-items:center; gap:18rpx; }
.recent-title { width:120rpx; font-size:28rpx; font-weight:800; }
.recent-item { flex:1; height:100%; position:relative; overflow:hidden; border-radius:12rpx; }
.recent-img { width:100%; height:100%; }
.recent-name { position:absolute; left:50%; top:50%; transform:translate(-50%,-50%); color:#fff; font-size:24rpx; font-weight:700; white-space:nowrap; text-shadow:0 2rpx 8rpx rgba(0,0,0,.65); }
.recent-clock { position:absolute; right:10rpx; top:8rpx; color:#fff; }

@media (min-width:900px) and (orientation:landscape) {
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
	.featured-sub { font-size:clamp(11px,.85vw,14px); }
	.featured-list { gap:.55vw; margin:1vh 0; }
	.featured-item { border-radius:8px; }
	.featured-btn { height:5.2vh; font-size:clamp(12px,.95vw,15px); }
	.recent { height:9.4vh; padding:1vh 1.2vw; gap:.8vw; border-radius:13px; }
	.recent-title { width:7vw; font-size:clamp(16px,1.3vw,21px); }
	.recent-name { font-size:clamp(12px,.95vw,15px); }
}
</style>
