<template>
    <div class="user-manage-container">
        <div class="header">
            <h1 class="title">图片资源管理</h1>
        </div>
        <a-card class="user-table-card">
            <a-space>
                <a-input v-model:value="searchParam.searchText" placeholder="输入图片内容"></a-input>
                <a-cascader v-model:value="searchParam.category" placeholder="选择分类" :options="categories" style="width: 100%" @change="onCategorySelected" />
                <a-select
                    v-model:value="searchParam.tags"
                    mode="multiple"
                    style="min-width: 150px; max-width: 300px;"
                    placeholder="选择标签内容查询"
                    :options="tagsOption"
                >
                </a-select>
                <a-select
                    ref="select"
                    v-model:value="searchParam.reviewStatus"
                    @change="console.log('reviewStatus changed:', searchParam.reviewStatus)"
                    style="width: 120px"
                    >
                    <a-select-option :value="PICTURE_LABEL_ENUM.ACCEPTED"><span  :style="{ color: ColorEnums.PIC_REVIEW_TAG_MAP[PICTURE_LABEL_ENUM.ACCEPTED]}">已通过</span></a-select-option>
                    <a-select-option :value="PICTURE_LABEL_ENUM.REJECTED"><span  :style="{ color: ColorEnums.PIC_REVIEW_TAG_MAP[PICTURE_LABEL_ENUM.REJECTED]}">已拒绝</span></a-select-option>
                    <a-select-option :value="PICTURE_LABEL_ENUM.REVIEWING"><span :style="{ color: ColorEnums.PIC_REVIEW_TAG_MAP[PICTURE_LABEL_ENUM.REVIEWING]}">审核中</span></a-select-option>
                    <a-select-option :value="3">全部</a-select-option>
                </a-select>
                <a-checkbox v-model:checked="isAscending">按时间降序</a-checkbox>
            </a-space>
            <a-spin :spinning="isLoading" size="large">
                <a-table :columns="columns" 
                    :data-source="pictures" 
                    :pagination="pagination" 
                    @change="handleTableChange"
                    size="small" row-key="id"
                    
                    >
                    <template #reviewStatus="{ record }">
                        <a-tag :color="ColorEnums.PIC_REVIEW_TAG_MAP[record.reviewStatus]">
                            {{  PICTURE_VALUE_ENUM[record.reviewStatus] }}
                        </a-tag>

                    </template>
                    <template #url="{ record }">
                        <img :src="record.url" alt="加载中.." style="width: 120px; object-fit: cover;" />
                    </template>
                    <template #category="{ record }">
                        <a-tag :color="ColorEnums.PIC_CATEGORY_COLOR_MAP[record.category]">
                            {{  record.category }}
                        </a-tag>
                    </template>
                    <template #tags="{ record }">
                        <a-tag v-for="tag in JSON.parse(record.tags)" :key="tag"
                            :color="ColorEnums.PIC_TAG_COLOR_MAP[tag]">
                            {{ tag }}
                        </a-tag>
                    </template>
                    <template #pictureInfo="{ record }">
                        <div>格式：{{ record.picFormat }}</div>
                        <div>宽度：{{ record.picWidth }}</div>
                        <div>高度：{{ record.picHeight }}</div>
                        <div>宽高比：{{ record.picScale }}</div>
                        <div>大小：{{ (record.picSize / 1024).toFixed(2) }}KB</div>
                    </template>
                    <template #createTime="{ record }">
                        {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
                    </template>
                    <template #editTime="{ record }">
                        {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
                    </template>

                    <template #action="{ record }">
                        <div v-if="deletedPictureIds.has(record.id)">
                            <span style="color: red;">已删除</span>
                        </div>
                        <div v-else>
                            <a-button type="link" @click="onPictureReview(record)" v-if="record.reviewStatus === PICTURE_LABEL_ENUM.REVIEWING">
                                审核 
                            </a-button>
                            <a-button type="link" @click="onPictureEdit(record)">
                                编辑 
                            </a-button>
                            <a-popconfirm
                                title="确定删除吗？"
                                ok-text="删除"
                                cancel-text="取消"
                                @confirm="confirmDelete(record.id)"
                                @cancel="isDeleting = false"
                            >
                            <a-button type="link" danger @click="isDeleting = true">
                                删除
                            </a-button>
                        </a-popconfirm> 
                        </div>
                    </template>
                </a-table>
            </a-spin>
        </a-card>
        <a-modal v-model:open="isEditing" :bodyStyle="{ overflow: 'auto' }" width="90vw" @cancel="cancelDelete">
            <template #title>
                当前编辑照片: {{ currentSelectedPicture?.name }} ID: {{ currentSelectedPicture?.id }}
            </template>
            <AddPictureView :pictureId="currentSelectedPicture?.id" :onEditSuccess="onPictureEditSuccess" />
        </a-modal>
        <a-modal v-model:open="isReviewing" :bodyStyle="{ overflow: 'auto' }" width="90vw" >
            <template #title>
                当前审核照片: {{ currentSelectedPicture?.name }} ID: {{ currentSelectedPicture?.id }}
            </template>
            <PictureReview 
                :pictureId="currentSelectedPicture?.id || ''" 
                :onSuccess="onPictureReviewingSuccess" />
        </a-modal>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import dayjs from 'dayjs';
import { message } from 'ant-design-vue';

import { global } from '@/global';
import { deletePictureUsingPost, listPictureByPageUsingPost, listPictureTagCategoryUsingGet } from '@/backend/service/api/pictureController';
import AddPictureView from './AddPictureView.vue';
import { PICTURE_LABEL_ENUM, PICTURE_VALUE_ENUM  } from '@/enum/PictureReviewEnum';
import ColorEnums from '@/enum/PictureColorEnum';
import PictureReview from '@/components/PictureReview.vue';


interface Option {
    value: string;
    label: string;
    children?: Option[];
};

interface Tag {
    value: string;
    label: string;
};

const tagsOption = ref<Tag[]>([]);
const categories = ref<Option[]>([]);

// 表格列配置
const columns = [
    {
        title: 'ID',
        dataIndex: 'id',
        key: 'id',
        width: 40
    },
    {
        title: '审核状态',
        dataIndex: 'reviewStatus',
        key: 'reviewStatus',
        width: 80,
        slots: { customRender: 'reviewStatus' }
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
        title: '分类',
        dataIndex: 'category',
        key: 'category',
        width: 80,
        slots: { customRender: 'category' }
    },
    {
        title: '标签',
        dataIndex: 'tags',
        key: 'tags',
        width: 80,
        slots: { customRender: 'tags' },
    },
    {
        title: '简介',
        dataIndex: 'introduction',
        key: 'introduction',
        ellipsis: true
    },
    {
        title: '创建用户ID',
        dataIndex: 'userId',
        key: 'userId',
    },
    {
        title: '图片信息',
        key: 'pictureInfo',
        width: 150,
        slots: { customRender: 'pictureInfo' }
    },
    {
        title: '创建时间',
        dataIndex: 'createTime',
        key: 'createTime',
        slots: { customRender: 'createTime' },
    },
    {
        title: '编辑时间',
        dataIndex: 'editTime',
        key: 'editTime',
        slots: { customRender: 'editTime' },
    },
    {
        title: '操作',
        key: 'action',
        width: 150,
        slots: { customRender: 'action' },
        fixed: 'right'
    },
];

const isAscending = ref(false);
const searchParam = ref<API.PictureQueryRequest>({
    reviewStatus: 3,
    searchText: "",
    category: "",
    name: "",
    tags: [],
}); 

watch(
    () => [searchParam.value.category, searchParam.value.searchText, searchParam.value.name, searchParam.value.tags, searchParam.value.reviewStatus, isAscending.value],
    () => {
        pagination.value.current = 1;
        fetchPictureList();
    },
)

// 用户数据
const pictures = ref<API.Picture[]>([]);

const isReviewing = ref(false);
const form = ref({
    reviewStatus: PICTURE_LABEL_ENUM.REVIEWING, 
    reviewMessage: '', 
});

const onPictureReview = (record: API.Picture) => {
    currentSelectedPicture.value = record;
    isReviewing.value = true;
}

const currentSelectedPicture = ref<API.Picture>(); 
const isEditing = ref(false); 

const onPictureEdit = (picture: API.Picture) => {
    currentSelectedPicture.value = picture;
    console.log(currentSelectedPicture.value);
    isEditing.value = true;
}

const onPictureReviewingSuccess = (value : number) => {
    console.log("审核成功 " + value);
    isReviewing.value = false;
    if (currentSelectedPicture.value) {
        currentSelectedPicture.value.reviewStatus = value;
    }
}

const onPictureEditSuccess = () => {
    console.log("编辑成功");
    isEditing.value = false;
}

// 分页配置
const pagination = ref({
    current: 1,
    pageSize: 10,
    total: 0,
    showQuickJumper: true,
    showSizeChanger: true,
    pageSizeOptions: ['10', '20', '30', '50'],
});

// 确认删除弹窗可见性
const confirmModalVisible = ref(false);
const isLoading = ref(false);

// 获取用户列表
const fetchPictureList = async () => {
    isLoading.value = true;
    // 定义一个 sleep 函数，用于暂停执行指定的毫秒数

    await global.sleep(1000);

    try {
        const { data } = await listPictureByPageUsingPost({
            current: pagination.value.current,
            pageSize: pagination.value.pageSize,
            sortField: "createTime",
            sortOrder: isAscending.value ? "ascend" : "descend",
            ...searchParam.value,
        });

        pictures.value = data.data?.records || [];
        console.log(data.data?.records);
        pagination.value.total = Number(data.data?.total) || 0;
    } catch (error) {
        console.error('获取图片列表失败', error);
    }

    isLoading.value = false;
};

// 确认删除
const isDeleting = ref(false);
const deletedPictureIds = ref<Set<String>>(new Set());
const confirmDelete = async (id : string) => {
    const response = await deletePictureUsingPost({ id: id });

    if (response.data.code !== 0) {
        console.error('删除照片失败', response.data.message);
        return;
    } else {
        deletedPictureIds.value.add(id);
        message.success('删除照片成功!');
    }

    confirmModalVisible.value = false;
};

// 取消删除
const cancelDelete = () => {
    isEditing.value = false;
};

// 表格分页、排序、筛选变化时触发
const handleTableChange = (page: any) => {
    pagination.value.current = page.current;
    pagination.value.pageSize = page.pageSize;
    fetchPictureList();
};

const onCategorySelected = (category: string[]) => {
    searchParam.value.category = category[0];
}

const loadTagsAndCategories = async () => {
    try {
    const response = await listPictureTagCategoryUsingGet();
    if (response.data.code === 0) {
        const categoryList = response.data.data?.categoryList;
        const tagList = response.data.data?.tagList;
        console.log(categoryList, tagList); // 打印分类和标签数据，用于调试

        categories.value = categoryList?.map(c => ({ value: c, label: c })) || [];
        tagsOption.value = tagList?.map(t => ({ value: t, label: t })) || [];
    }
    } catch (error) {
    console.error('获取标签和分类列表失败', error);
    }
}

onMounted(() => {
    fetchPictureList();
    loadTagsAndCategories();
});
</script>

<style scoped>
.user-manage-container {
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
