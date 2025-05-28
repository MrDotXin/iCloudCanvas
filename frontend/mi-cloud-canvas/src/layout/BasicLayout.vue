<template>
    <a-layout class="basic-layout" style="min-height: 100vh;">
        <!-- 导航栏 -->
        <div v-if="!route.fullPath.startsWith('/backend')">
            <a-layout-header class="custom-header" >
                <GlobalHeader />
            </a-layout-header>
            <!-- 内容区域 -->
            <a-layout-content class="content">
                <router-view></router-view>
            </a-layout-content>
        </div>
        <div v-else>
            <router-view></router-view>
        </div>
        <!-- 页脚 -->
        <a-footer class="custom-footer">
            <a-space direction="vertical">
                <span style="color: white;">©2024 我的云画布. 保留所有权利.</span> 
                <span><a href="https://github.com/MrDotXin" target="_blank">By MrDotXin</a></span>
            </a-space>
        </a-footer>
    </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue';
import { useRoute } from 'vue-router'

const route = useRoute();
</script>

<style scoped>

.basic-layout {
    min-height: 100vh;
    background: #f0f2f5; /* 添加整体背景色 */
    display: flex;
    flex-direction: column;
}

.custom-header {
    height: 60px; /* 增加高度 */
    line-height: 60px; /* 使菜单垂直居中 */
    background-color: white;
}

.custom-menu {
    line-height: 70px; /* 使菜单垂直居中 */
    background: transparent; /* 菜单背景透明 */
}

.custom-menu .ant-menu-item {
    font-size: 16px; /* 增大菜单项字体大小 */
    border-radius: 4px; /* 为菜单项添加圆角 */
}

.custom-menu .ant-menu-item:hover {
    background-color: rgba(255, 255, 255, 0.1); /* 菜单项悬停背景色 */
}

.content {
    padding: 24px;
    background: #fff;
    margin: 10px;
    border-radius: 8px; /* 添加圆角 */
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); /* 添加阴影 */
    min-height: 100vh;
}

.custom-footer {
    text-align: center;
    background: #02020f; /* 页脚背景色与整体背景一致 */
    padding: 24px;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1); /* 添加顶部阴影 */
}
</style>
