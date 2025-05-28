<template>
    <a-spin :spinning="isLoading" class="picture-info">
        <a-row>
            <!-- 图片展示区 -->
            <a-col :sm="24" :md="16" :xl="18">
                <a-card>
                    <template #title>
                        图片预览
                        <a-button type="primary" @click="doDownload" style="margin-left: 20px;">  
                            下载  
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
                    </a-descriptions>
                    <a-space wrap>  
                    </a-space>
                </a-card>
            </a-col>
        </a-row>
        <a-row>
            <a-form style="padding: 20px;" :label-col="{ span: 8 }" :wrapper-col="{ span: 20 }" layout="horizontal" :model="form"
            :rules="rules" @finish="onSubmit">

            <a-alert message="你选择通过审核" v-if="form.reviewStatus === PICTURE_LABEL_ENUM.ACCEPTED" type="success" />
            <a-alert message="你选择拒绝了审核" v-if="form.reviewStatus === PICTURE_LABEL_ENUM.REJECTED" type="error" />
            <a-form-item label="审核情况" name="reviewStatus">
                <a-radio-group v-model:value="form.reviewStatus">
                    <a-radio :value="PICTURE_LABEL_ENUM.ACCEPTED">通过</a-radio>
                    <a-radio :value="PICTURE_LABEL_ENUM.REJECTED">拒绝</a-radio>
                </a-radio-group>
            </a-form-item>
            <a-form-item v-if="form.reviewStatus === PICTURE_LABEL_ENUM.REJECTED" label="未过审的原因: " name="reviewMessage">
                <a-textarea  v-model:value="form.reviewMessage" :rows="3" style="width: 40vw" />
            </a-form-item>
            <a-form-item :wrapper-col="{ offset: 6, span: 18 }">
            <a-button html-type="submit" type="primary" size="large" style="width: 20vw;">
                <span v-if="!isSubmitting">
                    <div>提交审核</div>
                </span>
                <span v-else>
                    <a-space>
                        <LoadingOutlined />
                        <div>
                            提交中...
                        </div>
                    </a-space>
                </span>
            </a-button>
        </a-form-item>
            </a-form>
        </a-row>
    </a-spin>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { message } from 'ant-design-vue';
import type { PropType } from 'vue';
import { doPictureReviewUsingPost, getPictureVoByIdUsingGet, listPictureTagCategoryUsingGet } from '@/backend/service/api/pictureController';

import { formatSize } from '@/global/index';
import COLOR_ENUM from '@/enum/PictureColorEnum';
import { doFileDownload } from '@/global/index';
import { PICTURE_LABEL_ENUM } from '@/enum/PictureReviewEnum';
import {
    LoadingOutlined,
} from '@ant-design/icons-vue';

const props = defineProps({
    pictureId: { type: String, required: true },
    onSuccess:  {
        type: Function as PropType<(value : number) => void>,
        default: (value : number) => () => { }
    }
});

const form = ref({
    reviewStatus: null,
    reviewMessage: ''
});

const resetForm = () => {
    form.value = {
        reviewStatus: null,
        reviewMessage: ''
    };
}

watch(
    () => props.pictureId,
    () => {
        fetchPictureData();
    }
)

const rules = {
    reviewStatus: [
        { required: true, message: '请选择审核状态', trigger: 'blur' }
    ],
    reviewMessage: [
        { required: true, message: '请输入审核原因', trigger: 'blur' }
    ]
}

const onSubmit = async () => {
    isSubmitting.value = true;
    try {
        if (form.value.reviewStatus === null) {
            return;
        }
        const response = await doPictureReviewUsingPost({
            id: props.pictureId,
            reviewStatus: form.value.reviewStatus,
            reviewMessage: form.value.reviewMessage
        });

        if (response.data.code === 0) {
            message.success('提交审核成功');
            props.onSuccess(form.value.reviewStatus);
            resetForm();
        } else {
            message.error('提交审核失败');
        }
    } catch (error) {
        console.error('提交审核失败', error);
        message.error('提交审核失败');
    }
    isSubmitting.value = false;
};

const picture = ref<API.PictureVO>({});
const isLoading = ref(false);
const isSubmitting = ref(false);
const fetchPictureData = async () => {

    isLoading.value = true;
    try {
        const response = await getPictureVoByIdUsingGet({ id: props.pictureId as string });
        if (response.data.code === 0) {
            picture.value = response.data.data as API.PictureVO;
            resetForm();
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
});


</script>

<style scoped>
.picture-info {
    overflow: auto;
}

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
