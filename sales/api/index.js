import http from './http'
import base from './base'

/** 登录校验 */
export const auth = () => {
	if (!uni.getStorageSync("token")) {
		uni.reLaunch({
			url: '/pages/login/login'
		})
		return false
	}
	return true
}

/** 员工统一登录（后端以 @RequestParam 读取，故拼到 query） */
export const login = (data) => {
	const qs = `username=${encodeURIComponent(data.username)}&password=${encodeURIComponent(data.password)}`
	return http.post(`hyEmployee/login?${qs}`, {})
}

/** 当前登录员工 */
export const session = () => {
	return http.get(`hyEmployee/session`)
}

/** 退出 */
export const logout = () => {
	return http.get(`hyEmployee/logout`)
}

/** 列表分页：page(表名, {page,limit,...过滤}) */
export const page = (tableName, data) => {
	return http.get(`${tableName}/page`, data)
}

/** 列表：list(表名, {过滤}) */
export const list = (tableName, data) => {
	return http.get(`${tableName}/list`, data)
}

/** 详情：info(表名, id) -> /detail/{id} */
export const info = (tableName, id) => {
	return http.get(`${tableName}/info/${id}`)
}

/** 保存 */
export const save = (tableName, data) => {
	return http.post(`${tableName}/save`, data)
}

/** 更新 */
export const update = (tableName, data) => {
	return http.post(`${tableName}/update`, data)
}

/** 删除 */
export const del = (tableName, ids) => {
	return http.post(`${tableName}/delete`, ids)
}

/** 通用 GET（自定义接口） */
export const get = (url, data) => {
	return http.get(url, data)
}

/** 通用 POST（自定义接口） */
export const post = (url, data) => {
	return http.post(url, data)
}

/** 品牌/全局配置 */
export const config = () => {
	return http.get(`hyConfig/all`)
}

/** 上传文件，返回 file 字段 */
export const upload = (filePath, callback) => {
	uni.uploadFile({
		url: `${base.url}file/upload`,
		filePath: filePath,
		name: 'file',
		header: {
			'Token': uni.getStorageSync("token")
		},
		success: (res) => {
			let result = JSON.parse(res.data)
			if (result.code == 0) {
				callback(result)
			} else {
				uni.showToast({
					title: result.msg,
					icon: 'none'
				})
			}
		}
	})
}

export default {
	auth,
	login,
	session,
	logout,
	page,
	list,
	info,
	save,
	update,
	del,
	get,
	post,
	config,
	upload
}
