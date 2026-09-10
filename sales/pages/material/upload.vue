<template>
	<sales-shell active="material" title="批量上传素材" subtitle="选文件后自动缓存，中断可一键续传">
		<view slot="actions" class="head-actions">
			<text class="link" @click="goBack">返回素材库</text>
		</view>

		<view class="page">
			<view class="card meta">
				<text class="sec">应用到全部视频</text>
				<view class="row">
					<picker :range="bigNames" @change="onBigPick">
						<view class="picker">{{ form.industryBig || '选择行业' }}</view>
					</picker>
					<picker :range="subNames" @change="onSubPick">
						<view class="picker">{{ form.industrySub || '选择业态' }}</view>
					</picker>
				</view>
				<picker :range="contentTypes" @change="onTypePick">
					<view class="picker full">{{ form.contentType || '内容类型' }}</view>
				</picker>
				<input class="input" v-model="form.tags" placeholder="标签，如：火锅,后厨" @blur="persistMeta" />
			</view>

			<view class="card progress">
				<view class="prog-head">
					<text>上传进度</text>
					<text class="pct">{{ st.percent }}%</text>
				</view>
				<view class="bar"><view class="bar-in" :style="{width: st.percent + '%'}"></view></view>
				<view class="stat">
					<text class="ok">成功 {{ st.done }}</text>
					<text class="fail">失败 {{ st.fail }}</text>
					<text>上传中 {{ st.uploading }}</text>
					<text>待传 {{ st.pending }}</text>
				</view>
				<text class="tip">{{ envTip }}</text>
			</view>

			<view class="card">
				<view class="btn-primary" @click="pickFiles">选择视频/图片加入队列</view>
				<view class="btn-ghost" :class="{disabled: running || !canContinue}" @click="continueUpload">
					{{ running ? '上传中…' : '继续上传剩余文件' }}
				</view>
				<view class="btn-row">
					<view class="btn-ghost half" @click="doClearSuccess">清除已成功</view>
					<view class="btn-ghost half" @click="doRelink">重选丢失文件</view>
				</view>
			</view>

			<view class="card queue">
				<text class="sec">上传队列（{{ files.length }}）</text>
				<view v-if="files.length===0" class="empty">暂无文件，请先选择</view>
				<view v-for="item in files" :key="item.id" class="q-item">
					<text class="q-name">{{ item.name }}</text>
					<text class="q-tag" :class="item.status">{{ statusText(item.status) }}</text>
				</view>
			</view>
		</view>
	</sales-shell>
</template>

<script>
import salesShell from '@/components/sales-shell/sales-shell.vue'
import uploadBatch from '@/utils/uploadBatch.js'

export default {
	components: { salesShell },
	data() {
		return {
			allIndustry: [],
			bigList: [],
			subList: [],
			contentTypes: ['晒过程', '教知识', '讲故事', '说观点', '硬广'],
			form: {
				industryBig: '',
				industrySub: '',
				contentType: '硬广',
				tags: ''
			},
			files: [],
			running: false,
			st: { done: 0, fail: 0, uploading: 0, pending: 0, remain: 0, percent: 0, total: 0 }
		}
	},
	computed: {
		bigNames() { return this.bigList.map(i => i.name) },
		subNames() { return this.subList.map(i => i.name) },
		canContinue() {
			return this.files.some(f =>
				((f.status === 'pending' || f.status === 'fail') && f.localPath) ||
				f.status === 'need_reselect'
			)
		},
		envTip() {
			if (uploadBatch.isAppPlus()) {
				return 'Pad App：文件已缓存到本机，关掉再开可直接点「继续上传」'
			}
			return '当前是浏览器预览：刷新后需重选文件。正式请用打包 App 测试续传'
		}
	},
	onShow() {
		if (!this.$api.auth()) return
		this.loadIndustry()
		this.refreshFromStorage()
		this.reconcile()
	},
	methods: {
		goBack() {
			uni.navigateBack({ fail: () => uni.reLaunch({ url: '/pages/material/library' }) })
		},
		statusText(s) {
			return ({
				success: '已完成',
				fail: '失败',
				uploading: '上传中',
				pending: '待传',
				need_reselect: '需重选'
			})[s] || s
		},
		applyBatch(batch) {
			if (!batch) {
				this.files = []
				this.st = uploadBatch.stats(null)
				return
			}
			if (batch.industryBig) this.form.industryBig = batch.industryBig
			if (batch.industrySub) this.form.industrySub = batch.industrySub
			if (batch.contentType) this.form.contentType = batch.contentType
			if (batch.tags != null) this.form.tags = batch.tags
			this.files = (batch.files || []).slice()
			this.st = uploadBatch.stats(batch)
			if (batch.industryBig) this.fillSubs(batch.industryBig)
		},
		refreshFromStorage() {
			this.applyBatch(uploadBatch.readBatch())
		},
		persistMeta() {
			uploadBatch.updateBatchMeta({
				industryBig: this.form.industryBig,
				industrySub: this.form.industrySub,
				contentType: this.form.contentType,
				tags: this.form.tags
			})
		},
		loadIndustry() {
			this.$api.list('hyIndustry', {}).then(res => {
				this.allIndustry = res.data || []
				this.bigList = this.allIndustry.filter(i => i.level === 1)
				if (this.form.industryBig) this.fillSubs(this.form.industryBig)
			})
		},
		fillSubs(bigName) {
			const big = this.bigList.find(b => b.name === bigName)
			this.subList = big ? this.allIndustry.filter(i => i.parentId === big.id) : []
		},
		onBigPick(e) {
			const name = this.bigNames[e.detail.value]
			this.form.industryBig = name || ''
			this.form.industrySub = ''
			this.fillSubs(this.form.industryBig)
			this.persistMeta()
		},
		onSubPick(e) {
			this.form.industrySub = this.subNames[e.detail.value] || ''
			this.persistMeta()
		},
		onTypePick(e) {
			this.form.contentType = this.contentTypes[e.detail.value] || '硬广'
			this.persistMeta()
		},
		reconcile() {
			uni.showLoading({ title: '对账中', mask: true })
			uploadBatch.reconcileWithServer(this.$api).then(batch => {
				uni.hideLoading()
				this.applyBatch(batch || uploadBatch.readBatch())
				const need = this.files.filter(f => f.status === 'need_reselect').length
				const done = this.st.done
				if (need > 0) {
					uni.showToast({
						title: `成功${done}，需重选${need}`,
						icon: 'none'
					})
				}
			}).catch(() => {
				uni.hideLoading()
				this.refreshFromStorage()
			})
		},
		pickFiles() {
			this.persistMeta()
			// 先弹相册，再拷贝；loading 不能挡在选文件前面
			uploadBatch.addFilesFromPicker(this.form, (cur, total) => {
				uni.showLoading({ title: `缓存 ${cur}/${total}`, mask: true })
			}).then(({ added }) => {
				uni.hideLoading()
				this.refreshFromStorage()
				if (!added) {
					uni.showToast({ title: '未选择文件', icon: 'none' })
					return
				}
				uni.showToast({ title: `已加入${added}个`, icon: 'none' })
				this.$nextTick(() => this.continueUpload())
			}).catch(err => {
				uni.hideLoading()
				uni.showToast({ title: (err && (err.errMsg || err.message)) || '选择失败', icon: 'none' })
			})
		},
		continueUpload() {
			if (this.running) return
			this.refreshFromStorage()
			const hasReady = this.files.some(f => (f.status === 'pending' || f.status === 'fail') && f.localPath)
			if (!hasReady) {
				const need = this.files.filter(f => f.status === 'need_reselect')
				if (need.length) {
					uni.showModal({
						title: '有文件需重选',
						content: `有 ${need.length} 个本地缓存丢失，是否现在重新选择同名文件？`,
						success: r => {
							if (r.confirm) this.doRelink()
						}
					})
					return
				}
				uni.showToast({ title: '没有可上传的文件', icon: 'none' })
				return
			}
			this.persistMeta()
			this.running = true
			uploadBatch.pumpUpload(this.$api, batch => {
				this.applyBatch(batch)
			}).then(() => {
				this.running = false
				this.refreshFromStorage()
				const need = this.files.filter(f => f.status === 'need_reselect').length
				let title = '本批已全部完成'
				if (this.st.remain) title = `还剩${this.st.remain}个`
				if (need) title = `成功${this.st.done}，需重选${need}`
				uni.showToast({ title, icon: 'none' })
			}).catch(() => {
				this.running = false
				this.refreshFromStorage()
			})
		},
		doRelink() {
			uploadBatch.relinkByPicker((cur, total) => {
				uni.showLoading({ title: `匹配 ${cur}/${total}`, mask: true })
			}).then(r => {
				uni.hideLoading()
				this.refreshFromStorage()
				if (r.aborted) return
				if (r.linked > 0) {
					uni.showToast({ title: `已找回${r.linked}个`, icon: 'none' })
					this.$nextTick(() => this.continueUpload())
				} else {
					uni.showToast({ title: '未匹配到同名文件，请选回原来那批', icon: 'none' })
				}
			}).catch(err => {
				uni.hideLoading()
				uni.showToast({ title: (err && (err.errMsg || err.message)) || '重选失败', icon: 'none' })
			})
		},
		doClearSuccess() {
			this.applyBatch(uploadBatch.clearSuccess())
			uni.showToast({ title: '已清除成功项', icon: 'none' })
		}
	}
}
</script>

<style scoped>
.page { padding: 12px 16px 40px; }
.card {
	background: #fff;
	border-radius: 14px;
	padding: 14px 16px;
	margin-bottom: 12px;
	box-shadow: 0 1px 4px rgba(0,21,41,.05);
}
.sec { display: block; font-size: 14px; font-weight: 700; color: #1F2733; margin-bottom: 10px; }
.row { display: flex; gap: 8px; margin-bottom: 8px; }
.picker {
	flex: 1;
	background: #F4F6FA;
	border-radius: 8px;
	padding: 10px 12px;
	font-size: 13px;
	color: #1F2733;
}
.picker.full { margin-bottom: 8px; }
.input {
	background: #F4F6FA;
	border-radius: 8px;
	padding: 10px 12px;
	font-size: 13px;
}
.prog-head { display: flex; justify-content: space-between; font-size: 13px; color: #5a6473; }
.pct { color: #2F6BFF; font-weight: 700; }
.bar { height: 8px; background: #EEF1F5; border-radius: 4px; margin: 8px 0; overflow: hidden; }
.bar-in { height: 100%; background: #2F6BFF; border-radius: 4px; }
.stat { display: flex; flex-wrap: wrap; gap: 12px; font-size: 12px; color: #8A94A6; }
.stat .ok { color: #22B07D; }
.stat .fail { color: #FF5A5F; }
.tip { display: block; margin-top: 8px; font-size: 12px; color: #B8791F; line-height: 1.4; }
.btn-primary, .btn-ghost {
	text-align: center;
	padding: 12px;
	border-radius: 10px;
	font-size: 14px;
	font-weight: 600;
	margin-bottom: 8px;
}
.btn-primary { background: #2F6BFF; color: #fff; }
.btn-ghost { background: #F4F6FA; color: #1F2733; }
.btn-ghost.disabled { opacity: .45; }
.btn-row { display: flex; gap: 8px; }
.half { flex: 1; margin-bottom: 0; }
.empty { font-size: 12px; color: #a8b0bd; padding: 8px 0; }
.q-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 8px;
	padding: 10px 0;
	border-bottom: 1px dashed #F0F2F5;
}
.q-item:last-child { border-bottom: none; }
.q-name {
	flex: 1;
	font-size: 12px;
	color: #1F2733;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
.q-tag { font-size: 11px; padding: 2px 8px; border-radius: 10px; background: #EEF1F5; color: #5a6473; }
.q-tag.success { background: #E8F8F0; color: #22B07D; }
.q-tag.fail, .q-tag.need_reselect { background: #FFECEC; color: #FF5A5F; }
.q-tag.uploading { background: #FFF6E9; color: #FF8A3D; }
.q-tag.pending { background: #EAF1FF; color: #2F6BFF; }
.head-actions .link { font-size: 13px; color: #2F6BFF; }
</style>
