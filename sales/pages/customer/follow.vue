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
					<!-- 竖屏：互动类型（设计稿枚举） -->
					<view class="port-only-block">
						<text class="f-label">互动类型</text>
						<view class="chips chips-row4">
							<view v-for="o in interactTypes" :key="o" class="chip chip-eq" :class="{on:form.interactType===o}"
								@click="form.interactType=o">{{ o }}</view>
						</view>
					</view>

					<text class="f-label">客户意向</text>
					<view class="intent">
						<view class="it" :class="{on:form.intention==='高'}" @click="form.intention='高'">🔥 高</view>
						<view class="it" :class="{on:form.intention==='中'}" @click="form.intention='中'">🙂 中</view>
						<view class="it" :class="{on:form.intention==='低'}" @click="form.intention='低'">🌱 低</view>
					</view>

					<text class="f-label">本次联系结果</text>
					<view class="chips">
						<view v-for="o in contactResults" :key="o" class="chip" :class="{on:form.contactResult===o}"
							@click="form.contactResult=o">
							<text class="radio" :class="{on:form.contactResult===o}"></text>{{ o }}
						</view>
					</view>

					<!-- 竖屏：客户异议多选（设计稿枚举） -->
					<view class="port-only-block">
						<text class="f-label">客户异议（多选）</text>
						<view class="chips chips-row5">
							<view v-for="o in objectionOptions" :key="o" class="chip chip-check" :class="{on:form.objections.indexOf(o)>=0}"
								@click="toggleObjection(o)">
								<text class="chk">{{ form.objections.indexOf(o)>=0 ? '✓' : '' }}</text>{{ o }}
							</view>
						</view>
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
					<textarea v-model="form.remark" class="textarea" placeholder="例如：客户希望增加门店故事类内容，预算约3000元，周末方便拍摄。可直接使用键盘听写输入。" />
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
							<view class="sum-actions">
								<text v-if="summaryDirty && !summaryEditing" class="sum-reset" @click="resetSummary">恢复自动</text>
								<text class="sum-edit" @click="toggleSummaryEdit">{{ summaryEditing ? '完成' : '编辑摘要' }}</text>
							</view>
						</view>
						<textarea
							v-if="summaryEditing"
							v-model="summaryManual"
							class="sum-textarea"
							auto-height
							maxlength="500"
							placeholder="可直接修改跟进摘要"
						/>
						<text v-else class="sum-text">{{ displaySummary }}</text>
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
			interactTypes: ['面谈', '电话', '微信', '到店回访'],
			objectionOptions: ['价格', '时间', '效果', '决策人', '其他'],
			form: {
				interactType: '面谈',
				contactResult: '已联系',
				intention: '高',
				objections: [],
				nextAction: '发送续拍方案',
				nextTime: '',
				nextTimeText: '',
				remark: ''
			},
			summaryEditing: false,
			summaryDirty: false,
			summaryManual: ''
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
		autoSummary() {
			const d = new Date()
			let s = `${d.getMonth() + 1}月${d.getDate()}日，${this.recorderName}`
			if (this.form.interactType) s += `通过${this.form.interactType}`
			s += `${this.form.contactResult}${this.customerName}。`
			s += `客户意向${this.form.intention}。`
			if (this.form.objections && this.form.objections.length) {
				s += `异议：${this.form.objections.join('、')}。`
			}
			if (this.form.nextAction) s += `下一步：${this.form.nextAction}，计划${this.form.nextTimeText}再次跟进。`
			if (this.form.remark) s += `备注：${this.form.remark}`
			return s
		},
		displaySummary() {
			return this.summaryDirty ? this.summaryManual : this.autoSummary
		}
	},
	onLoad(opt) {
		this.customerId = opt.customerId
		this.customerName = decodeURIComponent(opt.customerName || '客户')
		this.recorderName = uni.getStorageSync('empName') || '我'
		this.quick(2)
	},
	methods: {
		toggleSummaryEdit() {
			if (this.summaryEditing) {
				const t = (this.summaryManual || '').trim()
				if (!t) {
					uni.showToast({ title: '摘要不能为空', icon: 'none' })
					return
				}
				this.summaryManual = t
				this.summaryDirty = true
				this.summaryEditing = false
				return
			}
			this.summaryManual = this.displaySummary
			this.summaryEditing = true
		},
		resetSummary() {
			this.summaryDirty = false
			this.summaryManual = ''
			this.summaryEditing = false
		},
		toggleObjection(o) {
			const i = this.form.objections.indexOf(o)
			if (i >= 0) this.form.objections.splice(i, 1)
			else this.form.objections.push(o)
		},
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
		save(withTask) {
			const payload = {
				customerId: this.customerId,
				customerName: this.customerName,
				recorderId: uni.getStorageSync('empId'),
				recorderName: this.recorderName,
				contactResult: this.form.contactResult,
				intention: this.form.intention,
				remark: this.form.remark,
				summary: this.displaySummary
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

.auto { background:#F0F7F3; padding:22rpx; }
.auto-title { font-size:26rpx; font-weight:700; }
.auto-item { font-size:24rpx; color:#22B07D; margin-top:14rpx; }
.summary { padding:22rpx; border:1rpx solid $line; }
.sum-head { display:flex; justify-content:space-between; align-items:center; gap:12rpx; }
.sum-title { font-size:26rpx; font-weight:700; }
.sum-actions { display:flex; align-items:center; gap:20rpx; flex-shrink:0; }
.sum-edit { font-size:24rpx; color:$brand; }
.sum-reset { font-size:24rpx; color:$muted; }
.sum-text { font-size:24rpx; color:$ink-2; line-height:1.6; margin-top:14rpx; display:block; }
.sum-textarea {
	width:100%;
	min-height:160rpx;
	margin-top:14rpx;
	padding:16rpx;
	border:1rpx solid $brand;
	border-radius:12rpx;
	font-size:24rpx;
	color:$ink-2;
	line-height:1.6;
	box-sizing:border-box;
	background:#F7F9FC;
}
.warn { background:#FFF6E9; color:#B8791F; font-size:23rpx; padding:18rpx; border-radius:14rpx; line-height:1.5; }

.m-foot { display:flex; justify-content:space-between; align-items:center; margin-top:28rpx; padding-top:22rpx; border-top:1rpx solid $line; }
.foot-info { font-size:23rpx; color:$muted; }
.foot-btns { display:flex; gap:18rpx; }
.foot-btns .btn { height:84rpx; padding:0 36rpx; font-size:27rpx; }

/* 竖屏专属字段：横屏隐藏 */
.port-only-block {
	display: none;
}

@include pad-portrait {
	.port-only-block {
		display: block;
	}
	.chips-row4 {
		display: flex;
		flex-wrap: nowrap;
		gap: p-px(10);
	}
	.chips-row4 .chip-eq {
		flex: 1;
		justify-content: center;
		min-width: 0;
		padding: p-px(12) p-px(6);
		font-size: p-px(13);
		border-radius: 999px;
		box-sizing: border-box;
		min-height: $p-follow-field-h;
	}
	.chips-row5 {
		display: flex;
		flex-wrap: wrap;
		gap: p-px(10);
	}
	.chips-row5 .chip-check {
		flex: 0 0 auto;
		padding: p-px(10) p-px(14);
		font-size: p-px(13);
		border-radius: 999px;
		min-height: $p-follow-field-h;
		box-sizing: border-box;
	}
	.chip-check .chk {
		display: inline-block;
		width: p-px(14);
		margin-right: p-px(4);
		font-size: p-px(12);
		line-height: 1;
		text-align: center;
	}
	.chip-check.on {
		border-color: $ok;
		color: $ok;
		background: #F0F7F3;
	}

	.mask {
		/* 垂直居中，避免贴底偏下 */
		align-items: center;
		justify-content: center;
		padding: p-px(24) $p-pad-x;
		box-sizing: border-box;
	}
	.modal {
		width: 100%;
		max-width: none;
		max-height: 86vh;
		border-radius: p-px(20);
		/* 06 内容区宽约 833dp，边距用 $p-pad-x */
		padding: p-px(16) $p-pad-x p-px(20);
		box-sizing: border-box;
	}
	.m-head {
		margin-bottom: p-px(10);
	}
	.m-title {
		font-size: p-px(20);
	}
	.m-sub {
		font-size: p-px(13);
	}
	.stepper {
		margin: p-px(14) 0 p-px(12);
	}
	.sp-l {
		font-size: p-px(12);
	}
	.m-body {
		flex-direction: column;
		gap: p-px(14);
		max-height: 68vh;
		overflow-y: auto;
	}
	.form,
	.side {
		flex: none;
		width: 100%;
	}
	.f-label {
		font-size: p-px(14);
		margin: p-px(14) 0 p-px(8);
	}
	.chips {
		gap: p-px(10);
	}
	.chip {
		padding: p-px(10) p-px(16);
		font-size: p-px(13);
		border-radius: 999px;
	}
	.intent {
		gap: p-px(12);
	}
	.it {
		/* 06 意图块约 170–181dp；竖屏三等分 */
		flex: 1;
		padding: p-px(14) 0;
		font-size: p-px(15);
		border-radius: p-px(12);
		min-height: $p-follow-field-h;
		box-sizing: border-box;
		display: flex;
		align-items: center;
		justify-content: center;
	}
	.time-row {
		flex-wrap: wrap;
		gap: p-px(10);
	}
	.date-box,
	.quick {
		padding: p-px(10) p-px(14);
		font-size: p-px(13);
		border-radius: p-px(12);
		min-height: $p-follow-field-h;
		box-sizing: border-box;
	}
	.textarea {
		/* 06 文本区相关高约 81–92，备注区加高 */
		height: p-px(120);
		min-height: $p-follow-field-h;
		font-size: p-px(14);
		border-radius: p-px(12);
		box-sizing: border-box;
	}
	.auto,
	.summary {
		padding: p-px(14) p-px(16);
		border-radius: p-px(12);
	}
	.sum-edit,
	.sum-reset {
		font-size: p-px(13);
	}
	.sum-text,
	.sum-textarea {
		font-size: p-px(13);
	}
	.sum-textarea {
		min-height: p-px(96);
		padding: p-px(10);
		border-radius: p-px(10);
	}
	.m-foot {
		flex-direction: column;
		align-items: stretch;
		gap: p-px(12);
		margin-top: p-px(14);
		padding-top: p-px(12);
	}
	.foot-info {
		text-align: center;
		font-size: p-px(12);
	}
	.foot-btns {
		flex-direction: row;
		justify-content: center;
		gap: p-px(12);
	}
	.foot-btns .btn {
		flex: 0 1 auto;
		height: $p-ctrl-h;
		min-height: $p-ctrl-h;
		font-size: p-px(14);
		border-radius: 999px;
		box-sizing: border-box;
		padding: 0 p-px(12);
		min-width: 0;
	}
	.foot-btns .btn-ghost {
		/* 06 H 690 → 345；窄屏可按比例收缩 */
		flex: 345 1 0;
		width: $p-follow-btn-l;
		max-width: $p-follow-btn-l;
	}
	.foot-btns .btn-danger {
		flex: 332 1 0;
		width: $p-follow-btn-r;
		max-width: $p-follow-btn-r;
	}
}
</style>
