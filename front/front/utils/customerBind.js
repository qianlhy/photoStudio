/** 客户档案绑定：按当前登录手机号匹配 hy_customer，避免 hyCustomerId 串号 */

export function normalizePhone(phone) {
	if (!phone) return ''
	return String(phone).replace(/\D/g, '')
}

export function phoneMatches(userPhone, customerPhone) {
	if (!userPhone || !customerPhone) return false
	if (userPhone === customerPhone) return true
	const ud = normalizePhone(userPhone)
	const cd = String(customerPhone)
	if (cd.includes('*') && ud.length >= 7) {
		const star = cd.indexOf('*')
		const lastStar = cd.lastIndexOf('*')
		const prefix = cd.substring(0, star)
		const suffix = cd.substring(lastStar + 1)
		return ud.startsWith(normalizePhone(prefix)) && ud.endsWith(normalizePhone(suffix))
	}
	const cdNorm = normalizePhone(cd)
	return ud.length >= 4 && cdNorm.length >= 4 && ud.endsWith(cdNorm.slice(-4))
}

export function clearCustomerCache() {
	uni.removeStorageSync('hyCustomerId')
	uni.removeStorageSync('hyCustomerPhone')
	uni.removeStorageSync('hyCustomerName')
}

export function saveCustomerCache(customer) {
	if (!customer || !customer.id) return
	uni.setStorageSync('hyCustomerId', customer.id)
	if (customer.phone) uni.setStorageSync('hyCustomerPhone', customer.phone)
	if (customer.name) uni.setStorageSync('hyCustomerName', customer.name)
}

export function bindCustomerByPhone(vm) {
	const table = uni.getStorageSync('nowTable') || 'yonghu'
	return vm.$api.session(table).then(res => {
		const phone = (res.data && res.data.shoujihaoma) || ''
		if (!phone) return Promise.reject({ msg: '请先完善手机号' })
		return new Promise((resolve, reject) => {
			uni.request({
				url: vm.$base.url + 'hyCustomer/bindByPhone',
				method: 'GET',
				data: { phone },
				header: { Token: uni.getStorageSync('token') },
				success: (r) => {
					const body = r.data || {}
					if (body.code === 0 && body.data && body.data.id) {
						saveCustomerCache(body.data)
						resolve(body.data)
					} else {
						reject(body)
					}
				},
				fail: reject
			})
		})
	})
}

/** 确保当前登录用户与缓存客户一致，不一致则按手机号重绑 */
export function ensureCustomerBound(vm) {
	const cachedId = uni.getStorageSync('hyCustomerId')
	const cachedPhone = uni.getStorageSync('hyCustomerPhone')
	const table = uni.getStorageSync('nowTable') || 'yonghu'
	return vm.$api.session(table).then(res => {
		const sessionPhone = (res.data && res.data.shoujihaoma) || ''
		if (!sessionPhone) return Promise.reject({ msg: '请先完善手机号' })
		if (cachedId && cachedPhone && phoneMatches(sessionPhone, cachedPhone)) {
			return { id: cachedId, phone: cachedPhone, name: uni.getStorageSync('hyCustomerName') || '' }
		}
		if (cachedId) {
			return vm.$api.list('hyCustomer', { id: cachedId }).then(r => {
				const row = (r.data && r.data[0]) || null
				if (row && row.phone && phoneMatches(sessionPhone, row.phone)) {
					saveCustomerCache(row)
					return row
				}
				clearCustomerCache()
				return bindCustomerByPhone(vm)
			})
		}
		return bindCustomerByPhone(vm)
	})
}
