const menu = {
    list() {
        return [{
            "backMenu": [{
                "child": [{
                    "appFrontIcon": "cuIcon-friend",
                    "buttons": ["新增", "查看", "修改", "删除", "划拨"],
                    "menu": "客户管理",
                    "menuJump": "列表",
                    "tableName": "hyCustomer"
                }], "menu": "客户管理"
            }, {
                "child": [{
                    "appFrontIcon": "cuIcon-form",
                    "buttons": ["查看", "标记", "详情"],
                    "menu": "订单管理",
                    "menuJump": "列表",
                    "tableName": "hyOrder"
                }], "menu": "订单管理"
            }, {
                "child": [{
                    "appFrontIcon": "cuIcon-video",
                    "buttons": ["新增", "查看", "修改", "删除", "上下架"],
                    "menu": "素材内容",
                    "menuJump": "列表",
                    "tableName": "hyMaterial"
                }], "menu": "素材内容"
            }, {
                "child": [{
                    "appFrontIcon": "cuIcon-medal",
                    "buttons": ["查看", "标记", "复用"],
                    "menu": "成品与优质作品",
                    "menuJump": "列表",
                    "tableName": "hyDeliverable"
                }], "menu": "成品与优质作品"
            }, {
                "child": [{
                    "appFrontIcon": "cuIcon-people",
                    "buttons": ["新增", "查看", "修改", "删除"],
                    "menu": "员工与系统",
                    "menuJump": "列表",
                    "tableName": "hyEmployee"
                }, {
                    "appFrontIcon": "cuIcon-settings",
                    "buttons": ["查看", "修改"],
                    "menu": "管理员账号",
                    "tableName": "users"
                }], "menu": "员工与系统"
            }],
            "frontMenu": [],
            "hasBackLogin": "是",
            "hasBackRegister": "否",
            "hasFrontLogin": "否",
            "hasFrontRegister": "否",
            "roleName": "管理员",
            "tableName": "users"
        }]
    }
}
export default menu;
