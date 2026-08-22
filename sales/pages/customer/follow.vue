<template>
	<view class="mask">
		<view class="modal">
			<view class="m-head">
				<view class="m-title-block">
					<text class="m-title">记录本次跟进</text>
					<text class="m-sub">{{ customerName }}</text>
				</view>
				<text class="m-close" @click="close">×</text>
			</view>

			<!-- 步骤 -->
			<view class="stepper">
				<view class="sp"><view class="sp-n on">1</view><text class="sp-l">跟进结果</text></view>
				<view class="sp-line"></view>
				<view class="sp"><view class="sp-n on">2</view><text class="sp-l">下一步计划</text></view>
				<view class="sp-line"></view>
				<view class="sp"><view class="sp-n on">3</view><text class="sp-l">确认保存</text></view>
			</view>

			<view class="m-body">
				<!-- 左表单 -->
				<view class="form">
					<text class="f-label">本次联系结果</text>
					<view class="chips">
						<view v-for="o in contactResults" :key="o" class="chip" :class="{on:form.contactResult===o}"
							@click="form.contactResult=o">
							<text class="radio" :class="{on:form.contactResult===o}"></text>{{ o }}
						</view>
					</view>

					<text class="f-label">客户意向</text>
					<view class="intent">
						<view class="it" :class="{on:form.intention==='高'}" @click="form.intention='高'">🔥 高</view>
						<view class="it" :class="{on:form.intention==='中'}" @click="form.intention='中'">🙂 中</view>
						<view class="it" :class="{on:form.intention==='低'}" @click="form.intention='低'">🌱 低</view>
					</view>

					<text class="f-label">下一步动作</text>
					<view class="chips">
						<view v-for="o in nextActions" :key="o" class="chip" :class="{on:form.nextAction===o}"
							@click="form.nextAction=o">{{ o }}</view>
					</view>

					<text class="f-label">下次跟进时间</text>
					<view class="time-row">
						<picker mode="date" :value="datePart" @change="onDate">
							<view class="date-box">📅 {{ form.nextTimeText }}</view>
						</picker>
						<view class="quick" @click="quick(1)">明天</view>
						<view class="quick" @click="quick(3)">3天后</view>
						<view class="quick" @click="quick(7)">下周</view>
					</view>

					<text class="f-label">补充备注（选填）</text>
					<textarea v-model="form.remark" class="textarea" placeholder="例如：客户希望增加门店故事类内容，预算约3000元，周末方便拍摄。" />
					<view class="voice-row">
						<view class="btn btn-ghost voice-btn" @click="toggleVoice">{{ recording ? '■ 结束语音' : '🎤 语音输入' }}</view>
						<text class="voice-hint">{{ recording ? '录音中…结束后写入备注' : '语音先记入备注，后续可接 AI 摘要' }}</text>
					</view>
				</view>

				<!-- 右预览 -->
				<view class="side">
					<view class="auto card">
						<text class="auto-title">系统将自动完成</text>
						<view class="auto-item">✓ 写入客户跟进时间线</view>
						<view class="auto-item">✓ 创建{{ form.nextTimeText }}跟进任务</view>
						<view class="auto-item">✓ 更新客户意向为{{ form.intention }}</view>
					</view>
					<view class="summary card">
						<view class="sum-head">
							<text class="sum-title">跟进摘要预览</text>
							<text class="sum-edit">编辑摘要</text>
						</view>
						<text class="sum-text">{{ summary }}</text>
					</view>
					<view class="warn">⚠ 保存后，该客户将进入「待跟进」列表；到期未完成将自动提醒。</view>
				</view>
			</view>

			<view class="m-foot">
				<text class="foot-info">记录人：{{ recorderName }} · {{ nowText }}</text>
				<view class="foot-btns">
					<view class="btn btn-ghost" @click="save(false)">仅保存记录</view>
					<view class="btn btn-danger" @click="save(true)">保存并创建下一步任务</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			customerId: null,
			customerName: '',
			recorderName: '',
			contactResults: ['已联系', '未接通', '客户考虑中', '已发送方案', '已确认续拍'],
			nextActions: ['发送续拍方案', '预约到店选片', '确认拍摄日期', '等待付款', '暂缓跟进'],
			form: {
				contactResult: '已联系',
				intention: '高',
				nextAction: '发送续拍方案',
				nextTime: '',
				nextTimeText: '',
				remark: ''
			},
			recording: false,
			recorder: null,
			recordStart: 0
		}
	},
	computed: {
		datePart() {
			return this.form.nextTime ? this.form.nextTime.slice(0, 10) : ''
		},
		nowText() {
			const d = new Date()
			return `${d.getMonth() + 1}月${d.getDate()}日 ${('0' + d.getHours()).slice(-2)}:${('0' + d.getMinutes()).slice(-2)}`
		},
		summary() {
			const d = new Date()
			let s = `${d.getMonth() + 1}月${d.getDate()}日，${this.recorderName}${this.form.contactResult}${this.customerName}。`
			s += `客户意向${this.form.intention}。`
			if (this.form.nextAction) s += `下一步：${this.form.nextAction}，计划${this.form.nextTimeText}再次跟进。`
			if (this.form.remark) s += `备注：${this.form.remark}`
			return s
		}
	},
	onLoad(opt) {
		this.customerId = opt.customerId
		this.customerName = decodeURIComponent(opt.customerName || '客户')
		this.recorderName = uni.getStorageSync('empName') || '我'
		this.quick(2)
		try {
			this.recorder = uni.getRecorderManager && uni.getRecorderManager()
			if (this.recorder) {
				this.recorder.onStop(() => {
					const sec = Math.max(1, Math.round((Date.now() - this.recordStart) / 1000))
					const note = `[语音备注 ${sec}秒]`
					this.form.remark = this.form.remark ? (this.form.remark + ' ' + note) : note
					this.recording = false
					uni.showToast({ title: '语音已记入备注', icon: 'none' })
				})
			}
		} catch (e) {}
	},
	methods: {
		quick(days) {
			const d = new Date()
			d.setDate(d.getDate() + days)
			d.setHours(10, 0, 0, 0)
			this.setTime(d)
		},
		onDate(e) {
			const d = new Date(e.detail.value.replace(/-/g, '/'))
			d.setHours(10, 0, 0, 0)
			this.setTime(d)
		},
		setTime(d) {
			const p = n => ('0' + n).slice(-2)
			this.form.nextTime = `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:00`
			const w = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][d.getDay()]
			this.form.nextTimeText = `${d.getMonth() + 1}月${d.getDate()}日 ${w} ${p(d.getHours())}:${p(d.getMinutes())}`
		},
		toggleVoice() {
			if (!this.recorder) {
				uni.showModal({
					title: '语音备注',
					editable: true,
					placeholderText: '当前环境无录音，请输入要点',
					success: (r) => {
						if (r.confirm && r.content) {
							this.form.remark = this.form.remark ? (this.form.remark + ' ' + r.content) : r.content
						}
					}
				})
				return
			}
			if (this.recording) {
				this.recorder.stop()
				return
			}
			this.recording = true
			this.recordStart = Date.now()
			this.recorder.start({ format: 'mp3', duration: 60000 })
			uni.showToast({ title: '开始录音', icon: 'none' })
		},
		save(withTask) {
			const payload = {
				customerId: this.customerId,
				customerName: this.customerName,
				recorderId: uni.getStorageSync('empId'),
				recorderName: this.recorderName,
				contactResult: this.form.contactResult,
				intention: this.form.intention,
				remark: this.form.remark,
				summary: this.summary
			}
			if (withTask) {
				payload.nextAction = this.form.nextAction
				payload.nextTime = this.form.nextTime
			}
			this.$api.save('hyFollowRecord', payload).then(() => {
				uni.showToast({ title: withTask ? '已保存并创建任务' : '已保存', icon: 'success' })
				setTimeout(() => this.close(), 600)
			})
		},
		close() {
			uni.navigateBack({ delta: 1, fail: () => uni.reLaunch({ url: '/pages/customer/customer' }) })
		}
	}
}
</script>

<style lang="scss" scoped>
.mask {
	width: 100%;
	height: 100vh;
	background: rgba(31, 39, 51, .45);
	display: flex;
	align-items: center;
	justify-content: center;
}
.modal {
	width: 1000rpx;
	max-width: 94%;
	max-height: 94vh;
	overflow-y: auto;
	background: #fff;
	border-radius: 24rpx;
	padding: 36rpx 40rpx 28rpx;
	box-shadow: 0 30rpx 80rpx rgba(0,0,0,.25);
}
.m-head { display:flex; justify-content:space-between; align-items:flex-start; }
.m-title { font-size: 36rpx; font-weight: 800; }
.m-sub { font-size: 24rpx; color: $muted; margin-top:6rpx; display:block; }
.m-close { font-size: 44rpx; color: $muted; line-height: 1; }

.stepper { display:flex; align-items:center; margin: 28rpx 0; }
.sp { display:flex; align-items:center; gap:12rpx; }
.sp-n { width:40rpx; height:40rpx; border-radius:50%; background:#E5E8EC; color:#fff; display:flex; align-items:center; justify-content:center; font-size:24rpx; }
.sp-n.on { background:$brand; }
.sp-l { font-size:24rpx; color:$ink-2; }
.sp-line { flex:1; height:2rpx; background:#E5E8EC; margin:0 20rpx; }

.m-body { display:flex; gap: 32rpx; }
.form { flex: 1.4; }
.side { flex: 1; display:flex; flex-direction:column; gap:18rpx; }
.f-label { font-size: 26rpx; font-weight:700; display:block; margin: 22rpx 0 14rpx; }
.chips { display:flex; flex-wrap:wrap; gap:14rpx; }
.chip { padding:14rpx 24rpx; border:1rpx solid $line; border-radius:14rpx; font-size:24rpx; color:$ink-2; display:flex; align-items:center; }
.chip.on { border-color:$brand; color:$brand; background:#EAF1FF; }
.radio { width:20rpx; height:20rpx; border-radius:50%; border:2rpx solid #C7CDD8; margin-right:10rpx; }
.radio.on { border-color:$brand; background:$brand; }
.intent { display:flex; gap:18rpx; }
.it { flex:1; text-align:center; padding:24rpx 0; border:1rpx solid $line; border-radius:16rpx; font-size:28rpx; }
.it.on { border-color:$brand; background:#EAF1FF; color:$brand; font-weight:700; }
.time-row { display:flex; align-items:center; gap:14rpx; }
.date-box { padding:18rpx 26rpx; border:1rpx solid $line; border-radius:14rpx; font-size:25rpx; }
.quick { padding:18rpx 24rpx; border:1rpx solid $line; border-radius:14rpx; font-size:24rpx; color:$ink-2; }
.textarea { width:100%; height:140rpx; border:1rpx solid $line; border-radius:14rpx; padding:18rpx; font-size:25rpx; margin-top:4rpx; }
.voice-row { display:flex; align-items:center; gap:16rpx; margin-top:14rpx; }
.voice-btn { height:64rpx; padding:0 24rpx; font-size:24rpx; }
.voice-hint { font-size:22rpx; color:$muted; flex:1; }

.auto { background:#F0F7F3; padding:22rpx; }
.auto-title { font-size:26rpx; font-weight:700; }
.auto-item { font-size:24rpx; color:#22B07D; margin-top:14rpx; }
.summary { padding:22rpx; border:1rpx solid $line; }
.sum-head { display:flex; justify-content:space-between; }
.sum-title { font-size:26rpx; font-weight:700; }
.sum-edit { font-size:24rpx; color:$brand; }
.sum-text { font-size:24rpx; color:$ink-2; line-height:1.6; margin-top:14rpx; display:block; }
.warn { background:#FFF6E9; color:#B8791F; font-size:23rpx; padding:18rpx; border-radius:14rpx; line-height:1.5; }

.m-foot { display:flex; justify-content:space-between; align-items:center; margin-top:28rpx; padding-top:22rpx; border-top:1rpx solid $line; }
.foot-info { font-size:23rpx; color:$muted; }
.foot-btns { display:flex; gap:18rpx; }
.foot-btns .btn { height:84rpx; padding:0 36rpx; font-size:27rpx; }
</style>
