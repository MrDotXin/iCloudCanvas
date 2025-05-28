<template>
    <a-cascader v-model:value="props.category" placeholder="选择分类" :options="categories" style="width: 100%" @change="props.onCategorySelected" />

</template>


<script lang="ts" setup>
import { listPictureCategoryAllUsingGet } from '@/backend/service/api/pictureController';
import { onMounted, ref } from 'vue';
import type { PropType } from 'vue';

const props = defineProps({
    category: {
        required: true
    },
    onCategorySelected: { 
        type: Function as PropType<(value: string[]) => void>,
        default: () => () => {}
    }
});

interface Option {
    value: string;
    label: string;
    children?: Option[];
};

const categories = ref<Option[]>([]);

onMounted(async () => {
    try {
        const response = await listPictureCategoryAllUsingGet();

        if (response.data.code === 0) {
            console.log(response.data.data); // 打印响应数据
            const categoryList = response.data.data || [];

            categories.value = categoryList?.map(c => ({ value: c, label: c })) || [];
        } else {
            console.error('获取图片分类列表失败:', response.data.message); // 打印错误信息
        }
    } catch (error) {
        console.error('获取图片分类列表失败:', error); // 打印错误信息
    }
});

</script>


<style scoped>

</style>