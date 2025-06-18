import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            redirect: '/manager/home'
        },
        //   每当访问根目录就重定向到这个界面
        {
            path: '/manager',
            component: () => import('../views/Manager.vue'),
            children: [
                {
                    path: 'home',
                    meta: {name: '主页'},
                    component: () => import('../views/Home.vue')
                },
                {
                    path: 'admin',
                    meta: {name: '管理员信息'},
                    component: () => import('../views/Admin.vue')
                },
                {
                    path: 'user',
                    meta: {name: '普通用户信息'},
                    component: () => import('../views/User.vue')
                },
                {
                    path: 'supplier',
                    meta: {name: '供应商信息'},
                    component: () => import('../views/Supplier.vue')
                },
                {
                    path: 'person',
                    meta: {name: '个人中心'},
                    component: () => import('../views/Person.vue')
                },
                {
                    path: 'updatePassword',
                    meta: {name: '修改密码'},
                    component: () => import('../views/UpdatePassword.vue')
                },
                {
                    path: 'notice',
                    meta: {name: '系统公告'},
                    component: () => import('../views/Notice.vue')
                },
                {
                    path: 'introduction',
                    meta: {name: '鸣潮攻略'},
                    component: () => import('../views/Introduction.vue')
                },
                {
                    path: 'goods',
                    meta: {name: '商品信息'},
                    component: () => import('../views/Goods.vue')
                },
                {
                    path: 'order',
                    meta: {name: '订单信息'},
                    component: () => import('../views/Order.vue')
                },
                {
                    path: 'category',
                    meta: {name: '攻略分类'},
                    component: () => import('../views/Category.vue')
                },
                {
                    path: 'recharge',
                    meta: {name: '充值中心'},
                    component: () => import('../views/Recharge.vue')
                },

            ]
        },
        {
            path: '/login',
            component: () => import('../views/Login.vue')
        },
        {
            path: '/register',
            component: () => import('../views/Register.vue')
        },
        {
            path: '/notFound',
            component: () => import('../views/404.vue')
        },
        {
            path: '/:pathMatch(.*)',
            redirect: '/notFound' // 注意这里改为 /notFound
        },
    ],
})

export default router