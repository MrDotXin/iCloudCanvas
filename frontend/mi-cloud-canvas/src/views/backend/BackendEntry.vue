<template>
    <a-layout style="height: 100vh;">
        <a-layout>
            <a-layout-sider 
                style="background-color: aliceblue" width="550" >
                <span class="animation">
                    <a-space direction="vertical" align="center">
                        <span style="font-size: 30px; font-weight: bold;">
                            智能协同云图库后台
                        </span>
                        <div style="margin-top: 100px;">
                            <ParingCubers />
                        </div>  
                    </a-space>
                </span>    
            </a-layout-sider>
            <a-layout-content>
                <FormBackendLogin v-if="isLogin"
                    style="margin: auto; margin-top: 20vh; transform: scale(1.2);" 
                    @switchLogin="onSwitchLogin"
                    @login="onLogin"
                />
                    <FormBackendRegister v-else 
                    style="margin: auto; margin-top: 20vh; transform: scale(1.2);" 
                    @switchLogin="onSwitchLogin"
                    @register = "onRegister"
                />

            </a-layout-content>
        </a-layout>
    </a-layout>
</template>


<script setup lang="ts">
import { ref, onMounted } from 'vue'

import { useLoginUserStore } from '@/stores/user.ts'

import ParingCubers from '@/components/Loader/ParingCubers.vue'
import FormBackendLogin from '@/components/Form/FormBackendLogin.vue' 
import FormBackendRegister from '@/components/Form/FormBackendRegister.vue'     

import { message } from 'ant-design-vue';

import { useRouter } from 'vue-router'

const isLogin = ref(true);

const router = useRouter();

const backendStore = useLoginUserStore();


const onSwitchLogin = () => {
    isLogin.value = !isLogin.value;
}

const onLogin = async (account : string, password : string) => {
    let res = await backendStore.userLogin({
        userAccount: account,
        userPassword: password
    });


    if (res?.code !== 0) {
        message.error(res?.message as string);
    } else {
        router.push('/backend/center');
    }
}

const onRegister = async () => {
    console.log("register");
}

onMounted(async () => { 
    if (backendStore.isLogin) {
        message.warn({ content: '你已经登录了!'});
        router.push('/'); // 如果用户已经登录，重定向到首页
    }

    let res = await backendStore.getLoginUser(); 
    if (res?.code === 0) {
        router.push('/backend/center');
    }
});

</script>


<style scoped>
.animation {
    display: flex; 
    justify-content: center; 
    align-items: center; 
    
    height: 100%; 
    width: 100%; 

}
</style>
