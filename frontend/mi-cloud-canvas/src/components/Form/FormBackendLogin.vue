<template>
    <form class="form" @submit.prevent="true">
        <p class="form-title">管理员账号登录</p>
            <div class="input-container">
            <input placeholder="账号" type="text" v-model="account">
        </div>
        <div class="input-container">
            <input placeholder="密码" type="password" v-model="password">

            <span>
                <svg stroke="currentColor" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" stroke-width="2" stroke-linejoin="round" stroke-linecap="round"></path>
                <path d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" stroke-width="2" stroke-linejoin="round" stroke-linecap="round"></path>
                </svg>
            </span>
            </div>
            <button class="submit clickable" @click="props.onLogin(account, password)">
            登录
        </button>

        <p class="signup-link">
            没有账号?
            <span class="clickable" @click="props.onSwitchLogin()" style="text-decoration: underline;">注册成为机构</span>
        </p>
    </form>
</template>

<script setup lang="ts">
import { defineProps, ref } from 'vue';

const account = ref('');
const password = ref('');
const props = defineProps({
    onSwitchLogin: {
        type: Function,
        default: () => {console.log(''); }
    },
    onLogin: {
        type: Function,
        default: (account : string, password : string) => {console.log(account, password); }
    }
});
</script>

<style scoped>
    .form {
    background-color: #fff;
    display: block;
    padding: 1rem;
    max-width: 350px;
    border-radius: 0.5rem;
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
    }

    .form-title {
    font-size: 1.25rem;
    line-height: 1.75rem;
    font-weight: 600;
    text-align: center;
    color: #000;
    }

    .input-container {
    position: relative;
    }

    .input-container input, .form button {
    outline: none;
    border: 1px solid #e5e7eb;
    margin: 8px 0;
    }

    .input-container input {
    background-color: #fff;
    padding: 1rem;
    padding-right: 3rem;
    font-size: 0.875rem;
    line-height: 1.25rem;
    width: 300px;
    border-radius: 0.5rem;
    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
    }

    .input-container span {
    display: grid;
    position: absolute;
    top: 0;
    bottom: 0;
    right: 0;
    padding-left: 1rem;
    padding-right: 1rem;
    place-content: center;
    }

    .input-container span svg {
    color: #9CA3AF;
    width: 1rem;
    height: 1rem;
    }

.submit {
    display: block;
    padding-top: 0.75rem;
    padding-bottom: 0.75rem;
    padding-left: 1.25rem;
    padding-right: 1.25rem;
    background-color: #4F46E5;
    color: #ffffff;
    font-size: 0.875rem;
    line-height: 1.25rem;
    font-weight: 500;
    width: 100%;
    border-radius: 0.5rem;
    text-transform: uppercase;

    &:hover {
        background-color: #2f2999;
    }

    &:active {
        background-color: #2f2999;
    }
}

    .signup-link {
    color: #6B7280;
    font-size: 0.875rem;
    line-height: 1.25rem;
    text-align: center;
    }

    .signup-link a {
    text-decoration: underline;
    }

    .clickable {

        &:hover {
            cursor: pointer;
        }

        &:active {
            cursor: arrow;
        }
    }

</style>