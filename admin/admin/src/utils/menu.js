const menu = {
    list() {
        return [{
            "backMenu": [{
                "child": [{
                    "appFrontIcon": "cuIcon-send",
                    "buttons": ["新增", "查看", "修改", "删除", "审核"],
                    "menu": "用户",
                    "menuJump": "列表",
                    "tableName": "yonghu"
                }], "menu": "用户管理"
            }, {
                "child": [{
                    "appFrontIcon": "cuIcon-qrcode",
                    "buttons": ["新增", "查看", "修改", "删除"],
                    "menu": "摄影师/策划师",
                    "menuJump": "列表",
                    "tableName": "cehuashi"
                }], "menu": "摄影师管理"
            }, {
                "child": [{
                    "appFrontIcon": "cuIcon-pic",
                    "buttons": ["新增", "查看", "修改", "删除"],
                    "menu": "套餐",
                    "menuJump": "列表",
                    "tableName": "taocan"
                }], "menu": "套餐管理"
            }, {
                "child": [{
                    "appFrontIcon": "cuIcon-form",
                    "buttons": ["查看", "修改", "删除"],
                    "menu": "订单",
                    "menuJump": "列表",
                    "tableName": "dingdan"
                }], "menu": "订单管理"
            }, {
                "child": [{
                    "appFrontIcon": "cuIcon-cardboardfill",
                    "buttons": ["新增", "查看", "修改", "删除"],
                    "menu": "成品",
                    "menuJump": "列表",
                    "tableName": "chengpin"
                }], "menu": "成品管理"
            }, {
                "child": [{
                    "appFrontIcon": "cuIcon-newshot",
                    "buttons": ["新增", "查看", "修改", "删除"],
                    "menu": "风格分类",
                    "menuJump": "列表",
                    "tableName": "leixing"
                }], "menu": "风格分类管理"
            }, {
                "child": [{
                    "appFrontIcon": "cuIcon-message",
                    "buttons": ["查看", "删除"],
                    "menu": "系统消息",
                    "menuJump": "列表",
                    "tableName": "message"
                }], "menu": "系统消息管理"
            }, {
                "child": [{
                    "appFrontIcon": "cuIcon-form",
                    "buttons": ["查看", "修改"],
                    "menu": "轮播图管理",
                    "tableName": "config"
                }, {
                    "appFrontIcon": "cuIcon-shop",
                    "buttons": ["查看", "修改"],
                    "menu": "门店与规则配置",
                    "tableName": "storeconfig"
                }, {
                    "appFrontIcon": "cuIcon-news",
                    "buttons": ["新增", "查看", "修改", "删除"],
                    "menu": "公告信息",
                    "tableName": "news"
                }], "menu": "系统管理"
            }],
            "frontMenu": [],
            "hasBackLogin": "是",
            "hasBackRegister": "否",
            "hasFrontLogin": "否",
            "hasFrontRegister": "否",
            "roleName": "管理员",
            "tableName": "users"
        }, {
            "backMenu": [{
                "child": [{
                    "appFrontIcon": "cuIcon-form",
                    "buttons": ["查看"],
                    "menu": "我的订单",
                    "menuJump": "列表",
                    "tableName": "dingdan"
                }], "menu": "我的订单"
            }, {
                "child": [{
                    "appFrontIcon": "cuIcon-favor",
                    "buttons": ["查看", "删除"],
                    "menu": "我的收藏管理",
                    "tableName": "storeup"
                }], "menu": "我的收藏管理"
            }],
            "frontMenu": [],
            "hasBackLogin": "否",
            "hasBackRegister": "否",
            "hasFrontLogin": "是",
            "hasFrontRegister": "是",
            "roleName": "用户",
            "tableName": "yonghu"
        }, {
            "backMenu": [{
                "child": [{
                    "appFrontIcon": "cuIcon-pic",
                    "buttons": ["查看"],
                    "menu": "套餐",
                    "menuJump": "列表",
                    "tableName": "taocan"
                }], "menu": "套餐管理"
            }, {
                "child": [{
                    "appFrontIcon": "cuIcon-form",
                    "buttons": ["查看", "修改"],
                    "menu": "订单",
                    "menuJump": "列表",
                    "tableName": "dingdan"
                }], "menu": "订单管理"
            }, {
                "child": [{
                    "appFrontIcon": "cuIcon-cardboardfill",
                    "buttons": ["新增", "查看", "修改", "删除"],
                    "menu": "成品",
                    "menuJump": "列表",
                    "tableName": "chengpin"
                }], "menu": "成品管理"
            }, {
                "child": [{
                    "appFrontIcon": "cuIcon-newshot",
                    "buttons": ["查看"],
                    "menu": "风格分类",
                    "menuJump": "列表",
                    "tableName": "leixing"
                }], "menu": "风格分类管理"
            }],
            "frontMenu": [],
            "hasBackLogin": "是",
            "hasBackRegister": "否",
            "hasFrontLogin": "否",
            "hasFrontRegister": "否",
            "roleName": "策划师",
            "tableName": "cehuashi"
        }]
    }
}
export default menu;
