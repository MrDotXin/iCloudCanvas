import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ACCESS_ENUM from '@/enum/AccessEnum'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/home',
      meta:{
        hidden: true
      }
    },
    {
      path: '/home',
      name: '主页',
      component: HomeView,
    },
    {
      path: '/about',
      name: '关于',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },  
    {
      path: '/login',
      name: '登录',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('@/views/UserLoginView.vue'),
      meta:{
        hidden: true
      }
    },  
    {
      path: '/register',
      name: '注册',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('@/views/UserRegisterView.vue'),
      meta:{
        hidden: true
      } 
    },  
    {
      path: '/backend/entry',
      name: '管理员后台登录',
      component: () => import('@/views/backend/BackendEntry.vue'),
      meta: {
        hidden: true
      }
    },
    {
      path: '/backend/center',
      name: '管理员后台中心',
      component: () => import('@/views/backend/BackendManage.vue'),
      meta: {
        hidden: true,
        access: ACCESS_ENUM.ADMIN
      }
    },
    {
      path: '/picture/:id',
      name: '图片信息详情页',
      component: () => import('@/views/PictureInfoView.vue'),
      meta: {
        hidden: true,
      }
    },
    {
      path: '/:catchAll(.*)',
      name: '404',
      component: () => import('@/views/F0FView.vue'),
      meta: {
        hidden: true
      }
    }
  ],
})

export default router
