import Vue from 'vue';
//配置路由
import VueRouter from 'vue-router'
//1.创建组件
import Index from '@/views/index'
import Home from '@/views/home'
import Login from '@/views/login'
import NotFound from '@/views/404'
import UpdatePassword from '@/views/update-password'
import pay from '@/views/pay'
import register from '@/views/register'
import center from '@/views/center'
import leixing from '@/views/modules/leixing/list'
import news from '@/views/modules/news/list'
import cehuashi from '@/views/modules/cehuashi/list'
import storeup from '@/views/modules/storeup/list'
import yonghu from '@/views/modules/yonghu/list'
import config from '@/views/modules/config/list'
import taocan from '@/views/modules/taocan/list'
import dingdan from '@/views/modules/dingdan/list'
import chengpin from '@/views/modules/chengpin/list'
import message from '@/views/modules/message/list'
import storeconfig from '@/views/modules/storeconfig/list'
import users from '@/views/modules/users/list'
// 合意传媒三端系统 · 管理后台模块
import hyCustomer from '@/views/modules/hyCustomer/list'
import hyOrder from '@/views/modules/hyOrder/list'
import hyMaterial from '@/views/modules/hyMaterial/list'
import hyDeliverable from '@/views/modules/hyDeliverable/list'
import hyEmployee from '@/views/modules/hyEmployee/list'

Vue.use(VueRouter);


//2.配置路由   注意：名字
const routes = [{
    path: '/index',
    name: '首页',
    component: Index,
    children: [{
        // 这里不设置值，是把main作为默认页面
        path: '/',
        name: '首页',
        component: Home,
        meta: {icon: '', title: 'center'}
    }, {
        path: '/updatePassword',
        name: '修改密码',
        component: UpdatePassword,
        meta: {icon: '', title: 'updatePassword'}
    }, {
        path: '/pay',
        name: '支付',
        component: pay,
        meta: {icon: '', title: 'pay'}
    }, {
        path: '/center',
        name: '个人信息',
        component: center,
        meta: {icon: '', title: 'center'}
    }
        , {
            path: '/leixing',
            name: '类型',
            component: leixing
        }
        , {
            path: '/news',
            name: '公告信息',
            component: news
        }
        , {
            path: '/cehuashi',
            name: '摄影师',
            component: cehuashi
        }
        , {
            path: '/storeup',
            name: '我的收藏管理',
            component: storeup
        }
        , {
            path: '/yonghu',
            name: '用户',
            component: yonghu
        }
        , {
            path: '/config',
            name: '轮播图管理',
            component: config
        }
        , {
            path: '/taocan',
            name: '套餐管理',
            component: taocan
        }
        , {
            path: '/dingdan',
            name: '订单管理',
            component: dingdan
        }
        , {
            path: '/chengpin',
            name: '成品管理',
            component: chengpin
        }
        , {
            path: '/message',
            name: '系统消息',
            component: message
        }
        , {
            path: '/storeconfig',
            name: '门店与规则配置',
            component: storeconfig
        }
        , {
            path: '/users',
            name: '管理员账号',
            component: users
        }
        , {
            path: '/hyCustomer',
            name: '客户管理',
            component: hyCustomer
        }
        , {
            path: '/hyOrder',
            name: '订单管理',
            component: hyOrder
        }
        , {
            path: '/hyMaterial',
            name: '素材内容',
            component: hyMaterial
        }
        , {
            path: '/hyDeliverable',
            name: '成品与优质作品',
            component: hyDeliverable
        }
        , {
            path: '/hyEmployee',
            name: '员工与系统',
            component: hyEmployee
        }
    ]
},
    {
        path: '/login',
        name: 'login',
        component: Login,
        meta: {icon: '', title: 'login'}
    },
    {
        path: '/register',
        name: 'register',
        component: register,
        meta: {icon: '', title: 'register'}
    },
    {
        path: '/',
        name: '首页',
        redirect: '/index'
    }, /*默认跳转路由*/
    {
        path: '*',
        component: NotFound
    }
]
//3.实例化VueRouter  注意：名字
const router = new VueRouter({
    mode: 'hash',
    /*hash模式改为history*/
    routes // （缩写）相当于 routes: routes
})

export default router;
