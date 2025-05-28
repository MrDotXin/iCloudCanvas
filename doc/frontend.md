# 前端

---

[toc]

---

## 基于vue3 + ant-deign-vue 创建项目

首先我们需要使用npm 创建vite 项目

```bash
npm create vue@latest
```

接着引入`ant-design-vue`

```bash
npm i --save ant-design-vue@4.x
```

> 为了使得ant-design-vue可以正常使用, 我们需要**删除`vite`自带的全局css防止css污染**
>
> > 分别是`base.css` 还有`main.css`

##  基于routes的自动加载导航栏

我们可以通过已经声明的`routes`组件来实现导航栏

```tsx
...
<a-menu v-model:selectedKeys="current" :items="items" mode="horizontal" @click="doMenuClick" />
...
const items: MenuProps['items'] = router.getRoutes().filter(doNavgatorFilter).map(route => ({
  key: route.path.replace('/', ''),
  label: route.name,
  icon: h(doIconSelect(route.name))
}));

```

我们可以在`doNavgatorFilter`里面做筛选, 哪些导航项应该展示

## 基于axios的请求拦截与自动登录跳转

我们可以通过请求拦截来做到当你访问的资源需要登录则给你自动跳到登录页面

```typescript
instance.interceptors.response.use(
    (response) => {
        const { data } = response;
        if (data.code === 40100) {
            // 根据用户行为判断是不是需要用户去登录
            if (
                !response.request.responseURL.includes('/get/login') &&
                !window.location.pathname.includes('/login')
            ) {
                message.warn("请先登录")
                window.location.href = `/user/login?redirect=${window.location.href}`;
            }
        }
        return response;
    },
    (error) => {
        return Promise.reject(error);
    }
);
```

## 基于umijs的接口文档生成

首先我们下载umijs

```bash
npm i --save-dev @umijs/openapi
```

配置生成配置文件

```bash
export default {
    requestLibPath: "import request from '@/backend/request';",
    schemaPath: "http://localhost:8501/api/v2/api-docs",
    serversPath: "./src/backend/service/"
}
```

在 `package.json` 的 `script` 中添加 api: `"openapi2ts": "openapi2ts",`

然后启动

```bash
npm run openapi2ts
```

就可以了

## 基于localStorage的刷新状态保存

我们使用`localStorage`进行`K-V`存储, 可以很方便实现某些操作下的黄状态保存

```typescript
// 载入保存的状态信息
onMounted(() => {
    console.log(localStorage.getItem("currentTabView"));
    console.log(localStorage.getItem("isSiderCollapsed"));
    console.log(localStorage.getItem("openKeys"));

    
    currentTabView.value = localStorage.getItem("currentTabView") || 'Info';
    isSiderCollapsed.value = localStorage.getItem("isSiderCollapsed") === "true";
    openKeys.value = JSON.parse(localStorage.getItem("openKeys") || "[\"Info\"]");
    preOpenKeys.value = [];
})

// 保存菜单栏收起状态信息
const handleSiderCollapse = () => {
    isSiderCollapsed.value = !isSiderCollapsed.value;
    openKeys.value = isSiderCollapsed.value ? [] : preOpenKeys.value;

    localStorage.setItem("isSiderCollapsed", isSiderCollapsed.value.toString());
};


watch(
    () => openKeys.value,
    (_, oldVal) => {
        preOpenKeys.value = oldVal;
        // 保存打开的键的信息
        localStorage.setItem("openKeys", JSON.stringify(openKeys.value));
    },
);
```

## 阶段一 图片模块与用户模块的开发

我们需要开发出下面的功能:

- 用户登录注册
- 用户管理
- 图片展示
- 图片页面列表
- 图片增删改查



## 阶段二 用户上传与审核



###  基于url的上传

首先对于后端使用`Hutool.downFile(url)` 可以直接获取到文件本身，其次我们要对`url`还有文件本身进行校验

## FAQ 

### 关于:model 与 v-model:model的区别

`v-model:value` 实际上是 `:value` + `@update:value` 的语法糖，它会：

- 在每次输入事件(input)时立即更新绑定的数据

而中文输入法的工作流程：

```
compositionstart → 输入拼音 → compositionend → 最终确认汉字
```

这就可能会导致你的中文输入法出错, 所有有时候你需要拆开来使用

```vue
    <a-input-search
      style="width: 100%; margin-bottom: 20px;"
      placeholder="请输入图片名称或者相关信息"
      :value="searchParam.searchText"
      @update:value="(value : string) => { searchParam.searchText = value;}"
      @search="doSearch"
    />
```

### 修复错误: 对象字面量只能指定已知属性，并且“requestType”不在类型“AxiosRequestConfig<any>”中

我们需要将这个改写进`header`里面

从

```typescript
  return request<API.BaseResponsePictureVO_>('/api/picture/upload', {
    method: 'POST',
    params: {
      ...params,
    },
    data: formData,
    requestType: 'form',
    ...(options || {}),
  })
```

变成

```typescript
  return request<API.BaseResponsePictureVO_>('/api/picture/upload', {
    method: 'POST',
    params: {
      ...params,
    },
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    data: formData,
    ...(options || {}),
  })
```

