<template>
    <a-layout-header class="header-nav">
        <div class="nav-container">
            <!-- 品牌标识 -->
            <div class="brand">
                <img src="@/assets/cloud.svg" class="logo" />
                <h1>ICC后台管理</h1>
            </div>


            <div class="nav-actions">
                <a-input-search placeholder="搜索" class="search-box" />

                <UserBrief />
            </div>
        </div>
    </a-layout-header>
    <a-layout class="admin-container">
        <a-layout-sider theme="light" :collapsed="isSiderCollapsed">
            <a-space direction="horizontal">
                <div class="logo" v-if="!isSiderCollapsed">管理项</div> 
                <a-button type="text" @click="handleMenuCollapse">
                    <DownOutlined v-if="!isMenuCollapsed" />
                    <UpOutlined v-else />
                </a-button>
            </a-space>
            <a-menu @select="onClickMenuItem" v-model:openKeys="openKeys" :selectedKeys="[currentTabView]"
                mode="inline">
                <a-menu-item key="Info">
                    <template #icon>
                        <UserOutlined />
                    </template>
                    首页
                </a-menu-item>
                <a-sub-menu key="2" title="功能管理">
                </a-sub-menu>
                    <template #icon>
                        <AppstoreOutlined />
                    </template>
                    <a-sub-menu key="Picture_manage" title="图片">
                        <template #icon>
                            <PictureOutlined />
                        </template>
                        <a-menu-item key="Picture">
                            <template #icon>
                                <DiffOutlined />
                            </template>
                            图片管理
                        </a-menu-item>
                        <a-menu-item key="PictureAdd">
                            <template #icon>
                                <PlusSquareOutlined />
                            </template>
                            添加新图
                        </a-menu-item>
                        <a-menu-item key="PictureScrapy">
                            <template #icon>
                                <PlusSquareOutlined />
                            </template>
                            批量抓取图片
                        </a-menu-item>
                    </a-sub-menu>
                    <a-menu-item key="UserManage"><template #icon>
                            <TeamOutlined />
                        </template>用户管理</a-menu-item>
                    <a-menu-item key="2-3"><template #icon>
                            <BookFilled />
                        </template>通讯录设置</a-menu-item>
            </a-menu>

            <a-button type="text" size="large" @click="handleSiderCollapsed">
                <MenuFoldOutlined v-if="!isSiderCollapsed" />
                <MenuUnfoldOutlined  v-else />
            </a-button>
        </a-layout-sider>

        <a-layout-content style="overflow: auto;">
            <component :is="tabViews[currentTabView as keyof typeof tabViews]" />
        </a-layout-content>
    </a-layout>
</template>

<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import InfoView from '@/views/backend/manage/InfoView.vue';
import UserManage from '@/views/backend/manage/UserManage.vue';
import { useLoginUserStore } from '@/stores/user';
import UserBrief from '@/components/UserBrief.vue';
import AddPictureView from './manage/AddPictureView.vue';
import PictureManage from './manage/PictureManage.vue';
import ScrapyPictureView from './manage/ScrapyPictureView.vue';

import {
    MenuFoldOutlined,
    MenuUnfoldOutlined,
    AppstoreOutlined,
    UserOutlined,
    PictureOutlined,
    PlusSquareOutlined,
    TeamOutlined,
    BookFilled,
    DownOutlined,
    UpOutlined,
    DiffOutlined
} from '@ant-design/icons-vue';


const userStore = useLoginUserStore();


const isMenuCollapsed = ref(false);
const isSiderCollapsed = ref(false);
const openKeys = ref(['Info']);
const preOpenKeys = ref(['Info']);
let currentTabView = ref('Info');

const tabViews = {
    'Info': InfoView,
    'Picture': PictureManage,
    "PictureAdd": AddPictureView,
    "UserManage": UserManage,
    "PictureScrapy": ScrapyPictureView
}

onMounted(() => {
    console.log(userStore.loginUser.userAvatar);
    console.log(localStorage.getItem("currentTabView"));
    console.log(localStorage.getItem("isMenuCollapsed"));
    console.log(localStorage.getItem("openKeys"));


    currentTabView.value = localStorage.getItem("currentTabView") || 'Info';
    isMenuCollapsed.value = localStorage.getItem("isMenuCollapsed") === "true";
    openKeys.value = JSON.parse(localStorage.getItem("openKeys") || "[\"Info\"]");
    preOpenKeys.value = [];

    isSiderCollapsed.value = localStorage.getItem("isSiderCollapsed") === "true";
})

const handleMenuCollapse = () => {
    isMenuCollapsed.value = !isMenuCollapsed.value;
    openKeys.value = isMenuCollapsed.value ? [] : preOpenKeys.value;

    localStorage.setItem("isMenuCollapsed", isMenuCollapsed.value.toString());
};

const handleSiderCollapsed = () => {
    isSiderCollapsed.value = !isSiderCollapsed.value;
    localStorage.setItem("isSiderCollapsed", isSiderCollapsed.value.toString());
}

watch(
    () => openKeys.value,
    (_, oldVal) => {
        preOpenKeys.value = oldVal;
        localStorage.setItem("openKeys", JSON.stringify(openKeys.value));
    },
);

function onClickMenuItem(obj: { item: any, key: string, selectedKeys: any }) {
    currentTabView.value = obj.key;
    localStorage.setItem("currentTabView", obj.key);
}

</script>

<style scoped>
.header-nav {
    height: 50px;
    margin-bottom: 10px;
    background: rgb(249, 252, 254);
    border-bottom: 1px solid var(--color-border);
    box-shadow: 0 1px 4px rgba(10, 10, 10, 0.1);
}

.nav-container {
    display: flex;
    margin-left: -2vw;
    justify-content: space-between;
    height: 100%;
}

.brand {
    display: flex;
    align-items: center;

    h1 {
        margin: 5px;
        font-size: 18px;
        color: var(--color-text-1);
    }

    .logo {
        width: 50px;
        height: 50px;
    }
}

.nav-actions {
    display: flex;
    align-items: center;
    gap: 16px;

    .search-box {
        width: 240px;
    }
}

.arco-menu-horizontal {
    :deep(.arco-menu-overflow-wrap) {
        padding: 0 12px;
    }

    :deep(.arco-menu-item) {
        padding: 0 16px;
    }
}

.admin-container {
    height: 100vh;
}

.logo {
    height: 64px;
    line-height: 64px;
    font-size: 18px;
    font-weight: 500;
    padding-left: 24px;
    color: var(--color-text-1);
}

.header {
    display: flex;
    align-items: center;
    padding: 0 24px;
    background: var(--color-bg-2);
    border-bottom: 1px solid var(--color-border);
}

.enterprise-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.content {
    padding: 24px;
    background: var(--color-fill-2);
}

.mt-16 {
    margin-top: 16px;
}

.entry-item {
    padding: 8px;
    text-align: center;
}
</style>