<template>
	<sales-shell active="material" title="素材星球" subtitle="点选行业，发现适合客户的高表现案例">
		<view slot="search" class="searchbar">
			<text class="s-ico"></text>
			<input v-model="keyword" class="s-input" placeholder="搜索行业、业态或关键词" @confirm="doSearch" />
		</view>
		<view slot="actions" class="total">全部 {{ total }} 条</view>

		<view class="planet">
			<!-- 气泡星云画布 -->
			<view class="canvas">
				<view v-for="h in halos" :key="'h'+h.name" class="halo"
					:style="{ left: h.x+'%', top: h.y+'%', width: h.size+'px', height: h.size+'px', marginLeft: '-'+(h.size/2)+'px', marginTop: '-'+(h.size/2)+'px', background: 'radial-gradient(circle, '+h.color+'1F 0%, '+h.color+'0A 55%, transparent 72%)' }"></view>
				<text v-for="lb in labels" :key="lb.name" class="cluster-label"
					:style="{left: lb.x+'%', top: lb.y+'%', color: lb.color}">{{ lb.name }}</text>
				<view v-for="b in bubbles" :key="b.id" class="bubble"
					:class="{on: selected&&selected.id===b.id}"
					:style="{ left: b.x+'%', top: b.y+'%', width: b.size+'px', height: b.size+'px', borderColor: b.color, marginLeft: '-'+(b.size/2)+'px', marginTop: '-'+(b.size/2)+'px' }"
					@click="pick(b)">
					<image class="b-img" :src="$img(b.cover)" mode="aspectFill"></image>
					<view class="b-mask"></view>
					<view class="b-txt">
						<text class="b-name" :style="{fontSize: nameSize(b.size)}">{{ b.name }}</text>
						<text class="b-cnt" :style="{fontSize: cntSize(b.size)}">{{ b.materialCount }}</text>
					</view>
				</view>
				<view class="hint">拖动画布浏览 · 双指缩放 · 点击气泡进入</view>
			</view>

			<!-- 右详情 -->
			<view class="detail card" v-if="selected">
				<view class="dt-head">
					<text class="dt-name">{{ selected.name }}</text>
					<text class="dt-close" @click="selected=null">×</text>
				</view>
				<text class="dt-cnt">{{ selected.materialCount }} 条案例</text>
				<text class="dt-hot">近30天最常被选择</text>
				<view class="dt-vids">
					<view v-for="m in detailMaterials" :key="m.id" class="dv">
						<image class="dv-img" :src="$img(m.cover)" mode="aspectFill"></image>
						<view class="dv-play"></view>
					</view>
					<view v-if="detailMaterials.length===0" class="dv-empty">暂无案例</view>
				</view>
				<view class="dt-tags">
					<text v-for="(t,i) in detailTags" :key="i" class="dt-tag">{{ t }}</text>
				</view>
				<view class="btn btn-danger dt-enter" @click="enter">进入{{ selected.name }}素材库 ›</view>
				<view class="btn btn-ghost dt-add" @click="addToReception">⊞ 加入本次接待</view>
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
			bubbles: [],
			labels: [],
			halos: [],
			groupColors: { '餐饮': '#FF7A59', '建筑': '#2F6BFF', '企业': '#22B07D' },
			selected: null,
			detailMaterials: [],
			detailTags: []
		}
	},
	onShow() {
		if (!this.$api.auth()) return
		this.loadTree()
		this.$api.page('hyMaterial', { page: 1, limit: 1 }).then(res => {
			this.total = (res.data && res.data.total) || 0
		})
	},
	methods: {
		loadTree() {
			this.$api.list('hyIndustry', {}).then(res => {
				const all = res.data || []
				const bigs = all.filter(i => i.level === 1)
				this.groups = bigs.map(b => {
					return {
						id: b.id,
						name: b.name,
						color: this.groupColors[b.name] || '#2F6BFF',
						children: all.filter(c => c.parentId === b.id).sort((a, b2) => (b2.materialCount || 0) - (a.materialCount || 0))
					}
				})
				this.$nextTick(() => setTimeout(() => this.computeLayout(), 60))
			})
		},
		// 测量画布尺寸后，对每个行业做紧凑圆形装箱，形成聚拢的“星团”
		// 尺寸/坐标全部按画布实测像素计算，避免 rpx 在不同端换算比例不一致
		computeLayout() {
			uni.createSelectorQuery().in(this).select('.canvas').boundingClientRect(rect => {
				if (!rect || !rect.width) { setTimeout(() => this.computeLayout(), 80); return }
				const W = rect.width, H = rect.height
				const all = []
				this.groups.forEach(g => (g.children || []).slice(0, 9).forEach(c => all.push(c.materialCount || 0)))
				if (!all.length) return
				const maxC = Math.max(...all), minC = Math.min(...all)
				const maxD = H * 0.29, minD = H * 0.105
				const diam = cnt => (maxC === minC)
					? (maxD + minD) / 2
					: minD + ((cnt - minC) / (maxC - minC)) * (maxD - minD)
				// 星团中心按行业名固定：餐饮左上、企业右上、建筑左下
				const preset = {
					'餐饮': { x: W * 0.22, y: H * 0.31 },
					'企业': { x: W * 0.72, y: H * 0.35 },
					'建筑': { x: W * 0.27, y: H * 0.77 }
				}
				const fallback = [
					{ x: W * 0.23, y: H * 0.37 }, { x: W * 0.71, y: H * 0.36 },
					{ x: W * 0.27, y: H * 0.77 }, { x: W * 0.73, y: H * 0.77 }
				]
				const bubbles = []
				const labels = []
				const halos = []
				this.groups.forEach((grp, gi) => {
					const c = preset[grp.name] || fallback[gi % fallback.length]
					const kids = (grp.children || []).slice(0, 9)
					const placed = []
					kids.forEach((b, i) => {
						const cnt = b.materialCount || 0
						const d = diam(cnt)
						const r = d / 2
						const pos = (i === 0) ? { x: c.x, y: c.y } : this.findSpot(placed, r, c)
						pos.x = Math.max(r + 2, Math.min(W - r - 2, pos.x))
						pos.y = Math.max(r + 26, Math.min(H - r - 26, pos.y))
						placed.push({ x: pos.x, y: pos.y, r })
						bubbles.push({
							id: b.id, name: b.name, materialCount: cnt, cover: b.cover, color: grp.color,
							size: Math.round(d),
							x: (pos.x / W) * 100,
							y: (pos.y / H) * 100
						})
					})
					if (!placed.length) return
					const minX = Math.min(...placed.map(p => p.x - p.r))
					const minY = Math.min(...placed.map(p => p.y - p.r))
					const maxX = Math.max(...placed.map(p => p.x + p.r))
					const maxY = Math.max(...placed.map(p => p.y + p.r))
					labels.push({
						name: grp.name, color: grp.color,
						x: Math.max(1, (minX / W) * 100),
						y: Math.max(0.5, ((minY - 26) / H) * 100)
					})
					halos.push({
						name: grp.name, color: grp.color,
						x: ((minX + maxX) / 2 / W) * 100,
						y: ((minY + maxY) / 2 / H) * 100,
						size: Math.round(Math.max(maxX - minX, maxY - minY) + 40)
					})
				})
				this.bubbles = bubbles
				this.labels = labels
				this.halos = halos
				const first = bubbles[0]
				if (first && !this.selected) this.pick(first)
			}).exec()
		},
		// 螺旋外扩找一个与已放置气泡不重叠的位置，实现紧凑聚拢
		findSpot(placed, r, center) {
			const pad = 4
			for (let ring = 1; ring < 120; ring++) {
				const R = ring * 4
				const steps = Math.max(12, Math.floor(R / 3))
				for (let s = 0; s < steps; s++) {
					const a = (s / steps) * Math.PI * 2 + ring * 0.7
					const x = center.x + Math.cos(a) * R
					const y = center.y + Math.sin(a) * R
					let ok = true
					for (const p of placed) {
						if (Math.hypot(x - p.x, y - p.y) < p.r + r + pad) { ok = false; break }
					}
					if (ok) return { x, y }
				}
			}
			return { x: center.x, y: center.y }
		},
		nameSize(size) { return Math.max(9, Math.min(16, Math.round(size * 0.115))) + 'px' },
		cntSize(size) { return Math.max(11, Math.min(22, Math.round(size * 0.155))) + 'px' },
		pick(b) {
			this.selected = b
			this.$api.page('hyMaterial', { page: 1, limit: 3, industrySub: b.name }).then(res => {
				this.detailMaterials = (res.data && res.data.list) || []
				const tags = new Set()
				this.detailMaterials.forEach(m => {
					if (m.contentType) tags.add(m.contentType)
				})
				this.detailTags = Array.from(tags).slice(0, 3)
				if (this.detailTags.length === 0) this.detailTags = ['获客', '硬广', '晒过程']
			})
		},
		doSearch() {
			if (!this.keyword) return
			const cid = uni.getStorageSync('hyActiveCustomerId')
			let url = `/pages/selection/selection?keyword=${encodeURIComponent(this.keyword)}`
			if (cid) url += `&customerId=${cid}`
			uni.navigateTo({ url })
		},
		enter() {
			uni.navigateTo({
				url: `/pages/material/library?biztype=${encodeURIComponent(this.selected.name)}`
			})
		},
		addToReception() {
			uni.setStorageSync('receptionBiztype', this.selected.name)
			uni.showToast({ title: `已加入本次接待：${this.selected.name}`, icon: 'none' })
		}
	}
}
</script>

<style lang="scss" scoped>
.searchbar { width: 560rpx; height:64rpx; background:#F4F6FA; border-radius:999rpx; display:flex; align-items:center; padding:0 24rpx; }
.s-ico { font-size:26rpx; margin-right:12rpx; }
.s-input { flex:1; font-size:26rpx; }
.total { font-size:24rpx; color:$muted; }

.planet { flex:1; min-height:0; height:100%; display:flex; gap:24rpx; align-items:stretch; }
.canvas {
	flex:1; min-width:0; min-height:0; height:100%;
	position:relative; overflow:hidden;
	background:radial-gradient(circle at 30% 30%, #F7FAFF 0%, #EEF2F8 70%);
	border-radius:20rpx;
}
.halo { position:absolute; z-index:1; border-radius:50%; pointer-events:none; }
.cluster-label { position:absolute; z-index:3; font-size:28rpx; font-weight:700; letter-spacing:-.5rpx; }
.bubble {
	position:absolute;
	border-radius:50%;
	overflow:hidden;
	border:3rpx solid rgba(255,255,255,.92);
	display:flex;
	align-items:flex-end;
	justify-content:center;
	box-shadow:0 7rpx 22rpx rgba(31,39,51,.14);
	z-index:2;
}
.bubble.on { box-shadow:0 0 0 5rpx rgba(47,107,255,.24), 0 10rpx 26rpx rgba(31,39,51,.18); z-index:4; }
.b-img { position:absolute; width:100%; height:100%; top:0; left:0; }
.b-mask { position:absolute; width:100%; height:100%; top:0; left:0; background:linear-gradient(180deg, rgba(0,0,0,.05), rgba(0,0,0,.58)); }
.b-txt { position:relative; z-index:2; text-align:center; padding-bottom:12%; width:100%; }
.b-name { color:#fff; font-weight:700; display:block; text-shadow:0 1px 3px rgba(0,0,0,.6); line-height:1.2; }
.b-cnt { color:#fff; font-weight:700; display:block; line-height:1.2; text-shadow:0 1px 3px rgba(0,0,0,.52); }
.hint { position:absolute; left:0; right:0; bottom:14rpx; z-index:3; text-align:center; font-size:22rpx; color:$muted; }

.detail { width:360rpx; flex-shrink:0; padding:24rpx; min-height:0; overflow-y:auto; }
.dt-head { display:flex; justify-content:space-between; align-items:center; }
.dt-name { font-size:32rpx; font-weight:700; }
.dt-close { font-size:40rpx; color:$muted; }
.dt-cnt { font-size:24rpx; color:$ink-2; display:block; margin-top:8rpx; }
.dt-hot { font-size:22rpx; color:#FF5A5F; display:block; margin:6rpx 0 16rpx; }
.dt-vids { display:flex; flex-direction:column; gap:14rpx; }
.dv { position:relative; width:100%; height:170rpx; border-radius:14rpx; overflow:hidden; background:#eee; box-shadow:0 4rpx 14rpx rgba(31,39,51,.06); }
.dv-img { width:100%; height:100%; }
.dv-play { position:absolute; left:50%; top:50%; transform:translate(-50%,-50%); width:56rpx; height:56rpx; border-radius:50%; background:rgba(255,255,255,.92); color:$brand; box-shadow:0 4rpx 14rpx rgba(31,39,51,.16); }
.dv-play::after { content:""; position:absolute; left:22rpx; top:16rpx; border-left:16rpx solid currentColor; border-top:12rpx solid transparent; border-bottom:12rpx solid transparent; }
.dv-empty { color:$muted; font-size:24rpx; padding:30rpx 0; text-align:center; }
.dt-tags { display:flex; gap:12rpx; margin:18rpx 0; flex-wrap:wrap; }
.dt-tag { padding:6rpx 18rpx; background:#F1F3F6; border-radius:999rpx; font-size:22rpx; color:$ink-2; }
.dt-enter { height:84rpx; font-size:27rpx; }
.dt-add { height:80rpx; font-size:26rpx; margin-top:14rpx; }

/* 1-5a 标注稿：气泡画布约 1027 宽，详情栏保持窄列 */
@media #{$pad-mq-landscape} {
	.searchbar {
		width: 25vw;
		height: 5.2vh;
		padding: 0 1.2vw;
	}
	.s-input, .total {
		font-size: clamp(12px, .95vw, 15px);
	}
	.planet {
		gap: .75vw;
	}
	.canvas {
		border-radius: 14px;
	}
	.cluster-label {
		font-size: clamp(17px, 1.4vw, 22px);
	}
	.hint {
		bottom: 1vh;
		font-size: clamp(11px, .85vw, 14px);
	}
	.detail {
		width: 18.2vw;
		box-sizing: border-box;
		padding: 1.8vh 1vw;
		border-radius: 13px;
		overflow: hidden;
		display: flex;
		flex-direction: column;
	}
	.dt-name {
		font-size: clamp(19px, 1.6vw, 26px);
	}
	.dt-close {
		font-size: clamp(23px, 1.9vw, 30px);
	}
	.dt-cnt {
		font-size: clamp(12px, .95vw, 15px);
		margin-top: .5vh;
	}
	.dt-hot {
		font-size: clamp(11px, .85vw, 14px);
		margin: .45vh 0 1vh;
	}
	.dt-vids {
		flex: 1;
		min-height: 0;
		gap: .8vh;
	}
	.dv {
		flex: 1;
		min-height: 0;
		height: auto;
		border-radius: 9px;
	}
	.dv-play {
		width: 34px;
		height: 34px;
	}
	.dt-tags {
		gap: .45vw;
		margin: 1vh 0;
	}
	.dt-tag {
		padding: .35vh .7vw;
		font-size: clamp(10px, .76vw, 12px);
	}
	.dt-enter, .dt-add {
		height: 5.8vh;
		font-size: clamp(12px, .95vw, 15px);
		flex-shrink: 0;
	}
	.dt-add {
		margin-top: .8vh;
	}
}
</style>
