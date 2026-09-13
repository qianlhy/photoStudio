/** H5 临时：浏览器强制竖屏预览（测完可删） */
const KEY = 'hyForcePortrait'

export function applyForcePortrait(on) {
	// #ifdef H5
	if (typeof document === 'undefined') return
	document.documentElement.classList.toggle('force-portrait', !!on)
	try {
		sessionStorage.setItem(KEY, on ? '1' : '0')
	} catch (e) {}
	// #endif
}

export function restoreForcePortrait() {
	// #ifdef H5
	if (typeof document === 'undefined') return false
	let on = false
	try {
		on = sessionStorage.getItem(KEY) === '1'
	} catch (e) {}
	applyForcePortrait(on)
	return on
	// #endif
	// #ifndef H5
	return false
	// #endif
}
