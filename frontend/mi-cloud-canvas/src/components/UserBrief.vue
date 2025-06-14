<template>
    <div>
        <template v-if="userStore.isLogin">
            <!-- 这里可以添加已登录时显示用户信息的内容 -->
            <a-space direction="horizontal" align="center">
                <a-popover 
                    trigger="click"
                >
                    <template #content>
                        <a-menu>
                            <a-menu-item key="userSpace" @click="handleToUserSpace">
                                <UserOutlined />
                                <span>个人中心</span>
                            </a-menu-item>
                            <a-menu-item key="userLogout" @click="handleLogout">
                                <LoginOutlined />
                                <span>退出登录</span>
                            </a-menu-item>
                        </a-menu>
                    </template>
                    <a-avatar 
                        class="clickable"
                        :size="36" 
                        :style="{ backgroundColor: '#3370ff' }"
                        :src="userStore.loginUser.userAvatar"
                        >
                    </a-avatar>
                </a-popover>
                <span style="font-size: 15px;">{{ userStore.loginUser.userName }}</span>
            </a-space>
        </template>
        <template v-else>
            <a-button type="secondary" @click="handleLogin">登录</a-button>
            <a-button type="secondary" @click="handleRegister">注册</a-button>
        </template>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useLoginUserStore } from '@/stores/user.ts';
import { LoginOutlined, UserOutlined } from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
// 创建用户存储实例
const userStore = useLoginUserStore();

onMounted(async () => {
    // 获取用户信息 
    await userStore.getLoginUser();
});

const router = useRouter();

// 处理登录逻辑
const handleLogin = () => {
    // 这里可以添加跳转到登录页面的逻辑
    console.log('跳转到登录页面');
    router.push('/login');
};

// 处理注册逻辑
const handleRegister = () => {
    // 这里可以添加跳转到注册页面的逻辑
    console.log('跳转到注册页面');
    router.push('/register');
};

const handleLogout = async () => {
    
    console.log('用户登出');    

    try {
        await userStore.userLogout();
    } catch (error : any) {
        message.error('登出失败 '  + error?.message);
    }

    window.location.reload();
};

const handleToUserSpace = () => {
    router.push('/user/space');
};

</script>
