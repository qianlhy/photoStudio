const listeners = []
let lastW = 0
let lastH = 0

function getOrientation() {
	try {
		const info = uni.getSystemInfoSync()
		return info.windowWidth > info.windowHeight ? 'landscape' : 'portrait'
	} catch (e) {
		return 'landscape'
	}
}

function notify() {
	const orientation = getOrientation()
	listeners.forEach(fn => {
		try { fn(orientation) } catch (e) {}
	})
}

function onOrientationChange(fn) {
	if (typeof fn !== 'function') return () => {}
	listeners.push(fn)
	return () => {
		const i = listeners.indexOf(fn)
		if (i >= 0) listeners.splice(i, 1)
	}
}

function bindResize() {
	try {
		const info = uni.getSystemInfoSync()
		lastW = info.windowWidth
		lastH = info.windowHeight
	} catch (e) {}
	uni.onWindowResize(res => {
		const w = res.size.windowWidth
		const h = res.size.windowHeight
		const wasLandscape = lastW >= lastH
		const isLandscape = w >= h
		lastW = w
		lastH = h
		if (wasLandscape !== isLandscape) notify()
	})
}

bindResize()

export default {
	onOrientationChange,
	getOrientation
}
