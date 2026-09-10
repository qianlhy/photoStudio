/**
 * Pad App 批量上传：选文件后立刻拷到私有目录，中断后可直接续传（无需重选）
 * H5/浏览器仅作开发预览：临时路径刷新后会失效，需重选
 */
import base from '../api/base.js'

const BATCH_KEY = 'hyMaterial_pad_upload_batch'
const CACHE_DIR_NAME = 'uploadBatch'
const CACHE_DIR = `_doc/${CACHE_DIR_NAME}/`
const CONCURRENCY = 2

function isAppPlus() {
	return typeof plus !== 'undefined' && !!plus.io
}

function readBatch() {
	try {
		const raw = uni.getStorageSync(BATCH_KEY)
		if (raw && typeof raw === 'object' && Array.isArray(raw.files)) return raw
	} catch (e) {}
	return null
}

function writeBatch(batch) {
	if (!batch || !batch.files || !batch.files.length) {
		try { uni.removeStorageSync(BATCH_KEY) } catch (e) {}
		return
	}
	uni.setStorageSync(BATCH_KEY, batch)
}

function clearBatch() {
	try { uni.removeStorageSync(BATCH_KEY) } catch (e) {}
}

function parseAddtime(t) {
	if (!t) return 0
	if (typeof t === 'number') return t
	const s = String(t).replace('T', ' ').replace(/-/g, '/')
	const ms = new Date(s).getTime()
	return isNaN(ms) ? 0 : ms
}

function basename(path) {
	const p = String(path || '').replace(/\\/g, '/').split('?')[0]
	const i = p.lastIndexOf('/')
	let name = i >= 0 ? p.slice(i + 1) : p
	try { name = decodeURIComponent(name) } catch (e) {}
	return name || ''
}

function safeFileName(name) {
	const n = String(name || 'file').replace(/[\\/:*?"<>|]/g, '_')
	return n.length > 80 ? n.slice(-80) : n
}

function ensureUploadDir() {
	return new Promise((resolve, reject) => {
		if (!isAppPlus()) {
			resolve(CACHE_DIR)
			return
		}
		plus.io.requestFileSystem(plus.io.PRIVATE_DOC, fs => {
			fs.root.getDirectory(CACHE_DIR_NAME, { create: true }, () => resolve(CACHE_DIR), reject)
		}, reject)
	})
}

function fileExists(localPath) {
	return new Promise(resolve => {
		if (!localPath) {
			resolve(false)
			return
		}
		if (!isAppPlus()) {
			resolve(!!localPath)
			return
		}
		const p = localPath
		plus.io.resolveLocalFileSystemURL(p, () => resolve(true), () => {
			// 兼容绝对路径
			try {
				const abs = plus.io.convertLocalFileSystemURL(p)
				plus.io.resolveLocalFileSystemURL(abs, () => resolve(true), () => resolve(false))
			} catch (e) {
				resolve(false)
			}
		})
	})
}

/** uni.uploadFile 在 App 上优先用绝对路径更稳 */
function toUploadPath(localPath) {
	if (!localPath || !isAppPlus()) return localPath
	try {
		if (localPath.indexOf('_doc/') === 0 || localPath.indexOf('_documents/') === 0 || localPath.indexOf('_www/') === 0) {
			return plus.io.convertLocalFileSystemURL(localPath) || localPath
		}
	} catch (e) {}
	return localPath
}

function getFileSize(path) {
	return new Promise(resolve => {
		if (!path) {
			resolve(0)
			return
		}
		if (typeof uni.getFileInfo === 'function') {
			uni.getFileInfo({
				filePath: path,
				success: r => resolve((r && r.size) || 0),
				fail: () => resolve(0)
			})
			return
		}
		resolve(0)
	})
}

/** 把临时文件拷到 App 私有目录，返回 _doc 相对路径（持久） */
function copyToPrivate(tempPath, displayName) {
	return new Promise(async (resolve, reject) => {
		if (!tempPath) {
			reject(new Error('空路径'))
			return
		}
		if (!isAppPlus()) {
			resolve(tempPath)
			return
		}
		try {
			await ensureUploadDir()
		} catch (e) {
			reject(e)
			return
		}
		const destName = `${Date.now()}_${Math.random().toString(36).slice(2, 7)}_${safeFileName(displayName)}`
		const tryCopy = (srcUrl) => {
			plus.io.resolveLocalFileSystemURL(srcUrl, entry => {
				plus.io.requestFileSystem(plus.io.PRIVATE_DOC, fs => {
					fs.root.getDirectory(CACHE_DIR_NAME, { create: true }, dir => {
						entry.copyTo(dir, destName, () => {
							resolve(CACHE_DIR + destName)
						}, err => reject(err || new Error('copyTo fail')))
					}, reject)
				}, reject)
			}, reject)
		}
		// 相册路径可能是 file:// 或绝对路径
		tryCopy(tempPath)
	})
}

function removeLocal(localPath) {
	return new Promise(resolve => {
		if (!isAppPlus() || !localPath || String(localPath).indexOf(CACHE_DIR) < 0) {
			resolve()
			return
		}
		plus.io.resolveLocalFileSystemURL(localPath, entry => {
			entry.remove(() => resolve(), () => resolve())
		}, () => resolve())
	})
}

function uploadFile(filePath) {
	return new Promise((resolve, reject) => {
		const path = toUploadPath(filePath)
		uni.uploadFile({
			url: `${base.url}file/upload`,
			filePath: path,
			name: 'file',
			header: { Token: uni.getStorageSync('token') || '' },
			success: res => {
				try {
					const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
					if (data && data.code === 0 && data.file) resolve(data)
					else reject(new Error((data && data.msg) || '上传失败'))
				} catch (e) {
					reject(e)
				}
			},
			fail: err => reject(new Error((err && (err.errMsg || err.message)) || '网络错误'))
		})
	})
}

function normalizePickedPaths(raw) {
	if (!raw) return []
	if (Array.isArray(raw)) return raw.filter(Boolean)
	if (typeof raw === 'string') {
		return raw.split(',').map(s => s.trim()).filter(Boolean)
	}
	if (raw.files) {
		if (Array.isArray(raw.files)) return raw.files.filter(Boolean)
		if (typeof raw.files === 'string') return raw.files.split(',').map(s => s.trim()).filter(Boolean)
	}
	return []
}

/** App 优先相册多选（更易保留原文件名）；失败再走 chooseMedia/chooseVideo */
function chooseLocalMedia(count) {
	const lim = count || 20
	return new Promise((resolve, reject) => {
		const finishFromPaths = async (paths) => {
			const list = []
			for (const p of paths) {
				const name = basename(p) || `media_${Date.now()}.mp4`
				const size = await getFileSize(p)
				list.push({ tempFilePath: p, size, name })
			}
			resolve(list)
		}

		if (isAppPlus() && plus.gallery && typeof plus.gallery.pick === 'function') {
			plus.gallery.pick(
				e => {
					const paths = normalizePickedPaths(e)
					if (!paths.length && typeof e === 'string') finishFromPaths([e])
					else finishFromPaths(paths)
				},
				err => {
					const msg = (err && (err.message || err.errMsg || String(err))) || ''
					if (/cancel|取消|User cancelled/i.test(msg)) {
						resolve([])
						return
					}
					// gallery 失败则降级
					chooseByUni(lim).then(resolve).catch(reject)
				},
				{
					filter: 'none',
					multiple: true,
					maximum: lim,
					system: false
				}
			)
			return
		}
		chooseByUni(lim).then(resolve).catch(reject)
	})
}

function chooseByUni(lim) {
	return new Promise((resolve, reject) => {
		if (typeof uni.chooseMedia === 'function') {
			uni.chooseMedia({
				count: lim,
				mediaType: ['video', 'image'],
				sourceType: ['album', 'camera'],
				success: async res => {
					const list = []
					for (const f of (res.tempFiles || [])) {
						const path = f.tempFilePath
						const name = f.name || basename(path) || `media_${Date.now()}.mp4`
						const size = f.size || await getFileSize(path)
						list.push({ tempFilePath: path, size, name })
					}
					resolve(list)
				},
				fail: err => {
					if (err && /cancel|取消/i.test(err.errMsg || '')) resolve([])
					else reject(err)
				}
			})
			return
		}
		uni.chooseVideo({
			sourceType: ['album', 'camera'],
			compressed: false,
			success: async res => {
				const path = res.tempFilePath
				resolve([{
					tempFilePath: path,
					size: res.size || await getFileSize(path),
					name: basename(path) || `video_${Date.now()}.mp4`
				}])
			},
			fail: err => {
				if (err && /cancel|取消/i.test(err.errMsg || '')) resolve([])
				else reject(err)
			}
		})
	})
}

/**
 * 选文件并入库（App 会立刻拷私有目录）
 * 注意：不要在调用前 showLoading，否则会挡住系统相册
 */
async function addFilesFromPicker(meta = {}, onCopyProgress) {
	const picked = await chooseLocalMedia(20)
	if (!picked.length) return { batch: readBatch(), added: 0 }

	if (typeof onCopyProgress === 'function') onCopyProgress(0, picked.length)

	let batch = readBatch()
	const allDone = batch && batch.files && batch.files.length && batch.files.every(f => f.status === 'success')
	if (!batch || !batch.files || !batch.files.length || allDone) {
		batch = {
			batchId: 'b_' + Date.now(),
			startedAt: Date.now(),
			industryBig: meta.industryBig || '',
			industrySub: meta.industrySub || '',
			contentType: meta.contentType || '硬广',
			tags: meta.tags || '',
			files: []
		}
	} else {
		batch.industryBig = meta.industryBig != null ? meta.industryBig : batch.industryBig
		batch.industrySub = meta.industrySub != null ? meta.industrySub : batch.industrySub
		batch.contentType = meta.contentType != null ? meta.contentType : batch.contentType
		batch.tags = meta.tags != null ? meta.tags : batch.tags
	}

	let added = 0
	let i = 0
	for (const p of picked) {
		i++
		const name = p.name || basename(p.tempFilePath)
		const dup = batch.files.find(f => f.name === name && Number(f.size || 0) === Number(p.size || 0) && f.status !== 'fail' && f.status !== 'need_reselect')
		if (dup) continue
		let localPath = p.tempFilePath
		try {
			localPath = await copyToPrivate(p.tempFilePath, name)
		} catch (e) {
			console.warn('[uploadBatch] copy fail, use temp', e)
			localPath = p.tempFilePath
		}
		batch.files.push({
			id: 'u_' + Date.now() + '_' + Math.random().toString(36).slice(2, 7),
			name,
			size: p.size || 0,
			localPath,
			status: 'pending',
			msg: ''
		})
		added++
		writeBatch(batch)
		if (typeof onCopyProgress === 'function') onCopyProgress(i, picked.length)
	}
	writeBatch(batch)
	return { batch, added }
}

/** 用服务器素材对账；App 上本地文件还在则直接 pending，否则 need_reselect */
async function reconcileWithServer(api) {
	const batch = readBatch()
	if (!batch || !batch.files || !batch.files.length) return null

	const since = Math.max(0, (Number(batch.startedAt) || 0) - 2 * 60 * 1000)
	const nameSet = {}
	batch.files.forEach(f => { if (f && f.name) nameSet[f.name] = true })

	let list = []
	try {
		const res = await api.list('hyMaterial', {})
		list = (res && res.data) || []
		if (!Array.isArray(list)) list = []
	} catch (e) {
		list = []
	}

	const hit = {}
	list.forEach(m => {
		const title = m.title || ''
		if (!nameSet[title]) return
		if (parseAddtime(m.addtime) >= since) hit[title] = true
	})

	for (const f of batch.files) {
		if (hit[f.name]) {
			f.status = 'success'
			f.msg = ''
			// 已在服务器，清掉本地缓存副本
			if (f.localPath) {
				await removeLocal(f.localPath)
				f.localPath = ''
			}
			continue
		}
		if (f.status === 'success') continue
		const exists = await fileExists(f.localPath)
		if (exists) {
			if (f.status === 'uploading') f.status = 'pending'
			else if (f.status !== 'fail') f.status = 'pending'
			f.msg = ''
		} else {
			f.status = 'need_reselect'
			f.localPath = ''
			f.msg = isAppPlus() ? '本地缓存丢失，请重新选择' : '浏览器临时文件已失效，请重新选择'
		}
	}
	writeBatch(batch)
	return batch
}

function updateBatchMeta(meta) {
	const batch = readBatch()
	if (!batch) return null
	Object.keys(meta || {}).forEach(k => {
		if (meta[k] != null) batch[k] = meta[k]
	})
	writeBatch(batch)
	return batch
}

function saveBatch(batch) {
	writeBatch(batch)
}

function stats(batch) {
	const files = (batch && batch.files) || []
	const done = files.filter(f => f.status === 'success').length
	const fail = files.filter(f => f.status === 'fail').length
	const uploading = files.filter(f => f.status === 'uploading').length
	const pending = files.filter(f => f.status === 'pending' || f.status === 'need_reselect').length
	const remain = files.filter(f => f.status === 'pending' || f.status === 'fail' || f.status === 'uploading' || f.status === 'need_reselect').length
	const percent = files.length ? Math.round(done / files.length * 100) : 0
	return { done, fail, uploading, pending, remain, percent, total: files.length }
}

async function pumpUpload(api, onProgress) {
	const batch = readBatch()
	if (!batch) return { batch: null, finished: true }

	const notify = () => {
		writeBatch(batch)
		if (typeof onProgress === 'function') onProgress(JSON.parse(JSON.stringify(batch)))
	}

	const runOne = async (item) => {
		item.status = 'uploading'
		item.msg = ''
		notify()
		try {
			if (!(await fileExists(item.localPath))) {
				throw new Error('本地文件不存在')
			}
			const up = await uploadFile(item.localPath)
			await api.save('hyMaterial', {
				title: item.name,
				cover: up.file,
				video: up.file,
				industryBig: batch.industryBig || '',
				industrySub: batch.industrySub || '',
				contentType: batch.contentType || '硬广',
				tags: batch.tags || '',
				status: '下架',
				heat: 0
			})
			item.status = 'success'
			item.msg = ''
			await removeLocal(item.localPath)
			item.localPath = ''
		} catch (e) {
			item.status = 'fail'
			item.msg = (e && (e.msg || e.message)) || '上传失败'
			if (item.status === 'fail' && /本地文件不存在/.test(item.msg)) {
				item.status = 'need_reselect'
				item.localPath = ''
			}
		}
		notify()
	}

	const queue = batch.files.filter(f => (f.status === 'pending' || f.status === 'fail') && f.localPath)
	queue.forEach(f => { if (f.status === 'fail') { f.status = 'pending'; f.msg = '' } })
	writeBatch(batch)

	if (!queue.length) return { batch: readBatch(), finished: true }

	let idx = 0
	async function worker() {
		while (idx < queue.length) {
			const cur = queue[idx++]
			await runOne(cur)
		}
	}
	const n = Math.min(CONCURRENCY, queue.length)
	const workers = []
	for (let i = 0; i < n; i++) workers.push(worker())
	await Promise.all(workers)

	const latest = readBatch()
	const still = latest && latest.files.some(f => f.status === 'pending' || f.status === 'uploading' || f.status === 'fail')
	return { batch: latest, finished: !still }
}

async function relinkByPicker(onCopyProgress) {
	const batch = readBatch()
	if (!batch) return { linked: 0 }
	const needs = batch.files.filter(f => f.status === 'need_reselect')
	if (!needs.length) return { linked: 0 }

	const picked = await chooseLocalMedia(20)
	if (!picked.length) return { linked: 0, aborted: true }

	if (typeof onCopyProgress === 'function') onCopyProgress(0, picked.length)
	let linked = 0
	let i = 0
	for (const p of picked) {
		i++
		const name = p.name || basename(p.tempFilePath)
		const item = needs.find(f => f.name === name && f.status === 'need_reselect')
		if (!item) continue
		try {
			item.localPath = await copyToPrivate(p.tempFilePath, name)
			item.size = p.size || item.size || 0
			item.status = 'pending'
			item.msg = ''
			linked++
		} catch (e) {
			item.localPath = p.tempFilePath
			item.status = 'pending'
			item.msg = ''
			linked++
		}
		writeBatch(batch)
		if (typeof onCopyProgress === 'function') onCopyProgress(i, picked.length)
	}
	writeBatch(batch)
	return { linked, batch }
}

function clearSuccess() {
	const batch = readBatch()
	if (!batch) return null
	batch.files = batch.files.filter(f => f.status !== 'success')
	if (!batch.files.length) {
		clearBatch()
		return null
	}
	writeBatch(batch)
	return batch
}

function remainCount() {
	return stats(readBatch()).remain
}

export default {
	isAppPlus,
	readBatch,
	writeBatch,
	clearBatch,
	saveBatch,
	addFilesFromPicker,
	reconcileWithServer,
	updateBatchMeta,
	stats,
	pumpUpload,
	relinkByPicker,
	clearSuccess,
	fileExists,
	remainCount
}
