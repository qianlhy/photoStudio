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
	return (baseUrl || base.url) + path
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

function isAppPlus() {
	// #ifdef APP-PLUS
	return typeof plus !== 'undefined'
	// #endif
	// #ifndef APP-PLUS
	return false
	// #endif
}

function ensureCacheDir() {
	return new Promise((resolve, reject) => {
		if (!isAppPlus()) {
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
		if (!isAppPlus() || !localPath) {
			resolve(false)
			return
		}
		plus.io.resolveLocalFileSystemURL(localPath, () => resolve(true), () => resolve(false))
	})
}

function removeFile(localPath) {
	return new Promise(resolve => {
		if (!isAppPlus() || !localPath) {
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
		if (!isAppPlus()) {
			reject(new Error('not app'))
			return
		}
		const task = plus.downloader.createDownload(url, { filename: destPath }, (d, status) => {
			if (status === 200) resolve(destPath)
			else reject(new Error('download failed: ' + status))
		})
		task.start()
	})
}

function toPlayableUrl(localPath) {
	if (!localPath) return ''
	if (!isAppPlus()) return localPath
	try {
		return plus.io.convertLocalFileSystemURL(localPath)
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
	const local = getLocalPath(material.id, 'cover')
	if (local) return toPlayableUrl(local)
	return fullUrl(material.cover, baseUrl)
}

function getVideoUrl(material, baseUrl) {
	if (!material) return ''
	const local = getPlayableVideo(material.id)
	if (local) return local
	return fullUrl(material.video, baseUrl)
}

function invalidate(materialId, field) {
	const manifest = readManifest()
	const id = String(materialId)
	const entry = manifest.items[id]
	if (!entry) return
	if (!field || field === 'video') {
		entry.localVideo = ''
	}
	if (!field || field === 'cover') {
		entry.localCover = ''
	}
	writeManifest(manifest)
}

async function cacheOne(material, baseUrl, manifest, field) {
	if (!material || !material.id) return
	const id = String(material.id)
	const remotePath = material[field]
	if (!remotePath) return

	const entry = manifest.items[id] || {}
	if (!needsUpdate(entry, material, field)) {
		const localKey = field === 'cover' ? 'localCover' : 'localVideo'
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
		await downloadToLocal(url, dest)
		const next = manifest.items[id] || {}
		next.id = material.id
		next.video = material.video || next.video || ''
		next.cover = material.cover || next.cover || ''
		next.addtime = material.addtime ? String(material.addtime) : (next.addtime || '')
		if (field === 'video') next.localVideo = dest
		if (field === 'cover') next.localCover = dest
		manifest.items[id] = next
	} catch (e) {
		console.warn('[materialCache] download fail', id, field, e)
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
	const total = jobs.length

	async function worker() {
		while (index < jobs.length) {
			const job = jobs[index++]
			await cacheOne(job.m, baseUrl, manifest, job.field)
			done++
			if (!options.silent && done % 5 === 0) {
				notify()
			}
		}
	}

	const workers = []
	const n = Math.min(MAX_CONCURRENT, Math.max(1, jobs.length))
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
	if (!isAppPlus()) {
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
			title: `素材已同步 ${st.cachedVideo}/${st.remoteTotal}`,
			icon: 'none',
			duration: 2500
		})
	}
	return getStatus()
}

function startSync(api, baseUrl, options = {}) {
	if (syncPromise) return syncPromise
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
	if (!isAppPlus() || !Array.isArray(materials) || materials.length === 0) return
	const slice = materials.slice(0, limit || 5)
	const manifest = readManifest()
	runQueue(slice, baseUrl || base.url, manifest, { silent: true }).then(() => {
		manifest.lastSyncAt = Date.now()
		writeManifest(manifest)
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
	isAppPlus
}
