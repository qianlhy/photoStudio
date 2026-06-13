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
