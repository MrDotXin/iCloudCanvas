<template>
  <div class="global_header"> 
    <a-row type="flex">
      <a-col flex="200px">
        <a-space class="title_bar" direction="horizontal">
          <RouterLink to="/">
            <img class="logo" src="@/assets/cloud.svg" alt="logo" />
          </RouterLink>
          <div class="subtitle">智能协同云图库</div>
        </a-space>
      </a-col>
      <a-col flex="auto">
        <div class="global_header">
          <a-menu v-model:selectedKeys="current" :items="items" mode="horizontal" @click="doMenuClick" />
        </div>
      </a-col>
    </a-row>

    <UserBrief />
  </div>
</template>

<script setup lang="ts">
import { h, ref } from 'vue'
import { HomeOutlined } from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue';
import { RouterLink, useRouter } from 'vue-router';
import UserBrief from '@/components/UserBrief.vue';

// 获取路由实例
const router = useRouter();

const current = ref<string[]>(['home'])

router.afterEach((to, from) => {
  current.value = [to.path.replace('/', '')];
});

const doNavgatorFilter = (route: any) => {
  return !(route.meta && route.meta.hidden === true);
}

const doIconSelect = (route: any) => {
  switch (route) {
    case '主页':
      return HomeOutlined;
  }

  return "";
}

// 获取路由列表并过滤掉 meta 属性中 hidden 为 true 的页面，然后映射到 a-menu 的 MenuProps 中
const items: MenuProps['items'] = router.getRoutes().filter(doNavgatorFilter).map(route => ({
  key: route.path.replace('/', ''),
  label: route.name,
  icon: h(doIconSelect(route.name))
}));

// 定义菜单点击事件处理函数
const doMenuClick = (e: { key: string }) => {
  router.push(
    {
      path: '/' + e.key
    }
  );
};

</script>

<style scoped>
.global_header {
  display: flex;
  justify-content: space-between;
}

.title_bar {
  width: 200px;
}

.logo {
  height: 5vh;
  width: 3vw;
}

.subtitle {
  /* 设置字体大小 */
  font-size: 14px;
  color: #333;
  font-weight: bold;
  line-height: 5vh;
  vertical-align: middle;
}
</style>
