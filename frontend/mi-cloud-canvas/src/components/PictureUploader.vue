<template>
    <div class="picture-upload">
        <a-tabs v-model:activeKey="currentUploadType" defaultActiveKey="multipart">
            <a-tab-pane key="multipart" tab="本地上传">
                <a-upload list-type="picture-card" :show-upload-list="false" :custom-request="handleUpload"
                    :before-upload="beforeUpload" :disable="loading" style="width: 30vw; height: 20vw;">
                    <img style="max-width: 25vw;" v-if="currentPictureExists()" :src="props.picture?.url"
                        alt="upload" />
                    <div v-else>
                        <div v-if="loading">
                            <loading-outlined></loading-outlined>
                            <div class="ant-upload-text">上传中...</div>
                        </div>
                        <div v-else>
                            <plus-outlined></plus-outlined>
                            <div class="ant-upload-text">点击或拖拽上传图片</div>
                        </div>
                    </div>
                </a-upload>
            </a-tab-pane>
            <a-tab-pane key="url" tab="URL导入" force-render>
                <a-space>
                    <a-input placeholder="请输入图片URL" v-model:value="uploadUrlSource" />
                    <a-button type="primary" @click="handleUpload" :disabled="loading">
                        <span v-if="!loading">
                            <div v-if="currentPictureExists()">更新图片</div>
                            <div v-else>上传图片</div>
                        </span>
                        <span v-else>
                            <div class="ant-upload-text">上传中...</div>
                        </span>
                    </a-button>
                </a-space>
                <img style="max-width: 30vw;" v-if="currentPictureExists()" :src="props.picture?.url" alt="upload" />
            </a-tab-pane>
        </a-tabs>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { uploadPictureByUrlUsingPost, uploadPictureUsingPost } from '@/backend/service/api/pictureController';
import { LoadingOutlined, PlusOutlined } from '@ant-design/icons-vue'; // 导入 LoadingOutlined 图标
interface Props {
    picture?: API.PictureVO;
    spaceId?: string;
    onSuccess?: (picture: API.PictureVO) => void; // 上传成功回调函数，返回图片信息
}

const props = defineProps<Props>(); // 定义 props 类型

const loading = ref(false) // 上传状态

const currentUploadType = ref("multipart"); // 当前上传类型，默认为 multipart

const uploadUrlSource = ref(""); // URL 上传的来源，默认为空

// 上传前的处理
const beforeUpload = (file: any) => {
    const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
    if (!isJpgOrPng) {
        message.error('不支持上传该格式的图片，推荐 jpg 或 png')
    }

    const isLt2M = file.size / 1024 / 1024 <= 10
    if (!isLt2M) {
        message.error('不能上传超过 10M 的图片')
    }
    return isJpgOrPng && isLt2M
}

const currentPictureExists = () => {
    return props.picture && props.picture.id !== undefined && props.picture.id !== "";
}

// 上传图片
const handleUpload = async ({ file }: any) => {
    console.log("上传图片 " + file);
    loading.value = true;
    if (props.picture) props.picture.url = "";
    try {
        const res = await uploadPicture({ file }) as any;

        if (res.data.code === 0) {
            message.success('上传成功');
            props.onSuccess?.(res.data.data || {});
        } else {
            message.error(res.data.message || '上传失败');
        }
    } catch (error) {
        message.error('上传失败');
    } finally {
        loading.value = false;
    }
}

const uploadPicture = async ({ file }: any) => {
    const params = props.picture && props.picture.id !== "" ? { id: props.picture.id, uploadType: currentUploadType.value } : {};

    if (currentUploadType.value === "multipart") {
        console.log("SPACE ID", props.spaceId);
        return uploadPictureUsingPost(
            {
                ...params,
                spaceId: props.spaceId,
            }, {}, file);
    } else if (currentUploadType.value === "url") {

        return uploadPictureByUrlUsingPost({
            id: props.picture?.id || "",
            url: uploadUrlSource.value,
            uploadType: currentUploadType.value
        });

    }
}


</script>