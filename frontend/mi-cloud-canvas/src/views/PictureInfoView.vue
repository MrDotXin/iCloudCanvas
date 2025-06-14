<template>
    <a-spin :spinning="isLoading" class="picture-info">
        <a-row :gutter="[16, 16]">
            <!-- 图片展示区 -->
            <a-col :sm="24" :md="16" :xl="18">
                <a-card>
                    <template #title>
                        图片预览
                        <a-button type="primary" @click="doDownload" style="margin-left: 20px;">  
                            免费下载  
                            <template #icon>  
                                <DownloadOutlined />  
                            </template>  
                        </a-button>
                    </template>
                    <a-image style="max-height: 600px; object-fit: contain" :src="picture.url" />
                </a-card>   
            </a-col>
            <!-- 图片信息区 -->
            <a-col :sm="24" :md="8" :xl="6">
                <a-card title="图片信息">
                    <a-descriptions :column="1">
                        <a-descriptions-item label="作者">
                            <a-space>
                                <a-avatar :size="24" :src="picture.user?.userAvatar" />
                                <div>{{ picture.user?.userName }}</div>
                            </a-space>
                        </a-descriptions-item>
                        <a-descriptions-item label="名称">
                            {{ picture.name ?? '未命名' }}
                        </a-descriptions-item>
                        <a-descriptions-item label="简介">
                            {{ picture.introduction ?? '-' }}
                        </a-descriptions-item>
                        <a-descriptions-item label="分类">
                            <a-tag :color="picture.category ? COLOR_ENUM.PIC_CATEGORY_COLOR_MAP[picture.category] : ''">
                                {{ picture.category ?? '默认' }}
                            </a-tag>
                        </a-descriptions-item>
                        <a-descriptions-item label="标签">
                            <a-tag v-for="tag in picture.tags" :key="tag" :color="COLOR_ENUM.PIC_TAG_COLOR_MAP[tag]">
                                {{ tag }}
                            </a-tag>
                        </a-descriptions-item>
                        <a-descriptions-item label="格式">
                            {{ picture.picFormat ?? '-' }}
                        </a-descriptions-item>
                        <a-descriptions-item label="宽度">
                            {{ picture.picWidth ?? '-' }}
                        </a-descriptions-item>
                        <a-descriptions-item label="高度">
                            {{ picture.picHeight ?? '-' }}
                        </a-descriptions-item>
                        <a-descriptions-item label="宽高比">
                            {{ picture.picScale ?? '-' }}
                        </a-descriptions-item>
                        <a-descriptions-item label="大小">
                            {{ formatSize(Number(picture.picSize)) }}
                        </a-descriptions-item>
                        <a-descriptions-item label="更新时间">
                            {{ dayjs(picture.updateTime).format('YYYY-MM-DD HH:mm:ss')  }}
                        </a-descriptions-item>
                    </a-descriptions>
                    <a-space wrap>  
                    <a-button v-if="canEdit" type="default" @click="doEdit">  
                        编辑  
                        <template #icon>  
                        <EditOutlined />  
                        </template>  
                    </a-button>  
                    <a-button v-if="canDelete" danger @click="doDelete">  
                        删除  
                        <template #icon>  
                        <DeleteOutlined />  
                        </template>  
                    </a-button>  
                    </a-space>

                </a-card>
            </a-col>
        </a-row>
        <a-modal v-model:open="isEditing" :bodyStyle="{ overflow: 'auto' }" width="90vw">
            <template #title>
                当前编辑照片: {{ picture?.name }} ID: {{ picture?.id }}
            </template>

            <AddPictureView :pictureId="picture.id" :onEditSuccess="onPictureEditSuccess" />
        </a-modal>
        <a-modal v-model:open="isDeleting" title="确认删除" @ok="confirmDelete" @cancel="cancelDelete">
            <p>确定要删除照片<b>{{ picture.name }}</b> ? </p>
        </a-modal>
    </a-spin>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';

import { deletePictureUsingPost, getPictureVoByIdUsingGet } from '@/backend/service/api/pictureController';

import { useRoute, useRouter } from 'vue-router';
import { formatSize } from '@/global/index';
import COLOR_ENUM from '@/enum/PictureColorEnum';
import { useLoginUserStore } from '@/stores/user';
import { ACCESS_ENUM } from '@/enum/AccessEnum';
import AddPictureView from './backend/manage/AddPictureView.vue';
import { doFileDownload } from '@/global/index';
import {
    EditOutlined,
    DeleteOutlined,
} from '@ant-design/icons-vue';
import dayjs from 'dayjs';

const picture = ref<API.PictureVO>({});
const userStore = useLoginUserStore();

const router = useRouter();

const onPictureEditSuccess = () => {
    console.log("编辑成功");
    isEditing.value = false;
    fetchPictureData();
}

const canEdit = ref(false);
const isEditing = ref(false);
const doEdit = () => { isEditing.value = true; }

const canDelete = ref(false);
const isDeleting = ref(false);
const doDelete = () => { isDeleting.value = true; }
const cancelDelete = () => { isDeleting.value = false; }
const confirmDelete = async () => {
    try {
        const response = await deletePictureUsingPost({id : picture.value.id});

        if (response.data.code === 0) {
            message.success("删除成功!");
            router.go(-1);
        } else {
            message.success("删除失败!");
        }
    } catch (error) {
        message.error("删除失败 " + error)
    }
}

const route = useRoute();
const isLoading = ref(false);
const fetchPictureData = async () => {
    isLoading.value = true;
    try {
        const response = await getPictureVoByIdUsingGet({ id: route.params.id as string });
        if (response.data.code === 0) {
            picture.value = response.data.data as API.PictureVO;
        } else {
            message.error("错误!");
        }
    } catch (error) {
        message.error('获取图片信息失败 ' + error);
    }
    isLoading.value = false;
}

const doDownload = () => {
    if (!picture.value.url || picture.value.url === "") {
        return 
    }
    const filename =  picture.value.url.substring(picture.value.url.lastIndexOf('_'));
    doFileDownload(picture.value.url, filename);
}



onMounted(async () => {
    await fetchPictureData();
    if (!picture.value.id) {
        message.error("该照片不存在!");
        router.push("/404");
    }

    if (userStore.loginUser.id === picture.value.user?.id || userStore.loginUser.userRole === ACCESS_ENUM.ADMIN) {
        canEdit.value = true;
        canDelete.value = true;
    }

});


</script>

<style scoped>
.pic-manage-container {
    padding: 24px;
    background-color: #f0f2f5;
    min-height: 100vh;
}

.header {
    margin-bottom: 24px;
}

.title {
    color: #333;
    font-size: 24px;
    font-weight: 600;
}

.user-table-card {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>
