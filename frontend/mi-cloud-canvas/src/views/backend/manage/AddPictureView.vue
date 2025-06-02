<template>
    <div class="add-picture">
        <a-card class="form-container" style="padding: 0;">
            <template #title v-if="!isEditMode">
                <a-button type="primary" shape="round" size="medium" @click="resetInfoModalVisible = true">
                    <UndoOutlined />新建
                </a-button>
                <a-modal v-model:open="resetInfoModalVisible" title="确认删除" @ok="confirmReset" @cancel="cancelReset">
                    确定重置当前修改的所有信息吗 ?
                </a-modal>
            </template>
            <a-spin :spinning="isSubmitting">
                <div class="form-layout">
                    <div class="upload-section">
                        <PictureUploader :picture="picture" :onSuccess="onSuccess" />

                        <a-card v-if="picture?.thumbnailUrl" title="缩略图">
                            <a-image :src="picture?.thumbnailUrl" style="max-width: 20vw; object-fit: contain;" />
                        </a-card>
                    </div>
                    <div class="form-section">
                        <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }" layout="horizontal" :model="form"
                            :rules="rules" @finish="onSubmit">
                            <a-form-item label="标题名称" name="name">
                                <a-input v-model:value="form.name" />
                            </a-form-item>
                            <a-form-item label="介绍" name="introduction">
                                <a-textarea v-model:value="form.introduction" :rows="4" />
                            </a-form-item>
                            <a-form-item label="分类" name="category">
                                <a-cascader v-model:value="form.category" :options="categories" style="width: 100%"
                                    @change="onCategorySelected" />
                            </a-form-item>
                            <a-form-item label="标签输入" name="tags">
                                <div class="tags-container">
                                    <a-auto-complete v-model:value="inputTags" :options="tagsOption" style="width: 100%"
                                        @select="onTagSelected" placeholder="输入标签" />
                                    <div class="tags-display">
                                        <a-tag v-for="(tag, index) in form.tags" :key="index"
                                            @click="onTagCanceled(tag)" class="tag-item clickable"
                                            :color="ColorEnum.PIC_TAG_COLOR_MAP[tag]">
                                            {{ tag }}
                                        </a-tag>
                                    </div>
                                </div>
                            </a-form-item>
                            <a-form-item :wrapper-col="{ offset: 6, span: 18 }">
                                <a-button html-type="submit" type="primary" size="large" style="width: 300px">
                                    <span v-if="!isSubmitting">
                                        <div v-if="submitCount > 0 || isEditMode">更新<b>{{ form.name || "" }}</b></div>
                                        <div v-else>上传图片</div>
                                    </span>
                                    <span v-else>
                                        <a-space>
                                            <LoadingOutlined />
                                            <div v-if="submitCount > 0 || isEditMode">更新中...</div>
                                            <div v-else>上传中...</div>
                                        </a-space>
                                    </span>
                                </a-button>
                            </a-form-item>
                        </a-form>
                    </div>
                </div>
            </a-spin>
        </a-card>
    </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue';
import PictureUploader from '@/components/PictureUploader.vue';
import { getPictureVoByIdUsingGet, listPictureTagCategoryUsingGet, updatePictureUsingPost } from '@/backend/service/api/pictureController';
import { message } from 'ant-design-vue';
import { LoadingOutlined, UndoOutlined } from '@ant-design/icons-vue';
import { global } from '@/global';
import ColorEnum from '@/enum/PictureColorEnum'

const picture = ref<API.PictureVO>();
const inputTags = ref("");

interface Option {
    value: string;
    label: string;
    children?: Option[];
};

interface Tag {
    value: string;
    label: string;
};

const props = defineProps({
    pictureId: {
        type: String,
        default: ""
    },
    onEditSuccess: {
        default: () => () => { console.log("泥嚎"); }
    }
});

watch(
    () => props.pictureId,
    () => {
        if (props.pictureId) {
            console.log("开始加载图片信息 ", props.pictureId);
            tryEditMode();
        }
    }
)

const form = ref({
    name: '',
    introduction: '',
    tags: [] as string[],
    category: "" as string
});

const rules = {
    name: [
        { required: true, message: '请输入标题名称', trigger: 'blur' }
    ],
    introduction: [
        { required: true, message: '请输入图片介绍', trigger: 'blur' }
    ],
    category: [
        { required: true, message: '请选择图片分类', trigger: 'blur' }
    ],
    tags: [
        { required: true, message: '请选择图片标签', trigger: 'blur' }
    ]
}

const resetInfoModalVisible = ref(false);
const confirmReset = () => {
    resetInfoModalVisible.value = false;
    form.value = {
        name: '',
        introduction: '',
        tags: [] as string[],
        category: "" as string
    };

    picture.value = {};

    message.success("重置成功!");
}

const loadFromPictureVO = (pic : API.PictureVO | undefined) => {
    if (pic) {
        form.value = {
            name: pic.name || "",
            introduction: pic.introduction || "",
            category: pic.category || "",
            tags: pic.tags || []
        }
    }
}

const cancelReset = () => {
    resetInfoModalVisible.value = false;
}

const tagsOption = ref<Tag[]>([]);
const categories = ref<Option[]>([]);

const onTagSelected = (tag: string) => {
    if (form.value.tags.includes(tag)) {
        return;
    }
    form.value.tags.push(tag);
}

const onCategorySelected = (category: string[]) => {
    form.value.category = category[0];
}

const onTagCanceled = (tag: string) => {
    console.log('Removing tag:', tag); // 调试日志
    const index = form.value.tags.findIndex(t => t === tag);
    if (index !== -1) {
        form.value.tags.splice(index, 1);
    }
    console.log('Updated tags:', form.value.tags); //    调试更新后的标签
}

const loadCategoryAndTags = async () => {
    const response = await listPictureTagCategoryUsingGet();
    if (response.data.code === 0) {
        // 处理返回的数据，例如更新分类和标签的选项
        const categoryList = response.data.data?.categoryList;
        const tagList = response.data.data?.tagList;
        console.log(categoryList, tagList); // 打印分类和标签数据，用于调试

        categories.value = categoryList?.map(c => ({ value: c, label: c })) || [];
        tagsOption.value = tagList?.map(t => ({ value: t, label: t })) || [];
    }
}


const isSubmitting = ref(false);

const submitCount = ref(0);
const onSubmit = async () => {
    console.log('Form submitted:', form.value.category);
    isSubmitting.value = true;

    await global.sleep(1000);
    if (!picture.value) {
        message.error("请上传图片");
        return;
    } else {
        try {
            form.value.tags.sort((a : string, b : string) => { return a < b ? -1 : a === b ? 0 : 1; });
            const response = await updatePictureUsingPost({
                ...form.value,
                id: picture.value.id
            });
            if (response.data.code === 0) {
                message.success("上传成功!");
                submitCount.value++;
                props.onEditSuccess();
            } else {
                message.error("上传失败!");
            }
        } catch (error) {
            console.error('Error submitting form:', error);
            message.error("上传失败");
        }
    }
    isSubmitting.value = false;
};

// 如果是编辑模式(父组件传入了已存在的Picture对象)，则加载图片信息
const isEditMode = ref(false);
onMounted(async () => {
    loadCategoryAndTags();
    tryEditMode();
});

const tryEditMode = async () => {
    if (props.pictureId !== "") {
        isEditMode.value = true;
        try {
            const response = await getPictureVoByIdUsingGet({ id : props.pictureId});

            if (response.data.code === 0) {
                picture.value = response.data.data 
                loadFromPictureVO(picture.value);
            } else {
                message.error("加载已存在数据失败!");
                isEditMode.value = false;
            }
        } catch (error) {
            message.error("加载已存在数据失败! " + error);
        }
    }
}

const onSuccess = (newPicture: API.PictureVO) => {
    console.log(newPicture);
    picture.value = newPicture;
}

</script>


<style scoped>
.add-picture {
    padding: 20px;
}

.form-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 24px;
}

.form-layout {
    display: flex;
    gap: 32px;
}

.upload-section {
    flex: 1;
    min-width: 300px;
}

.form-section {
    flex: 2;
    min-width: 500px;
}

.tags-container {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.tags-display {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.tag-item {
    margin: 0;
}

@media (max-width: 768px) {
    .form-layout {
        flex-direction: column;
    }

    .upload-section,
    .form-section {
        min-width: 100%;
    }
}

/* 保持原有的上传组件样式 */
.picture-upload :deep(.ant-upload) {
    width: 100% !important;
    height: 100% !important;
    min-height: 152px;
    min-width: 152px;
}

.ant-upload-select-picture-card i {
    font-size: 32px;
    color: #999;
}

.ant-upload-select-picture-card .ant-upload-text {
    margin-top: 8px;
    color: #666;
}
</style>