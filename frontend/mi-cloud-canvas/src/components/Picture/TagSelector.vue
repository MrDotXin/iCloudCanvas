<template>
    <a-select
        v-model:value="props.tag"
        mode="multiple"
        style="min-width: 150px;"
        placeholder="选择标签内容查询"
        :options="tagsOption"
        @change="props.onTagSelected"
    >
    </a-select>
</template>


<script lang="ts" setup>
import { listPictureTagAllUsingGet } from '@/backend/service/api/pictureController';
import { onMounted, ref } from 'vue';
import type { PropType } from 'vue';

const props = defineProps({
    tag: {
        required: true,
    },
    onTagSelected: { 
        type: Function as PropType<(value: string[]) => void>,
        default: () => () => {}
    }
});

interface Option {
    value: string;
    label: string;
    children?: Option[];
};

const tagsOption = ref<Option[]>([]);

onMounted(async () => {
    try {
        const response = await listPictureTagAllUsingGet();

        if (response.data.code === 0) {
            console.log(response.data.data); // 打印响应数据
            const tagList = response.data.data || [];

            tagsOption.value = tagList?.map(c => ({ value: c, label: c })) || [];
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