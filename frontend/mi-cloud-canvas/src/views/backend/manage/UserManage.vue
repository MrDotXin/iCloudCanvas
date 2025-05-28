<template>
    <div class="user-manage-container">
        <div class="header">
            <h1 class="title">用户管理</h1>
        </div>
        <a-card class="user-table-card">
            <a-spin :spinning="isLoading" size="large">
                <a-table :columns="columns" :data-source="users" :pagination="pagination" @change="handleTableChange"
                    row-key="id" >
                    <template #userAvatar="{ record }">
                        <a-avatar 
                            :size="40" 
                            :style="{ backgroundColor: '#3370ff' }"
                            :src="record.userAvatar"
                        >
                        </a-avatar>
                    </template>
                    <template #userRole="{ record }">
                        <a-tag :color="record.userRole === 'ROLE_ADMIN' ? 'yellow' : 'blue' " >
                            {{ record.userRole === 'ROLE_ADMIN' ? "管理员" : "用户" }}
                        </a-tag>
                    </template>
                    <template #createTime="{ record }">
                        <b>{{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}</b>
                    </template>
                    <template #action="{ record }">
                        <a-button danger @click="deleteUser(record.id, record.userName)">
                            删除
                        </a-button>
                    </template>
                </a-table>
            </a-spin>
        </a-card>
        <a-modal v-model:open="confirmModalVisible" title="确认删除" @ok="confirmDelete" @cancel="cancelDelete">
            <p>确定要删除id为 <b>{{ deletingUserId }}</b> 且用户名为 <b>{{ deletingUserName }}</b> 的用户吗？ </p>
        </a-modal>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { deleteUserUsingPost, listUserByPageUsingPost } from '@/backend/service/api/userController';
import dayjs from 'dayjs';
import { message } from 'ant-design-vue';

import { global } from '@/global';

// 表格列配置
const columns = [
    {
        title: 'ID',
        dataIndex: 'id',
        key: 'id',
    },
    {
        title: '头像',
        dataIndex: 'userAvatar',
        key: 'userAvatar',
        slots: { customRender: 'userAvatar' },
    },
    {
        title: '用户名',
        dataIndex: 'userName',
        key: 'userName',
    },
    {
        title: '权限',
        dataIndex: 'userRole',
        key: 'userRole',
        slots: { customRender: 'userRole' },
    },
    {
        title: '加入时间',
        dataIndex: 'createTime',
        key: 'createTime',
        slots: { customRender: 'createTime' },
    },
    {
        title: '操作',
        key: 'action',
        width: 150,
        slots: { customRender: 'action' },
    },
];

// 用户数据
const users = ref<API.User[]>([]);
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
// 待删除用户 ID
const deletingUserId = ref<string>("");

const deletingUserName = ref<string>("");

const isLoading = ref(false);

// 获取用户列表
const fetchUserList = async () => {
    isLoading.value = true;
    // 定义一个 sleep 函数，用于暂停执行指定的毫秒数

    await global.sleep(1000);

    try {
        const { data } = await listUserByPageUsingPost({
            current: pagination.value.current,
            pageSize: pagination.value.pageSize,
        });
        users.value = data.data?.records || [];
        console.log(data.data?.records);
        pagination.value.total = Number(data.data?.total) || 0; ;
    } catch (error) {
        console.error('获取用户列表失败', error);
    }

    isLoading.value = false;
};

// 触发删除操作，弹出确认弹窗
const deleteUser = async (id : string, userName : string) => {
    deletingUserId.value = id;
    deletingUserName.value = userName;
    confirmModalVisible.value = true;
};


// 确认删除
const confirmDelete = async () => {
    const response = await deleteUserUsingPost({ id: deletingUserId.value });

    if (response.data.code !== 0) {
        console.error('删除用户失败', response.data.message);
        return;
    } else {
        message.success('删除用户成功!');
        fetchUserList(); // 刷新用户列表，删除成功后更新用户列表的状态
    }

    confirmModalVisible.value = false;
    deletingUserId.value = "";
};

// 取消删除
const cancelDelete = () => {
    confirmModalVisible.value = false;
    deletingUserId.value = "";
};

// 表格分页、排序、筛选变化时触发
const handleTableChange = (page : any) => {
    pagination.value.current = page.current;
    pagination.value.pageSize = page.pageSize;
    fetchUserList();
};

onMounted(() => {
    fetchUserList();
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
