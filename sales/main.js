import Vue from 'vue'
import App from './App'
import api from './api/index.js'
import base from './api/base.js'

Vue.prototype.$api = api
Vue.prototype.$base = base

/** 拼接资源完整地址 */
Vue.prototype.$img = function(path) {
	if (!path) return ''
	if (/^https?:\/\//.test(path)) return path
	return base.url + path
}

Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
	...App
})
app.$mount()
