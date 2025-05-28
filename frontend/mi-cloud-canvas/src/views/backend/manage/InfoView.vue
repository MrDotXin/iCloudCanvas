<template>
    <a-row :gutter="16">
        <a-col :span="6" v-for="(item, index) in stats" :key="index">
            <a-card>
                <statistic-card :title="item.title" :value="item.value" />
            </a-card>
        </a-col>
    </a-row>

    <a-card title="我的常用入口" class="mt-16">
        <a-grid :cols="6">
            <a-grid-item v-for="(entry, index) in quickEntries" :key="index" class="entry-item">
                <a-button long type="outline">
                    <template #icon>
                        <component :is="entry.icon" />
                    </template>
                    {{ entry.label }}
                </a-button>
            </a-grid-item>
        </a-grid>
    </a-card>

    <a-card title="常用项" class="mt-16">
        <a-space>
            <a-button type="primary">添加成员</a-button>
            <a-button status="warning">退出成员</a-button>
        </a-space>
        <member-table :data="members" class="mt-16" />
    </a-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 类型定义
interface StatItem {
    title: string;
    value: number;
}

interface Member {
    id: string;
    name: string;
    role: string;
    department: string;
}

// 数据
const stats = ref<StatItem[]>([
    { title: '总人数', value: 2 },
    { title: '邀请人数', value: 2 },
    { title: '来访人数', value: 0 },
    { title: '部门数', value: 0 }
]);

const quickEntries = ref([
    { label: '会议室', icon: 'icon-meeting-room' },
    { label: '工作台', icon: 'icon-dashboard' },
    // 其他入口...
]);

const members = ref<Member[]>([
    { id: '001', name: '张三', role: '管理员', department: '技术部' },
    { id: '002', name: '李四', role: '成员', department: '运营部' }
]);

</script>