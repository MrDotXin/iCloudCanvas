<template>
  <div style=" min-height: 125vh;" v-if="!isLoadingPage">
    <div>
      <a-select v-model:value="searchParam.tags" mode="multiple" style="min-width: 20vw; height: 20px;"
        placeholder="选择标签内容查询" :options="tagsOption" @change="doSearch">
      </a-select>
      <a-input-search style="width: 50vw; height: 20px; margin-bottom: 20px;" placeholder="请输入图片名称或者相关信息"
        :value="searchParam.searchText" @update:value="(value: string) => { searchParam.searchText = value; }"
        @search="doSearch" />
    </div>
    <div style="display: flex; justify-content: space-between;">
      <a-tabs v-model:activeKey="currentedCategory" @change="doSearch">
        <a-tab-pane key="all" tab="所有"></a-tab-pane>
        <a-tab-pane v-for="category in categoryList" :key="category" :tab="category"></a-tab-pane>
      </a-tabs>
      <a-space direction="horizontal" style="margin-bottom: 20px;">
      <a-select ref="select" v-model:value="isNewest" style="width: 120px; margin-bottom: 20px;" @change="handleIsNewest">
        <a-select-option :value="1">最新</a-select-option>
        <a-select-option :value="0">最旧</a-select-option>
      </a-select>
      <a-select ref="select" v-model:value="timeSelector" style="width: 120px; margin-bottom: 20px;" @change="handleChange">
          <a-select-option :value="TIME_SELECTOR.TIME_VALUE_SELECT.ALL">{{TIME_SELECTOR.TIME_LABEL_SELECT[1] }}</a-select-option>
          <a-select-option :value="TIME_SELECTOR.TIME_VALUE_SELECT.TODAY">{{ TIME_SELECTOR.TIME_LABEL_SELECT[2]}}</a-select-option>
          <a-select-option :value="TIME_SELECTOR.TIME_VALUE_SELECT.THIS_WEEK">{{ TIME_SELECTOR.TIME_LABEL_SELECT[3]}}</a-select-option>
          <a-select-option :value="TIME_SELECTOR.TIME_VALUE_SELECT.THIS_MONTH">{{ TIME_SELECTOR.TIME_LABEL_SELECT[4]}}</a-select-option>
        </a-select>
      </a-space>
    </div>
    <a-list :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }" :data-source="pictures"
      :pagination="pagination" :loading="isLoading">
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0;">
          <div @click="toPictureDetail(picture)">
            <a-card hoverable>
              <template #cover>
                <img :src="picture.thumbnailUrl ? picture.thumbnailUrl : picture.url"
                  style="height: 180px; object-fit: cover;" :alt="picture.name" />
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
    <a-skeleton style="width: 100vw; height: 100vh;" :active="true" :paragraph="{ rows: 5 }" />
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

import TIME_SELECTOR from '@/enum/TimeSelectEnum'
import dayjs from 'dayjs';


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

const timeSelector = ref(1);
const handleChange = async (value: number) => {
    timeSelector.value = value;
    await fetchPictureList();
};  

const isNewest = ref(1);
const handleIsNewest = async (newStatus : number) => {
    isNewest.value = newStatus;
    await fetchPictureList();
}

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

const setTimeRange = (timeSelector: number) => {
  if (timeSelector > TIME_SELECTOR.TIME_VALUE_SELECT.ALL) {
    const timeUnit = TIME_SELECTOR.getTimeRangeWithFixdUnit(timeSelector);
    searchParam.value.editTimeBegin = dayjs().startOf(timeUnit).format();
    searchParam.value.editTimeEnd = dayjs().endOf(timeUnit).format();
  } else {
    searchParam.value.editTimeBegin = "";
    searchParam.value.editTimeEnd = "";
  }
}

const setTimeOrder = () => {
  if (isNewest.value === 1) {
    searchParam.value.sortField = "editTime";
    searchParam.value.sortOrder = "descend";
  } else {
    searchParam.value.sortField = "editTime";
    searchParam.value.sortOrder = "ascend";
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

    setTimeRange(timeSelector.value);
    setTimeOrder();
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

const router = useRouter();
const toPictureDetail = (picture: API.PictureVO) => {
  console.log(picture);
  router.push({
    path: `/picture/${picture.id}`
  });
}

onMounted(async () => {
  isLoadingPage.value = true;
  await fetchPictureList();
  await loadTagsAndCategories();
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
