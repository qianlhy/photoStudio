import base from '../api/base.js'

const MANIFEST_KEY = 'hyMaterialCacheManifest'
const CACHE_DIR = '_doc/materialCache/'
const MAX_CONCURRENT = 2

let syncing = false
let syncPromise = null
const listeners = []

function readManifest() {
	try {
		const raw = uni.getStorageSync(MANIFEST_KEY)
		if (raw && typeof raw === 'object') return raw
	} catch (e) {}
	return { items: {}, lastSyncAt: 0, remoteTotal: 0 }
}

function writeManifest(manifest) {
	uni.setStorageSync(MANIFEST_KEY, manifest)
	notify()
}

function notify() {
	const status = getStatus()
	listeners.forEach(fn => {
		try { fn(status) } catch (e) {}
	})
}

function onStatusChange(fn) {
	if (typeof fn === 'function') listeners.push(fn)
	return () => {
		const i = listeners.indexOf(fn)
		if (i >= 0) listeners.splice(i, 1)
	}
}

function fullUrl(path, baseUrl) {
	if (!path) return ''
	if (/^https?:\/\//.test(path)) return path
	let p = String(path).replace(/^\//, '')
	if (p && !p.startsWith('upload/')) p = 'upload/' + p
	return (baseUrl || base.url) + p
}

function extFromPath(path, fallback) {
	const m = String(path || '').match(/\.([a-zA-Z0-9]+)(?:\?|$)/)
	return m ? '.' + m[1].toLowerCase() : fallback
}

function itemKey(id, field) {
	return field === 'cover' ? `${id}_cover` : `${id}_video`
}

function localFilePath(id, field, remotePath) {
	const ext = extFromPath(remotePath, field === 'cover' ? '.jpg' : '.mp4')
	return `${CACHE_DIR}${itemKey(id, field)}${ext}`
}

/** App 打包后恒为 true；H5 为 false。避免 onLaunch 时 plus 未注入误判 */
function isAppPlus() {
	// #ifdef APP-PLUS
	return true
	// #endif
	// #ifndef APP-PLUS
	return typeof plus !== 'undefined' && !!plus.io
	// #endif
}

function ensurePlusReady() {
	return new Promise(resolve => {
		if (!isAppPlus()) {
			resolve(false)
			return
		}
		if (typeof plus !== 'undefined' && plus.io) {
			resolve(true)
			return
		}
		const done = () => resolve(typeof plus !== 'undefined' && !!plus.io)
		if (typeof document !== 'undefined' && document.addEventListener) {
			document.addEventListener('plusready', done, { once: true })
		}
		// 兜底：部分机型事件已过
		setTimeout(done, 2500)
	})
}

function ensureCacheDir() {
	return new Promise((resolve, reject) => {
		if (!isAppPlus() || typeof plus === 'undefined') {
			resolve('')
			return
		}
		plus.io.requestFileSystem(plus.io.PRIVATE_DOC, fs => {
			fs.root.getDirectory('materialCache', { create: true }, () => resolve(CACHE_DIR), reject)
		}, reject)
	})
}

function fileExists(localPath) {
	return new Promise(resolve => {
		if (!isAppPlus() || !localPath || typeof plus === 'undefined') {
			resolve(false)
			return
		}
		plus.io.resolveLocalFileSystemURL(localPath, () => resolve(true), () => {
			try {
				const abs = plus.io.convertLocalFileSystemURL(localPath)
				if (!abs) {
					resolve(false)
					return
				}
				const url = abs.indexOf('file://') === 0 ? abs : ('file://' + abs)
				plus.io.resolveLocalFileSystemURL(url, () => resolve(true), () => resolve(false))
			} catch (e) {
				resolve(false)
			}
		})
	})
}

function removeFile(localPath) {
	return new Promise(resolve => {
		if (!isAppPlus() || !localPath || typeof plus === 'undefined') {
			resolve()
			return
		}
		plus.io.resolveLocalFileSystemURL(localPath, entry => {
			entry.remove(() => resolve(), () => resolve())
		}, () => resolve())
	})
}

function downloadToLocal(url, destPath) {
	return new Promise((resolve, reject) => {
		if (!isAppPlus() || typeof plus === 'undefined') {
			reject(new Error('not app'))
			return
		}
		const task = plus.downloader.createDownload(url, { filename: destPath }, (d, status) => {
			if (status === 200) {
				const saved = (d && d.filename) || destPath
				resolve(saved)
			} else {
				reject(new Error('download failed: ' + status))
			}
		})
		task.start()
	})
}

/** video/image 本地播放路径：优先 _doc 相对路径；必要时补 file:// */
function toPlayableUrl(localPath) {
	if (!localPath) return ''
	if (!isAppPlus() || typeof plus === 'undefined') return localPath
	// uni-app 原生 video 对 _doc/ 相对路径支持最好（需 runmode=liberate）
	if (String(localPath).indexOf('_doc/') === 0 || String(localPath).indexOf('_documents/') === 0) {
		return localPath
	}
	try {
		let abs = plus.io.convertLocalFileSystemURL(localPath)
		if (!abs) return localPath
		if (abs.indexOf('file://') === 0) return abs
		if (abs.charAt(0) === '/') return 'file://' + abs
		return abs
	} catch (e) {
		return localPath
	}
}

function needsUpdate(entry, material, field) {
	if (!entry) return true
	const remotePath = material[field]
	if (!remotePath) return false
	if (entry[field] !== remotePath) return true
	const remoteTime = material.addtime ? String(material.addtime) : ''
	if (remoteTime && entry.addtime !== remoteTime) return true
	return !entry['local' + (field === 'cover' ? 'Cover' : 'Video')]
}

function getStatus() {
	const manifest = readManifest()
	const items = manifest.items || {}
	const ids = Object.keys(items)
	let cachedVideo = 0
	let cachedCover = 0
	ids.forEach(id => {
		const it = items[id]
		if (it && it.localVideo) cachedVideo++
		if (it && it.localCover) cachedCover++
	})
	return {
		syncing,
		lastSyncAt: manifest.lastSyncAt || 0,
		remoteTotal: manifest.remoteTotal || ids.length,
		cachedVideo,
		cachedCover,
		total: ids.length
	}
}

function getLocalPath(materialId, field) {
	const manifest = readManifest()
	const entry = manifest.items && manifest.items[String(materialId)]
	if (!entry) return ''
	const key = field === 'cover' ? 'localCover' : 'localVideo'
	return entry[key] || ''
}

function getPlayableVideo(materialId) {
	const local = getLocalPath(materialId, 'video')
	return local ? toPlayableUrl(local) : ''
}

function getCoverUrl(material, baseUrl) {
	if (!material) return ''
	// H5 / 非 App：不要读本地缓存路径（manifest 可能残留无效路径）
	if (isAppPlus()) {
		const local = getLocalPath(material.id, 'cover')
		if (local) return toPlayableUrl(local)
	}
	return fullUrl(material.cover, baseUrl)
}

function getVideoUrl(material, baseUrl) {
	if (!material) return ''
	if (isAppPlus()) {
		const local = getPlayableVideo(material.id)
		if (local) return local
	}
	return fullUrl(material.video, baseUrl)
}

function invalidate(materialId, field) {
	const manifest = readManifest()
	const id = String(materialId)
	const entry = manifest.items[id]
	if (!entry) return
	if (!field || field === 'video') {
		const old = entry.localVideo
		entry.localVideo = ''
		if (old) removeFile(old)
	}
	if (!field || field === 'cover') {
		const old = entry.localCover
		entry.localCover = ''
		if (old) removeFile(old)
	}
	writeManifest(manifest)
}

async function cacheOne(material, baseUrl, manifest, field) {
	if (!material || !material.id) return
	const id = String(material.id)
	const remotePath = material[field]
	if (!remotePath) return

	const entry = manifest.items[id] || {}
	const localKey = field === 'cover' ? 'localCover' : 'localVideo'
	if (!needsUpdate(entry, material, field)) {
		if (entry[localKey] && await fileExists(entry[localKey])) return
	}

	const dest = localFilePath(id, field, remotePath)
	const url = fullUrl(remotePath, baseUrl)
	try {
		if (entry.localVideo && field === 'video' && entry.video !== remotePath) {
			await removeFile(entry.localVideo)
		}
		if (entry.localCover && field === 'cover' && entry.cover !== remotePath) {
			await removeFile(entry.localCover)
		}
		const saved = await downloadToLocal(url, dest)
		if (!(await fileExists(saved))) {
			throw new Error('downloaded file missing: ' + saved)
		}
		const next = manifest.items[id] || {}
		next.id = material.id
		next.video = material.video || next.video || ''
		next.cover = material.cover || next.cover || ''
		next.addtime = material.addtime ? String(material.addtime) : (next.addtime || '')
		if (field === 'video') next.localVideo = saved
		if (field === 'cover') next.localCover = saved
		manifest.items[id] = next
	} catch (e) {
		console.warn('[materialCache] download fail', id, field, url, e)
	}
}

async function runQueue(materials, baseUrl, manifest, options) {
	const jobs = []
	materials.forEach(m => {
		if (m.video) jobs.push({ m, field: 'video' })
		if (m.cover) jobs.push({ m, field: 'cover' })
	})
	let index = 0
	let done = 0

	async function worker() {
		while (index < jobs.length) {
			const job = jobs[index++]
			await cacheOne(job.m, baseUrl, manifest, job.field)
			done++
			// 进度落盘，避免中途杀进程全丢
			if (done % 3 === 0) {
				writeManifest(manifest)
			} else if (!options.silent) {
				notify()
			}
		}
	}

	if (!jobs.length) return
	const workers = []
	const n = Math.min(MAX_CONCURRENT, jobs.length)
	for (let i = 0; i < n; i++) workers.push(worker())
	await Promise.all(workers)
}

async function cleanupRemoved(manifest, remoteIds) {
	const removed = Object.keys(manifest.items).filter(id => remoteIds.indexOf(id) < 0)
	for (const id of removed) {
		const entry = manifest.items[id]
		if (entry) {
			await removeFile(entry.localVideo)
			await removeFile(entry.localCover)
		}
		delete manifest.items[id]
	}
}

async function fetchAllMaterials(api) {
	// 只缓存上架素材（与选片页一致）
	const res = await api.list('hyMaterial', { status: '上架' })
	let list = (res && res.data) || []
	if (!Array.isArray(list)) list = []
	if (list.length === 0) {
		const pageRes = await api.page('hyMaterial', { page: 1, limit: 500, status: '上架' })
		list = (pageRes.data && pageRes.data.list) || []
	}
	return list
}

function detectUpdates(materials) {
	const manifest = readManifest()
	const remoteIds = []
	let newCount = 0
	let changedCount = 0

	materials.forEach(m => {
		const id = String(m.id)
		remoteIds.push(id)
		const entry = manifest.items[id]
		if (!entry) {
			newCount++
			return
		}
		if (needsUpdate(entry, m, 'video') || needsUpdate(entry, m, 'cover')) {
			changedCount++
		}
	})

	const removedCount = Object.keys(manifest.items).filter(id => remoteIds.indexOf(id) < 0).length
	return { newCount, changedCount, removedCount, hasUpdates: newCount + changedCount + removedCount > 0 }
}

async function syncMaterials(api, baseUrl, options = {}) {
	const ready = await ensurePlusReady()
	if (!ready) {
		return getStatus()
	}
	await ensureCacheDir()
	const materials = await fetchAllMaterials(api)
	const manifest = readManifest()
	const remoteIds = materials.map(m => String(m.id))

	await cleanupRemoved(manifest, remoteIds)
	await runQueue(materials, baseUrl || base.url, manifest, options)

	manifest.lastSyncAt = Date.now()
	manifest.remoteTotal = materials.length
	writeManifest(manifest)

	if (!options.silent) {
		const st = getStatus()
		uni.showToast({
			title: `素材已缓存 ${st.cachedVideo}/${st.remoteTotal}`,
			icon: 'none',
			duration: 2500
		})
	}
	return getStatus()
}

function startSync(api, baseUrl, options = {}) {
	if (syncPromise) return syncPromise
	if (!isAppPlus()) {
		if (!options.silent) {
			uni.showToast({ title: '请在 Pad App 中使用本地缓存', icon: 'none' })
		}
		return Promise.resolve(getStatus())
	}
	syncing = true
	notify()
	syncPromise = syncMaterials(api, baseUrl, options)
		.catch(err => {
			console.warn('[materialCache] sync error', err)
			if (!options.silent) {
				uni.showToast({ title: '素材同步失败，将使用在线播放', icon: 'none' })
			}
			return getStatus()
		})
		.finally(() => {
			syncing = false
			syncPromise = null
			notify()
		})
	return syncPromise
}

function prefetchList(materials, baseUrl, limit) {
	if (!isAppPlus() || !Array.isArray(materials) || materials.length === 0) return Promise.resolve()
	return ensurePlusReady().then(ready => {
		if (!ready) return
		const slice = materials.slice(0, limit || 5)
		const manifest = readManifest()
		return runQueue(slice, baseUrl || base.url, manifest, { silent: true }).then(() => {
			writeManifest(manifest)
		})
	}).catch(() => {})
}

function prefetchOne(material, baseUrl) {
	if (!material) return
	prefetchList([material], baseUrl, 1)
}

export default {
	startSync,
	syncMaterials,
	prefetchList,
	prefetchOne,
	getStatus,
	getLocalPath,
	getPlayableVideo,
	getCoverUrl,
	getVideoUrl,
	invalidate,
	detectUpdates,
	onStatusChange,
	isAppPlus,
	ensurePlusReady
}
