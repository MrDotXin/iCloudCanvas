<template>
    <a-layout style="min-height: 100vh">
        <!-- 左侧用户信息Sider -->
        <a-layout-sider width="256" :collapsible="true" v-model:collapsed="isSiderCollapsed" :trigger="null"
            style="background: linear-gradient(135deg, #42a5f5 0%, #26c6da 100%); color: white;">
            <div class="sider-trigger" @click="isSiderCollapsed = !isSiderCollapsed">
                <a-button type="text" style="color: white; font-size: 18px;">
                    <MenuUnfoldOutlined v-if="isSiderCollapsed" />
                    <MenuFoldOutlined v-else />
                </a-button>
            </div>
            <div class="user-info-container">
                <a-skeleton :loading="userLoading" active>
                    <div class="user-avatar">
                        <a-avatar :size="64" :src="userStore.loginUser.userAvatar || defaultAvatar">
                            <user-outlined v-if="!userStore.loginUser.userAvatar" />
                        </a-avatar>
                    </div>
                    <div class="user-name">{{ userStore.loginUser.userName || '未登录' }}</div>
                    <div class="user-role">{{ ACCESS_ENUM_VALUE[userStore.loginUser.userRole as string]?.value ||
                        '加载中...' }}</div>
                    <div class="user-join-date">加入时间: {{ userStore.loginUser.createTime ?
                        dayjs(userStore.loginUser.createTime).format('YYYY-MM-DD') : '未知' }}</div>
                    <a-divider style="background-color: rgba(255,255,255,0.3);" />
                    <div class="user-stats">
                        <div class="stat-item">
                            <div class="stat-label">已使用空间</div>
                            <div class="stat-value">{{ formatSize(spaceInfo?.totalSize) }}</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-label">已上传图片</div>
                            <div class="stat-value">{{ spaceInfo?.totalCount || 0 }} 张</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-label">用户个人简介</div>
                            <div class="stat-value"> {{ userStore.loginUser.userProfile || '暂无简介' }}</div>
                        </div>
                    </div>
                </a-skeleton>
            </div>
        </a-layout-sider>

        <!-- 右侧内容区域 -->
        <a-layout-content style="background: #f0f2f5; padding: 24px">
            <!-- 空间不存在时显示创建引导 -->
            <div v-if="!spaceInfo" class="create-space-container">
                <a-card title="您还没有个人空间" style="max-width: 800px; margin: 0 auto">
                    <p>创建个人空间后，您可以上传、管理和分享您的图片资源。</p>
                    <a-steps :current="currentStep" @change="onStepChange">
                        <a-step title="选择空间等级" description="选择适合您的空间容量" />
                        <a-step title="设置空间名称" description="为您的空间命名" />
                        <a-step title="完成创建" description="确认信息并创建空间" />
                    </a-steps>

                    <div class="steps-content">
                        <a-space direction="vertical" v-if="currentStep === 0">
                            <a-radio-group v-model:value="spaceLevel" button-style="solid"
                                @click="handleLevelSelectionChange">
                                <a-radio-button v-for="level in spaceLevels" :key="level.value" :value="level.value">
                                    {{ level.levelName }} ({{ formatSize(level.levelMaxSize) }})
                                </a-radio-button>
                            </a-radio-group>
                            <a-descriptions v-if="spaceLevel !== undefined && spaceLevels[spaceLevel]" title="等级详情"
                                style="margin-top: 16px;" bordered>
                                <a-descriptions-item label="描述">{{ spaceLevels[spaceLevel].levelDescription
                                    }}</a-descriptions-item>
                                <a-descriptions-item label="存储空间">{{ formatSize(spaceLevels[spaceLevel].levelMaxSize)
                                    }}</a-descriptions-item>
                                <a-descriptions-item label="最大存储数量">{{ spaceLevels[spaceLevel].levelMaxCount
                                    }}张</a-descriptions-item>
                            </a-descriptions>
                        </a-space>
                        <div v-if="currentStep === 1">
                            <a-input v-model:value="spaceName" placeholder="请输入空间名称" />
                        </div>
                        <div v-if="currentStep === 2">
                            <a-descriptions title="空间信息确认">
                                <a-descriptions-item label="空间等级">
                                    {{ spaceLevels[spaceLevel].levelName }}
                                </a-descriptions-item>
                                <a-descriptions-item label="空间名称">{{ spaceName }}</a-descriptions-item>
                            </a-descriptions>
                        </div>
                    </div>

                    <div class="steps-action">
                        <a-button v-if="currentStep > 0" @click="prevStep">上一步</a-button>
                        <a-button v-if="currentStep < 2" type="primary" @click="nextStep" :loading="isSubmitting">
                            下一步
                        </a-button>
                        <a-button v-if="currentStep === 2" type="primary" @click="createSpace" :loading="isSubmitting">
                            创建空间
                        </a-button>
                    </div>
                </a-card>
            </div>

            <!-- 空间存在时显示内容 -->
            <div v-else>
                <!-- 空间状态信息 -->
                <a-skeleton :loading="spaceLoading" active>
                    <a-card style="margin-bottom: 24px">
                        <div class="space-status-container">
                            <div class="status-item">
                                <div class="status-label">总容量</div>  
                                <div class="status-value">{{ formatSize(spaceInfo.maxSize) }}</div>
                            </div>
                            <div class="status-item">
                                <div class="status-label">已使用</div>
                                <div class="status-value">{{ formatSize(spaceInfo.totalSize) }}</div>
                                <a-progress :percent="calculateUsagePercentage()" size="small"
                                    :status="calculateUsagePercentage() > 90 ? 'exception' : 'active'" />
                            </div>
                            <div class="status-item">
                                <div class="status-label">图片容量</div>
                                <div class="status-value">{{ formatSize(spaceInfo.totalSize) }} / {{
                                    formatSize(spaceInfo.maxSize) }}</div>
                            </div>
                            <div class="status-item">
                                <div class="status-label">公开状态</div>
                                <div class="status-value">
                                    <a-switch checked-children="公开" un-checked-children="私有"
                                        v-model:checked="spacePublic" @change="handlePublicChange" />
                                </div>
                            </div>
                        </div>
                    </a-card>
                </a-skeleton>

                <!-- 图片列表 -->
                <div class="picture-list-container">
                    <div style="display: flex; justify-content: space-between; margin-bottom: 16px" >
                        <h2>我的图片</h2>
                        <a-button type="primary" @click="startSubmitPicture = true">
                            <template #icon>
                                <upload-outlined />
                            </template>
                            上传图片
                        </a-button>
                    </div>

                    <a-list :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }" :data-source="pictures"
                        :pagination="pagination" :loading="isLoading">
                        <template #loading>
                            <div v-for="i in 6" :key="i" style="margin-bottom: 16px;">
                                <a-skeleton active>
                                    <a-skeleton-image style="width: 100%; height: 180px;" />
                                    <a-skeleton-paragraph :rows="2" />
                                </a-skeleton>
                            </div>
                        </template>
                        <template #renderItem="{ item: picture }">
                            <a-list-item style="padding: 0">
                                <div @click="toPictureDetail(picture)">
                                    <a-card hoverable>
                                        <template #cover>
                                            <img :src="picture.thumbnailUrl ? picture.thumbnailUrl : picture.url"
                                                style="height: 180px; object-fit: cover" :alt="picture.name" />
                                        </template>
                                        <a-card-meta :title="picture.name">
                                            <template #description>
                                                <a-space wrap>
                                                    <a-tag :color="getCategoryColor(picture.category)">{{
                                                        picture.category }}</a-tag>
                                                    <a-tag v-for="tag in JSON.parse(picture.tags)" :key="tag" color="blue">
                                                        {{ tag }}
                                                    </a-tag>
                                                </a-space>
                                            </template>
                                        </a-card-meta>
                                    </a-card>
                                </div>
                            </a-list-item>
                        </template>
                    </a-list>
                </div>
            </div>
        </a-layout-content>
    </a-layout>


    <a-modal v-if="!!spaceInfo" v-model:open="startSubmitPicture" :bodyStyle="{ overflow: 'auto' }" width="90vw" @confirm="cancelDelete" @cancel="cancelDelete">
        <template #title>
            向空间 {{ spaceInfo.spaceName }} 上传照片
        </template>
        <AddPictureView :spaceId="spaceInfo.id" :onEditSuccess="onSuccess" />
    </a-modal>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import {
    UserOutlined,
    UploadOutlined,
    MenuUnfoldOutlined,
    MenuFoldOutlined,
} from '@ant-design/icons-vue';

import dayjs from 'dayjs';
import { ACCESS_ENUM_VALUE } from '@/enum/AccessEnum';
import AddPictureView from '../backend/manage/AddPictureView.vue';

// 状态管理
import { useLoginUserStore } from '@/stores/user.ts';
const userStore = useLoginUserStore();

// API 导入
import {
    changeSpaceTypeUsingPost,
    getSpaceVoByUserIdUsingGet,
    listSpaceLevelInfoUsingGet
} from '@/backend/service/api/spaceController';
import {
    listPictureByPageUsingPost,
} from '@/backend/service/api/pictureController';
import {
    addSpaceUsingPost,
} from '@/backend/service/api/spaceController';

// 路由
const router = useRouter();

// 响应式数据
const spaceInfo = ref<API.SpaceVO | null>(null);
const pictures = ref<API.Picture[]>([]);
const isLoading = ref(false);
const isSubmitting = ref(false);
const spaceLoading = ref(false);
const userLoading = ref(false);

// 分页
const pagination = ref({
    current: 1,
    pageSize: 12,
    total: 0,
    showSizeChanger: true,
    showQuickJumper: true,
    pageSizeOptions: ['12', '24', '36'],
    onChange: (current: number, pageSize: number) => {
        pagination.value.current = current;
        pagination.value.pageSize = pageSize;
        fetchPictures();
    },
});

// 空间创建步骤
const currentStep = ref(0);
const spaceLevel = ref(0);
const spaceName = ref('');
const spacePublic = ref(false);
const isSiderCollapsed = ref(false);
const spaceLevels = ref<API.SpaceLevelInfo[]>([]);
const defaultAvatar = 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png';


const handleLevelSelectionChange = (newLevel: number) => {
    spaceLevel.value = newLevel;
}
// 生命周期
const fetchSpaceLevels = async () => {
    try {
        const response = await listSpaceLevelInfoUsingGet();
        if (response.data.code === 0) {
            spaceLevels.value = response.data.data || [];
            console.log(spaceLevels.value);
        }
    } catch (error) {
        console.error('获取空间等级信息失败', error);
        message.error('获取空间等级信息失败');
    }
};

onMounted(async () => {
    userLoading.value = true;
    await userStore.getLoginUser();
    userLoading.value = false;
    console.log(spaceLevel.value);
    if (userStore.isLogin) {
        await fetchSpaceInfo();
        await fetchSpaceLevels();
    }
});

// 方法
const fetchSpaceInfo = async () => {
    try {
        spaceLoading.value = true;
        isLoading.value = true;
        const response = await getSpaceVoByUserIdUsingGet({
            userId: userStore.loginUser.id || '',
        });
        if (response.data.code === 0) {
            spaceInfo.value = response.data.data as API.SpaceVO;
            spacePublic.value = spaceInfo.value?.spaceType === 1 || false;
            if (spaceInfo.value) {
                fetchPictures();
            }
        }
    } catch (error) {
        console.error('获取空间信息异常', error);
        message.error('获取空间信息异常');
    } finally {
        spaceLoading.value = false;
        isLoading.value = false;
    }
};

const fetchPictures = async () => {
    try {
        isLoading.value = true;
        const response = await listPictureByPageUsingPost({
            current: pagination.value.current,
            pageSize: pagination.value.pageSize,
            userId: userStore.loginUser.id,
            spaceId: spaceInfo.value?.id,
            sortField: 'createTime',
            sortOrder: 'descend',
        });
        if (response.data.code === 0) {
            pictures.value = response.data.data?.records || [];
            pagination.value.total = Number(response.data.data?.total) || 0;
        } else {
            message.error('获取图片列表失败: ' + response.data.message);
        }
    } catch (error) {
        console.error('获取图片列表异常', error);
        message.error('获取图片列表异常');
    } finally {
        spaceLoading.value = false;
        isLoading.value = false;
    }
};

const createSpace = async () => {
    if (!spaceName.value.trim()) {
        message.warning('请输入空间名称');
        return;
    }

    try {
        isSubmitting.value = true;
        const response = await addSpaceUsingPost({
            spaceLevel: spaceLevel.value,
            spaceName: spaceName.value,
        });
        if (response.data.code === 0) {
            message.success('空间创建成功');
            await fetchSpaceInfo();
        } else {
            message.error('空间创建失败: ' + response.data.message);
        }
    } catch (error) {
        console.error('空间创建异常', error);
        message.error('空间创建异常');
    } finally {
        isSubmitting.value = false;
    }
};

const toPictureDetail = (picture: API.PictureVO) => {
    router.push(`/picture/${picture.id}`);
};

const formatSize = (sizeInBytes?: string): string => {
    if (!sizeInBytes) return '0 B';
    const size = Number(sizeInBytes);
    if (size < 1024) return `${size} B`;
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(2)} KB`;
    if (size < 1024 * 1024 * 1024) return `${(size / (1024 * 1024)).toFixed(2)} MB`;
    return `${(size / (1024 * 1024 * 1024)).toFixed(2)} GB`;
};

const calculateUsagePercentage = (): number => {
    if (!spaceInfo.value?.maxSize || !spaceInfo.value?.totalSize) return 0;
    const maxSize = Number(spaceInfo.value.maxSize);
    const usedSize = Number(spaceInfo.value.totalSize);
    return maxSize > 0 ? (usedSize / maxSize) * 100 : 0;
};

const getCategoryColor = (category?: string): string => {
    const colorMap: Record<string, string> = {
        '风景': 'geekblue',
        '人物': 'purple',
        '动物': 'orange',
        '建筑': 'green',
        '美食': 'red',
        '其他': 'gray',
    };
    return colorMap[category || '其他'] || 'blue';
};

const onStepChange = (current: number) => {
    currentStep.value = current;
};

const nextStep = () => {
    if (currentStep.value === 1 && !spaceName.value.trim()) {
        message.warning('请输入空间名称');
        return;
    }
    currentStep.value++;
};

const prevStep = () => {
    currentStep.value--;
};

const handlePublicChange = async (checked: boolean) => {
    spacePublic.value = checked;
    const response = await changeSpaceTypeUsingPost({
        spaceId: spaceInfo.value?.id,
        spaceType: spacePublic.value ? 1 : 0, // 0 为私有，1 为公开,
    })

    if (response.data.code === 0) {
        message.success(`空间已${checked? '设为公开' : '设为私有'}`);
    } else {
        message.error('空间设置失败:'+ response.data.message);
    }
};


// 上传图片
const startSubmitPicture = ref(false);

const cancelDelete = () => {
    startSubmitPicture.value = false;
};

const onSuccess = async () => {
    startSubmitPicture.value = false;
    await fetchPictures();
}
</script>

<style scoped>
.user-info-container {
    padding: 24px;
    text-align: center;
}

.user-avatar {
    margin-bottom: 16px;
}

.user-name {
    font-size: 18px;
    font-weight: 500;
    margin-bottom: 8px;
}

.user-role {
    color: rgba(255, 255, 255, 0.9);
    margin-bottom: 8px;
    font-weight: 500;
}

.user-email {
    color: rgba(255, 255, 255, 0.8);
    font-size: 14px;
    margin-bottom: 4px;
}

.user-join-date {
    color: rgba(255, 255, 255, 0.7);
    font-size: 12px;
    margin-bottom: 24px;
}

.sider-trigger {
    text-align: right;
    padding: 12px;
}

.user-stats {
    text-align: left;
}

.stat-item {
    margin-bottom: 16px;
}

.stat-label {
    color: rgba(255, 255, 255, 0.8);
    font-size: 14px;
    margin-bottom: 4px;
}

.stat-value {
    color: white;
    font-size: 16px;
    font-weight: 500;
}

.create-space-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: calc(100vh - 100px);
}

.steps-content {
    margin-top: 24px;
    min-height: 200px;
    padding-top: 40px;
}

.steps-action {
    margin-top: 24px;
    text-align: right;
}

.space-status-container {
    display: flex;
    flex-wrap: wrap;
    gap: 24px;
}

.status-item {
    flex: 1;
    min-width: 200px;
}

.status-label {
    color: #666;
    font-size: 14px;
    margin-bottom: 8px;
}

.status-value {
    font-size: 20px;
    font-weight: 500;
    margin-bottom: 8px;
}

.picture-list-container {
    background: #fff;
    padding: 24px;
    border-radius: 2px;
}
</style>