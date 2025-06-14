import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { ACCESS_ENUM } from '@/enum/AccessEnum'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/home',
    },
    {
      path: '/home',
      name: '主页',
      component: HomeView,
      meta:{
        show: true
      }
    },
    {
      path: '/about',
      name: '关于',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
      meta:{
        show: true
      }
    },  
    {
      path: '/login',
      name: '登录',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('@/views/UserLoginView.vue'),

    },  
    {
      path: '/register',
      name: '注册',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('@/views/UserRegisterView.vue'), 
    },  
    {
      path: '/backend/entry',
      name: '管理员后台登录',
      component: () => import('@/views/backend/BackendEntry.vue'),
    },
    {
      path: '/backend/center',
      name: '管理员后台中心',
      component: () => import('@/views/backend/BackendManage.vue'),
      meta: {
        access: ACCESS_ENUM.ADMIN
      }
    },
    {
      path: '/picture/:id',
      name: '图片信息详情页',
      component: () => import('@/views/PictureInfoView.vue'),
    },
    {
      path: '/user',
      name: '用户信息',
      meta: {
        access: ACCESS_ENUM.USER
      },
      children: [
        {
          path: '/user/space',
          name: '用户个人空间',
          component: () => import('@/views/User/UserSpace.vue'),
        }
      ]
    },
    {
      path: '/:catchAll(.*)',
      name: '404',
      component: () => import('@/views/F0FView.vue'),
      meta: {
        disableGlobalHeader: true
      }
    }
  ],
})

export default router
