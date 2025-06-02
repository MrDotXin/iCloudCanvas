<template>
  <div style=" min-height: 125vh;" v-if="!isLoadingPage">
    <div style="display: flex; justify-content: space-between;">
    <a-select
        v-model:value="searchParam.tags"
        mode="multiple"
        style="min-width: 44vw; height: 20px;"
        placeholder="选择标签内容查询"
        :options="tagsOption"
        @change="doSearch"
    >
    </a-select>
    <a-input-search
      style="width: 50vw; height: 20px; margin-bottom: 20px;"
      placeholder="请输入图片名称或者相关信息"
      :value="searchParam.searchText"
      @update:value="(value : string) => { searchParam.searchText = value;}"
      @search="doSearch"
    />
  </div>
    <a-tabs v-model:activeKey="currentedCategory" @change="doSearch">
      <a-tab-pane key="all" tab="所有"></a-tab-pane>
      <a-tab-pane v-for="category in categoryList" :key="category" :tab="category"></a-tab-pane>
    </a-tabs>
    <a-list 
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }" 
      :data-source="pictures"
      :pagination="pagination" :loading="isLoading"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0;" >
          <div @click="toPictureDetail(picture)">
          <a-card hoverable >
            <template #cover>
                <img  :src="picture.thumbnailUrl ? picture.thumbnailUrl : picture.url" style="height: 180px; object-fit: cover;" :alt="picture.name" />
              </template>
              <a-card-meta :title="picture.name">
                <template #description>
                  <a-flex>
                  <a-tag :color="ColorEnums.PIC_CATEGORY_COLOR_MAP[picture.category]">
                    {{ picture.category }}
                  </a-tag>
                  <a-tag v-for="tag in picture.tags" :key="tag" :color="ColorEnums.PIC_TAG_COLOR_MAP[tag]">
                    {{ tag }}
                  </a-tag>
                </a-flex>
              </template>
            </a-card-meta>
          </a-card>
        </div>
        </a-list-item>
      </template>
    </a-list>
  </div>
  <div v-else>
    <a-skeleton style="width: 100vw;" :active="true" :paragraph="{ rows: 14 }" />
    <WaitingRunningCar style="position: absolute; top: 40%; width: 90%;" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';

import { global } from '@/global';

import ColorEnums from '@/enum/PictureColorEnum';
import { listPictureTagCategoryUsingGet, listPictureVoByPageUsingPost } from '@/backend/service/api/pictureController';

import WaitingRunningCar from '@/components/Loader/WaitingRunningCar.vue';

import { useRouter } from 'vue-router';


interface Option {
    value: string;
    label: string;
    children?: Option[];
};

interface Tag {
    value: string;
    label: string;
};

const pictures = ref<API.PictureVO[]>([]);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showQuickJumper: true,
  showSizeChanger: true,
  onChange: (current: number, pageSize: number) => {
    pagination.value.current = current;
    pagination.value.pageSize = pageSize;
    fetchPictureList();
  },
  pageSizeOptions: ['10', '15', '20', '25'],
});

const searchParam = ref<API.PictureQueryRequest>({
  category: "",
  introduction: "",
  searchText: "",
  name: "",
  sortField: "",
  sortOrder: "",
  tags: []
});

const currentedCategory = ref<string>("all");

const doSearch = () => {
  pagination.value.current = 1;
  fetchPictureList();
}
const isLoading = ref(false);
const isLoadingPage = ref(false);

const tagList = ref<string[]>([]);
const categoryList = ref<string[]>([]);

const tagsOption = ref<Tag[]>([]);
const categories = ref<Option[]>([]);

const loadTagsAndCategories = async () => {
  try {
    const { data } = await listPictureTagCategoryUsingGet();
    if (data.code === 0) {
      tagList.value = data.data?.tagList || [];
      categoryList.value = data.data?.categoryList || []

      categories.value = categoryList?.value.map(c => ({ value: c, label: c })) || [];
      tagsOption.value = tagList?.value.map(t => ({ value: t, label: t })) || [];
    }
  } catch (error) {
    console.error('获取标签和分类列表失败', error);
  }
}

const fetchPictureList = async () => {
  isLoading.value = true;

  await global.sleep(1000);

  try {
    if (currentedCategory.value !== "all") {
      searchParam.value.category = currentedCategory.value;
    } else {
      searchParam.value.category = "";
    }

    const response = await listPictureVoByPageUsingPost({
      current: pagination.value.current,
      pageSize: pagination.value.pageSize,
      ...searchParam.value
    });

    if (response.data.code === 0) {
      pictures.value = response.data.data?.records || [];
      pagination.value.total = Number(response.data.data?.total) || 0;
    } else {
      message.error("哥, 我也没图给你了...");
    }
  } catch (error) {
    console.error('获取图片列表失败', error);
  }

  isLoading.value = false;
};

// 表格分页、排序、筛选变化时触发
const handleListChange = (page: any) => {
  pagination.value.current = page.current;
  pagination.value.pageSize = page.pageSize;
  fetchPictureList();
};

const router = useRouter();
const toPictureDetail = (picture: API.PictureVO) => {
  console.log(picture);
  router.push({
    path: `/picture/${picture.id}`
  });
}

onMounted(() => {
  isLoadingPage.value = true;
  fetchPictureList();
  loadTagsAndCategories();
  isLoadingPage.value = false;
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
