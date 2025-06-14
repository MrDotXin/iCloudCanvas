<template>
    <a-card title="批量导入" style="min-height: 100%;">
        <a-form name="custom-validation" :model="formState" :rules="rules" v-bind="layout" @finish="handleFinish"
            @finishFailed="handleFinishFailed" :labelCol="{ span: 10 }">
            <a-space>
                <a-form-item has-feedback label="搜索关键字" name="搜索关键字">
                    <a-input v-model:value="formState.searchText" />
                </a-form-item>
                <a-form-item has-feedback label="批量导入的文件前缀" name="文件名称">
                    <a-input v-model:value="formState.picPrefix" />
                </a-form-item>
                <a-form-item has-feedback label="获取数量" name="获取数量">
                    <a-input-number :max="30" :min="1" v-model:value="formState.count" />
                </a-form-item>
                <a-form-item :wrapper-col="{ span: 14, offset: 4 }">
                    <a-button type="primary" html-type="submit" :disabled="isSubmitting">
                        <div v-if="!isSubmitting">提交</div>
                        <div v-else> <LoadingOutlined /> 提交中...</div>
                    </a-button>
                </a-form-item>
            </a-space>
        </a-form>
        <a-card style="overflow: auto;">
            <template #title>
                抓取结果
                <a-button type="primary" @click="onChangePage()" :disabled="isSubmitting">下一批</a-button>
            </template>
            <a-spin :spinning="isSubmittingInfo">
                <a-card title="图片设置">
                    <a-space>
                        <TagSelector style="min-width: 30vw;" :tag="form.tags" :onTagSelected="onTagSelected"></TagSelector>
                        <CategorySelector style="min-width: 16vw;" :category="form.category" :onCategorySelected="onCategorySelected"></CategorySelector>
                        <a-button :disabled="!(resultPictureList.length > 0)" type="primary" @click="onSubmitInfo">设置统一信息</a-button>
                        <a-button :disabled="!(resultPictureList.length > 0) || isAllOpered" danger @click="onRejectAll">一键拒绝</a-button>
                        <a-button :disabled="!(resultPictureList.length > 0) || isAllOpered" type="primary" @click="handleReviewAcceptAll">一键过审</a-button>
                    </a-space>
                </a-card>
                <a-table :columns="columns" :data-source="resultPictureList" size="small" row-key="id" >
                    <template #url="{ record }">
                        <img :src="record.url" alt="加载中.." style="width: 120px; object-fit: cover;" />
                    </template>
                    <template #pictureInfo="{ record }">
                        <div>格式：{{ record.picFormat }}</div>
                        <div>宽度：{{ record.picWidth }}</div>
                        <div>高度：{{ record.picHeight }}</div>
                        <div>宽高比：{{ record.picScale }}</div>
                        <div>大小：{{ (record.picSize / 1024).toFixed(2) }}KB</div>
                    </template>
                    <template #action="{ record }">
                        <div v-if="rejectedList.has(record.id)">
                            <span style="color: red;">已拒绝</span>
                        </div>
                        <div v-else>
                            <a-button 
                                :danger="isDangerMode && selectedDeleteId === record.id"
                                type="link" @click="deletePicture(record.id)"
                                @mouseenter="handleHover(true, record.id)" 
                                @mouseleave="handleHover(false, record.id)"
                            >
                                {{ isDangerMode && selectedDeleteId === record.id ? '拒绝' : '已保存'  }}
                            </a-button>
                        </div>
                    </template>
                </a-table>
            </a-spin>
        </a-card>
    </a-card>
</template>
<script lang="ts" setup>
import { deletePictureUsingPost, doPictureReviewUsingPost, updatePictureUsingPost, grabPicturesByPromptUsingPost } from '@/backend/service/api/pictureController';
import { message } from 'ant-design-vue';
import { ref } from 'vue';

import { LoadingOutlined } from '@ant-design/icons-vue';

import TagSelector from '@/components/Picture/TagSelector.vue';
import CategorySelector from '@/components/Picture/CategorySelector.vue';

const formState = ref<API.PictureUploadBatchRequest>({
    count: 0,
    searchText: "",
    picPrefix: ""
});

const form = ref({
    tags: [] as string[],   
    category: "" as string,
})

const onCategorySelected = (category: string[]) => {
    form.value.category = category[0];
}

const onTagSelected = (tags: string[]) => {
    form.value.tags = tags;
}

// 表格列配置
const columns = [
    {
        title: 'ID',
        dataIndex: 'id',
        key: 'id',
        width: 40
    },
    {
        title: '内容',
        dataIndex: 'url',
        key: 'url',
        slots: { customRender: 'url' },
        width: 150,
    },
    {
        title: '名称',
        dataIndex: 'name',
        key: 'name',
        width: 100
    },
    {
        title: '图片信息',
        key: 'pictureInfo',
        width: 150,
        slots: { customRender: 'pictureInfo' }
    },
    {
        title: '操作',
        key: 'action',
        width: 150,
        slots: { customRender: 'action' },
        fixed: 'right'
    },
];

const rejectedList = ref<Set<string>>(new Set()); // 用于存储已拒绝的ID
const isDangerMode = ref(false);
const selectedDeleteId = ref(''); 
const handleHover = (isHover: boolean, id : string) => {
    if (isHover) {
        selectedDeleteId.value = id;
        isDangerMode.value = true;
    } else {
        selectedDeleteId.value = "";
        isDangerMode.value = false;
    }
}

// 触发删除操作，弹出确认弹窗
const deletePicture = async (id: string) => {
    const response = await deletePictureUsingPost({ id: id });

    if (response.data.code !== 0) {
        console.error('删除照片失败', response.data.message);
        return;
    } else {
        rejectedList.value.add(id);
        message.success('删除照片成功!');
    }

};

const isAllOpered = ref(false); 
const onRejectAll = async () => {
    for (const picture of resultPictureList.value) {
        if (picture.id) {
            if (!rejectedList.value.has(picture.id)) {
                deletePictureUsingPost({ id: picture.id }).then(response => {
                    if (response.data.code!== 0) {
                        console.error(`照片${picture.name}删除失败! ${response.data.message}`);
                        rejectedList.value.delete(picture.id as string);
                    } 
                });
                rejectedList.value.add(picture.id);
            }
        }
    }
    message.success('批量拒绝图片成功!');
    isAllOpered.value = true;
}

const isSubmittingInfo = ref(false);
const onSubmitInfo = async () => {
    isSubmittingInfo.value = true;
    for (const picture of resultPictureList.value) {
        if (picture.id && !rejectedList.value.has(picture.id)) {
            const response = await updatePictureUsingPost({
                id: picture.id,
                tags: form.value.tags,
                category: form.value.category,
            });

            if (response.data.code !== 0) {
                console.error(`照片${picture.name}更新照片信息失败! ${response.data.message}`);
            }
        }
    }

    message.success('批量更新照片信息成功!');
    isSubmittingInfo.value = false;
}

const rules = {
    count: [{ required: true, trigger: 'blur' }],
    searchText: [{ required: true, trigger: 'blur' }],
    picPrefix: [{ required: true, trigger: 'blur' }],
    page: [{ required: true, trigger: 'blur' }],
};

const layout = {
    labelCol: { span: 4 },
    wrapperCol: { span: 14 },
};

const resultPictureList = ref<API.PictureVO[]>([]); // 用于存储图片列表的数组

const isSubmitting = ref(false); // 用于控制按钮是否处于提交状态
const handleReviewAcceptAll = async () => {
    for (const picture of resultPictureList.value) {
        if (picture.id) {
            if (!rejectedList.value.has(picture.id)) {
                const response = await doPictureReviewUsingPost({ id: picture.id, reviewStatus: 1, reviewMessage: "通过" });

                if (response.data.code!== 0) {
                    message.error(`照片${picture.name}通过失败! ${response.data.message}`);
                } 
            }
        }
    }

    message.success('批量过审图片成功!');
    isAllOpered.value = true;
}

const onChangePage = async () => {
    isAllOpered.value = false;
    await handleFinish();
}

let currentStart = 1;
let isFinish = false;
const handleFinish = async () => {
    if (isFinish) {
        message.success('没有更多数据了!');
        return;
    }

    console.log(formState.value);
    isSubmitting.value = true;
    try {
        const response = await grabPicturesByPromptUsingPost({
            count: formState.value.count,
            searchText: formState.value.searchText,
            picPrefix: formState.value.picPrefix,
            start: currentStart,
        });

        if (response.data.code !== 0) {
            console.error('批量导入图片失败', response.data.message);
            message.error('批量导入图片失败! ' + response.data.message);
        } else {
            message.success('批量导入图片成功!');

            resultPictureList.value = response.data.data || [];
            currentStart += resultPictureList.value.length;
            if (formState.value.count && resultPictureList.value.length < formState.value.count) {
                isFinish = true;
            }
        }
    } catch (error) {
        console.log(error);
        message.error("错误! " + error)
    }

    isSubmitting.value = false;
}

const handleFinishFailed = (errors: string) => {
    console.log(errors);
};

</script>
