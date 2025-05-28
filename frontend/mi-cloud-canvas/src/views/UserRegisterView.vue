<template>
    <div>
        <UserRegisterView class="FormStyle" @submit="onUserRegister"  />
    </div>
</template>


<script lang="ts" setup>
import UserRegisterView from '@/components/Form/UserRegisterForm.vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/user'
import { userRegisterUsingPost } from '@/backend/service/api/userController';
import { onMounted } from 'vue';

const router = useRouter();
const userStore = useLoginUserStore();

async function onUserRegister(account : string, password : string) {
    const response = await userRegisterUsingPost({ userAccount: account, userPassword: password });
    if (response?.data.code === 0) {
        message.success({content: '注册成功!', duration: 500});
        
        router.back();   
    } else {
        message.error({content: '注册失败! ' + response?.data.message, duration: 1000});
    }
}

onMounted(async () => {
    if (userStore.isLogin) {
        message.warn({ content: '你已经登录了! 请退出登录后注册'});
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