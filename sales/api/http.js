/**
 * 合意传媒 · 销售经理端 通用网络请求（基于 Promise，自动携带 Token）
 */
import base from './base'

export default {
	config: {
		baseUrl: base.url,
		header: {
			'Content-Type': 'application/json;charset=UTF-8'
		},
		data: {},
		method: "GET",
		dataType: "json",
		success() {},
		fail() {},
		complete() {}
	},
	request(options) {
		if (!options) options = {}
		options.baseUrl = options.baseUrl || this.config.baseUrl
		options.dataType = options.dataType || this.config.dataType
		options.url = options.baseUrl + options.url
		options.data = options.data || {}
		options.method = options.method || this.config.method
		let token = {
			'Token': uni.getStorageSync("token")
		}
		options.header = Object.assign({}, options.header, token)
		return new Promise((resolve, reject) => {
			options.complete = (response) => {
				let statusCode = response.statusCode
				if (statusCode === 200) {
					var rs = response.data;
					if (rs.code === 0) {
						resolve(response.data);
					} else if (rs.code == 401) {
						uni.reLaunch({
							url: '/pages/login/login'
						})
					} else {
						uni.showToast({
							title: rs.msg,
							icon: 'none',
							duration: 2000
						});
						reject(rs)
					}
				} else {
					uni.showToast({
						title: "接口执行异常",
						icon: 'none',
						duration: 2000
					});
					reject(response)
				}
			}
			uni.request(options);
		});
	},
	get(url, data, options) {
		options = options || {}
		options.url = url
		options.data = data
		options.method = 'GET'
		return this.request(options)
	},
	post(url, data, options) {
		options = options || {}
		options.url = url
		options.data = data
		options.method = 'POST'
		return this.request(options)
	}
}
