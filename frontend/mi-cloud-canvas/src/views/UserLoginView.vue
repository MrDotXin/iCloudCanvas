<template>
    <div>
        <UserLoginView class="FormStyle" @submit="onUserLogin" />
    </div>
</template>

<script lang="ts" setup>
import UserLoginView from '@/components/Form/UserLoginForm.vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/user'
import { onMounted } from 'vue'

const router = useRouter();
const route = useRoute();
const userStore = useLoginUserStore();


async function onUserLogin(account: string, password: string) {
    const response = await userStore.userLogin({ userAccount: account, userPassword: password });
    if (response?.code === 0) {
        message.success({ content: '登录成功! 欢迎'});
        if (route.query.redirect) {
            router.push(route.query.redirect as string); 
        } else {
            router.push('/');
        }
    } else {
        message.error({ content: '登录失败! ' + response?.message});
    }
}

onMounted(async () => {
    if (userStore.isLogin) {
        message.warn({ content: '你已经登录了!'})
        router.push('/'); // 如果用户已经登录，重定向到首页
    }
})

</script>


<style scoped>
.FormStyle {
    position: absolute;
    top: 8%;
    left: 30%;
}
</style>