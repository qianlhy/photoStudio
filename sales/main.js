import Vue from 'vue'
import App from './App'
import api from './api/index.js'
import base from './api/base.js'
import materialCache from './utils/materialCache.js'

Vue.prototype.$api = api
Vue.prototype.$base = base
Vue.prototype.$materialCache = materialCache

/** 拼接资源完整地址 */
Vue.prototype.$img = function(path) {
	if (!path) return ''
	if (/^https?:\/\//.test(path)) return path
	return base.url + path
}

/** 素材资源：优先本地缓存，否则走远程 */
Vue.prototype.$media = function(material, field) {
	if (!material) return ''
	const f = field === 'cover' ? 'cover' : 'video'
	if (f === 'cover') return materialCache.getCoverUrl(material, base.url)
	return materialCache.getVideoUrl(material, base.url)
}

Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
	...App
})
app.$mount()
