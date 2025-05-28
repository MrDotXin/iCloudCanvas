<template>
  <div style="display: flex; justify-content: center; align-items: center; transform: scale(1.5);">
    <FOF />
  </div>
</template>

<script setup lang="ts">
import FOF from '@/components/Loader/F0F.vue'; // 请根据实际路径修改
import { message } from 'ant-design-vue';


message.warn({ content: '找不到该页面'}); // 提示用户已经登录



</script>
