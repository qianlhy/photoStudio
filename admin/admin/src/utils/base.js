const base = {
    get() {
        return {
            // 后台页面走 https 时，上传/接口也必须 https，否则浏览器会拦截混合内容
            url: "https://hycm.baibaiyeye.com.cn/ssm48yhg/",
            name: "ssm48yhg",
            indexUrl: 'https://hycm.baibaiyeye.com.cn/ssm48yhg/front/h5/index.html'
        };
    },
    getProjectName() {
        return {
            projectName: "合意传媒"
        }
    }
}
export default base
