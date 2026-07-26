<template>
	<sales-shell active="material" title="素材星球" subtitle="点选行业，发现适合客户的高表现案例">
		<view slot="search" class="searchbar">
			<text class="s-ico">🔍</text>
			<input v-model="keyword" class="s-input" placeholder="搜索行业、业态或关键词" @confirm="doSearch" />
		</view>
		<view slot="actions" class="total">全部 {{ total }} 条</view>

		<view class="planet">
			<!-- 气泡画布 -->
			<scroll-view scroll-y class="canvas">
				<view v-for="grp in groups" :key="grp.id" class="cluster">
					<text class="cluster-label" :style="{color: grp.color}">{{ grp.name }}</text>
					<view class="bubbles">
						<view v-for="b in grp.children" :key="b.id" class="bubble"
							:class="{on: selected&&selected.id===b.id}"
							:style="bubbleStyle(b, grp)" @click="pick(b)">
							<image class="b-img" :src="$img(b.cover)" mode="aspectFill"></image>
							<view class="b-mask"></view>
							<view class="b-txt">
								<text class="b-name">{{ b.name }}</text>
								<text class="b-cnt">{{ b.materialCount }}</text>
							</view>
						</view>
					</view>
				</view>
				<view class="hint">🖐 拖动画布浏览 · 双指缩放 · 点击气泡进入</view>
			</scroll-view>

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
						<view class="dv-play">▶</view>
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
				const first = this.groups[0] && this.groups[0].children[0]
				if (first) this.pick(first)
			})
		},
		bubbleStyle(b, grp) {
			const c = b.materialCount || 0
			const size = Math.max(120, Math.min(260, 110 + c * 0.8))
			return {
				width: size + 'rpx',
				height: size + 'rpx',
				borderColor: grp.color
			}
		},
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
			uni.navigateTo({ url: `/pages/selection/selection?keyword=${encodeURIComponent(this.keyword)}` })
		},
		enter() {
			const cid = uni.getStorageSync('currentCustomerId')
			let url = `/pages/selection/selection?biztype=${encodeURIComponent(this.selected.name)}`
			if (cid) url += `&customerId=${cid}`
			uni.navigateTo({ url })
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

.planet { display:flex; gap:24rpx; align-items:flex-start; }
.canvas { flex:1; height: calc(100vh - 200rpx); }
.cluster { margin-bottom: 30rpx; }
.cluster-label { font-size:30rpx; font-weight:800; display:block; margin-bottom:12rpx; }
.bubbles { display:flex; flex-wrap:wrap; gap:20rpx; align-items:center; }
.bubble {
	position:relative;
	border-radius:50%;
	overflow:hidden;
	border:3rpx solid #ccc;
	display:flex;
	align-items:flex-end;
	justify-content:center;
}
.bubble.on { box-shadow:0 0 0 6rpx rgba(47,107,255,.25); }
.b-img { position:absolute; width:100%; height:100%; top:0; left:0; }
.b-mask { position:absolute; width:100%; height:100%; top:0; left:0; background:linear-gradient(180deg, rgba(0,0,0,.05), rgba(0,0,0,.55)); }
.b-txt { position:relative; z-index:2; text-align:center; padding-bottom:18rpx; }
.b-name { color:#fff; font-size:24rpx; font-weight:700; display:block; text-shadow:0 2rpx 6rpx rgba(0,0,0,.5); }
.b-cnt { color:#fff; font-size:30rpx; font-weight:800; display:block; }
.hint { text-align:center; font-size:22rpx; color:$muted; padding:20rpx 0 40rpx; }

.detail { width:360rpx; flex-shrink:0; padding:24rpx; position:sticky; top:0; }
.dt-head { display:flex; justify-content:space-between; align-items:center; }
.dt-name { font-size:32rpx; font-weight:800; }
.dt-close { font-size:40rpx; color:$muted; }
.dt-cnt { font-size:24rpx; color:$ink-2; display:block; margin-top:8rpx; }
.dt-hot { font-size:22rpx; color:#FF5A5F; display:block; margin:6rpx 0 16rpx; }
.dt-vids { display:flex; flex-direction:column; gap:14rpx; }
.dv { position:relative; width:100%; height:170rpx; border-radius:14rpx; overflow:hidden; background:#eee; }
.dv-img { width:100%; height:100%; }
.dv-play { position:absolute; left:50%; top:50%; transform:translate(-50%,-50%); width:56rpx; height:56rpx; border-radius:50%; background:rgba(255,255,255,.85); display:flex; align-items:center; justify-content:center; color:$brand; }
.dv-empty { color:$muted; font-size:24rpx; padding:30rpx 0; text-align:center; }
.dt-tags { display:flex; gap:12rpx; margin:18rpx 0; flex-wrap:wrap; }
.dt-tag { padding:6rpx 18rpx; background:#F1F3F6; border-radius:999rpx; font-size:22rpx; color:$ink-2; }
.dt-enter { height:84rpx; font-size:27rpx; }
.dt-add { height:80rpx; font-size:26rpx; margin-top:14rpx; }
</style>
